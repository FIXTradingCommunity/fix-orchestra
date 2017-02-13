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
import java.util.Objects;

/**
 * Represents an mutable value of a DSL expression <br/>
 * If immutable symbols are desired, control of {@link #assign(FixValue)} must be guarded
 * externally.
 * 
 * @author Don Mendelson
 * 
 * TODO: handling for missing or null value
 */
public class FixValue<T> implements FixNode {

  /**
   * Creates a new object with the specified name and values of the operand
   * 
   * @param name identifier of the new object
   * @param operand value to copy
   * @return a new FixValue instance
   * @throws ScoreException if the data type is not handled
   */
  public static FixValue<?> copy(String name, FixValue<?> operand) throws ScoreException {
    String valueType = operand.getValue().getClass().getName();
    switch (valueType) {
      case "java.lang.Integer":
        return new FixValue<Integer>(name, operand.getType(), (Integer) operand.getValue());
      case "java.lang.Boolean":
        return new FixValue<Boolean>(name, operand.getType(), (Boolean) operand.getValue());
      case "java.lang.Character":
        return new FixValue<Character>(name, operand.getType(), (Character) operand.getValue());
      case "java.lang.String":
        return new FixValue<String>(name, operand.getType(), (String) operand.getValue());
      case "java.math.BigDecimal":
        return new FixValue<BigDecimal>(name, operand.getType(), (BigDecimal) operand.getValue());
      default:
        throw new ScoreException("Unable to copy type " + valueType);
    }
  }

  private final String name;
  private final FixType type;
  private T value;

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
   * Construct with a name only for search
   * 
   * @param name message component name
   */
  public FixValue(String name) {
    this(name, null, null);
  }

  /**
   * Construct a named expression without value
   * 
   * @param name named value
   * @param type a FIX data type
   */
  public FixValue(String name, FixType type) {
    this(name, type, null);
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
    Objects.requireNonNull(operand, "Missing operand");
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
    Objects.requireNonNull(operand, "Missing operand");
    if (this.getType() != FixType.BooleanType || operand.getType() != FixType.BooleanType) {
      throw new IllegalArgumentException("Logical and operand not Boolean");
    }

    return new FixValue<Boolean>(FixType.BooleanType,
        (Boolean) this.getValue() && (Boolean) operand.getValue());
  }


  /**
   * Assigns a value to this FixValue
   * 
   * @param operand other FixValue
   * @throws ScoreException if a type conflict occurs
   */
  @SuppressWarnings("unchecked")
  public void assign(FixValue<?> operand) throws ScoreException {
    Objects.requireNonNull(operand, "Missing operand");
    if (this.type.getBaseType() != operand.getType().getBaseType()) {
      throw new ScoreException(
          String.format("Data type mismatch between %s and %s", this.type, operand.getType()));
    }
    this.value = (T) operand.getValue();
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
    Objects.requireNonNull(divisor, "Missing operand");
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
    Objects.requireNonNull(operand, "Missing operand");
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @SuppressWarnings("rawtypes")
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    FixValue other = (FixValue) obj;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
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
    Objects.requireNonNull(operand, "Missing operand");
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
    Objects.requireNonNull(operand, "Missing operand");
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
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
    Objects.requireNonNull(operand, "Missing operand");
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
    Objects.requireNonNull(operand, "Missing operand");
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
    Objects.requireNonNull(divisor, "Missing operand");
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
    Objects.requireNonNull(operand, "Missing operand");
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
    Objects.requireNonNull(operand, "Missing operand");
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
    Objects.requireNonNull(operand, "Missing operand");
    if (this.getType() != FixType.BooleanType || operand.getType() != FixType.BooleanType) {
      throw new IllegalArgumentException("Logical or operand not Boolean");
    }

    return new FixValue<Boolean>(FixType.BooleanType,
        (Boolean) this.getValue() || (Boolean) operand.getValue());
  }


  /**
   * @param value the value to set
   */
  public void setValue(T value) {
    this.value = value;
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
    Objects.requireNonNull(operand, "Missing operand");
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

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "FixValue [" + (name != null ? "name=" + name + ", " : "")
        + (type != null ? "type=" + type + ", " : "") + (value != null ? "value=" + value : "")
        + "]";
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
    return operand1.compareTo(operand2) == 0;
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
    } else if ((this.type.getBaseType() == FixType.intType
        || this.type.getBaseType() == FixType.floatType)
        && (operandType.getBaseType() == FixType.intType
            || operandType.getBaseType() == FixType.floatType)) {
      // special case: integer promotes to float, not specified in FIX
      resultType = FixType.floatType;
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
