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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

public class FixValueTest {

  private FixValueOperations fixValueOperations = new FixValueOperations();
  
  @Test
  public void testAddInt() {
    FixValue<Integer> op1 = new FixValue<Integer>("op1", FixType.intType, 3);
    FixValue<Integer> op2 = new FixValue<Integer>("op2", FixType.intType, 4);  
    assertEquals(Integer.valueOf(7), fixValueOperations.add.apply(op1, op2).getValue());
  }
  
  @Test
  public void testAddFloat() {
    FixValue<BigDecimal> op1 = new FixValue<BigDecimal>("op1", FixType.floatType, BigDecimal.valueOf(3));
    FixValue<BigDecimal> op2 = new FixValue<BigDecimal>("op2", FixType.floatType, BigDecimal.valueOf(4));
    assertEquals(BigDecimal.valueOf(7), fixValueOperations.add.apply(op1, op2).getValue());
  }
  
  @Test
  public void testAddAmt() {
    FixValue<BigDecimal> op1 = new FixValue<BigDecimal>("op1", FixType.Amt, BigDecimal.valueOf(3));
    FixValue<BigDecimal> op2 = new FixValue<BigDecimal>("op2", FixType.Amt, BigDecimal.valueOf(4));
    final FixValue<?> result = fixValueOperations.add.apply(op1, op2);
    assertEquals(FixType.Amt, result.getType());
    assertEquals(BigDecimal.valueOf(7), result.getValue());
  }
  
  @Test
  public void testAddIntFloat() {
    FixValue<BigDecimal> op1 = new FixValue<BigDecimal>("op1", FixType.floatType, BigDecimal.valueOf(3));
    FixValue<Integer> op2 = new FixValue<Integer>("op2", FixType.intType, 4);
    assertEquals(BigDecimal.valueOf(7), fixValueOperations.add.apply(op1, op2).getValue());
  }
  
  @Test
  public void testAddTimeDuration() {
    FixValue<Instant> op1 = new FixValue<Instant>("op1", FixType.UTCTimestamp, Instant.now());
    FixValue<Duration> op2 = new FixValue<Duration>("op2", FixType.Duration, Duration.ofSeconds(30));
    assert(fixValueOperations.add.apply(op1, op2).getValue() instanceof Instant);
  }

  @Test
  public void testAddIntNull() {
    FixValue<Integer> op1 = new FixValue<Integer>("op1", FixType.intType, 3);
    FixValue<Integer> op2 = new FixValue<Integer>("op2", FixType.intType);
    Exception exception =
        assertThrows(NullPointerException.class, () -> fixValueOperations.add.apply(op1, op2));
  }
}
