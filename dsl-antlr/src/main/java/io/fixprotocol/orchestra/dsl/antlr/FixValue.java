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
package io.fixprotocol.orchestra.dsl.antlr;

import java.math.BigDecimal;

/**
 * Represents an immutable value of a DSL expression
 * 
 * @author Don Mendelson
 *
 */
public class FixValue<T> {

  private final String name;
  private final FixType type;
  private final T value;

  /**
   * Construct an unnamed expression value
   * 
   * @param name named value
   * @param type a FIX data type
   * @param value an immutable value
   */
  public FixValue(FixType type, T value) {
    this(null, type, value);
  }

  /**
   * Construct a named expression value
   * 
   * @param name named value
   * @param type a FIX data type
   * @param value an immutable value
   */
  public FixValue(String name, FixType type, T value) {
    this.name = name;
    this.type = type;
    this.value = value;
  }

  /**
   * Add another FixValue to this one
   * 
   * @param operand another value
   * @return a new immutable FixValue of appropriate type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> add(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Integer>(resultType, add((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<BigDecimal>(resultType,
            add((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;
  }

  /**
   * Boolean and operation
   * 
   * @param operand another logical value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if operand types are not Boolean
   */
  public FixValue<?> and(FixValue<?> operand) {
    if (this.getType() != FixType.BooleanType || operand.getType() != FixType.BooleanType) {
      throw new IllegalArgumentException("Logical and operand not Boolean");
    }

    return new FixValue<Boolean>(FixType.BooleanType,
        (Boolean) this.getValue() && (Boolean) operand.getValue());
  }

  /**
   * Divide another FixValue into this one
   * 
   * @param divisor another value
   * @return a new immutable FixValue of appropriate type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */

