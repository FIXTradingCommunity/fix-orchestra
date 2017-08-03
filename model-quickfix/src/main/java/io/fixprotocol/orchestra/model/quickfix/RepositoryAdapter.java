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
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.Datatypes;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Repository;

/**
 * Wraps a Repository
 * 
 * @author Don Mendelson
 * 
 *         TODO consider building dictionaries from repository structures if sequential search
 *         becomes a performance problem.
 */
class RepositoryAdapter {

  private final Repository repository;

  /**
   * Constructor wraps a populated Repository
   */
  public RepositoryAdapter(Repository repository) {
    this.repository = repository;
  }

  CodeSetType getCodeset(String name) {
    List<CodeSetType> codeSetList = repository.getCodeSets().getCodeSet();
      for (CodeSetType codeSet : codeSetList) {
        if (name.equals(codeSet.getName())) {
          return codeSet;
        }
    }
    return null;
  }

  ComponentType getComponent(ComponentRefType componentRefType) {
    List<ComponentType> components =
        repository.getComponents().getComponentOrGroup();
    for (ComponentType component : components) {
      if (component.getId().equals(componentRefType.getId())) {
        return component;
      }
    }
    return null;
  }

  Datatype getDatatype(String name) {
    Datatypes datatypes = repository.getDatatypes();
    List<Datatype> datatypeList = datatypes.getDatatype();
    for (Datatype datatype : datatypeList) {
      if (name.equals(datatype.getName())) {
        return datatype;
      }
    }
    return null;
  }

  String getFieldDatatype(int id) {
    List<FieldType> fields = repository.getFields().getField();
    for (FieldType fieldType : fields) {
      if (fieldType.getId().intValue() == id) {
        return fieldType.getType();
      }
    }
    return null;
  }

  GroupType getGroup(GroupRefType groupRefType) {
    List<ComponentType> components =
        repository.getComponents().getComponentOrGroup();
    for (ComponentType component : components) {
      if (component.getId().equals(groupRefType.getId())) {
        return (GroupType) component;
      }
    }
    return null;
  }

  MessageType getMessage(String name, String context) {
    List<MessageType> messageList = repository.getMessages().getMessage();
    for (MessageType messageType : messageList) {
      if (name.equals(messageType.getName()) && context.equals(messageType.getScenario())) {

        return messageType;
      }
    }
    return null;
  }

}
