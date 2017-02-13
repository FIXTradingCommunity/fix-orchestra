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
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.FixNode;
import io.fixprotocol.orchestra.dsl.antlr.FixValue;
import io.fixprotocol.orchestra.dsl.antlr.PathStep;
import io.fixprotocol.orchestra.dsl.antlr.Scope;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.dsl.antlr.SymbolResolver;
import quickfix.Group;

/**
 * Symbol Scope for an entry of a repeating group
 * @author Don Mendelson
 *
 */
class GroupEntryScope extends AbstractMessageScope implements Scope {

  private final GroupType groupType;

  public GroupEntryScope(Group group, GroupType groupType, RepositoryAdapter repository, SymbolResolver symbolResolver, Evaluator evaluator) {
    super(group, repository, symbolResolver, evaluator);
    this.groupType = groupType;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * io.fixprotocol.orchestra.dsl.antlr.Scope#assign(io.fixprotocol.orchestra.dsl.antlr.PathStep,
   * io.fixprotocol.orchestra.dsl.antlr.FixValue)
   */
  @Override
  public FixValue<?> assign(PathStep arg0, FixValue<?> arg1) throws ScoreException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fixprotocol.orchestra.dsl.antlr.FixNode#getName()
   */
  @Override
  public String getName() {
    return groupType.getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see io.fixprotocol.orchestra.dsl.antlr.Scope#nest(io.fixprotocol.orchestra.dsl.antlr.PathStep,
   * io.fixprotocol.orchestra.dsl.antlr.Scope)
   */
  @Override
  public void nest(PathStep arg0, Scope arg1) {
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
    String unqualified = pathStep.getName();
    int index = unqualified.indexOf('.');
    if (index > 0) {
      unqualified = pathStep.getName().substring(index+1);
    }

    List<Object> members = groupType.getComponentRefOrGroupRefOrFieldRef();
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        if (fieldRefType.getName().equals(unqualified)) {
          return resolveField(fieldRefType);
        }
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRefType = (GroupRefType) member;
        if (groupRefType.getName().equals(unqualified)) {
          return resolveGroup(pathStep, groupRefType);
        }
      }
    }
    return null;
  }
  
 
}
