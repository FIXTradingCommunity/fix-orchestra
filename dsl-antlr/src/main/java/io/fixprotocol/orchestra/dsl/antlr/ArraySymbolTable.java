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

import java.util.ArrayList;

/**
 * Symbol space for a repeating group. Key is index and name.
 * 
 * @author Don Mendelson
 *
 */
public class ArraySymbolTable implements Scope {

  private final ArrayList<Scope> entries = new ArrayList<>();
  private final String name;

  /**
   * @param name
   */
  public ArraySymbolTable(String name) {
    this.name = name;
  }

  /**
   * If the symbol already exists, assign it to the new value. Otherwise, insert the value in the
   * table, possibly adding new entries in the array.
   * 
   * @param pathStep
   * @param value
   * @throws FixException
   */
  public FixValue<?> assign(PathStep pathStep, FixValue<?> value) throws FixException {
    int index = pathStep.getIndex();
    // todo handle predicate, for now only index
    if (index == PathStep.NO_INDEX) {
      return null;
    }
    for (int i = entries.size(); i <= index; i++) {
      entries.add(new TreeSymbolTable(getName() + (i + 1)));
    }
    final Scope entry = entries.get(index);
    return entry.assign(pathStep, value);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void nest(PathStep pathStep, Scope nested) {
    int index = pathStep.getIndex();
    if (index != PathStep.NO_INDEX) {
      entries.add(index, nested);
    } else {
      entries.add(nested);
    }

  }

  public FixNode resolve(PathStep pathStep) {
    final Scope entry = entries.get(pathStep.getIndex());
    if (entry != null) {
      return entry.resolve(pathStep);
    } else
      return null;
  }

}
