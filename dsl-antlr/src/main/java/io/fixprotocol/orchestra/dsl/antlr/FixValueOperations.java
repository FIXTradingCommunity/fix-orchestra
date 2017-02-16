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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Operations on {@link FixValue} <br/>
 * This implementation has many workarounds for Java type erasure. If raw types are removed from
 * Java in the future as suggested, this code will break. The expectation is that change will be
 * accompanied by reified types.
 * 
 * @author Don Mendelson
 */
class FixValueOperations {

  private static class Operation extends OperationKey {
    /**
     * Lookup for double dispatch with swapped operands
     * 
     * @param key types of operands
     * @param operations gives function to dispatch
     * @return operation to dispatch
     */
    static Operation commutativeMatch(OperationKey key, Operation[] operations) {
      for (Operation operation : operations) {
        if (operation.fixType1 == key.fixType2 && operation.fixType2 == key.fixType1
            && operation.valueType1 == key.valueType2 && operation.valueType2 == key.valueType1) {
          return operation;
        }
      }
      return null;
    }

    /**
     * Lookup for double dispatch
     * 
     * @param key types of operands
     * @param operations gives function to dispatch
     * @return operation to dispatch
     */
    static Operation exactMatch(OperationKey key, Operation[] operations) {
      for (Operation operation : operations) {
        if (operation.fixType1 == key.fixType1 && operation.fixType2 == key.fixType2
            && operation.valueType1 == key.valueType1 && operation.valueType2 == key.valueType2) {
          return operation;
        }
      }
      return null;
    }

    @SuppressWarnings("rawtypes")
    BiFunction evaluate;
    FixType resultType;
    Class<? extends Object> resultValueType;

    @SuppressWarnings("rawtypes")
    Operation(FixType fixType1, FixType fixType2, FixType resultType,
        Class<? extends Object> valueType1, Class<? extends Object> valueType2,
        Class<? extends Object> resultValueType, BiFunction evaluate) {
      super(fixType1, fixType2, valueType1, valueType2);
      this.resultType = resultType;
      this.resultValueType = resultValueType;
      this.evaluate = evaluate;
    }
  }

  private static class OperationKey {
    FixType fixType1;
    FixType fixType2;
    Class<? extends Object> valueType1;
    Class<? extends Object> valueType2;

    /**
     * @param fixType1
     * @param fixType2
     * @param valueType1
     * @param valueType2
     */
    OperationKey(FixType fixType1, FixType fixType2, Class<? extends Object> valueType1,
        Class<? extends Object> valueType2) {
      this.fixType1 = fixType1;
      this.fixType2 = fixType2;
      this.valueType1 = valueType1;
      this.valueType2 = valueType2;
    }
  }

  static final BiFunction<BigDecimal, BigDecimal, BigDecimal> addDecimal = (x, y) -> x.add(y);

  static final BiFunction<Instant, Duration, Instant> addDuration = (x, y) -> x.plus(y);

  static final BiFunction<Integer, Integer, Integer> addInteger = (x, y) -> x + y;

  static final BiFunction<Integer, BigDecimal, BigDecimal> addIntegerDecimal =
      (x, y) -> y.add(BigDecimal.valueOf(x));

  static final BiFunction<BigDecimal, BigDecimal, BigDecimal> divideDecimal = (x, y) -> x.divide(y);

  static final BiFunction<BigDecimal, Integer, BigDecimal> divideDecimalInteger =
      (x, y) -> x.divide(BigDecimal.valueOf(y));

  static final BiFunction<Duration, Integer, Duration> divideDuration = (x, y) -> x.dividedBy(y);

  static final BiFunction<Integer, Integer, Integer> divideInteger = (x, y) -> x / y;

  static final BiFunction<Integer, BigDecimal, Integer> divideIntegerDecimal =
      (x, y) -> x / y.intValue();

  static final BiFunction<Character, Character, Boolean> eqCharacter = (x, y) -> x.equals(y);

  static final BiFunction<BigDecimal, BigDecimal, Boolean> eqDecimal =
      (x, y) -> x.compareTo(y) == 0;

  static final BiFunction<BigDecimal, Integer, Boolean> eqDecimalInteger =
      (x, y) -> x.compareTo(BigDecimal.valueOf(y)) == 0;

  static final BiFunction<Duration, Duration, Boolean> eqDuration = (x, y) -> x.equals(y);

