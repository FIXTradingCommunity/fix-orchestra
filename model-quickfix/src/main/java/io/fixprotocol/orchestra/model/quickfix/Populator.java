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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.fixprotocol._2016.fixrepository.BlockAssignmentType;
import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.model.FixValue;
import io.fixprotocol.orchestra.model.ModelException;
import io.fixprotocol.orchestra.model.PathStep;
import io.fixprotocol.orchestra.model.Scope;
import io.fixprotocol.orchestra.model.SymbolResolver;
import quickfix.FieldMap;
import quickfix.Group;
import quickfix.Message;

/**
 * @author Don Mendelson
 *
 */
public class Populator implements io.fixprotocol.orchestra.model.Populator<Message> {

  private final Evaluator evaluator;
  private final RepositoryAdapter repositoryAdapter;
  private final SymbolResolver symbolResolver;
  private final Function<Integer, Group> groupFactory;

  /**
   * Constructor
   * 
   * @param repositoryAdapter repository wrapper
   * @param symbolResolver resolves symbols in expressions
   * @param groupFactory creates instances of QuickFIX Group
   */
  public Populator(RepositoryAdapter repositoryAdapter, SymbolResolver symbolResolver,
      Function<Integer, Group> groupFactory) {
    this.repositoryAdapter = repositoryAdapter;
    this.symbolResolver = symbolResolver;
    evaluator = new Evaluator(symbolResolver);
    this.groupFactory = groupFactory;
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

      List<Object> members = repositoryAdapter.getMessageMembers(outboundMessageType);
      
      populateFieldMap(outboundMessage, members, outScope);

    } catch (Exception e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
  }

  private void populateFieldMap(FieldMap fieldMap, List<?> members, Scope outScope)
      throws ModelException {
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRefType = (FieldRefType) member;
        String assignExpression = fieldRefType.getAssign();
        if (assignExpression != null) {
          try {
            String dataTypeString = repositoryAdapter.getFieldDatatype(fieldRefType.getId().intValue());
            CodeSetType codeSet = repositoryAdapter.getCodeset(dataTypeString);
            if (codeSet != null) {
              symbolResolver.nest(new PathStep("^"), new CodeSetScope(codeSet) );
            }
            FixValue<?> fixValue = evaluator.evaluate(assignExpression);
            if (fixValue != null) {
              outScope.assign(new PathStep(fieldRefType.getName()), fixValue);
            }
          } catch (ScoreException e) {
            throw new ModelException("Failed to assign field " + fieldRefType.getName(), e);
          }
        }
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRefType = (GroupRefType) member;
        List<BlockAssignmentType> blockAssignments = groupRefType.getBlockAssignment();
        GroupType groupType = repositoryAdapter.getGroup(groupRefType);

        for (int i = 0; i < blockAssignments.size(); i++) {
          Group group = groupFactory.apply(groupType.getNumInGroupId().intValue());
          try (GroupInstanceScope groupScope =
              new GroupInstanceScope(group, groupType, repositoryAdapter, symbolResolver, evaluator)) {
            try (Scope local = (Scope) symbolResolver.resolve(SymbolResolver.LOCAL_ROOT)) {
              local.nest(new PathStep(groupType.getName()), groupScope);
              populateFieldMap(group, blockAssignments.get(i).getFieldRef(), groupScope);
              fieldMap.addGroup(group);
            }
          } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      } else if (member instanceof ComponentRefType) {
        ComponentRefType componentRefType = (ComponentRefType) member;
        List<BlockAssignmentType> blockAssignments = componentRefType.getBlockAssignment();
        if (blockAssignments.size() > 0) {
          List<?> blockElements = blockAssignments.get(0).getFieldRef();
          populateFieldMap(fieldMap, blockElements, outScope);
        }
      }
    }
  }

}

