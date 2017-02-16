 package io.fixprotocol.orchestra.dsl.antlr;
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

/**
 * Confederated symbol space
 * 
 * @author Don Mendelson
 *
 */
public class SymbolResolver extends TreeSymbolTable {

  private static final PathStep variableRoot = new PathStep("$");

  /**
   * Constructor
   */
  public SymbolResolver() {
    super("global");
    nest(variableRoot, new TreeSymbolTable("variables"));
  }

  @Override
  public FixValue<?> assign(PathStep pathStep, FixValue<?> value) throws ScoreException {
    PathStep qualified = pathStep;
    if (!pathStep.getName().contains(".") && !pathStep.getName().startsWith("$") && !pathStep.getName().startsWith("^")) {
      qualified = new PathStep("this." + pathStep.getName());
      qualified.setIndex(pathStep.getIndex());
      qualified.setPredicate(pathStep.getPredicate());
    } 
    
    return super.assign(qualified, value);
  }

  /**
   * Implicit top level scope is 'this.'
   */
  @Override
  public FixNode resolve(PathStep pathStep) {
    PathStep qualified = pathStep;
    if (!pathStep.getName().contains(".") && !pathStep.getName().startsWith("$") && !pathStep.getName().startsWith("^")) {
      qualified = new PathStep("this." + pathStep.getName());
      qualified.setIndex(pathStep.getIndex());
      qualified.setPredicate(pathStep.getPredicate());
    } 
    
    return super.resolve(qualified);
  }
}
