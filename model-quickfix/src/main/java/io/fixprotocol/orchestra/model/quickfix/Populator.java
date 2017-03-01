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

import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.ModelException;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import quickfix.Message;

/**
 * @author Don Mendelson
 *
 */
public class Populator implements io.fixprotocol.orchestra.model.Populator<Message> {

  private final Evaluator evaluator;
  private RepositoryAdapter repositoryAdapter;
  private SymbolResolver symbolResolver;

  public Populator(RepositoryAdapter repositoryAdapter, SymbolResolver symbolResolver) {
    this.repositoryAdapter = repositoryAdapter;
    this.symbolResolver = symbolResolver;
    evaluator = new Evaluator(symbolResolver);
  }

  @Override
  public void populate(Message inboundMessage, MessageType inboundMessageType,
      Message outboundMessage, MessageType outboundMessageType) throws ModelException {
    try (
        Scope inScope = symbolResolver.nest(new PathStep("in."),
            new MessageScope(inboundMessage, inboundMessageType, repositoryAdapter, symbolResolver,
                evaluator));
        Scope outScope = symbolResolver.nest(new PathStep("out."), new MessageScope(outboundMessage,
            outboundMessageType, repositoryAdapter, symbolResolver, evaluator))) {

      List<Object> elements =
          outboundMessageType.getStructure().getComponentOrComponentRefOrGroup();
      for (Object element : elements) {
        if (element instanceof FieldRefType) {
          FieldRefType fieldRefType = (FieldRefType) element;
          String assignExpression = fieldRefType.getAssign();
          if (assignExpression != null) {
            try {
              FixValue<?> fixValue = evaluator.evaluate(assignExpression);
              if (fixValue != null) {
                outScope.assign(new PathStep(fieldRefType.getName()), fixValue);
              }
            } catch (ScoreException e) {
              throw new ModelException("Failed to assign field " + fieldRefType.getName(), e);
            }
          }
        }
      }
    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }

}
