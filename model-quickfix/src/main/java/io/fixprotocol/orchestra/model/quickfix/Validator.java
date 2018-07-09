package io.fixprotocol.orchestra.model.quickfix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiPredicate;

import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldRuleType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.dsl.antlr.SemanticErrorListener;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import io.fixprotocol.orchestra.model.TestException;
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
 * Only validates the message body, not session level header and trailer.
 * This implementation is a demonstration of capabilities. There is no claim to high performance.
 * 
 * @author Don Mendelson
 *
 */
public class Validator implements io.fixprotocol.orchestra.model.Validator<Message> {

  private class ErrorListener implements SemanticErrorListener {

    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    @Override
    public void onError(String msg) {
      try {
        messages.put(msg);
      } catch (InterruptedException e) {
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
          } catch (ScoreException e) {
            testException.addDetail(e.getMessage());
          }
          return false;
        }
      };
  
  private final RepositoryAdapter repositoryAdapter;
  
  private final SymbolResolver symbolResolver;

  public Validator(RepositoryAdapter repositoryAdapter, SymbolResolver symbolResolver) {
    this.repositoryAdapter = repositoryAdapter;
    this.symbolResolver = symbolResolver;
    evaluator = new Evaluator(symbolResolver, errorListener );
  }

  @Override
  public void validate(Message message, MessageType messageType) throws TestException {
    TestException testException =
        new TestException("Invalid message type " + messageType.getName());
    try (final MessageScope messageScope =
        new MessageScope(message, messageType, repositoryAdapter, symbolResolver, evaluator)) {
      symbolResolver.nest(new PathStep("in."), messageScope);
      try (Scope local = (Scope) symbolResolver.resolve(SymbolResolver.LOCAL_ROOT)) {
        local.nest(new PathStep(messageType.getName()), messageScope);

        List<Object> members = repositoryAdapter.getMessageMembers(messageType);

        validateFieldMap(message, testException, members);
      }
    } catch (Exception e) {
      throw new RuntimeException("Internal error", e);
    } finally {
      if (testException.hasDetails()) {
        throw testException;
      }
    }
  }

  private void validateField(FieldMap fieldMap, TestException testException,
      FieldRefType fieldRefType) {
    int id = fieldRefType.getId().intValue();
    PresenceT presence = fieldRefType.getPresence();
    String dataTypeString = repositoryAdapter.getFieldDatatype(id);
    CodeSetType codeSet = repositoryAdapter.getCodeset(dataTypeString);
    if (codeSet != null) {
      symbolResolver.nest(new PathStep("^"), new CodeSetScope(codeSet) );
    }
    boolean isPresentInMessage = fieldMap.isSetField(id);

    switch (presence) {
      case CONSTANT:
        break;
      case FORBIDDEN:
        if (isPresentInMessage) {
          testException.addDetail("Forbidden field " + id + " is present", "FORBIDDEN", "present");
        }
        break;
      case IGNORED:
        break;
      case OPTIONAL:
        // Evaluate rules if present
        List<FieldRuleType> rules = fieldRefType.getRule();
        for (FieldRuleType rule : rules) {
          String when = rule.getWhen();
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
        String value = fieldMap.getString(id);
        String datatypeName = repositoryAdapter.getFieldDatatype(id);
        Datatype datatype = repositoryAdapter.getDatatype(datatypeName);
        if (datatype == null) {
          List<CodeType> codeList = codeSet.getCode();
          boolean matchesCode = false;
          for (CodeType codeType : codeList) {
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
      } catch (FieldNotFound e) {
        // already tested for presence
      }
    }
  }

  private void validateFieldMap(FieldMap fieldMap, TestException testException,
      List<Object> members) {
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        validateField(fieldMap, testException, fieldRefType);
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRefType = (GroupRefType) member;
        GroupType groupType = repositoryAdapter.getGroup(groupRefType);       
        List<Group> groups = fieldMap.getGroups(groupType.getNumInGroupId().intValue());
        for (Group group : groups) {
          validateFieldMap(group, testException,
              groupType.getComponentRefOrGroupRefOrFieldRef());
        }
      } else if (member instanceof ComponentRefType) {
        ComponentRefType componentRefType = (ComponentRefType) member;
        ComponentType component = repositoryAdapter.getComponent(componentRefType);
        if (!component.getName().equals("StandardHeader") && !component.getName().equals("StandardTrailer"))
        validateFieldMap(fieldMap, testException,
            component.getComponentRefOrGroupRefOrFieldRef());
      }
    }
  }

  MessageType getMessage(String name) {
    return repositoryAdapter.getMessage(name, "base");
  }

}