  static final BiFunction<Instant, Instant, Boolean> eqInstant = (x, y) -> x.equals(y);

  static final BiFunction<Integer, Integer, Boolean> eqInteger = (x, y) -> x == y;

  static final BiFunction<Integer, BigDecimal, Boolean> eqIntegerDecimal =
      (x, y) -> y.compareTo(BigDecimal.valueOf(x)) == 0;

  static final BiFunction<LocalDate, LocalDate, Boolean> eqLocalDate = (x, y) -> x.equals(y);

  static final BiFunction<LocalTime, LocalTime, Boolean> eqLocalTime = (x, y) -> x.equals(y);

  static final BiFunction<String, String, Boolean> eqString = (x, y) -> x.equals(y);

  static final BiFunction<ZonedDateTime, ZonedDateTime, Boolean> eqZonedDateTime =
      (x, y) -> x.equals(y);

  static final BiFunction<BigDecimal, BigDecimal, Boolean> geDecimal =
      (x, y) -> x.compareTo(y) >= 0;

  static final BiFunction<BigDecimal, Integer, Boolean> geDecimalInteger =
      (x, y) -> x.compareTo(BigDecimal.valueOf(y)) >= 0;

  static final BiFunction<Duration, Duration, Boolean> geDuration = (x, y) -> x.compareTo(y) >= 0;

  static final BiFunction<Instant, Instant, Boolean> geInstant = (x, y) -> !x.isBefore(y);

  static final BiFunction<Integer, Integer, Boolean> geInteger = (x, y) -> x >= y;

  static final BiFunction<Integer, BigDecimal, Boolean> geIntegerDecimal =
      (x, y) -> BigDecimal.valueOf(x).compareTo(y) >= 0;

  static final BiFunction<LocalDate, LocalDate, Boolean> geLocalDate = (x, y) -> !x.isBefore(y);

  static final BiFunction<LocalTime, LocalTime, Boolean> geLocalTime = (x, y) -> !x.isBefore(y);

  static final BiFunction<ZonedDateTime, ZonedDateTime, Boolean> geZonedDateTime =
      (x, y) -> !x.isBefore(y);

  static final BiFunction<BigDecimal, BigDecimal, Boolean> gtDecimal = (x, y) -> x.compareTo(y) > 0;

  static final BiFunction<BigDecimal, Integer, Boolean> gtDecimalInteger =
      (x, y) -> x.compareTo(BigDecimal.valueOf(y)) > 0;

  static final BiFunction<Duration, Duration, Boolean> gtDuration = (x, y) -> x.compareTo(y) > 0;

  static final BiFunction<Instant, Instant, Boolean> gtInstant = (x, y) -> x.isAfter(y);

  static final BiFunction<Integer, Integer, Boolean> gtInteger = (x, y) -> x > y;

  static final BiFunction<Integer, BigDecimal, Boolean> gtIntegerDecimal =
      (x, y) -> BigDecimal.valueOf(x).compareTo(y) > 0;

  static final BiFunction<LocalDate, LocalDate, Boolean> gtLocalDate = (x, y) -> x.isAfter(y);

  static final BiFunction<LocalTime, LocalTime, Boolean> gtLocalTime = (x, y) -> x.isAfter(y);

  static final BiFunction<ZonedDateTime, ZonedDateTime, Boolean> gtZonedDateTime =
      (x, y) -> x.isAfter(y);


  static final BiFunction<BigDecimal, BigDecimal, Boolean> leDecimal =
      (x, y) -> x.compareTo(y) <= 0;

  static final BiFunction<BigDecimal, Integer, Boolean> leDecimalInteger =
      (x, y) -> x.compareTo(BigDecimal.valueOf(y)) <= 0;

  static final BiFunction<Duration, Duration, Boolean> leDuration = (x, y) -> x.compareTo(y) <= 0;

  static final BiFunction<Instant, Instant, Boolean> leInstant = (x, y) -> !x.isAfter(y);

  static final BiFunction<Integer, Integer, Boolean> leInteger = (x, y) -> x <= y;

  static final BiFunction<Integer, BigDecimal, Boolean> leIntegerDecimal =
      (x, y) -> BigDecimal.valueOf(x).compareTo(y) <= 0;

  static final BiFunction<LocalDate, LocalDate, Boolean> leLocalDate = (x, y) -> !x.isAfter(y);

  static final BiFunction<LocalTime, LocalTime, Boolean> leLocalTime = (x, y) -> !x.isAfter(y);

