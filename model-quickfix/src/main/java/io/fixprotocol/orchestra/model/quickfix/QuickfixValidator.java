/*
 * Copyright 2017-2020 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package io.fixprotocol.orchestra.model.quickfix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiPredicate;
import io.fixprotocol._2020.orchestra.repository.CodeSetType;
import io.fixprotocol._2020.orchestra.repository.CodeType;
import io.fixprotocol._2020.orchestra.repository.ComponentRefType;
import io.fixprotocol._2020.orchestra.repository.ComponentType;
import io.fixprotocol._2020.orchestra.repository.Datatype;
import io.fixprotocol._2020.orchestra.repository.FieldRefType;
import io.fixprotocol._2020.orchestra.repository.FieldRuleType;
import io.fixprotocol._2020.orchestra.repository.GroupRefType;
import io.fixprotocol._2020.orchestra.repository.GroupType;
import io.fixprotocol._2020.orchestra.repository.MessageType;
import io.fixprotocol._2020.orchestra.repository.PresenceT;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.dsl.antlr.SemanticErrorListener;
import io.fixprotocol.orchestra.message.CodeSetScope;
import io.fixprotocol.orchestra.message.TestException;
import io.fixprotocol.orchestra.message.Validator;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import quickfix.FieldMap;
import quickfix.FieldNotFound;
import quickfix.Group;
import quickfix.Message;

/**
 * Validates a FIX message against an Orchestra file
 * <p>
 * Features:
 * <ul>
 * <li>Checks field presence including conditionally required field rule</li>
 * <li>Checks code membership in a codeSet</li>
 * </ul>
 * Only validates the message body, not session level header and trailer. This implementation is a
 * demonstration of capabilities. There is no claim to high performance.
 *
 * @author Don Mendelson
 *
 */
public class QuickfixValidator implements Validator<Message> {

  private static class ErrorListener implements SemanticErrorListener {

    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    @Override
    public void onError(String msg) {
      try {
        messages.put(msg);
      } catch (final InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    void getErrors(Collection<String> toReceive) {
      messages.drainTo(toReceive);
    }

    boolean hasError() {
      return !messages.isEmpty();
    }
  }

  private final ErrorListener errorListener = new ErrorListener();
  private final Evaluator evaluator;

  private final BiPredicate<String, TestException> predicateEvaluator =
      new BiPredicate<String, TestException>() {

        @Override
        public boolean test(String expression, TestException testException) {
          FixValue<?> fixValue;
          try {
            fixValue = evaluator.evaluate(expression);
            final ArrayList<String> toReceive = new ArrayList<>();
            errorListener.getErrors(toReceive);
            toReceive.forEach(testException::addDetail);

            if (fixValue.getValue() == Boolean.TRUE) {
              return true;
            }
          } catch (final ScoreException e) {
            testException.addDetail(e.getMessage());
          }
          return false;
        }
      };

  private final RepositoryAccessor repositoryAdapter;

  private final SymbolResolver symbolResolver;

  public QuickfixValidator(RepositoryAccessor repositoryAdapter, SymbolResolver symbolResolver) {
    this.repositoryAdapter = repositoryAdapter;
    this.symbolResolver = symbolResolver;
    evaluator = new Evaluator(symbolResolver, errorListener);
  }

  @Override
  public void validate(Message message, MessageType messageType) throws TestException {
    final TestException testException =
        new TestException("Invalid message type " + messageType.getName());
    try (final MessageScope messageScope =
        new MessageScope(message, messageType, repositoryAdapter, symbolResolver, evaluator)) {
      symbolResolver.nest(new PathStep("in."), messageScope);
      try (Scope local = (Scope) symbolResolver.resolve(SymbolResolver.LOCAL_ROOT)) {
        local.nest(new PathStep(messageType.getName()), messageScope);

        final List<Object> members = repositoryAdapter.getMessageMembers(messageType);

        validateFieldMap(message, testException, members);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Internal error", e);
    } finally {
      if (testException.hasDetails()) {
        throw testException;
      }
    }
  }

  private void validateField(FieldMap fieldMap, TestException testException,
      FieldRefType fieldRefType) {
    final int id = fieldRefType.getId().intValue();
    final String scenario = fieldRefType.getScenario();
    final PresenceT presence = fieldRefType.getPresence();
    final String dataTypeString = repositoryAdapter.getFieldDatatype(id, scenario);
    final CodeSetType codeSet = repositoryAdapter.getCodeset(dataTypeString, scenario);
    if (codeSet != null) {
      symbolResolver.nest(new PathStep("^"), new CodeSetScope(codeSet));
    }
    final boolean isPresentInMessage = fieldMap.isSetField(id);

    switch (presence) {
      case CONSTANT:
      case IGNORED:
        break;
      case FORBIDDEN:
        if (isPresentInMessage) {
          testException.addDetail("Forbidden field " + id + " is present", "FORBIDDEN", "present");
        }
        break;
      case OPTIONAL:
        // Evaluate rules if present
        final List<FieldRuleType> rules = fieldRefType.getRule();
        for (final FieldRuleType rule : rules) {
          final String when = rule.getWhen();
          if (predicateEvaluator.test(when, testException) && !isPresentInMessage) {
            testException.addDetail("Missing required field " + id, "REQUIRED", "(not present)");
          }
        }
        break;
      case REQUIRED:
        if (!isPresentInMessage) {
          testException.addDetail("Missing required field " + id, "REQUIRED", "(not present)");
        }
        break;
    }

    if (isPresentInMessage) {
      try {
        final String value = fieldMap.getString(id);
        final String datatypeName = repositoryAdapter.getFieldDatatype(id, scenario);
        final Datatype datatype = repositoryAdapter.getDatatype(datatypeName);
        if (datatype == null) {
          final List<CodeType> codeList = codeSet.getCode();
          boolean matchesCode = false;
          for (final CodeType codeType : codeList) {
            if (value.equals(codeType.getValue())) {
              matchesCode = true;
              break;
            }
          }
          if (!matchesCode) {
            testException.addDetail("Invalid code in field " + id,
                "in codeSet " + codeSet.getName(), value);
          }

        }
      } catch (final FieldNotFound e) {
        // already tested for presence
      }
    }
  }

  private void validateFieldMap(FieldMap fieldMap, TestException testException,
      List<Object> members) {
    for (final Object member : members) {
      if (member instanceof FieldRefType) {
        final FieldRefType fieldRefType = (FieldRefType) member;
        validateField(fieldMap, testException, fieldRefType);
      } else if (member instanceof GroupRefType) {
        final GroupRefType groupRefType = (GroupRefType) member;
        final GroupType groupType = repositoryAdapter.getGroup(groupRefType);
        final List<Group> groups = fieldMap.getGroups(groupType.getNumInGroup().getId().intValue());
        for (final Group group : groups) {
          validateFieldMap(group, testException, groupType.getComponentRefOrGroupRefOrFieldRef());
        }
      } else if (member instanceof ComponentRefType) {
        final ComponentRefType componentRefType = (ComponentRefType) member;
        final ComponentType component = repositoryAdapter.getComponent(componentRefType);
        if (!component.getName().equals("StandardHeader")
            && !component.getName().equals("StandardTrailer"))
          validateFieldMap(fieldMap, testException,
              component.getComponentRefOrGroupRefOrFieldRef());
      }
    }
  }

  MessageType getMessage(String name) {
    return repositoryAdapter.getMessage(name, "base");
  }

}
