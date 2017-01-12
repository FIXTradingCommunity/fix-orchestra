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
package io.fixprotocol.orchestra.owl.repository2016;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import org.purl.dc.elements._1.SimpleLiteral;

import io.fixprotocol._2016.fixrepository.Annotation;
import io.fixprotocol._2016.fixrepository.CodeSetType;
import io.fixprotocol._2016.fixrepository.CodeSets;
import io.fixprotocol._2016.fixrepository.CodeType;
import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.ComponentType;
import io.fixprotocol._2016.fixrepository.Components;
import io.fixprotocol._2016.fixrepository.Datatype;
import io.fixprotocol._2016.fixrepository.Datatypes;
import io.fixprotocol._2016.fixrepository.Documentation;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.FieldType;
import io.fixprotocol._2016.fixrepository.Fields;
import io.fixprotocol._2016.fixrepository.GroupType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Messages;
import io.fixprotocol._2016.fixrepository.PresenceT;
import io.fixprotocol._2016.fixrepository.Protocol;
import io.fixprotocol._2016.fixrepository.Repository;
import io.fixprotocol.orchestra.owl.MessageEntity;
import io.fixprotocol.orchestra.owl.MessageOntologyManager;
import io.fixprotocol.orchestra.owl.Model;

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
   *        <li>owl-file-name</li>
   *        <li>uri of ontology</li>
   *        </ol>
   */
  public static void main(String[] args) {
    if (args.length < 3) {
      usage();
    } else {
      try {
        RepositoryTool tool = new RepositoryTool();
        FileInputStream inputStream = new FileInputStream(args[0]);
        FileOutputStream outputStream = new FileOutputStream(args[1]);
        tool.init(args[2]);
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
    System.out.println("Usage: RepositoryTool <repository-file-name> <owl-file-name> <IRI>");
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
   * @param uri
   * 
   * @throws Exception if resources cannot be initialized
   */
  public void init(String uri) throws Exception {
    String modelUri = uri;
    if (!uri.endsWith("#")) {
      modelUri += "#";
    }

    ontologyManager.init();
    model = ontologyManager.createNewModel(URI.create(modelUri));
  }

  /**
   * Parses a FIX repository
   * 
   * @param inputStream stream of the repository contents
   * @throws JAXBException if a parsing failure occurs
   */
  public void parse(InputStream inputStream) throws JAXBException {
    Repository repository = Serializer.unmarshal(inputStream);
    List<JAXBElement<SimpleLiteral>> metaDataList = repository.getMetadata().getAny();
    for (JAXBElement<SimpleLiteral> metadataElement : metaDataList) {
      String name = metadataElement.getName().getLocalPart();
      String namespace = metadataElement.getName().getNamespaceURI();
      List<String> value = metadataElement.getValue().getContent();
      ontologyManager.setMetadata(model, namespace, name, value);
    }
    
    Datatypes datatypes = repository.getDatatypes();
    List<Datatype> datatypeList = datatypes.getDatatype();
    for (Datatype datatype : datatypeList) {
      MessageEntity messageEntity = ontologyManager.createDataType(model, datatype.getName());
      Annotation annotation = datatype.getAnnotation();
      if (annotation != null) {
        addAnnotation(messageEntity, annotation);
      }
    }
    final List<CodeSets> codeSetsList = repository.getCodeSets();
    for (CodeSets codeSetsCollection : codeSetsList) {
      final List<CodeSetType> codeSetList = codeSetsCollection.getCodeSet();
      for (CodeSetType codeSet : codeSetList) {
        String codeSetName = codeSet.getName();
        String type = codeSet.getType();
        MessageEntity messageEntity = ontologyManager.createCodeSet(model, codeSetName, type);
        Annotation annotation = codeSet.getAnnotation();
        if (annotation != null) {
          addAnnotation(messageEntity, annotation);
        }
        List<CodeType> codeList = codeSet.getCode();
        for (CodeType code : codeList) {
          MessageEntity codeMessageEntity = ontologyManager.createCode(model, codeSetName, code.getName(), code.getValue());
          annotation = code.getAnnotation();
          if (annotation != null) {
            addAnnotation(codeMessageEntity, annotation);
          }

        }
      }
    }

    Fields fields = repository.getFields();
    List<FieldType> fieldList = fields.getField();
    for (FieldType field : fieldList) {
      MessageEntity messageEntity = ontologyManager.createField(model, field.getId().intValue(), field.getName(),
          field.getType());
      Annotation annotation = field.getAnnotation();
      if (annotation != null) {
        addAnnotation(messageEntity, annotation);
      }

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
        Annotation annotation = component.getAnnotation();
        if (annotation != null) {
          addAnnotation(parent, annotation);
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
        Annotation annotation = message.getAnnotation();
        if (annotation != null) {
          addAnnotation(parent, annotation);
        }
        List<Object> messageEntityList = message.getStructure().getComponentOrComponentRefOrGroup();
        for (Object messageEntity : messageEntityList) {
          addEntity(parent, messageEntity);
        }
      }
    }
  }

  private void addAnnotation(MessageEntity messageEntity, Annotation annotation) {
    List<Object> objList = annotation.getDocumentationOrAppinfo();
    for (Object obj : objList) {
      if (obj instanceof Documentation) {
        Documentation documentation = (Documentation) obj;
        String lang = documentation.getLangId();
        String purpose = documentation.getPurpose();
        List<Object> content = documentation.getContent();
        ontologyManager.setDocumentation(messageEntity, lang, purpose, content);
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