  static final BiFunction<ZonedDateTime, ZonedDateTime, Boolean> leZonedDateTime =
      (x, y) -> !x.isAfter(y);

  static final BiFunction<BigDecimal, BigDecimal, Boolean> ltDecimal = (x, y) -> x.compareTo(y) < 0;

  static final BiFunction<BigDecimal, Integer, Boolean> ltDecimalInteger =
      (x, y) -> x.compareTo(BigDecimal.valueOf(y)) < 0;

  static final BiFunction<Duration, Duration, Boolean> ltDuration = (x, y) -> x.compareTo(y) < 0;

  static final BiFunction<Instant, Instant, Boolean> ltInstant = (x, y) -> x.isBefore(y);

  static final BiFunction<Integer, Integer, Boolean> ltInteger = (x, y) -> x < y;

  static final BiFunction<Integer, BigDecimal, Boolean> ltIntegerDecimal =
      (x, y) -> BigDecimal.valueOf(x).compareTo(y) < 0;

  static final BiFunction<LocalDate, LocalDate, Boolean> ltLocalDate = (x, y) -> x.isBefore(y);

  static final BiFunction<LocalTime, LocalTime, Boolean> ltLocalTime = (x, y) -> x.isBefore(y);

  static final BiFunction<ZonedDateTime, ZonedDateTime, Boolean> ltZonedDateTime =
      (x, y) -> x.isBefore(y);

  static final BiFunction<BigDecimal, BigDecimal, Integer> modDecimal =
      (x, y) -> x.intValue() % y.intValue();

  static final BiFunction<Integer, Integer, Integer> modInteger = (x, y) -> x % y;

  static final BiFunction<BigDecimal, BigDecimal, BigDecimal> multiplyDecimal =
      (x, y) -> x.multiply(y);

  static final BiFunction<Integer, Integer, Integer> multiplyInteger = (x, y) -> x * y;

  static final BiFunction<Integer, BigDecimal, BigDecimal> multiplyIntegerDecimal =
      (x, y) -> y.multiply(BigDecimal.valueOf(x));

  static final BiFunction<Character, Character, Boolean> neCharacter = (x, y) -> !x.equals(y);

  static final BiFunction<BigDecimal, BigDecimal, Boolean> neDecimal =
      (x, y) -> x.compareTo(y) != 0;

  static final BiFunction<BigDecimal, Integer, Boolean> neDecimalInteger =
      (x, y) -> x.compareTo(BigDecimal.valueOf(y)) != 0;

  static final BiFunction<Duration, Duration, Boolean> neDuration = (x, y) -> !x.equals(y);

  static final BiFunction<Instant, Instant, Boolean> neInstant = (x, y) -> !x.equals(y);

  static final BiFunction<Integer, Integer, Boolean> neInteger = (x, y) -> x != y;

  static final BiFunction<Integer, BigDecimal, Boolean> neIntegerDecimal =
      (x, y) -> y.compareTo(BigDecimal.valueOf(x)) != 0;

  static final BiFunction<LocalDate, LocalDate, Boolean> neLocalDate = (x, y) -> !x.equals(y);

  static final BiFunction<LocalTime, LocalTime, Boolean> neLocalTime = (x, y) -> !x.equals(y);

  static final BiFunction<String, String, Boolean> neString = (x, y) -> !x.equals(y);

  static final BiFunction<ZonedDateTime, ZonedDateTime, Boolean> neZonedDateTime =
      (x, y) -> !x.equals(y);

  static final BiFunction<BigDecimal, BigDecimal, BigDecimal> subtractDecimal =
      (x, y) -> x.subtract(y);

  static final BiFunction<BigDecimal, Integer, BigDecimal> subtractDecimalInteger =
      (x, y) -> x.subtract(BigDecimal.valueOf(y));

  static final BiFunction<Instant, Duration, Instant> subtractDuration = (x, y) -> x.minus(y);

  static final BiFunction<Integer, Integer, Integer> subtractInteger = (x, y) -> x - y;

  static final BiFunction<Integer, BigDecimal, BigDecimal> subtractIntegerDecimal =
      (x, y) -> BigDecimal.valueOf(x).subtract(y);


