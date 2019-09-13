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
package io.fixprotocol.orchestra.model.quickfix;

import java.util.List;

import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.model.FixNode;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.ModelException;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import io.fixprotocol.orchestra.repository.RepositoryAccessor;
import quickfix.Message;

/**
 * @author Don Mendelson
 *
 */
public class MessageScope extends AbstractMessageScope implements Scope {

  private final MessageType messageType;
  private Scope parent;

  /**
   * Constructor
   * 
   * @param message FIX message to expose
   * @param messageType metadata about the FIX message type
   * @param repository FIX Repository contains metadata
   * @param symbolResolver used by DSL to resolve symbols
   * @param evaluator evalutes DSL expressions
   */
  public MessageScope(Message message, MessageType messageType, RepositoryAccessor repository,
      SymbolResolver symbolResolver, Evaluator evaluator) {
    super(message, repository, symbolResolver, evaluator);
    this.messageType = messageType;
   }


  /*
   * (non-Javadoc)
   * 
   * @see java.lang.AutoCloseable#close()
   */
  @Override
  public void close() throws Exception {
    if (parent != null) {
      parent.remove(new PathStep(messageType.getName()));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fixprotocol.orchestra.dsl.antlr.FixNode#getName()
   */
  @Override
  public String getName() {
    return messageType.getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fixprotocol.orchestra.dsl.antlr.Scope#nest(io.fixprotocol.orchestra.dsl.antlr.PathStep,
   * io.fixprotocol.orchestra.dsl.antlr.Scope)
   */
  @Override
  public Scope nest(PathStep arg0, Scope arg1) {
    throw new UnsupportedOperationException("Message structure is immutable");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#remove(io.fixprotocol.orchestra.dsl.antlr.PathStep)
   */
  @Override
  public FixNode remove(PathStep arg0) {
    throw new UnsupportedOperationException("Message structure is immutable");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#resolve(io.fixprotocol.orchestra.dsl.antlr.PathStep)
   */
  @Override
  public FixNode resolve(PathStep pathStep) {
    String name = pathStep.getName();
    List<Object> members = getRepository().getMessageMembers(messageType);
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        String fieldName = getRepository().getFieldName(fieldRefType.getId().intValue(), fieldRefType.getScenario());
        if (name.equals(fieldName)) {
          return resolveField(fieldRefType);
        }
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRefType = (GroupRefType) member;
        GroupType group = getRepository().getGroup(groupRefType);
        if (name.equals(group.getName())) {
          return resolveGroup(pathStep, groupRefType);
        }
      }
    }
    return null;
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


  /* (non-Javadoc)
   * @see io.fixprotocol.orchestra.model.Scope#assign(io.fixprotocol.orchestra.model.PathStep, io.fixprotocol.orchestra.model.FixValue)
   */
  @Override
  public FixValue<?> assign(PathStep pathStep, FixValue<?> value) throws ModelException {
    if (value.getValue() == null) {
      throw new ModelException(String.format("Assigning field %s null not allowed", value.getName()));
    }
    String name = pathStep.getName();
    List<Object> members = getRepository().getMessageMembers(messageType);
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        String fieldName = getRepository().getFieldName(fieldRefType.getId().intValue(), fieldRefType.getScenario());
        if (name.equals(fieldName)) {
          assignField(fieldRefType, value);
          return value;
        }
      }
    }
    return null;
  }

}
