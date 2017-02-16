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
package io.fixprotocol.orchestra.model.quickfix;

import java.util.List;

import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeType;

import io.fixprotocol.orchestra.dsl.antlr.FixType;
import io.fixprotocol.orchestra.dsl.antlr.FixValue;
import io.fixprotocol.orchestra.dsl.antlr.FixValueFactory;
import io.fixprotocol.orchestra.dsl.antlr.PathStep;
import io.fixprotocol.orchestra.dsl.antlr.Scope;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;

/**
 * Scope for a code set
 * 
 * @author Don Mendelson
 *
 */
public class CodeSetScope implements Scope {

  private CodeSetType codeSet;

  /**
   * Constructor
   * @param codeSet from metadata
   */
  public CodeSetScope(CodeSetType codeSet) {
    this.codeSet = codeSet;
  }

  /* (non-Javadoc)
   * @see io.fixprotocol.orchestra.dsl.antlr.FixNode#getName()
   */
  @Override
  public String getName() {
    return codeSet.getName();
  }

  /* (non-Javadoc)
   * @see io.fixprotocol.orchestra.dsl.antlr.Scope#assign(io.fixprotocol.orchestra.dsl.antlr.PathStep, io.fixprotocol.orchestra.dsl.antlr.FixValue)
   */
  @Override
  public FixValue<?> assign(PathStep arg0, FixValue<?> arg1) throws ScoreException {
    throw new UnsupportedOperationException("Message structure is immutable");
  }

  /* (non-Javadoc)
   * @see io.fixprotocol.orchestra.dsl.antlr.Scope#nest(io.fixprotocol.orchestra.dsl.antlr.PathStep, io.fixprotocol.orchestra.dsl.antlr.Scope)
   */
  @Override
  public void nest(PathStep arg0, Scope arg1) {
    throw new UnsupportedOperationException("Message structure is immutable");
  }

  /* (non-Javadoc)
   * @see io.fixprotocol.orchestra.dsl.antlr.Scope#resolve(io.fixprotocol.orchestra.dsl.antlr.PathStep)
   */
  @SuppressWarnings("unchecked")
  @Override
  public FixValue<?> resolve(PathStep pathStep) {
    String dataTypeString = codeSet.getType();
    FixType dataType = FixType.forName(dataTypeString);
    String name = pathStep.getName();
    List<CodeType> codes = codeSet.getCode();
    for (CodeType code : codes) {
      if (code.getName().equals(name)) {
        @SuppressWarnings("rawtypes")
        FixValue fixValue;
        try {
          fixValue = FixValueFactory.create(name, dataType, dataType.getValueClass());
          fixValue.setValue(dataType.getValueClass().cast(dataType.fromString(code.getValue())));
          return fixValue;
        } catch (ScoreException e) {
          return null;
        } 
      }
    }
    return null;
  }

}
