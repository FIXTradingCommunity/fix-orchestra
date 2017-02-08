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



/**
 * A symbol space for FIX elements
 * 
 * @author Don Mendelson
 *
 */
public interface Scope extends FixNode {

  /**
   * If the symbol already exists, assign it to the new value. Otherwise, insert the value in the
   * table.
   * 
   * @param pathStep search criteria
   * @param value new value to assign or insert
   * @return the assigned value
   * @throws FixException if a name or type conflict occurs
   */
  FixValue<?> assign(PathStep pathStep, FixValue<?> value) throws FixException;

  /**
   * Nest another Scope within this Scope
   * <br/>
   * If a nested Scope already exists at the specified path, it is replaced.
   * @param pathStep location
   * @param nested another Scope
   */
  void nest(PathStep pathStep, Scope nested);

  /**
   * Search for a symbol in the table
   * 
   * @param pathStep search criteria
   * @return a value if found or {@code null} if not found
   */
  FixNode resolve(PathStep pathStep);

}