  /**
   * Add operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<?>> add =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<?>>() {

        @SuppressWarnings("unchecked")
        @Override
        public FixValue<?> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, addOperations);
          if (operation == null) {
            operation = Operation.commutativeMatch(key, addOperations);
            swapOperands = true;
          }
          if (operation == null) {
            return null;
          }

          @SuppressWarnings("rawtypes")
          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand2.getValue()),
                      operation.valueType2.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Logical and operator
   */
  public final BiFunction<FixValue<Boolean>, FixValue<Boolean>, FixValue<Boolean>> and =
      new BiFunction<FixValue<Boolean>, FixValue<Boolean>, FixValue<Boolean>>() {

        @SuppressWarnings("unchecked")
        @Override
        public FixValue<Boolean> apply(FixValue<Boolean> operand1, FixValue<Boolean> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          FixValue<Boolean> result;
          try {
            result = FixValueFactory.create(null, FixType.BooleanType, Boolean.class);

            result.setValue(operand1.getValue() && operand2.getValue());
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Divide operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<?>> divide =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<?>>() {

        @SuppressWarnings("unchecked")
        @Override
        public FixValue<?> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, divideOperations);
          if (operation == null) {
            return null;
          }

          @SuppressWarnings("rawtypes")
          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Equality operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>> eq =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>>() {

        @SuppressWarnings("unchecked")
        @Override
        public FixValue<Boolean> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, eqOperations);
          if (operation == null) {
            operation = Operation.commutativeMatch(key, addOperations);
            swapOperands = true;
          }
          if (operation == null) {
            return null;
          }

          @SuppressWarnings("rawtypes")
          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Greater-than-or-equal operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>> ge =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>>() {

        @SuppressWarnings("unchecked")
        @Override
        public FixValue<Boolean> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, geOperations);
          if (operation == null) {
            return null;
          }

          @SuppressWarnings("rawtypes")
          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Greater-than operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>> gt =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>>() {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public FixValue<Boolean> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, gtOperations);
          if (operation == null) {
            return null;
          }

          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Less-than-or-equal operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>> le =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>>() {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public FixValue<Boolean> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, leOperations);
          if (operation == null) {
            return null;
          }

          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Less-than operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>> lt =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>>() {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public FixValue<Boolean> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, ltOperations);
          if (operation == null) {
            return null;
          }

          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Modulus operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<?>> mod =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<?>>() {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public FixValue<?> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, modOperations);
          if (operation == null) {
            return null;
          }

          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Multiply operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<?>> multiply =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<?>>() {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public FixValue<?> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, multiplyOperations);
          if (operation == null) {
            operation = Operation.commutativeMatch(key, multiplyOperations);
            swapOperands = true;
          }
          if (operation == null) {
            return null;
          }

          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand2.getValue()),
                      operation.valueType2.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Not-equal operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>> ne =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<Boolean>>() {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public FixValue<Boolean> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, neOperations);
          if (operation == null) {
            return null;
          }

          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Logical-not unary operator
   */
  public final Function<FixValue<Boolean>, FixValue<Boolean>> not =
      new Function<FixValue<Boolean>, FixValue<Boolean>>() {

        @SuppressWarnings("unchecked")
        @Override
        public FixValue<Boolean> apply(FixValue<Boolean> operand1) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          FixValue<Boolean> result;
          try {
            result = FixValueFactory.create(null, FixType.BooleanType, Boolean.class);

            result.setValue(!operand1.getValue());
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Logical or operator
   */
  public final BiFunction<FixValue<Boolean>, FixValue<Boolean>, FixValue<Boolean>> or =
      new BiFunction<FixValue<Boolean>, FixValue<Boolean>, FixValue<Boolean>>() {

        @SuppressWarnings("unchecked")
        @Override
        public FixValue<Boolean> apply(FixValue<Boolean> operand1, FixValue<Boolean> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          FixValue<Boolean> result;
          try {
            result = FixValueFactory.create(null, FixType.BooleanType, Boolean.class);

            result.setValue(operand1.getValue() || operand2.getValue());
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  /**
   * Subtract operator
   */
  public final BiFunction<FixValue<?>, FixValue<?>, FixValue<?>> subtract =
      new BiFunction<FixValue<?>, FixValue<?>, FixValue<?>>() {

        @SuppressWarnings({"rawtypes", "unchecked"})
        @Override
        public FixValue<?> apply(FixValue<?> operand1, FixValue<?> operand2) {
          Objects.requireNonNull(operand1, "Missing operand 1");
          Objects.requireNonNull(operand2, "Missing operand 2");

          OperationKey key = new OperationKey(operand1.getType(), operand2.getType(),
              operand1.getValue().getClass(), operand2.getValue().getClass());

          boolean swapOperands = false;
          Operation operation = Operation.exactMatch(key, subtractOperations);
          if (operation == null) {
            return null;
          }

          FixValue result;
          try {
            result = FixValueFactory.create(null, operation.resultType, operation.resultValueType);

            if (swapOperands) {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType2.cast(operand2.getValue()),
                      operation.valueType1.cast(operand1.getValue()))));
            } else {
              result.setValue(operation.resultValueType
                  .cast(operation.evaluate.apply(operation.valueType1.cast(operand1.getValue()),
                      operation.valueType2.cast(operand2.getValue()))));
            }
            return result;
          } catch (ScoreException e) {
            return null;
          }
        }
      };

  // By listing all combinations, the need for separate data type promotion or cast logic is avoided
  private final Operation[] addOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.intType, Integer.class, Integer.class,
          Integer.class, addInteger),
      new Operation(FixType.Qty, FixType.Qty, FixType.Qty, Integer.class, Integer.class,
          Integer.class, addInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.floatType, BigDecimal.class,
          BigDecimal.class, BigDecimal.class, addDecimal),
      new Operation(FixType.Amt, FixType.Amt, FixType.Amt, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, addDecimal),
      new Operation(FixType.Qty, FixType.Qty, FixType.Qty, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, addDecimal),
      new Operation(FixType.Price, FixType.PriceOffset, FixType.Price, BigDecimal.class,
          BigDecimal.class, BigDecimal.class, addDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.floatType, Integer.class,
          BigDecimal.class, BigDecimal.class, addIntegerDecimal),
      new Operation(FixType.UTCTimestamp, FixType.Duration, FixType.UTCTimestamp, Instant.class,
          Duration.class, Instant.class, addDuration),};

  private final Operation[] divideOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.intType, Integer.class, Integer.class,
          Integer.class, divideInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.floatType, BigDecimal.class,
          BigDecimal.class, BigDecimal.class, divideDecimal),
      new Operation(FixType.Amt, FixType.Qty, FixType.Price, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, divideDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.intType, Integer.class,
          BigDecimal.class, Integer.class, divideIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.floatType, BigDecimal.class,
          Integer.class, BigDecimal.class, divideDecimalInteger),
      new Operation(FixType.Amt, FixType.Qty, FixType.Price, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, divideDecimalInteger),
      new Operation(FixType.Duration, FixType.intType, FixType.Duration, Instant.class,
          Duration.class, Instant.class, divideDuration),};

