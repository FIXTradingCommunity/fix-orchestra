package io.fixprotocol.orchestra.model;

import java.util.function.Consumer;

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

/**
 * @author Don Mendelson
 *
 */
public abstract class AbstractScope implements Scope {

  protected static boolean trace = false;
  protected static Consumer<String> traceHandler = System.out::println;

  /**
   * @return the trace
   */
  public boolean isTrace() {
    return AbstractScope.trace;
  }

  /**
   * @param trace the trace to set
   */
  public void setTrace(boolean trace) {
    AbstractScope.trace = trace;
  }

  public void setTraceHandler(Consumer<String> traceHandler) {
    AbstractScope.traceHandler = traceHandler;
  }

  protected void traceNest(PathStep pathStep, Scope nested) {
    if (isTrace()) {
      traceHandler.accept(String.format("Path %s scope %s nested under scope %s",
          pathStep.getName(), nested.getName(), getName()));
    }
  }

  protected void traceRemove(PathStep pathStep, FixNode removed) {
    if (isTrace()) {
      traceHandler.accept(String.format("Path %s node %s removed from scope %s", pathStep.getName(),
          removed.getName(), getName()));
    }
  }

}
