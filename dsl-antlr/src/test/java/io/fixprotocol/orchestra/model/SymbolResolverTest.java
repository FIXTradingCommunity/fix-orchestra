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

import static io.fixprotocol.orchestra.model.SymbolResolver.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import io.fixprotocol.orchestra.model.TreeSymbolTable;

public class SymbolResolverTest {

  private SymbolResolver symbolResolver;
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    symbolResolver = new SymbolResolver();
    //symbolResolver.setTrace(true);
  }

  /**
   * Test method for {@link io.fixprotocol.orchestra.model.SymbolResolver#assign(io.fixprotocol.orchestra.model.PathStep, io.fixprotocol.orchestra.model.FixValue)}.
   * @throws ModelException 
   */
  @Test
  public void testAssign() throws ModelException {
    FixValue<?> value = new FixValue<Integer>("x", FixType.intType, 33);
    PathStep pathStep = new PathStep("$x");
    symbolResolver.assign(pathStep, value);
    @SuppressWarnings("unchecked")
    FixValue<Integer> found = (FixValue<Integer>) symbolResolver.resolve(pathStep);
    assertNotNull(found);
    assertEquals(33, found.getValue().intValue());
  }

  /**
   * Test method for {@link io.fixprotocol.orchestra.model.SymbolResolver#resolve(io.fixprotocol.orchestra.model.PathStep)}.
   * @throws ModelException 
   */
  @Test
  public void testResolve() throws ModelException {
    symbolResolver.nest(CODE_SET_ROOT, new TreeSymbolTable("Codes"));
    Scope node = (Scope) symbolResolver.resolve(CODE_SET_ROOT);
    FixValue<?> value = new FixValue<Character>("Market", FixType.charType, '1');
    PathStep pathStep2 = new PathStep(value.getName());
    node.assign(pathStep2, value);
    @SuppressWarnings("unchecked")
    FixValue<Character> found = (FixValue<Character>) node.resolve(pathStep2);
    assertNotNull(found);
    assertEquals(Character.valueOf('1'), found.getValue());
  }
 
  @Test
  public void testResolveThis() throws ModelException {
    Scope local = (Scope) symbolResolver.resolve(LOCAL_ROOT);
    Scope nested = new TreeSymbolTable("Data");
    local.nest(new PathStep("table"), nested);
    FixValue<?> value = new FixValue<Integer>("this.x", FixType.intType, 33);   
    PathStep pathStep2 = new PathStep(value.getName());
    symbolResolver.assign(pathStep2, value);
    @SuppressWarnings("unchecked")
    FixValue<Integer> found = (FixValue<Integer>) symbolResolver.resolve(pathStep2);
    assertNotNull(found);
    assertEquals(33, found.getValue().intValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testResolveNestedThis() throws Exception {
    Scope local = (Scope) symbolResolver.resolve(LOCAL_ROOT);

    FixValue<?> value = new FixValue<Integer>("this.x", FixType.intType, 33);   
    PathStep pathStep = new PathStep(value.getName());
    Scope nested = new TreeSymbolTable("level1");
    local.nest(new PathStep("level1"), nested);
    symbolResolver.assign(pathStep, value);
    
    FixValue<?> value2 = new FixValue<Integer>("this.y", FixType.intType, 44); 
    PathStep pathStep2 = new PathStep(value2.getName());

    try (Scope nested2 = new TreeSymbolTable("level2")) {
      local.nest(new PathStep("level2"), nested2);
      symbolResolver.assign(pathStep2, value2);
      FixValue<Integer> found2 = (FixValue<Integer>) symbolResolver.resolve(pathStep2);
      assertNotNull(found2);
      assertEquals(44, found2.getValue().intValue());
      // also works without "this"
      found2 = (FixValue<Integer>) symbolResolver.resolve(new PathStep("y"));
      assertNotNull(found2);
      assertEquals(44, found2.getValue().intValue());
    } // level2 out of scope
    FixValue<Integer> found = (FixValue<Integer>) symbolResolver.resolve(pathStep);
    assertNotNull(found);
    assertEquals(33, found.getValue().intValue());
    
    FixNode notFound = symbolResolver.resolve(pathStep2);
    assertNull(notFound);
  }
}