  private final Operation[] eqOperations = new Operation[] {
      new Operation(FixType.charType, FixType.charType, FixType.BooleanType, Character.class,
          Character.class, Boolean.class, eqCharacter),
      new Operation(FixType.StringType, FixType.StringType, FixType.BooleanType, String.class,
          String.class, Boolean.class, eqString),
      new Operation(FixType.intType, FixType.intType, FixType.BooleanType, Integer.class,
          Integer.class, Boolean.class, eqInteger),
      new Operation(FixType.Qty, FixType.intType, FixType.BooleanType, Integer.class, Integer.class,
          Boolean.class, eqInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, eqDecimal),
      new Operation(FixType.Price, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, eqDecimal),
      new Operation(FixType.PriceOffset, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, eqDecimal),
      new Operation(FixType.Amt, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, eqDecimal),
      new Operation(FixType.Qty, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, eqDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.BooleanType, Integer.class,
          BigDecimal.class, Boolean.class, eqIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.BooleanType, BigDecimal.class,
          Integer.class, Boolean.class, eqDecimalInteger),
      new Operation(FixType.Duration, FixType.Duration, FixType.BooleanType, Duration.class,
          Duration.class, Boolean.class, eqDuration),
      new Operation(FixType.UTCTimestamp, FixType.UTCTimestamp, FixType.BooleanType, Instant.class,
          Instant.class, Boolean.class, eqInstant),
      new Operation(FixType.LocalMktDate, FixType.LocalMktDate, FixType.BooleanType,
          LocalDate.class, LocalDate.class, Boolean.class, eqLocalDate),
      new Operation(FixType.LocalMktTime, FixType.LocalMktTime, FixType.BooleanType,
          LocalTime.class, LocalTime.class, Boolean.class, eqLocalTime),
      new Operation(FixType.TZTimestamp, FixType.TZTimestamp, FixType.BooleanType,
          ZonedDateTime.class, ZonedDateTime.class, Boolean.class, eqZonedDateTime),};

