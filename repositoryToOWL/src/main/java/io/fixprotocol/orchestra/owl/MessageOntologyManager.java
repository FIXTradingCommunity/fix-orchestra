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
package io.fixprotocol.orchestra.owl;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.AddOntologyAnnotation;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyIRIMapper;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.PriorityCollection;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

/**
 * Populates a message ontology
 * 
 * @author Don Mendelson
 *
 */
public class MessageOntologyManager {

  class CodeObject implements ObjectHolder, MessageEntity {

    private final OWLNamedIndividual codeInd;
    private final MessageModel model;

    CodeObject(MessageModel messageModel, OWLNamedIndividual codeInd) {
      this.model = messageModel;
      this.codeInd = codeInd;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      CodeObject other = (CodeObject) obj;
      if (!getOuterType().equals(other.getOuterType()))
        return false;
      if (getName() == null) {
        if (other.getName() != null)
          return false;
      } else if (!getName().equals(other.getName()))
        return false;
      return true;
    }

    @Override
    public MessageModel getModel() {
      return model;
    }

    public String getName() {
      String name = null;
      Set<OWLLiteral> values =
          model.getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        name = first.getLiteral();
      }

      return name;
    }

    public OWLNamedIndividual getObject() {
      return codeInd;
    }

    private MessageOntologyManager getOuterType() {
      return MessageOntologyManager.this;
    }

    public String getValue() {
      String value = null;
      Set<OWLLiteral> values =
          model.getReasoner().getDataPropertyValues(getObject(), hasValueProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        value = first.getLiteral();
      }

      return value;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
      return result;
    }

    public CodeObject withName(String name) {
      Objects.requireNonNull(name, "Name cannot be null");

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasNameProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
      return this;
    }

