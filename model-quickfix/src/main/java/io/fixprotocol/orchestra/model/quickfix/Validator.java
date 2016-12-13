package io.fixprotocol.orchestra.model.quickfix;

import java.util.List;

import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeSets;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.Datatypes;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.model.TestException;
import quickfix.FieldNotFound;
import quickfix.Message;

/**
 * Validates a FIX message against an Orchestra file
 * <p>
 * Features:
 * <ul>
 * <li>Checks field presence</li>
 * <li>Checks code membership in a codeSet</li>
 * </ul>
 * This implementation is a demonstration of capabilities. There is no claim to high performance.
 * <p>
 * todo: 
 * <ul>
 * <li>Check range min/max</li>
 * <li>Check length min/max</li>
 * <li>Check conditional rules</li>
 * </ul>
 * @author Don Mendelson
 *
 */
public class Validator implements io.fixprotocol.orchestra.model.Validator<Message> {


  private final Repository repository;

  public Validator(Repository repository) {
    this.repository = repository;
  }

  @Override
  public void validate(Message message, MessageType messageType) throws TestException {
    TestException testException = new TestException("Invalid message type " + messageType.getName());
    List<Object> elements = messageType.getStructure().getComponentOrComponentRefOrGroup();

    for (Object element : elements) {
      if (element instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) element;
        int id = fieldRefType.getId().intValue();
        PresenceT presence = fieldRefType.getPresence();

        boolean isPresentInMessage = message.isSetField(id);
        switch (presence) {
          case CONDITIONAL:
            // todo: evaluate rule
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
                testException.addDetail("Invalid code in field " + id, "in codeSet " + codeSet.getName(),
                    value);
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
      if (name.equals(messageType.getName()) &&
          context.equals(messageType.getContext())) {
        
        return messageType;
      }
    }
    return null;
  }
}
