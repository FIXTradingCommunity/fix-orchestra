package io.fixprotocol.orchestra.model.quickfix;

import java.util.List;
import java.util.function.BiPredicate;

import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldRuleType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.FixValue;
import io.fixprotocol.orchestra.dsl.antlr.PathStep;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.dsl.antlr.SymbolResolver;
import io.fixprotocol.orchestra.model.TestException;
import quickfix.FieldNotFound;
import quickfix.Message;

/**
 * Validates a FIX message against an Orchestra file
 * <p>
 * Features:
 * <ul>
 * <li>Checks field presence including conditionally required field rule</li>
 * <li>Checks code membership in a codeSet</li>
 * <li>
 * </ul>
 * This implementation is a demonstration of capabilities. There is no claim to high performance.
 * 
 * @author Don Mendelson
 *
 */
public class Validator implements io.fixprotocol.orchestra.model.Validator<Message> {

  private final Evaluator evaluator;
  private final RepositoryAdapter repositoryAdapter;
  private final SymbolResolver symbolResolver;

  private final BiPredicate<String, TestException> predicateEvaluator =
      new BiPredicate<String, TestException>() {
        public boolean test(String expression, TestException testException) {
          FixValue<?> fixValue;
          try {
            fixValue = evaluator.evaluate(expression);
            if (fixValue.getValue() == Boolean.TRUE) {
              return true;
            }
          } catch (ScoreException e) {
            testException.addDetail(e.getMessage());
          }
          return false;
        }
      };

  public Validator(RepositoryAdapter repositoryAdapter, SymbolResolver symbolResolver) {
    this.repositoryAdapter = repositoryAdapter;
    this.symbolResolver = symbolResolver;
    evaluator = new Evaluator(symbolResolver);
  }

  @Override
  public void validate(Message message, MessageType messageType) throws TestException {
    symbolResolver.nest(new PathStep("in."),
        new MessageScope(message, messageType, repositoryAdapter, symbolResolver, evaluator));

    TestException testException =
        new TestException("Invalid message type " + messageType.getName());
    List<Object> elements = messageType.getStructure().getComponentOrComponentRefOrGroup();

    for (Object element : elements) {
      if (element instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) element;
        int id = fieldRefType.getId().intValue();
        PresenceT presence = fieldRefType.getPresence();

        boolean isPresentInMessage = message.isSetField(id);
        switch (presence) {
          case CONDITIONAL:
            // TODO check for default value if not present

            // Evaluate rule
            List<FieldRuleType> rules = fieldRefType.getRule();
            for (FieldRuleType rule : rules) {
              String when = rule.getWhen();
              if (predicateEvaluator.test(when, testException) && !isPresentInMessage) {
                testException.addDetail("Missing required field " + id, "REQUIRED",
                    "(not present)");
              }
            }
            break;
          case CONSTANT:
            break;
          case FORBIDDEN:
            if (isPresentInMessage) {
              testException.addDetail("Forbidden field " + id + " is present", "FORBIDDEN",
                  "present");
            }
            break;
          case IGNORED:
            break;
          case OPTIONAL:
            break;
          case REQUIRED:
            if (!isPresentInMessage) {
              testException.addDetail("Missing required field " + id, "REQUIRED", "(not present)");
            }
            break;
        }

        if (isPresentInMessage) {
          try {
            String value = message.getString(id);
            String datatypeName = repositoryAdapter.getFieldDatatype(id);
            Datatype datatype = repositoryAdapter.getDatatype(datatypeName);
            if (datatype == null) {
              CodeSetType codeSet = repositoryAdapter.getCodeset(datatypeName);
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
    }
    /*
     * {@link ComponentType } {@link ComponentRefType } {@link GroupType } {@link GroupRefType }
     * {@link FieldType } {@link FieldRefType }
     */

    if (testException.hasDetails()) {
      throw testException;
    }
  }

  MessageType getMessage(String name) {
    return repositoryAdapter.getMessage(name, "base");
  }

}
