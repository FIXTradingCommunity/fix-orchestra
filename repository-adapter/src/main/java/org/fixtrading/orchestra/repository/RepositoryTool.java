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
package org.fixtrading.orchestra.repository;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.fixtrading.orchestra.messages.MessageEntity;
import org.fixtrading.orchestra.messages.MessageOntologyManager;
import org.fixtrading.orchestra.repository.jaxb.Component;
import org.fixtrading.orchestra.repository.jaxb.ComponentRef;
import org.fixtrading.orchestra.repository.jaxb.ComponentTypeT;
import org.fixtrading.orchestra.repository.jaxb.Components;
import org.fixtrading.orchestra.repository.jaxb.Datatype;
import org.fixtrading.orchestra.repository.jaxb.Datatypes;
import org.fixtrading.orchestra.repository.jaxb.Enum;
import org.fixtrading.orchestra.repository.jaxb.Field;
import org.fixtrading.orchestra.repository.jaxb.FieldRef;
import org.fixtrading.orchestra.repository.jaxb.Fields;
import org.fixtrading.orchestra.repository.jaxb.Fix;
import org.fixtrading.orchestra.repository.jaxb.FixRepository;
import org.fixtrading.orchestra.repository.jaxb.Message;
import org.fixtrading.orchestra.repository.jaxb.MessageEntityT;
import org.fixtrading.orchestra.repository.jaxb.Messages;
import org.fixtrading.orchestra.repository.jaxb.RepeatingGroup;
import org.fixtrading.orchestra.repository.messages.Serializer;

/**
 * @author Donald
 *
 */
public class RepositoryTool {

  /**
   * @param args
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

  public static void usage() {
    System.out.println("Usage: RepositoryParser <repository-file-name> [ontology-file-name]");
  }

  private MessageOntologyManager ontologyManager = new MessageOntologyManager();

  /**
   * @throws Exception
   * 
   */
  public RepositoryTool() throws Exception {}

  public void init() throws Exception {
    ontologyManager.init();
    ontologyManager.createNewModel("fix", URI.create("http://FixRepositoryOntology/"));
  }

  public void parse(InputStream inputStream) throws JAXBException {
    FixRepository fixRepository = Serializer.unmarshal(inputStream);
    List<Fix> fixList = fixRepository.getFix();
    for (Fix fix : fixList) {
      Datatypes datatypes = fix.getDatatypes();
      List<Datatype> datatypeList = datatypes.getDatatype();
      for (Datatype datatype : datatypeList) {
        ontologyManager.createDataType(datatype.getName());
      }

      Fields fields = fix.getFields();
      List<Field> fieldList = fields.getField();
      for (Field field : fieldList) {
        MessageEntity fieldObject =
            ontologyManager.createField(field.getId(), field.getName(), field.getType());

        List<Enum> enumList = null;
        BigInteger referencedTag = field.getEnumDatatype();
        if (referencedTag != null) {
          Field referencedField = findField(referencedTag, fieldList);
          enumList = referencedField.getEnum();
        } else {
          enumList = field.getEnum();
        }

        for (Enum fixEnum : enumList) {
          ontologyManager.createState(fieldObject, fixEnum.getSymbolicName(), fixEnum.getValue());
        }
      }
      
      Components components = fix.getComponents();
      List<Component> componentList = components.getComponent();
      for (Component component : componentList) {
        BigInteger id = component.getId();
        String name = component.getName();
        ComponentTypeT componentType = component.getType();
        MessageEntity parent = null;
        switch (componentType) {
          case BLOCK:
          case IMPLICIT_BLOCK:
          case XML_DATA_BLOCK:
            parent = ontologyManager.createComponent(id, name);
            break;
          case BLOCK_REPEATING:
          case IMPLICIT_BLOCK_REPEATING:
          case OPTIMISED_IMPLICIT_BLOCK_REPEATING:
            parent = ontologyManager.createRepeatingGroup(id, name);
            break;
        }
        List<JAXBElement<? extends MessageEntityT>> messageEntityList = component.getMessageEntity();
        for (JAXBElement<? extends MessageEntityT> messageEntity : messageEntityList) {
          MessageEntityT entity = messageEntity.getValue();
          addEntity(parent, entity);
        }
      }
      
      Messages messages = fix.getMessages();
      List<Message> messageList = messages.getMessage();
      for (Message message : messageList) {
        MessageEntity parent = ontologyManager.createMessage(message.getId(), message.getName(), message.getMsgType());
        List<JAXBElement<? extends MessageEntityT>> messageEntityList = message.getMessageEntity();
        for (JAXBElement<? extends MessageEntityT> messageEntity : messageEntityList) {
          MessageEntityT entity = messageEntity.getValue();
          addEntity(parent, entity);
        }        
      }
    }
  }

  public void addEntity(MessageEntity parent, MessageEntityT entity) {
    BigInteger entityId = entity.getId();
    String name = entity.getName();
    boolean isRequired = entity.getRequired() != 0;
    if (entity instanceof FieldRef) {
      ontologyManager.addField(parent, entityId, name, isRequired);
    } else if (entity instanceof ComponentRef) {
      ontologyManager.addComponent(parent, entityId, name, isRequired);      
    } else if (entity instanceof RepeatingGroup) {
      ontologyManager.addNumInGroupField(parent, entityId, name, isRequired); 
      RepeatingGroup group = (RepeatingGroup) entity;
      List<JAXBElement<? extends MessageEntityT>> entityList = group.getMessageEntity();
      for (JAXBElement<? extends MessageEntityT> childElement: entityList) {
        MessageEntityT child = childElement.getValue();
        addEntity(parent, child);
      }
    }
  }

  /**
   * @param outputStream
   * @throws Exception
   */
  public void store(FileOutputStream outputStream) throws Exception {
    ontologyManager.storeModel(outputStream);
  }

  private Field findField(BigInteger referencedTag, List<Field> fieldList) {
    for (Field field : fieldList) {
      if (field.getId().equals(referencedTag)) {
        return field;
      }
    }
    return null;
  }

}
