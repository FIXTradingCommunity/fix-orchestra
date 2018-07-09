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
 * Helper methods for a Repository
 * 
 * @author Don Mendelson
 */
public class RepositoryAdapter {

  private final Repository repository;

  /**
   * Constructor wraps a populated Repository
   */
  public RepositoryAdapter(Repository repository) {
    this.repository = repository;
  }

  /**
   * Get a Code Set by name
   * @param name Code Set name
   * @return A Code Set or {@code null} if not found
   */
  CodeSetType getCodeset(String name) {
    List<CodeSetType> codeSetList = repository.getCodeSets().getCodeSet();
      for (CodeSetType codeSet : codeSetList) {
        if (name.equals(codeSet.getName())) {
          return codeSet;
        }
    }
    return null;
  }

  /**
   * Get a component by name
   * @param name component name
   * @return A component or {@code null} if not found
   */ 
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

  /**
   * Get a datatype by name
   * @param name datatype name
   * @return A datatype or {@code null} if not found
   */ 
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

  /**
   * Get the datatype of a field by its ID
   * @param id field ID
   * @return name of the datatype or {@code null} if the field is not found
   */
  String getFieldDatatype(int id) {
    List<FieldType> fields = repository.getFields().getField();
    for (FieldType fieldType : fields) {
      if (fieldType.getId().intValue() == id) {
        return fieldType.getType();
      }
    }
    return null;
  }
  
  /**
   * Get the name of a field by its ID
   * @param id field ID
   * @return name of the field or {@code null} if the field is not found
   */
  String getFieldName(int id) {
    List<FieldType> fields = repository.getFields().getField();
    for (FieldType fieldType : fields) {
      if (fieldType.getId().intValue() == id) {
        return fieldType.getName();
      }
    }
    return null;
  }

  /**
   * Get a group given a reference to it
   * @param groupRefType group reference
   * @return a group or {@code null} if not found
   */
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

  /**
   * Get a message by its name and scenario name
   * @param name message name
   * @param scenario scenario name
   * @return a message or {@code null} if not found
   */
  MessageType getMessage(String name, String scenario) {
    List<MessageType> messageList = repository.getMessages().getMessage();
    for (MessageType messageType : messageList) {
      if (name.equals(messageType.getName()) && scenario.equals(messageType.getScenario())) {

        return messageType;
      }
    }
    return null;
  }

  /**
   * Get a combined list of a message members including members from base scenarios
   * @param messageType a message
   * @return a combined list of members
   */
  List<Object> getMessageMembers(MessageType messageType) {
    List<Object> elements = new ArrayList<Object>();
   
    elements.addAll(
        messageType.getStructure().getComponentRefOrGroupRefOrFieldRef());

    String baseScenario = messageType.getExtends();
    while (baseScenario != null) {
      MessageType baseMessageType = getMessage(messageType.getName(), baseScenario);
      elements.addAll(
          baseMessageType.getStructure().getComponentRefOrGroupRefOrFieldRef());
      baseScenario = baseMessageType.getExtends();
    }
    return elements;
  }

}