    public CodeObject withValue(String value) {
      Objects.requireNonNull(value, "Value cannot be null");

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(value);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasValueProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
      return this;
    }

  }
  class CodeSetObject extends DataTypeObject {

    CodeSetObject(MessageModel messageModel, OWLNamedIndividual messageObject) {
      super(messageModel, messageObject);
    }

    public CodeSetObject getDataType() {
      CodeSetObject datatype = null;
      Set<OWLNamedIndividual> values = getModel().getReasoner()
          .getObjectPropertyValues(getObject(), hasDataTypeProperty).entities().collect(Collectors.toSet());
      final OWLNamedIndividual first = values.iterator().next();
      if (first != null) {
        datatype = new CodeSetObject(getModel(), first);
      }

      return datatype;
    }

    public CodeSetObject withDataType(String name) {
      OWLNamedIndividual datatypeInd = getDataFactory().getOWLNamedIndividual(":type-" + name,
          getModel().getPrefixManager());

      OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
          .getOWLObjectPropertyAssertionAxiom(hasDataTypeProperty, getObject(), datatypeInd);
      getOntologyManager().addAxiom(getModel().getDerivedModel(), propertyAssertion);
      OWLLiteral literal = getDataFactory().getOWLLiteral(name);
      OWLAnnotation label = getDataFactory().getOWLAnnotation(
          getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
      OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
          .getOWLAnnotationAssertionAxiom(datatypeInd.asOWLNamedIndividual().getIRI(), label);
      getOntologyManager().addAxiom(getModel().getDerivedModel(), annotationAxiom);

      return this;
    }
  }
  class DataTypeObject implements MessageEntity, ObjectHolder {
    private final OWLNamedIndividual dataTypeObject;
    private final MessageModel model;

    DataTypeObject(MessageModel messageModel, OWLNamedIndividual dataTypeObject) {
      this.dataTypeObject = dataTypeObject;
      this.model = messageModel;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      DataTypeObject other = (DataTypeObject) obj;
      return getName().equals(other.getName());
    }

    public MessageModel getModel() {
      return model;
    }

    public String getName() {
      String name = null;
      Set<OWLLiteral> values =
          model.getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        name = first.getLiteral();
      }

      return name;
    }

    public OWLNamedIndividual getObject() {
      return dataTypeObject;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getName().hashCode();
      return result;
    }

    public DataTypeObject withName(String name) {
      Objects.requireNonNull(name, "Name cannot be null");

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasNameProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
      return this;
    }
  }
  class FieldObject extends MessageObject implements MessageEntity {
    /**
     * @param messageModel
     * @param messageObject
     */
    FieldObject(MessageModel messageModel, OWLNamedIndividual messageObject) {
      super(messageModel, messageObject);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      FieldObject other = (FieldObject) obj;
      return getName().equals(other.getName());
    }

    public DataTypeObject getDataType() {
      DataTypeObject datatype = null;
      Set<OWLNamedIndividual> values = getModel().getReasoner()
          .getObjectPropertyValues(getObject(), hasDataTypeProperty).entities().collect(Collectors.toSet());
      final OWLNamedIndividual first = values.iterator().next();
      if (first != null) {
        if (isCodeSet(getModel(), first)) {
          datatype = new CodeSetObject(getModel(), first);
        } else {
          datatype = new DataTypeObject(getModel(), first);
        }
      }

      return datatype;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getName().hashCode();
      return result;
    }

    /**
     * @param name of data type
     */
    public FieldObject withDataType(String name) {
      OWLNamedIndividual datatypeInd = getDataFactory().getOWLNamedIndividual(":type-" + name,
          getModel().getPrefixManager());
      OWLClassAssertionAxiom classAssertion =
          getDataFactory().getOWLClassAssertionAxiom(dataTypeClass, datatypeInd);
      getOntologyManager().addAxiom(getModel().getDerivedModel(), classAssertion);

      OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
          .getOWLObjectPropertyAssertionAxiom(hasDataTypeProperty, getObject(), datatypeInd);
      getOntologyManager().addAxiom(getModel().getDerivedModel(), propertyAssertion);

      return this;
    }
  }

  private class MessageModel implements Model {

    private final OWLOntology derivedModel;
    private final PrefixManager prefixManager;
    private final OWLReasoner reasoner;

    /**
     * @param derivedModel
     * @param prefixManager
     * @param reasoner
     *
     */
    public MessageModel(OWLOntology derivedModel, PrefixManager prefixManager,
        OWLReasoner reasoner) {
      this.derivedModel = derivedModel;
      this.reasoner = reasoner;
      this.prefixManager = prefixManager;
    }

    /**
     * @return the derivedModel
     */
    public OWLOntology getDerivedModel() {
      return derivedModel;
    }

    public PrefixManager getPrefixManager() {
      return prefixManager;
    }

    /**
     * @return the reasoner
     */
    public OWLReasoner getReasoner() {
      return reasoner;
    }

  }

  class MessageObject implements MessageEntity, ObjectHolder {
    private final OWLNamedIndividual messageObject;
    private final MessageModel model;

    MessageObject(MessageModel model, OWLNamedIndividual messageObject) {
      this.messageObject = messageObject;
      this.model = model;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj)
        return true;
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      MessageObject other = (MessageObject) obj;
      return getName().equals(other.getName());
    }

    int getId() {
      int id = 0;
      Set<OWLLiteral> values =
          model.getReasoner().getDataPropertyValues(getObject(), hasIdProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        id = first.parseInteger();
      }

      return id;
    }

    /**
     * @return the model
     */
    public MessageModel getModel() {
      return model;
    }

    public String getName() {
      String name = null;
      Set<OWLLiteral> values =
          model.getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        name = first.getLiteral();
      }

      return name;
    }

    public OWLNamedIndividual getObject() {
      return messageObject;
    }

    public String getShortName() {
      String name = null;
      Set<OWLLiteral> values =
          model.getReasoner().getDataPropertyValues(getObject(), hasShortNameProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        name = first.getLiteral();
      }

      return name;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + getName().hashCode();
      return result;
    }

    public MessageObject withId(int id) {
      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(id);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasIdProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
      return this;
    }

    public MessageObject withName(String name) {
      Objects.requireNonNull(name, "Name cannot be null");
      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasNameProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
      return this;
    }

    public MessageObject withShortName(String name) {
      Objects.requireNonNull(name, "Name cannot be null");
      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasShortNameProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
      return this;
    }

  }

  interface ObjectHolder {
    MessageModel getModel();

    default String getName() {
      return getObject().getIRI().getRemainder().get();
    }
    OWLNamedIndividual getObject();
  }

  private static OWLClass codeSetClass;

  private static final String DCTERMS_PREFIX = "dcterms";

  private static final String DCTERMS_URI = "http://purl.org/dc/terms#";

  private static final String ORCHESTRA_PREFIX = "orch";

  private static final String ORCHESTRA_URI = "http://fixprotocol.io/2016/orchestra#";

  public static boolean isCodeSet(MessageModel model, OWLNamedIndividual dataTypeObject) {
    return model.getReasoner().getTypes(dataTypeObject, true).containsEntity(codeSetClass);
  }

  private OWLClass codeLiteralClass;
  private OWLClass componentClass;
  private OWLDataFactory dataFactory;
  private OWLClass dataTypeClass;
  private OWLClass fieldClass;
  private OWLObjectProperty hasDataTypeProperty;
  private OWLDataProperty hasIdProperty;
  private OWLDataProperty hasNameProperty;
  private OWLObjectProperty hasProperty;
  private OWLDataProperty hasShortNameProperty;
  private OWLObjectProperty hasSizeFieldProperty;
  private OWLDataProperty hasValueProperty;
  private OWLObjectProperty isSizeOfProperty;
  private OWLObjectProperty memberProperty;
  private OWLClass messageClass;
  private OWLOntologyManager ontologyManager;
  private OWLClass repeatingGroupClass;

  private OWLObjectProperty requiresProperty;

  /**
   * Adds a FIX message component to its parent component
   * 
   * @param parent component to which the new component will be added
   * @param entityId unique ID of the added component
   * @param name name of the added component
   * @param isRequired the component is required if {@code true}
   */
  public void addComponent(MessageEntity parent, int entityId, String name, boolean isRequired) {
    Objects.requireNonNull(parent, "Parent component cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageObject parentObject = (MessageObject) parent;
    OWLNamedIndividual parentInd = parentObject.getObject();

    MessageModel model = parentObject.getModel();

    OWLNamedIndividual entity =
        getDataFactory().getOWLNamedIndividual(":comp-" + name, model.getPrefixManager());

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(
            isRequired ? requiresProperty : hasProperty, parentInd, entity);
    getOntologyManager().addAxiom(model.getDerivedModel(), propertyAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(entity.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(model.getDerivedModel(), annotationAxiom);
  }

  /**
   * 
   * Adds a FIX message field to its parent component
   * 
   * @param parent component to which the new field will be added
   * @param entityId unique ID of the added field
   * @param name name of the added field
   * @param isRequired the field is required if {@code true}
   */
  public void addField(MessageEntity parent, int entityId, String name, boolean isRequired) {
    Objects.requireNonNull(parent, "Parent component cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageObject parentObject = (MessageObject) parent;
    OWLNamedIndividual parentInd = parentObject.getObject();

    MessageModel model = parentObject.getModel();

    OWLNamedIndividual entity =
        getDataFactory().getOWLNamedIndividual(":fld-" + name, model.getPrefixManager());

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(
            isRequired ? requiresProperty : hasProperty, parentInd, entity);
    getOntologyManager().addAxiom(model.getDerivedModel(), propertyAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(entity.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(model.getDerivedModel(), annotationAxiom);
  }

  public void addNumInGroupField(MessageEntity parent, int entityId, String name) {
    Objects.requireNonNull(parent, "Parent component cannot be null");

    MessageObject parentObject = (MessageObject) parent;
    OWLNamedIndividual parentInd = parentObject.getObject();

    MessageModel model = parentObject.getModel();

    OWLNamedIndividual entity = getFieldById(entityId, model);

    OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
        .getOWLObjectPropertyAssertionAxiom(hasSizeFieldProperty, parentInd, entity);
    getOntologyManager().addAxiom(model.getDerivedModel(), propertyAssertion);
  }

  public void associateCodeList(Model model, String name, int referencedTag) {
    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual field = getField(messageModel, name).getObject();

    OWLNamedIndividual referencedField = getFieldById(referencedTag, messageModel);
    FieldObject refFieldObj = new FieldObject(messageModel, referencedField);

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasDataTypeProperty, field,
            refFieldObj.getDataType().getObject());
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), propertyAssertion);
  }

  public void associateFields(Model model, int id, int associatedDataTag) {
    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual field = getFieldById(id, messageModel);

    OWLNamedIndividual associatedField = getFieldById(associatedDataTag, messageModel);
    OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
        .getOWLObjectPropertyAssertionAxiom(isSizeOfProperty, field, associatedField);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), propertyAssertion);
  }

  /**
   * Create a new valid value of a field.
   * <p>
   * This is known as an 'enum' in FIX repository, but it is not truly an enumeration since codes
   * have no inherent order or ordinal. An Alt, by contrast, has no order.
   * 
   * @param model ontology to update
   * @param codeSetName name of the code set
   * @param name symbolic name of the new value
   * @param valueAsString the valid value in string format. It may need to be cast or converted to
   *        the true data type of the field.
   * @return 
   */
  public CodeObject createCode(Model model, String codeSetName, String name, String valueAsString) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(codeSetName, "Code set name cannot be null");
    Objects.requireNonNull(name, "Symbolic name cannot be null");
    Objects.requireNonNull(valueAsString, "Value cannot be null");

    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual codeInd = getDataFactory().getOWLNamedIndividual(
        ":type-" + codeSetName + "-" + name, messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(codeLiteralClass, codeInd);
    final OWLOntology derivedModel = messageModel.getDerivedModel();
    getOntologyManager().addAxiom(derivedModel, classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(codeSetName + "/" + name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(codeInd.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(derivedModel, annotationAxiom);


    OWLNamedIndividual codeSetInd = getDataFactory()
        .getOWLNamedIndividual(":type-" + codeSetName, messageModel.getPrefixManager());

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(memberProperty, codeSetInd, codeInd);
    getOntologyManager().addAxiom(derivedModel, propertyAssertion);

    OWLLiteral nameLiteral = getDataFactory().getOWLLiteral(name);

    OWLDataPropertyAssertionAxiom namePropertyAssertion =
        getDataFactory().getOWLDataPropertyAssertionAxiom(hasNameProperty, codeInd, nameLiteral);
    getOntologyManager().addAxiom(derivedModel, namePropertyAssertion);

    OWLLiteral valueLiteral = getDataFactory().getOWLLiteral(valueAsString);

    OWLDataPropertyAssertionAxiom valuePropertyAssertion =
        getDataFactory().getOWLDataPropertyAssertionAxiom(hasValueProperty, codeInd, valueLiteral);
    getOntologyManager().addAxiom(derivedModel, valuePropertyAssertion);
    
    return new CodeObject(messageModel, codeInd);
  }

  public CodeSetObject createCodeSet(Model model, String codeSetName,
      String primitiveDatatypeName) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(codeSetName, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;
    OWLNamedIndividual datatype = getDataFactory()
        .getOWLNamedIndividual(":type-" + codeSetName, messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(codeSetClass, datatype);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(codeSetName);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(datatype.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), annotationAxiom);


    CodeSetObject dataTypeObject = new CodeSetObject(messageModel, datatype);
    dataTypeObject.withDataType(primitiveDatatypeName).withName(codeSetName);
    return dataTypeObject;
  }

  /**
   * Create a new component in the model
   * 
   * @param model ontology to update
   * @param id unique identifier of the new component
   * @param name name of the new component
   * @return a wrapper for the new component
   */
  public MessageObject createComponent(Model model, int id, String name) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual component = getDataFactory().getOWLNamedIndividual(":comp-" + name,
        messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(componentClass, component);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(component.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), annotationAxiom);

    MessageObject messageObject = new MessageObject(messageModel, component);
    messageObject.withId(id).withName(name);
    return messageObject;
  }

  /**
   * Create a new data type in the model
   * 
   * @param model ontology to update
   * @param name name of the new data type
   * @return a wrapper for the new data type
   */
  public DataTypeObject createDataType(Model model, String name) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;
    OWLNamedIndividual datatype = getDataFactory().getOWLNamedIndividual(":type-" + name,
        messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(dataTypeClass, datatype);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(datatype.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), annotationAxiom);


    DataTypeObject dataTypeObject = new DataTypeObject(messageModel, datatype);
    dataTypeObject.withName(name);
    return dataTypeObject;
  }

  public FieldObject createField(Model model, int id, String name) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual field =
        getDataFactory().getOWLNamedIndividual(":fld-" + name, messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(fieldClass, field);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(field.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), annotationAxiom);


    FieldObject messageObject = new FieldObject(messageModel, field);
    messageObject.withId(id).withName(name);
    return messageObject;
  }

  /**
   * Create a new field in the model
   * 
   * @param model ontology to update
   * @param id unique identifier of the new field
   * @param name name of the new field
   * @param dataType data type of the field
   * @return a wrapper for the new field
   */
  public FieldObject createField(Model model, int id, String name, String dataType) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(dataType, "Data type cannot be null");

    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual field =
        getDataFactory().getOWLNamedIndividual(":fld-" + name, messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(fieldClass, field);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(field.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), annotationAxiom);

    FieldObject messageObject = new FieldObject(messageModel, field);
    messageObject.withDataType(dataType).withId(id).withName(name);
    return messageObject;
  }

  /**
   * Create a new message in the model
   * 
   * @param model ontology to update
   * @param id unique identifier of the new message
   * @param name name of the new field
   * @param shortName alternative name of the new field
   * @return a wrapper for the new field
   */
  public MessageObject createMessage(Model model, int id, String name, String shortName) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual message = getDataFactory().getOWLNamedIndividual(":msg-" + name,
        messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(messageClass, message);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(message.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), annotationAxiom);


    MessageObject messageObject = new MessageObject(messageModel, message);
    messageObject.withId(id).withName(name).withShortName(shortName);
    return messageObject;
  }

  /**
   * Create a new ontology model
   * 
   * @param uri identifier of the model
   * 
   * @throws Exception if an ontology cannot be created
   */
  public Model createNewModel(URI uri) throws Exception {
    IRI derivedIRI = IRI.create(uri);
    DefaultPrefixManager prefixManager =
        new DefaultPrefixManager(null, null, derivedIRI.toString());

    StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    final OWLOntology derivedModel = ontologyManager.createOntology(derivedIRI);
    final OWLReasoner reasoner = reasonerFactory.createReasoner(derivedModel);

    final IRI orchestraIRI = IRI.create(ORCHESTRA_URI);
    OWLImportsDeclaration importDeclaration =
        this.ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(orchestraIRI);
    this.ontologyManager.applyChange(new AddImport(derivedModel, importDeclaration));
    prefixManager.setPrefix(ORCHESTRA_PREFIX, ORCHESTRA_URI);
    
    URLClassLoader classLoader = new URLClassLoader(new URL [] {new URL("file://")});
    final URL document = classLoader.getResource("orchestra2016.ttl");

    PriorityCollection<OWLOntologyIRIMapper> iriMappers = ontologyManager.getIRIMappers();
    iriMappers.add(new SimpleIRIMapper(orchestraIRI, IRI.create(document)));

    final IRI dctermsIRI = IRI.create(DCTERMS_URI);
    OWLImportsDeclaration importDeclaration2 =
        this.ontologyManager.getOWLDataFactory().getOWLImportsDeclaration(dctermsIRI);
    this.ontologyManager.applyChange(new AddImport(derivedModel, importDeclaration2));
    prefixManager.setPrefix(DCTERMS_PREFIX, DCTERMS_URI);

    URL document2 = ClassLoader.class.getResource("/dcterms.ttl");
    iriMappers.add(new SimpleIRIMapper(dctermsIRI, IRI.create(document2)));

    prepareBaseModel(derivedModel, prefixManager);

    return new MessageModel(derivedModel, prefixManager, reasoner);
  }

  /**
   * Create a new repeating group component in the model
   * 
   * @param model ontology to update
   * @param id unique identifier of the new component
   * @param name name of the new component
   * @return a wrapper for the new component
   */

  public MessageEntity createRepeatingGroup(Model model, int id, String name) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;

    OWLNamedIndividual component = getDataFactory().getOWLNamedIndividual(":comp-" + name,
        messageModel.getPrefixManager());
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(repeatingGroupClass, component);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);
    OWLLiteral literal = getDataFactory().getOWLLiteral(name);
    OWLAnnotation label = getDataFactory().getOWLAnnotation(
        getDataFactory().getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), literal);
    OWLAnnotationAssertionAxiom annotationAxiom = getDataFactory()
        .getOWLAnnotationAssertionAxiom(component.asOWLNamedIndividual().getIRI(), label);
    getOntologyManager().addAxiom(messageModel.getDerivedModel(), annotationAxiom);


    MessageObject messageObject = new MessageObject(messageModel, component);
    messageObject.withId(id).withName(name);
    return messageObject;
  }

  public Set<CodeObject> getCodes(Model model, String codeSetName) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(codeSetName, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;
    OWLNamedIndividual codeSetInd = getDataFactory()
        .getOWLNamedIndividual(":type-" + codeSetName, messageModel.getPrefixManager());

    Set<OWLNamedIndividual> members = messageModel.getReasoner()
        .getObjectPropertyValues(codeSetInd, memberProperty).entities().collect(Collectors.toSet());
    return members.stream().map(m -> new CodeObject(messageModel, m)).collect(Collectors.toSet());
  }

  /**
   * @return the dataFactory
   */
  protected OWLDataFactory getDataFactory() {
    return dataFactory;
  }

  /**
   * Returns a named field
   * 
   * @param name name of a field
   * @return a wrapped field object
   */
  public FieldObject getField(Model model, String name) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;
    OWLNamedIndividual field =
        getDataFactory().getOWLNamedIndividual(":fld-" + name, messageModel.getPrefixManager());
    return new FieldObject(messageModel, field);
  }

  private OWLNamedIndividual getFieldById(int entityId, MessageModel model) {
    String fieldName = null;
    Set<OWLNamedIndividual> fields =
        model.getReasoner().getInstances(fieldClass, true).entities().collect(Collectors.toSet());
    for (OWLNamedIndividual fieldInd : fields) {
      Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(fieldInd, hasIdProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null && first.parseInteger() == entityId) {
        Set<OWLLiteral> names =
            model.getReasoner().getDataPropertyValues(fieldInd, hasNameProperty);
        fieldName = names.iterator().next().getLiteral();
        break;
      }
    }

    return getDataFactory().getOWLNamedIndividual(":fld-" + fieldName, model.getPrefixManager());
  }

  /**
   * Returns a named message
   * 
   * @param name name of a Message
   * @return a wrapped message object
   */
  public MessageEntity getMessage(Model model, String name) {
    Objects.requireNonNull(model, "Model cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageModel messageModel = (MessageModel) model;
    OWLNamedIndividual message = getDataFactory().getOWLNamedIndividual(":msg-" + name,
        messageModel.getPrefixManager());
    return new MessageObject(messageModel, message);
  }

  /**
   * Returns all message objects in the ontology
   * 
   * @return collection of messages
   */
  public Set<MessageEntity> getMessages(Model model) {
    MessageModel messageModel = (MessageModel) model;

    NodeSet<OWLNamedIndividual> instances =
        messageModel.getReasoner().getInstances(messageClass, true);
    Set<OWLNamedIndividual> objects = instances.entities().collect(Collectors.toSet());
    return objects.stream().map(o -> new MessageObject(messageModel, o))
        .collect(Collectors.toSet());
  }

  /**
   * @return the ontologyManager
   */
  protected OWLOntologyManager getOntologyManager() {
    return ontologyManager;
  }

  public void getOptionalFields(MessageEntity parent, Set<FieldObject> fields) {
    Objects.requireNonNull(parent, "Message entity cannot be null");

    MessageObject parentObject = (MessageObject) parent;
    OWLNamedIndividual parentInd = parentObject.getObject();
    MessageModel messageModel = parentObject.getModel();

    Set<OWLNamedIndividual> hasSet =
        messageModel.getReasoner().getObjectPropertyValues(parentInd, hasProperty).entities().collect(Collectors.toSet());

    for (OWLNamedIndividual ind : hasSet) {
      Set<OWLClass> classes = messageModel.getReasoner().getTypes(ind, true).entities().collect(Collectors.toSet());
      if (classes.contains(fieldClass)) {
        fields.add(new FieldObject(messageModel, ind));
      } else if (classes.contains(componentClass)) {
        // Recursively expand components
        getOptionalFields(new MessageObject(messageModel, ind), fields);
      }
    }
  }

  public void getRequiredFields(MessageEntity parent, Set<FieldObject> fields) {
    Objects.requireNonNull(parent, "Message entity cannot be null");

    MessageObject parentObject = (MessageObject) parent;
    OWLNamedIndividual parentInd = parentObject.getObject();
    MessageModel messageModel = parentObject.getModel();

    Set<OWLNamedIndividual> requiredSet = messageModel.getReasoner()
        .getObjectPropertyValues(parentInd, requiresProperty).entities().collect(Collectors.toSet());

    for (OWLNamedIndividual ind : requiredSet) {
      Set<OWLClass> classes = messageModel.getReasoner().getTypes(ind, true).entities().collect(Collectors.toSet());
      if (classes.contains(fieldClass)) {
        fields.add(new FieldObject(messageModel, ind));
      } else if (classes.contains(componentClass)) {
        // Recursively expand components
        getRequiredFields(new MessageObject(messageModel, ind), fields);
      }
    }
  }


  /**
   * Initialize resources and base ontology
   */
  public void init() throws Exception {
    this.ontologyManager = OWLManager.createOWLOntologyManager();
    this.dataFactory = OWLManager.getOWLDataFactory();
  }

  /**
   * Load an ontology model from an input stream
   * 
   * @param in input stream
   * @return a wrapped reference to the ontology
   * @throws Exception if an ontology cannot be read or parsed
   */
  public Model loadModel(InputStream in) throws Exception {
    URLClassLoader classLoader = new URLClassLoader(new URL [] {new URL("file://")});
    final URL document = classLoader.getResource("orchestra2016.ttl");
    final IRI orchestraIRI = IRI.create(ORCHESTRA_URI);
    PriorityCollection<OWLOntologyIRIMapper> iriMappers = ontologyManager.getIRIMappers();
    iriMappers.add(new SimpleIRIMapper(orchestraIRI, IRI.create(document)));
    DefaultPrefixManager prefixManager = new DefaultPrefixManager();
    prefixManager.setPrefix(ORCHESTRA_PREFIX, ORCHESTRA_URI);

    OWLOntology derivedModel = loadOntologyModel(in);
    Optional<IRI> optional = derivedModel.getOntologyID().getOntologyIRI();
    if (optional.isPresent()) {
      prefixManager.setDefaultPrefix(optional.get().toString());
      prepareBaseModel(derivedModel, prefixManager);

      StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
      OWLReasoner reasoner = reasonerFactory.createReasoner(derivedModel);
      reasoner.precomputeInferences();
      return new MessageModel(derivedModel, prefixManager, reasoner);
    } else {
      throw new RuntimeException("No ontology IRI found");
    }
  }

  /**
   * Create the base ontology model
   * 
   * @param in input stream
   * @return base model
   * @throws OWLOntologyCreationException If there was a problem in creating and loading the
   *         ontology.
   */
  OWLOntology loadOntologyModel(InputStream in) throws OWLOntologyCreationException {
    return ontologyManager.loadOntologyFromOntologyDocument(in);
  }

  private void prepareBaseModel(final OWLOntology derivedModel, PrefixManager prefixManager) {
    messageClass = getDataFactory().getOWLClass("orch:Message", prefixManager);
    fieldClass = getDataFactory().getOWLClass("orch:Field", prefixManager);
    dataTypeClass = getDataFactory().getOWLClass("orch:DataType", prefixManager);
    codeSetClass = getDataFactory().getOWLClass("orch:CodeSet", prefixManager);
    codeLiteralClass = getDataFactory().getOWLClass("orch:Code", prefixManager);
    componentClass = getDataFactory().getOWLClass("orch:Component", prefixManager);
    repeatingGroupClass = getDataFactory().getOWLClass("orch:RepeatingGroup", prefixManager);

    hasSizeFieldProperty =
        getDataFactory().getOWLObjectProperty("orch:hasSizeField", prefixManager);
    isSizeOfProperty = getDataFactory().getOWLObjectProperty("orch:isSizeOf", prefixManager);
    hasDataTypeProperty = getDataFactory().getOWLObjectProperty("orch:hasDataType", prefixManager);
    requiresProperty = getDataFactory().getOWLObjectProperty("orch:requires", prefixManager);
    hasProperty = getDataFactory().getOWLObjectProperty("orch:has", prefixManager);
    memberProperty = getDataFactory().getOWLObjectProperty("rdfs:member", prefixManager);

    hasNameProperty = getDataFactory().getOWLDataProperty("orch:hasName", prefixManager);
    hasValueProperty = getDataFactory().getOWLDataProperty("orch:hasValue", prefixManager);
    hasIdProperty = dataFactory.getOWLDataProperty("orch:hasId", prefixManager);
    hasShortNameProperty = getDataFactory().getOWLDataProperty("orch:hasShortName", prefixManager);
  }

  void removeOntology(Model model) {
    MessageModel messageModel = (MessageModel) model;

    ontologyManager.removeOntology(messageModel.getDerivedModel());
    messageModel.getReasoner().dispose();

  }

  public void setDocumentation(MessageEntity messageEntity, String lang, String purpose,
      List<Object> content) {
    Objects.requireNonNull(messageEntity, "MessageEntity cannot be null");
    ObjectHolder objectHolder = (ObjectHolder) messageEntity;
    OWLAnnotationProperty commentProperty = getDataFactory().getRDFSComment();

    for (Object item : content) {
      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(item.toString(), lang);
      OWLAnnotation commentAnno = getDataFactory().getOWLAnnotation(commentProperty, dataLiteral);
      OWLAxiom axiom = getDataFactory()
          .getOWLAnnotationAssertionAxiom(objectHolder.getObject().getIRI(), commentAnno);
      ontologyManager.applyChange(new AddAxiom(objectHolder.getModel().getDerivedModel(), axiom));
    }
  }

  public void setMetadata(Model model, String namespace, String name, List<String> value) {
    Objects.requireNonNull(model, "Model cannot be null");
    MessageModel messageModel = (MessageModel) model;

    OWLLiteral dataLiteral;
    switch (name) {
      case "date":
        OWLDatatype dateTime = getDataFactory().getOWLDatatype(OWL2Datatype.XSD_DATE_TIME.getIRI());
        dataLiteral = getDataFactory().getOWLLiteral(value.get(0), dateTime);
        break;
      default:
        dataLiteral = getDataFactory().getOWLLiteral(value.get(0));
    }

    OWLAnnotation annotation = getDataFactory().getOWLAnnotation(getDataFactory()
        .getOWLAnnotationProperty("dcterms:" + name, messageModel.getPrefixManager()), dataLiteral);

    ontologyManager
        .applyChange(new AddOntologyAnnotation(messageModel.getDerivedModel(), annotation));
  }

  /**
   * Store the current ontology model to an output stream
   * 
   * @param model ontology to update
   * @param out output stream
   * @throws Exception if the model cannot be written
   */
  public void storeModel(Model model, OutputStream out) throws Exception {
    MessageModel messageModel = (MessageModel) model;
    writeAsTurtle(messageModel.getDerivedModel(), messageModel.getPrefixManager(), out);
  }

  /**
   * Save an ontology to an output stream as Turtle format
   * 
   * @param ontology a populated ontology
   * @param out output stream
   * @throws OWLOntologyStorageException If there was a problem saving this ontology to the
   *         specified output stream
   */
  void writeAsTurtle(OWLOntology ontology, PrefixManager prefixManager, OutputStream out)
      throws OWLOntologyStorageException {
    TurtleDocumentFormat ontologyFormat = new TurtleDocumentFormat();
    ontologyFormat.setPrefixManager(prefixManager);
    ontologyManager.saveOntology(ontology, ontologyFormat, out);
  }
}