  private final Operation[] geOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.BooleanType, Integer.class,
          Integer.class, Boolean.class, geInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, geDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.BooleanType, Integer.class,
          BigDecimal.class, Boolean.class, geIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.BooleanType, BigDecimal.class,
          Integer.class, Boolean.class, geDecimalInteger),
      new Operation(FixType.Duration, FixType.Duration, FixType.BooleanType, Duration.class,
          Duration.class, Boolean.class, geDuration),
      new Operation(FixType.UTCTimestamp, FixType.UTCTimestamp, FixType.BooleanType, Instant.class,
          Instant.class, Boolean.class, geInstant),
      new Operation(FixType.LocalMktDate, FixType.LocalMktDate, FixType.BooleanType,
          LocalDate.class, LocalDate.class, Boolean.class, geLocalDate),
      new Operation(FixType.LocalMktTime, FixType.LocalMktTime, FixType.BooleanType,
          LocalTime.class, LocalTime.class, Boolean.class, geLocalTime),
      new Operation(FixType.TZTimestamp, FixType.TZTimestamp, FixType.BooleanType,
          ZonedDateTime.class, ZonedDateTime.class, Boolean.class, geZonedDateTime),};

  private final Operation[] gtOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.BooleanType, Integer.class,
          Integer.class, Boolean.class, gtInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, gtDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.BooleanType, Integer.class,
          BigDecimal.class, Boolean.class, gtIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.BooleanType, BigDecimal.class,
          Integer.class, Boolean.class, gtDecimalInteger),
      new Operation(FixType.Duration, FixType.Duration, FixType.BooleanType, Duration.class,
          Duration.class, Boolean.class, gtDuration),
      new Operation(FixType.UTCTimestamp, FixType.UTCTimestamp, FixType.BooleanType, Instant.class,
          Instant.class, Boolean.class, gtInstant),
      new Operation(FixType.LocalMktDate, FixType.LocalMktDate, FixType.BooleanType,
          LocalDate.class, LocalDate.class, Boolean.class, gtLocalDate),
      new Operation(FixType.LocalMktTime, FixType.LocalMktTime, FixType.BooleanType,
          LocalTime.class, LocalTime.class, Boolean.class, gtLocalTime),
      new Operation(FixType.TZTimestamp, FixType.TZTimestamp, FixType.BooleanType,
          ZonedDateTime.class, ZonedDateTime.class, Boolean.class, gtZonedDateTime),};

  private final Operation[] leOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.BooleanType, Integer.class,
          Integer.class, Boolean.class, leInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, leDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.BooleanType, Integer.class,
          BigDecimal.class, Boolean.class, leIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.BooleanType, BigDecimal.class,
          Integer.class, Boolean.class, leDecimalInteger),
      new Operation(FixType.Duration, FixType.Duration, FixType.BooleanType, Duration.class,
          Duration.class, Boolean.class, leDuration),
      new Operation(FixType.UTCTimestamp, FixType.UTCTimestamp, FixType.BooleanType, Instant.class,
          Instant.class, Boolean.class, leInstant),
      new Operation(FixType.LocalMktDate, FixType.LocalMktDate, FixType.BooleanType,
          LocalDate.class, LocalDate.class, Boolean.class, leLocalDate),
      new Operation(FixType.LocalMktTime, FixType.LocalMktTime, FixType.BooleanType,
          LocalTime.class, LocalTime.class, Boolean.class, leLocalTime),
      new Operation(FixType.TZTimestamp, FixType.TZTimestamp, FixType.BooleanType,
          ZonedDateTime.class, ZonedDateTime.class, Boolean.class, leZonedDateTime),};


  private final Operation[] ltOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.BooleanType, Integer.class,
          Integer.class, Boolean.class, ltInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, ltDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.BooleanType, Integer.class,
          BigDecimal.class, Boolean.class, ltIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.BooleanType, BigDecimal.class,
          Integer.class, Boolean.class, ltDecimalInteger),
      new Operation(FixType.Duration, FixType.Duration, FixType.BooleanType, Duration.class,
          Duration.class, Boolean.class, ltDuration),
      new Operation(FixType.UTCTimestamp, FixType.UTCTimestamp, FixType.BooleanType, Instant.class,
          Instant.class, Boolean.class, ltInstant),
      new Operation(FixType.LocalMktDate, FixType.LocalMktDate, FixType.BooleanType,
          LocalDate.class, LocalDate.class, Boolean.class, ltLocalDate),
      new Operation(FixType.LocalMktTime, FixType.LocalMktTime, FixType.BooleanType,
          LocalTime.class, LocalTime.class, Boolean.class, ltLocalTime),
      new Operation(FixType.TZTimestamp, FixType.TZTimestamp, FixType.BooleanType,
          ZonedDateTime.class, ZonedDateTime.class, Boolean.class, ltZonedDateTime),};

  private final Operation[] modOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.intType, Integer.class, Integer.class,
          Integer.class, modInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.floatType, BigDecimal.class,
          BigDecimal.class, Integer.class, modDecimal),};

  private final Operation[] multiplyOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.intType, Integer.class, Integer.class,
          Integer.class, multiplyInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.floatType, BigDecimal.class,
          BigDecimal.class, BigDecimal.class, multiplyDecimal),
      new Operation(FixType.Price, FixType.Qty, FixType.Amt, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, multiplyDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.floatType, Integer.class,
          BigDecimal.class, BigDecimal.class, multiplyIntegerDecimal),
      new Operation(FixType.Qty, FixType.Price, FixType.Amt, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, multiplyIntegerDecimal),};

  private final Operation[] neOperations = new Operation[] {
      new Operation(FixType.charType, FixType.charType, FixType.BooleanType, Character.class,
          Character.class, Boolean.class, neCharacter),
      new Operation(FixType.StringType, FixType.StringType, FixType.BooleanType, String.class,
          String.class, Boolean.class, neString),
      new Operation(FixType.intType, FixType.intType, FixType.BooleanType, Integer.class,
          Integer.class, Boolean.class, neInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.BooleanType, BigDecimal.class,
          BigDecimal.class, Boolean.class, neDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.BooleanType, Integer.class,
          BigDecimal.class, Boolean.class, neIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.BooleanType, BigDecimal.class,
          Integer.class, Boolean.class, neDecimalInteger),
      new Operation(FixType.Duration, FixType.Duration, FixType.BooleanType, Duration.class,
          Duration.class, Boolean.class, neDuration),
      new Operation(FixType.UTCTimestamp, FixType.UTCTimestamp, FixType.BooleanType, Instant.class,
          Instant.class, Boolean.class, neInstant),
      new Operation(FixType.LocalMktDate, FixType.LocalMktDate, FixType.BooleanType,
          LocalDate.class, LocalDate.class, Boolean.class, neLocalDate),
      new Operation(FixType.LocalMktTime, FixType.LocalMktTime, FixType.BooleanType,
          LocalTime.class, LocalTime.class, Boolean.class, neLocalTime),
      new Operation(FixType.TZTimestamp, FixType.TZTimestamp, FixType.BooleanType,
          ZonedDateTime.class, ZonedDateTime.class, Boolean.class, neZonedDateTime),};


  private final Operation[] subtractOperations = new Operation[] {
      new Operation(FixType.intType, FixType.intType, FixType.intType, Integer.class, Integer.class,
          Integer.class, subtractInteger),
      new Operation(FixType.Qty, FixType.Qty, FixType.Qty, Integer.class, Integer.class,
          Integer.class, subtractInteger),
      new Operation(FixType.floatType, FixType.floatType, FixType.floatType, BigDecimal.class,
          BigDecimal.class, BigDecimal.class, subtractDecimal),
      new Operation(FixType.Amt, FixType.Amt, FixType.Amt, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, subtractDecimal),
      new Operation(FixType.Qty, FixType.Qty, FixType.Qty, BigDecimal.class, BigDecimal.class,
          BigDecimal.class, subtractDecimal),
      new Operation(FixType.Price, FixType.PriceOffset, FixType.Price, BigDecimal.class,
          BigDecimal.class, BigDecimal.class, subtractDecimal),
      new Operation(FixType.intType, FixType.floatType, FixType.floatType, Integer.class,
          BigDecimal.class, BigDecimal.class, subtractIntegerDecimal),
      new Operation(FixType.floatType, FixType.intType, FixType.floatType, BigDecimal.class,
          Integer.class, BigDecimal.class, subtractDecimalInteger),
      new Operation(FixType.UTCTimestamp, FixType.Duration, FixType.UTCTimestamp, Instant.class,
          Duration.class, Instant.class, subtractDuration),};
}
