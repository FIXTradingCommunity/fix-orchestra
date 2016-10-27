/**
 * Copyright 2016 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.repository2016;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBException;

import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeSets;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Components;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.Datatypes;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.Fields;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Messages;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Protocol;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.messages.MessageEntity;
import io.fixprotocol.orchestra.messages.MessageOntologyManager;
import io.fixprotocol.orchestra.messages.Model;

/**
 * Imports FIX Repository 2010 edition
 * 
 * @author Don Mendelson
 *
 */
public class RepositoryTool {

  /**
   * Translates a FIX Repository file to an ontology file
   * 
   * @param args command line arguments
   *        <ol>
   *        <li>repository-file-name</li>
   *        <li>ontology-file-name</li>
   *        </ol>
   */
  public static void main(String[] args) {
    if (args.length < 2) {
      usage();
    } else {
      try {
        RepositoryTool tool = new RepositoryTool();
        FileInputStream inputStream = new FileInputStream(args[0]);
        FileOutputStream outputStream = new FileOutputStream(args[1]);
        tool.init();
        tool.parse(inputStream);
        tool.store(outputStream);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }

  /**
   * Prints application usage
   */
  public static void usage() {
    System.out.println("Usage: RepositoryTool <repository-file-name> <ontology-file-name>");
  }

  private Model model;
  private final MessageOntologyManager ontologyManager = new MessageOntologyManager();

  /**
   * Add a message element to the ontology
   * 
   * @param parent containing component
   * @param entity an element to add
   */
  void addEntity(MessageEntity parent, Object entity) {
    if (entity instanceof FieldRefType) {
      FieldRefType fieldRefType = (FieldRefType) entity;
      BigInteger entityId = fieldRefType.getId();
      String name = fieldRefType.getName();
      boolean isRequired = fieldRefType.getPresence().equals(PresenceT.REQUIRED);
      ontologyManager.addField(parent, entityId.intValue(), name, isRequired);
    } else if (entity instanceof ComponentRefType) {
      ComponentRefType componentRefType = (ComponentRefType) entity;
      BigInteger entityId = componentRefType.getId();
      String name = componentRefType.getName();
      boolean isRequired = componentRefType.getPresence().equals(PresenceT.REQUIRED);
      ontologyManager.addComponent(parent, entityId.intValue(), name, isRequired);
    }

  }

  /**
   * Initializes resources
   * 
   * @throws Exception if resources cannot be initialized
   */
  public void init() throws Exception {
    ontologyManager.init();
    model =
        ontologyManager.createNewModel("fix", URI.create("http://fixtrading.io/2010/orchestra"));
  }

  /**
   * Parses a FIX repository
   * 
   * @param inputStream stream of the repository contents
   * @throws JAXBException if a parsing failure occurs
   */
  public void parse(InputStream inputStream) throws JAXBException {
    Repository repository = Serializer.unmarshal(inputStream);
    Datatypes datatypes = repository.getDatatypes();
    List<Datatype> datatypeList = datatypes.getDatatype();
    for (Datatype datatype : datatypeList) {
      ontologyManager.createDataType(model, datatype.getName());
    }
    final List<CodeSets> codeSetsList = repository.getCodeSets();
    for (CodeSets codeSetsCollection : codeSetsList) {
      final List<CodeSetType> codeSetList = codeSetsCollection.getCodeSet();
      for (CodeSetType codeSet : codeSetList) {
        String codeSetName = codeSet.getName();
        String type = codeSet.getType();
        ontologyManager.createCodeSet(model, codeSetName, type);
        List<CodeType> codeList = codeSet.getCode();
        for (CodeType code : codeList) {
          ontologyManager.createCode(model, codeSetName, code.getSymbolicName(), code.getValue());
        }
      }
    }

    Fields fields = repository.getFields();
    List<FieldType> fieldList = fields.getField();
    for (FieldType field : fieldList) {
      ontologyManager.createField(model, field.getId().intValue(), field.getName(),
          field.getType());

      // final BigInteger referencedTag = field.getEnumDatatype();
      // if (referencedTag != null) {
      // ontologyManager.associateCodeList(model, field.getName(), referencedTag.intValue());
    }
    // Make a second pass to associate fields after they are all created
    for (FieldType field : fieldList) {
      final BigInteger associatedDataTag = field.getLengthId();
      if (associatedDataTag != null) {
        ontologyManager.associateFields(model, associatedDataTag.intValue(), 
            field.getId().intValue());
      }
    }

    List<Protocol> protocolList = repository.getProtocol();
    for (Protocol protocol : protocolList) {


      Components components = protocol.getComponents();
      List<ComponentType> componentList = components.getComponentOrGroup();
      for (ComponentType component : componentList) {
        BigInteger id = component.getId();
        String name = component.getName();
        List<Object> messageEntityList = component.getComponentRefOrGroupRefOrFieldRef();
        MessageEntity parent;
        if (component instanceof GroupType) {
          GroupType groupType = (GroupType) component;
          parent = ontologyManager.createRepeatingGroup(model, id.intValue(), name);
          ontologyManager.addNumInGroupField(parent, groupType.getNumInGroupId().intValue(), 
              groupType.getNumInGroupName());

        } else {
          parent = ontologyManager.createComponent(model, id.intValue(), name);
        }

        for (Object messageEntity : messageEntityList) {
          addEntity(parent, messageEntity);
        }
      }

      Messages messages = protocol.getMessages();
      List<MessageType> messageList = messages.getMessage();
      for (MessageType message : messageList) {
        MessageEntity parent = ontologyManager.createMessage(model, message.getId().intValue(),
            message.getName(), message.getMsgType());
        List<Object> messageEntityList = message.getStructure().getComponentOrComponentRefOrGroup();
        for (Object messageEntity : messageEntityList) {
          addEntity(parent, messageEntity);
        }
      }
    }
  }

  /**
   * Stores a translated ontology
   * 
   * @param outputStream stream of the ontology contents
   * @throws Exception if the ontology cannot be written
   */
  public void store(FileOutputStream outputStream) throws Exception {
    ontologyManager.storeModel(model, outputStream);
  }

}
