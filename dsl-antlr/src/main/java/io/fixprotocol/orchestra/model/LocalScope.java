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

import java.util.ArrayDeque;

/**
 * A stack of local scopes
 *
 * @author Don Mendelson
 *
 */
public class LocalScope extends AbstractScope {

  private final String name;
  private Scope parent;
  private final ArrayDeque<Scope> stack = new ArrayDeque<>();

  /**
   * Construct a LocalScope by name
   * 
   * @param name name of the LocalScope
   */
  public LocalScope(String name) {
    this.name = name;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#assign(io.fixprotocol.orchestra.dsl.antlr.PathStep,
   * io.fixprotocol.orchestra.dsl.antlr.FixValue)
   */
  @Override
  public FixValue<?> assign(PathStep pathStep, FixValue<?> value) throws ModelException {
    final Scope local = stack.peekFirst();
    if (local != null) {
      return local.assign(pathStep, value);
    } else {
      throw new ModelException("No local scope to assign value");
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

  /*
   * (non-Javadoc)
   *
   * @see io.fixprotocol.orchestra.dsl.antlr.FixNode#getName()
   */
  @Override
  public String getName() {
    return name;
  }

  @Override
  public Scope nest(PathStep pathStep, Scope nested) {
    stack.addFirst(nested);
    nested.setParent(this);
    traceNest(pathStep, nested);
    return nested;
  }

  @Override
  public FixNode remove(PathStep pathStep) {
    final Scope removed = stack.pollFirst();
    traceRemove(pathStep, removed);
    return removed;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#resolve(io.fixprotocol.orchestra.dsl.antlr.PathStep)
   */
  @Override
  public FixNode resolve(PathStep pathStep) {
    final Scope local = stack.peekFirst();
    if (local != null) {
      return local.resolve(pathStep);
    } else {
      return null;
    }
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

  @Override
  protected void traceNest(PathStep pathStep, Scope nested) {
    if (isTrace()) {
      traceHandler.accept(String.format("Path %s scope %s nested under scope %s",
          pathStep.getName(), nested.getName(), getName()));
    }
  }

  @Override
  protected void traceRemove(PathStep pathStep, FixNode removed) {
    if (isTrace()) {
      final Scope current = stack.peekFirst();
      traceHandler.accept(String.format("Scope %s removed from local scope; current scope is %s",
          pathStep.getName(), current != null ? current.getName() : "none"));
    }
  }

}
