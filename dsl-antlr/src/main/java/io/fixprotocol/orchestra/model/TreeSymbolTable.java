/*
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

import java.util.HashMap;
import java.util.Map;

/**
 * A flat symbol space. The only key is name.
 *
 * @author Don Mendelson
 *
 */
public class TreeSymbolTable extends AbstractScope {

  private final String name;
  private Scope parent;
  private final Map<String, FixNode> symbols = new HashMap<>();

  /**
   * Constructor
   *
   * @param name table name
   */
  public TreeSymbolTable(String name) {
    this.name = name;
  }

  @Override
  public FixValue<?> assign(PathStep pathStep, FixValue<?> value) throws ModelException {
    final FixNode node = symbols.get(pathStep.getName());
    if (node instanceof FixValue) {
      final FixValue<?> val = (FixValue<?>) node;
      val.assign(value);
      return val;
    } else if (node == null) {
      symbols.put(pathStep.getName(), value);
      return value;
    } else {
      throw new ModelException("FixNode already exists named " + pathStep.getName());
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.AutoCloseable#close()
   */
  @Override
  public void close() throws Exception {
    if (parent != null) {
      parent.remove(new PathStep(name));
    }
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Scope nest(PathStep pathStep, Scope nested) {
    symbols.put(pathStep.getName(), nested);
    traceNest(pathStep, nested);
    return nested;
  }

  @Override
  public FixNode remove(PathStep pathStep) {
    final FixNode removed = symbols.remove(pathStep.getName());
    traceRemove(pathStep, removed);
    return removed;
  }

  @Override
  public FixNode resolve(PathStep pathStep) {
    return symbols.get(pathStep.getName());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#setParent(io.fixprotocol.orchestra.dsl.antlr.Scope)
   */
  @Override
  public void setParent(Scope parent) {
    this.parent = parent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "TreeSymbolTable [" + (name != null ? "name=" + name + ", " : "")
        + (parent != null ? "parent=" + parent + ", " : "") + "symbols=" + symbols + "]";
  }

}
