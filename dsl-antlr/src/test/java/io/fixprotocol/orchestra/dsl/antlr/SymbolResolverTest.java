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

import org.junit.Before;
import org.junit.Test;

public class SymbolResolverTest {

  private SymbolResolver symbolResolver;
  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    symbolResolver = new SymbolResolver();
  }

  /**
   * Test method for {@link io.fixprotocol.orchestra.dsl.antlr.SymbolResolver#assign(io.fixprotocol.orchestra.dsl.antlr.PathStep, io.fixprotocol.orchestra.dsl.antlr.FixValue)}.
   * @throws ScoreException 
   */
  @Test
  public void testAssign() throws ScoreException {
    PathStep pathStep = new PathStep("$");
    Scope node = (Scope) symbolResolver.resolve(pathStep);
    FixValue<?> value = new FixValue<Integer>("x", FixType.intType, 33);
    PathStep pathStep2 = new PathStep(value.getName());
    node.assign(pathStep2, value);
    @SuppressWarnings("unchecked")
    FixValue<Integer> found = (FixValue<Integer>) node.resolve(pathStep2);
    assertNotNull(found);
    assertEquals(33, found.getValue().intValue());
  }

  /**
   * Test method for {@link io.fixprotocol.orchestra.dsl.antlr.SymbolResolver#resolve(io.fixprotocol.orchestra.dsl.antlr.PathStep)}.
   * @throws ScoreException 
   */
  @Test
  public void testResolve() throws ScoreException {
    PathStep pathStep = new PathStep("^");
    symbolResolver.nest(pathStep, new TreeSymbolTable("Codes"));
    Scope node = (Scope) symbolResolver.resolve(pathStep);
    FixValue<?> value = new FixValue<Character>("Market", FixType.charType, '1');
    PathStep pathStep2 = new PathStep(value.getName());
    node.assign(pathStep2, value);
    @SuppressWarnings("unchecked")
    FixValue<Character> found = (FixValue<Character>) node.resolve(pathStep2);
    assertNotNull(found);
    assertEquals(new Character('1'), found.getValue());
  }

  @Test
  public void testResolveThis() throws ScoreException {
    PathStep pathStep = new PathStep("this.");
    final TreeSymbolTable nested = new TreeSymbolTable("local");
    symbolResolver.nest(pathStep, nested);
    FixValue<?> value = new FixValue<Integer>("x", FixType.intType, 33);   
    PathStep pathStep2 = new PathStep(value.getName());
    symbolResolver.assign(pathStep2, value);
    @SuppressWarnings("unchecked")
    FixValue<Integer> found = (FixValue<Integer>) symbolResolver.resolve(pathStep2);
    assertNotNull(found);
    assertEquals(33, found.getValue().intValue());
  }

}
