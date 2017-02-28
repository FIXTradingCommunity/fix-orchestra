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
package io.fixprotocol.orchestra.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Creates new instances of {@link FixValue} 
 * <br/>
 * Workaround for Java erasure. Class of a generic must be literal.
 * 
 * @author Don Mendelson
 *
 */
public final class FixValueFactory {

  /**
   * Create a new instance of {@link FixValue}
   * @param name name of the instance, may be {@code null}
   * @param type the FIX data type represented
   * @param valueClass the storage class for value
   * @return a new instance
   * @throws ModelException  if the type cannot be created
   */
  @SuppressWarnings("rawtypes")
  public static FixValue create(String name, FixType type, Class<? extends Object> valueClass) throws ModelException {
    Objects.requireNonNull(type, "FIX type missing");
    Objects.requireNonNull(valueClass, "Value class missing");
    
    switch (valueClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Integer>(name, type);
      case "java.lang.String":
        return new FixValue<String>(name, type);
      case "java.math.BigDecimal":
        return new FixValue<BigDecimal>(name, type);
      case "java.lang.Boolean":
        return new FixValue<Boolean>(name, type);
      case "java.lang.Character":
        return new FixValue<Character>(name, type);
      case "[B":
        return new FixValue<byte []>(name, type);
      case "java.time.Instant":
        return new FixValue<Instant>(name, type);
      case "java.time.LocalDate":
        return new FixValue<LocalDate>(name, type);
      case "java.time.LocalTime":
        return new FixValue<LocalTime>(name, type);
      case "java.time.ZonedDateTime":
        return new FixValue<ZonedDateTime>(name, type);
      default:
        throw new ModelException("Unable to create type " + valueClass.getName());
    }
  }
  
  /**
   * Creates a new object with the specified name and value of the operand
   * 
   * @param name identifier of the new object
   * @param operand value to copy
   * @return a new FixValue instance
   * @throws ModelException if the data type is not handled
   */
  public static FixValue<?> copy(String name, FixValue<?> operand) throws ModelException {
    String valueClassname = operand.getValue().getClass().getName();
    FixType type = operand.getType();
    switch (valueClassname) {
      case "java.lang.Integer":
        return new FixValue<Integer>(name, type, (Integer)operand.getValue());
      case "java.lang.String":
        return new FixValue<String>(name, type, (String)operand.getValue());
      case "java.math.BigDecimal":
        return new FixValue<BigDecimal>(name, type, (BigDecimal)operand.getValue());
      case "java.lang.Boolean":
        return new FixValue<Boolean>(name, type, (Boolean)operand.getValue());
      case "java.lang.Character":
        return new FixValue<Character>(name, type, (Character)operand.getValue());
      case "[B":
        return new FixValue<byte []>(name, type, (byte [])operand.getValue());
      case "java.time.Instant":
        return new FixValue<Instant>(name, type, (Instant)operand.getValue());
      case "java.time.LocalDate":
        return new FixValue<LocalDate>(name, type, (LocalDate)operand.getValue());
      case "java.time.LocalTime":
        return new FixValue<LocalTime>(name, type, (LocalTime)operand.getValue());
      case "java.time.ZonedDateTime":
        return new FixValue<ZonedDateTime>(name, type, (ZonedDateTime)operand.getValue());
      default:
        throw new ModelException("Unable to copy type " + valueClassname);
    }

   }

}
