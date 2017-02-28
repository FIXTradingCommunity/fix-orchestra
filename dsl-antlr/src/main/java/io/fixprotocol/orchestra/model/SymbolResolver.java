package io.fixprotocol.orchestra.model;
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

  /**
   * High level qualifier for variables
   */
  public static final PathStep VARIABLE_ROOT = new PathStep("$");

  /**
   * High level qualifier for a code set
   */
  public static final PathStep CODE_SET_ROOT = new PathStep("^");

  /**
   * High level qualifier for local scope
   */
  public static final PathStep LOCAL_ROOT = new PathStep("this.");

  /**
   * Constructor
   */
  public SymbolResolver() {
    super("global");
    nest(VARIABLE_ROOT, new TreeSymbolTable("variables"));
    nest(LOCAL_ROOT, new LocalScope("local"));
  }

  @Override
  public FixValue<?> assign(PathStep pathStep, FixValue<?> value) throws ModelException {
    String pathName = pathStep.getName();
    if (pathName.startsWith("$")) {
      final Scope variables = (Scope) super.resolve(VARIABLE_ROOT);
      final PathStep vPathStep = new PathStep(pathName.substring(1));
      vPathStep.setIndex(pathStep.getIndex());
      vPathStep.setPredicate(pathStep.getPredicate());
      return variables.assign(vPathStep, value);
    } else if (pathName.startsWith("this.")) {
      final Scope variables = (Scope) super.resolve(LOCAL_ROOT);
      final PathStep vPathStep = new PathStep(pathName.substring(5));
      vPathStep.setIndex(pathStep.getIndex());
      vPathStep.setPredicate(pathStep.getPredicate());
      return variables.assign(vPathStep, value);
    } else {
      return super.assign(pathStep, value);
    }
  }

  /**
   * Implicit top level scope is 'this.'
   */
  @Override
  public FixNode resolve(PathStep pathStep) {
    FixNode node = null;
    String pathName = pathStep.getName();
    if (pathName.length() > 1 && pathName.startsWith(VARIABLE_ROOT.getName())) {
      final Scope variables = (Scope) super.resolve(VARIABLE_ROOT);
      final PathStep vPathStep = new PathStep(pathName.substring(1));
      vPathStep.setIndex(pathStep.getIndex());
      vPathStep.setPredicate(pathStep.getPredicate());
      node = variables.resolve(vPathStep);
    } else if (pathName.length() > 5 && pathName.startsWith(LOCAL_ROOT.getName())) {
      final Scope variables = (Scope) super.resolve(LOCAL_ROOT);
      final PathStep vPathStep = new PathStep(pathName.substring(5));
      vPathStep.setIndex(pathStep.getIndex());
      vPathStep.setPredicate(pathStep.getPredicate());
      node = variables.resolve(vPathStep);
    } else {
      node = super.resolve(pathStep);
    }
    // If unqualified, try in local scope
    if (node == null) {
      final Scope local = (Scope) super.resolve(LOCAL_ROOT);
      node = local.resolve(pathStep);
    }
    return node;
  }
}
