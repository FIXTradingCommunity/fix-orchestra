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

import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Test;

/**
 * @author donme
 *
 */
public class FixValueTest {

  @Test
  public void testAddInt() {
    FixValue<Integer> op1 = new FixValue<Integer>("op1", FixType.intType, 3);
    FixValue<Integer> op2 = new FixValue<Integer>("op2", FixType.intType, 4);
    assertEquals(Integer.valueOf(7), op1.add(op2).getValue());
  }
  
  @Test
  public void testAddFloat() {
    FixValue<BigDecimal> op1 = new FixValue<BigDecimal>("op1", FixType.floatType, BigDecimal.valueOf(3));
    FixValue<BigDecimal> op2 = new FixValue<BigDecimal>("op2", FixType.floatType, BigDecimal.valueOf(4));
    assertEquals(BigDecimal.valueOf(7), op1.add(op2).getValue());
  }
  
  @Test
  public void testAddIntFloat() {
    FixValue<BigDecimal> op1 = new FixValue<BigDecimal>("op1", FixType.floatType, BigDecimal.valueOf(3));
    FixValue<Integer> op2 = new FixValue<Integer>("op2", FixType.intType, 4);
    assertEquals(BigDecimal.valueOf(7), op1.add(op2).getValue());
  }

  // TODO discuss whether this is the correct behavior
  @Test(expected=NullPointerException.class)
  public void testAddIntNull() {
    FixValue<Integer> op1 = new FixValue<Integer>("op1", FixType.intType, 3);
    FixValue<Integer> op2 = new FixValue<Integer>("op2", FixType.intType);
    assertEquals(Integer.valueOf(7), op1.add(op2).getValue());
  }
}
