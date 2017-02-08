/**
 * Copyright 2017 FIX Protocol Ltd
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol.orchestra.dsl.antlr.FixException;
import io.fixprotocol.orchestra.dsl.antlr.FixNode;
import io.fixprotocol.orchestra.dsl.antlr.FixType;
import io.fixprotocol.orchestra.dsl.antlr.FixValue;
import io.fixprotocol.orchestra.dsl.antlr.PathStep;
import io.fixprotocol.orchestra.dsl.antlr.Scope;
import quickfix.BytesField;
import quickfix.FieldNotFound;
import quickfix.Message;

/**
 * @author Don Mendelson
 *
 */
class MessageScope implements Scope {

  private final Message message;
  private final MessageType messageType;
  private Validator validator;

  /**
   * @param message
   * @param messageType
   * @param validator
   * 
   */
  public MessageScope(Message message, MessageType messageType, Validator validator) {
    this.message = message;
    this.messageType = messageType;
    this.validator = validator;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fixprotocol.orchestra.dsl.antlr.FixNode#getName()
   */
  @Override
  public String getName() {
    return messageType.getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#assign(io.fixprotocol.orchestra.dsl.antlr.PathStep,
   * io.fixprotocol.orchestra.dsl.antlr.FixValue)
   */
  @Override
  public FixValue<?> assign(PathStep arg0, FixValue<?> arg1) throws FixException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fixprotocol.orchestra.dsl.antlr.Scope#nest(io.fixprotocol.orchestra.dsl.antlr.PathStep,
   * io.fixprotocol.orchestra.dsl.antlr.Scope)
   */
  @Override
  public void nest(PathStep arg0, Scope arg1) {
    throw new UnsupportedOperationException("Message structure is immutable");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#resolve(io.fixprotocol.orchestra.dsl.antlr.PathStep)
   */
  @SuppressWarnings("unchecked")
  @Override
  public FixNode resolve(PathStep pathStep) {
    String name = pathStep.getName();
    List<Object> members = messageType.getStructure().getComponentOrComponentRefOrGroup();
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        if (fieldRefType.getName().equals(name)) {
          FixValue<?> fixValue = null;
          BigInteger id = fieldRefType.getId();
          String dataTypeString = validator.getFieldDatatype(id.intValue());
          FixType dataType = FixType.valueOf(dataTypeString);
          try {
            switch (dataType) {
              case StringType:
              case MultipleCharValue:
              case MultipleStringValue:
              case Country:
              case Currency:
              case Exchange:
              case MonthYear:
              case XMLData:
              case Language:
                fixValue = new FixValue<String>(name, dataType);
                ((FixValue<String>) fixValue).setValue(message.getString(id.intValue()));
                break;
              case BooleanType:
                fixValue = new FixValue<Boolean>(name, dataType);
                ((FixValue<Boolean>) fixValue).setValue(message.getBoolean(id.intValue()));
                break;
              case charType:
                fixValue = new FixValue<Character>(name, dataType);
                ((FixValue<Character>) fixValue).setValue(message.getChar(id.intValue()));
                break;
              case intType:
              case Length:
              case TagNum:
              case SeqNum:
              case NumInGroup:
              case DayOfMonth:
                fixValue = new FixValue<Integer>(name, dataType);
                ((FixValue<Integer>) fixValue).setValue(message.getInt(id.intValue()));
                break;
              case Amt:
              case floatType:
              case Qty:
              case Price:
              case PriceOffset:
              case Percentage:
                fixValue = new FixValue<BigDecimal>(name, dataType);
                ((FixValue<BigDecimal>) fixValue).setValue(message.getDecimal(id.intValue()));
                break;
              case UTCTimestamp:
              case TZTimestamp:
                fixValue = new FixValue<Instant>(name, dataType);
                ((FixValue<Instant>) fixValue)
                    .setValue(message.getUtcTimeStamp(id.intValue()).toInstant());
                break;
              case UTCTimeOnly:
              case TZTimeOnly:
              case LocalMktTime:
                fixValue = new FixValue<LocalTime>(name, dataType);
                ((FixValue<LocalTime>) fixValue)
                    .setValue(LocalTime.from(message.getUtcTimeOnly(id.intValue()).toInstant()));
                break;
              case UTCDateOnly:
              case LocalMktDate:
                fixValue = new FixValue<LocalDate>(name, dataType);
                ((FixValue<LocalDate>) fixValue)
                    .setValue(LocalDate.from(message.getUtcTimeOnly(id.intValue()).toInstant()));
                break;
              case data:
                fixValue = new FixValue<byte[]>(name, dataType);
                BytesField bytesField = new BytesField(id.intValue());
                message.getField(bytesField);
                ((FixValue<byte[]>) fixValue).setValue(bytesField.getValue());
                break;
            }
          } catch (FieldNotFound e) {
            // value remains null
          }
          return fixValue;
        }
      }
      // todo return nested scope for GroupRefType ComponentRefType
    }
    return null;
  }

}
