package io.fixprotocol.orchestra.model.quickfix;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeSets;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.Datatypes;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldRuleType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.dsl.antlr.FixValue;
import io.fixprotocol.orchestra.dsl.antlr.PathStep;
import io.fixprotocol.orchestra.dsl.antlr.ScoreLexer;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser;
import io.fixprotocol.orchestra.dsl.antlr.ScoreVisitorImpl;
import io.fixprotocol.orchestra.dsl.antlr.SymbolResolver;
import io.fixprotocol.orchestra.dsl.antlr.ScoreParser.AnyExpressionContext;
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


  private final Repository repository;
  private SymbolResolver symbolResolver;
  private ScoreVisitorImpl visitor;

  public Validator(Repository repository) {
    this.repository = repository;
    symbolResolver = new SymbolResolver();
    visitor = new ScoreVisitorImpl(symbolResolver);
  }

  @Override
  public void validate(Message message, MessageType messageType) throws TestException {
    symbolResolver.nest(new PathStep("in."), new MessageScope(message, messageType, this));

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
            // Evaluate rule
            List<FieldRuleType> rules = fieldRefType.getRule();
            for (FieldRuleType rule : rules) {
              String when = rule.getWhen();
              ScoreParser parser;
              try {
                parser = parse(when);
                AnyExpressionContext ctx = parser.anyExpression();
                FixValue<?> expression = visitor.visitAnyExpression(ctx);
                if ((Boolean) expression.getValue() && !isPresentInMessage) {
                  testException.addDetail("Missing required field " + id, "REQUIRED",
                      "(not present)");
                }
              } catch (IOException e) {
                throw new TestException("Unable to parse conditional rule", e);
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
            String datatypeName = getFieldDatatype(id);
            Datatype datatype = getDatatype(datatypeName);
            if (datatype == null) {
              CodeSetType codeSet = getCodeset(datatypeName);
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

  private ScoreParser parse(String expression) throws IOException {
    ScoreLexer l = new ScoreLexer(new ANTLRInputStream(new StringReader(expression)));
    ScoreParser p = new ScoreParser(new CommonTokenStream(l));
    p.addErrorListener(new BaseErrorListener() {
      @Override
      public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, RecognitionException e) {
        throw new IllegalStateException(String.format(
            "Failed to parse at line %d position %d due to %s", line, charPositionInLine, msg), e);
      }
    });
    return p;
  }

  CodeSetType getCodeset(String name) {
    List<CodeSets> allCodeSets = repository.getCodeSets();
    for (CodeSets codeSets : allCodeSets) {
      List<CodeSetType> codeSetList = codeSets.getCodeSet();
      for (CodeSetType codeSet : codeSetList) {
        if (name.equals(codeSet.getName())) {
          return codeSet;
        }
      }
    }
    return null;
  }

  Datatype getDatatype(String name) {
    Datatypes datatypes = repository.getDatatypes();
    List<Datatype> datatypeList = datatypes.getDatatype();
    for (Datatype datatype : datatypeList) {
      if (name.equals(datatype.getName())) {
        return datatype;
      }
    }
    return null;
  }

  String getFieldDatatype(int id) {
    List<FieldType> fields = repository.getFields().getField();
    for (FieldType fieldType : fields) {
      if (fieldType.getId().intValue() == id) {
        return fieldType.getType();
      }
    }
    return null;
  }

  MessageType getMessage(String name) {
    return getMessage(name, "base");
  }

  MessageType getMessage(String name, String context) {
    List<MessageType> messageList = repository.getProtocol().get(0).getMessages().getMessage();
    for (MessageType messageType : messageList) {
      if (name.equals(messageType.getName()) && context.equals(messageType.getScenario())) {

        return messageType;
      }
    }
    return null;
  }
}