  public FixValue<?> divide(FixValue<?> divisor) {
    FixType resultType = sameFixTypeOrWiden(divisor.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = divisor.getValue();
    if (!divisor.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(divisor.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Integer>(resultType, divide((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<BigDecimal>(resultType,
            divide((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;

  }

  /**
   * Equality comparison
   * 
   * @param operand another value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> eq(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Boolean>(FixType.BooleanType,
            eq((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<Boolean>(FixType.BooleanType,
            eq((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;

  }

  /**
   * Greater-than-or-equal comparison
   * 
   * @param operand another value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> ge(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Boolean>(FixType.BooleanType,
            ge((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<Boolean>(FixType.BooleanType,
            ge((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;

  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the type
   */
  public FixType getType() {
    return type;
  }

  /**
   * @return the value
   */
  public T getValue() {
    return value;
  }

  /**
   * Greater-than comparison
   * 
   * @param operand another value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> gt(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Boolean>(FixType.BooleanType,
            gt((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<Boolean>(FixType.BooleanType,
            gt((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;

  }

  /**
   * Less-than-or-equal comparison
   * 
   * @param operand another value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> le(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Boolean>(FixType.BooleanType,
            le((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<Boolean>(FixType.BooleanType,
            le((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;

  }

  /**
   * Less-than comparison
   * 
   * @param operand another value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> lt(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Boolean>(FixType.BooleanType,
            lt((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<Boolean>(FixType.BooleanType,
            lt((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;
  }

  /**
   * Modulo operation
   * 
   * @param divisor value
   * @return a new immutable FixValue of appropriate type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> mod(FixValue<?> divisor) {
    FixType resultType = sameFixTypeOrWiden(divisor.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = divisor.getValue();
    if (!divisor.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(divisor.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Integer>(resultType, mod((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<Integer>(resultType, mod((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;

  }

  /**
   * Multiply another FixValue by this one
   * 
   * @param operand another value
   * @return a new immutable FixValue of appropriate type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> multiply(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Integer>(resultType, multiply((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<BigDecimal>(resultType,
            multiply((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;
  }

  /**
   * Not-equal comparison
   * 
   * @param operand another value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> ne(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Boolean>(FixType.BooleanType,
            ne((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<Boolean>(FixType.BooleanType,
            ne((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;
  }

  /**
   * Boolean negation
   * 
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> not() {
    if (this.getType() != FixType.BooleanType) {
      throw new IllegalArgumentException("Logical not operand not Boolean");
    }

    return new FixValue<Boolean>(FixType.BooleanType, !(Boolean) this.getValue());
  }

  /**
   * Boolean or operation
   * 
   * @param operand another logical value
   * @return a new immutable FixValue of Boolean type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> or(FixValue<?> operand) {
    if (this.getType() != FixType.BooleanType || operand.getType() != FixType.BooleanType) {
      throw new IllegalArgumentException("Logical or operand not Boolean");
    }

    return new FixValue<Boolean>(FixType.BooleanType,
        (Boolean) this.getValue() || (Boolean) operand.getValue());
  }

  /**
   * Subtract another FixValue from this one
   * 
   * @param operand another value
   * @return a new immutable FixValue of appropriate type
   * @exception IllegalArgumentException if data types are incompatible
   * @exception AritmeticException if an arithmetic error occurs
   */
  public FixValue<?> subtract(FixValue<?> operand) {
    FixType resultType = sameFixTypeOrWiden(operand.getType());
    Class<?> resultClass = resultType.getValueClass();
    Object operand1 = value;
    if (!value.getClass().isAssignableFrom(resultClass)) {
      operand1 = promote(value, resultClass);
    }
    Object operand2 = operand.getValue();
    if (!operand.getValue().getClass().isAssignableFrom(resultClass)) {
      operand2 = promote(operand.getValue(), resultClass);
    }
    switch (resultClass.getName()) {
      case "java.lang.Integer":
        return new FixValue<Integer>(resultType, subtract((Integer) operand1, (Integer) operand2));
      case "java.math.BigDecimal":
        return new FixValue<BigDecimal>(resultType,
            subtract((BigDecimal) operand1, (BigDecimal) operand2));
    }
    return null;
  }

  private BigDecimal add(BigDecimal operand1, BigDecimal operand2) {
    return operand1.add(operand2);
  }

  private Integer add(Integer operand1, Integer operand2) {
    return operand1 + operand2;
  }

  private BigDecimal divide(BigDecimal operand1, BigDecimal operand2) {
    return operand1.divide(operand2);
  }

  private Integer divide(Integer operand1, Integer operand2) {
    return operand1 / operand2;
  }

  private Boolean eq(BigDecimal operand1, BigDecimal operand2) {
    return operand1.equals(operand2);
  }

  private Boolean eq(Integer operand1, Integer operand2) {
    return operand1 == operand2;
  }

  private Boolean ge(BigDecimal operand1, BigDecimal operand2) {
    return operand1.compareTo(operand2) >= 0;
  }

  private Boolean ge(Integer operand1, Integer operand2) {
    return operand1 >= operand2;
  }

  private Boolean gt(BigDecimal operand1, BigDecimal operand2) {
    return operand1.compareTo(operand2) > 0;
  }

  private Boolean gt(Integer operand1, Integer operand2) {
    return operand1 > operand2;
  }

  private Boolean le(BigDecimal operand1, BigDecimal operand2) {
    return operand1.compareTo(operand2) <= 0;
  }

  private Boolean le(Integer operand1, Integer operand2) {
    return operand1 <= operand2;
  }

  private Boolean lt(BigDecimal operand1, BigDecimal operand2) {
    return operand1.compareTo(operand2) < 0;
  }

  private Boolean lt(Integer operand1, Integer operand2) {
    return operand1 < operand2;
  }

  private Integer mod(BigDecimal operand1, BigDecimal operand2) {
    return mod(operand1.intValue(), operand2.intValue());
  }

  private Integer mod(Integer operand1, Integer operand2) {
    return operand1 % operand2;
  }

  private BigDecimal multiply(BigDecimal operand1, BigDecimal operand2) {
    return operand1.multiply(operand2);
  }

  private Integer multiply(Integer operand1, Integer operand2) {
    return operand1 * operand2;
  }

  private Boolean ne(BigDecimal operand1, BigDecimal operand2) {
    return !operand1.equals(operand2);
  }

  private Boolean ne(Integer operand1, Integer operand2) {
    return operand1 != operand2;
  }

  private Object promote(Object original, Class<?> resultClass) {
    switch (original.getClass().getName()) {
      case "java.lang.Integer":
        switch (resultClass.getName()) {
          case "java.math.BigDecimal":
            return new BigDecimal(((Integer) original).intValue());
        }
        break;

    }
    return null;
  }

  private FixType sameFixTypeOrWiden(FixType operandType) {
    FixType resultType = this.type;
    if (this.type.getBaseType() == operandType
        || this.type.getBaseType() == operandType.getBaseType()) {
      resultType = this.type.getBaseType();
    } else {
      throw new IllegalArgumentException(
          String.format("Data type mismatch between %s and %s", this.type, operandType));
    }
    return resultType;
  }

  private BigDecimal subtract(BigDecimal operand1, BigDecimal operand2) {
    return operand1.subtract(operand2);
  }

  private Integer subtract(Integer operand1, Integer operand2) {
    return operand1 - operand2;
  }
}
