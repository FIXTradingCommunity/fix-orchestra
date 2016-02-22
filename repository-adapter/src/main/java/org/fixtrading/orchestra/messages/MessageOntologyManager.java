/**
 * Copyright 2015 FIX Protocol Ltd
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
package org.fixtrading.orchestra.messages;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import com.google.common.base.Optional;

/**
 * Populates message ontology 
 * @author Don Mendelson
 *
 */
public class MessageOntologyManager {

  class DataTypeObject implements MessageEntity, ObjectHolder {
    private final OWLNamedIndividual messageObject;

    DataTypeObject(OWLNamedIndividual messageObject) {
      this.messageObject = messageObject;
    }

    public String getName() {
      String name = null;
      OWLDataProperty hasNameProperty =
          getDataFactory().getOWLDataProperty(":hasName", prefixManager);
      Set<OWLLiteral> values = getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        name = first.getLiteral();
      }

      return name;
    }

    public OWLNamedIndividual getObject() {
      return messageObject;
    }

    public DataTypeObject withName(String name) {
      Objects.requireNonNull(name, "Name cannot be null");

      OWLDataProperty hasNameProperty =
          getDataFactory().getOWLDataProperty(":hasName", prefixManager);

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasNameProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);
      return this;
    }
  }

  class FieldObject extends MessageObject implements MessageEntity {

    /**
     * @param messageObject
     */
    FieldObject(OWLNamedIndividual messageObject) {
      super(messageObject);
    }


    /**
     * @param dataType
     */
    public FieldObject withDataType(String dataType) {
      OWLObjectProperty hasProperty =
          getDataFactory().getOWLObjectProperty(":hasDataType", getPrefixManager());

      OWLNamedIndividual datatypeInd =
          getDataFactory().getOWLNamedIndividual(createDatatypeIRI(dataType));
      OWLClassAssertionAxiom classAssertion =
          getDataFactory().getOWLClassAssertionAxiom(dataTypeClass, datatypeInd);
      getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

      OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
          .getOWLObjectPropertyAssertionAxiom(hasProperty, getObject(), datatypeInd);
      getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);

      return this;
    }
  }

  class MessageObject implements MessageEntity, ObjectHolder {
    private final OWLNamedIndividual messageObject;

    MessageObject(OWLNamedIndividual messageObject) {
      this.messageObject = messageObject;
    }

    public String getName() {
      String name = null;
      OWLDataProperty hasNameProperty =
          getDataFactory().getOWLDataProperty(":hasName", prefixManager);
      Set<OWLLiteral> values = getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
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
      OWLDataProperty hasShortNameProperty =
          getDataFactory().getOWLDataProperty(":hasShortName", prefixManager);
      Set<OWLLiteral> values =
          getReasoner().getDataPropertyValues(getObject(), hasShortNameProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        name = first.getLiteral();
      }

      return name;
    }

    public MessageObject withId(int id) {

      OWLDataProperty hasIdProperty = getDataFactory().getOWLDataProperty(":hasId", prefixManager);

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(id);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasIdProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);
      return this;
    }

    public MessageObject withName(String name) {
      Objects.requireNonNull(name, "Name cannot be null");

      OWLDataProperty hasNameProperty =
          getDataFactory().getOWLDataProperty(":hasName", prefixManager);

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasNameProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);
      return this;
    }

    public MessageObject withShortName(String name) {
      Objects.requireNonNull(name, "Name cannot be null");

      OWLDataProperty hasShortNameProperty =
          getDataFactory().getOWLDataProperty(":hasShortName", prefixManager);

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasShortNameProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);
      return this;
    }

    int getId() {
      int id = 0;
      OWLDataProperty hasIdProperty = getDataFactory().getOWLDataProperty(":hasId", prefixManager);
      Set<OWLLiteral> values = getReasoner().getDataPropertyValues(getObject(), hasIdProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        id = first.parseInteger();
      }

      return id;
    }

  }

  interface ObjectHolder {
    default String getName() {
      return getObject().getIRI().getRemainder().get();
    }

    OWLNamedIndividual getObject();
  }

  private IRI baseIRI;
  private OWLOntology baseModel;
  private OWLClass componentClass;
  private OWLDataFactory dataFactory;
  private OWLClass dataTypeClass;
  private IRI derivedIRI;
  private OWLOntology derivedModel;
  private OWLClass fieldClass;
  private OWLClass messageClass;
  private OWLOntologyManager ontologyManager;
  private String prefix;
  private PrefixManager prefixManager;
  private OWLReasoner reasoner;
  private OWLClass stateClass;
  private OWLClass repeatingGroupClass;

  /**
   * @param id
   * @param name
   * @return
   */
  public MessageObject createComponent(BigInteger id, String name) {
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    OWLNamedIndividual component = getDataFactory().getOWLNamedIndividual(createComponentIRI(name));
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(componentClass, component);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    MessageObject messageObject = new MessageObject(component);
    messageObject.withId(id.intValue()).withName(name);
    return messageObject;
  }

  /**
   * @param name
   * @return
   */
  public DataTypeObject createDataType(String name) {
    Objects.requireNonNull(name, "Name cannot be null");

    OWLNamedIndividual field = getDataFactory().getOWLNamedIndividual(createDatatypeIRI(name));
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(dataTypeClass, field);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    DataTypeObject dataTypeObject = new DataTypeObject(field);
    dataTypeObject.withName(name);
    return dataTypeObject;
  }

  /**
   * @param id
   * @param name
   * @param dataType
   * @return
   */
  public MessageEntity createField(BigInteger id, String name, String dataType) {
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(dataType, "Data type cannot be null");

    OWLNamedIndividual field = getDataFactory().getOWLNamedIndividual(createFieldIRI(name));
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(fieldClass, field);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    FieldObject messageObject = new FieldObject(field);
    messageObject.withDataType(dataType).withId(id.intValue()).withName(name);
    return messageObject;
  }

  public MessageObject createMessage(BigInteger id, String name, String shortName) {
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    OWLNamedIndividual message = getDataFactory().getOWLNamedIndividual(createMessageIRI(name));
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(messageClass, message);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    MessageObject messageObject = new MessageObject(message);
    messageObject.withId(id.intValue()).withName(name).withShortName(shortName);
    return messageObject;
  }


  /**
   * Create a new ontology model
   * 
   * @param prefix prefix for the identifier
   * @param uri identifier of the model
   * @throws Exception if an ontology cannot be created
   */
  public void createNewModel(String prefix, URI uri) throws Exception {
    removeOntology();
    this.derivedIRI = IRI.create(uri);
    this.derivedModel = ontologyManager.createOntology(derivedIRI);
    this.prefixManager.setPrefix(prefix, derivedIRI.toString());
    this.prefix = prefix;
    StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    this.reasoner = reasonerFactory.createReasoner(getDerivedModel());
  }

  /**
   * @param field
   * @param name
   * @param valueAsString
   */
  public void createState(MessageEntity field, String name, String valueAsString) {
    Objects.requireNonNull(field, "Field cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");
    Objects.requireNonNull(valueAsString, "Value cannot be null");

    FieldObject fieldObject = (FieldObject) field;
    String fieldName = fieldObject.getName();
    OWLNamedIndividual fieldInd = fieldObject.getObject();

    OWLNamedIndividual state =
        getDataFactory().getOWLNamedIndividual(createStateIRI(name, fieldName));
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(stateClass, state);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    OWLObjectProperty hasProperty =
        getDataFactory().getOWLObjectProperty(":hasState", getPrefixManager());

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, fieldInd, state);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);

    OWLDataProperty hasValueProperty =
        getDataFactory().getOWLDataProperty(":hasValue", getPrefixManager());

    OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(valueAsString);

    OWLDataPropertyAssertionAxiom dataPropertyAssertion =
        getDataFactory().getOWLDataPropertyAssertionAxiom(hasValueProperty, state, dataLiteral);
    getOntologyManager().addAxiom(derivedModel, dataPropertyAssertion);
  }

  /**
   * Returns a named Message
   * 
   * @param messageName name of a Message
   * @return a Message
   */
  public MessageEntity getMessage(String messageName) {
    OWLNamedIndividual message = getInstance(getPrefix() + ":" + messageName);
    return new MessageObject(message);
  }

  /**
   * Returns a collection of Message objects
   * 
   * @return collection of messages
   */
  public Set<MessageEntity> getMessages() {
    NodeSet<OWLNamedIndividual> instances = getReasoner().getInstances(messageClass, true);
    Set<OWLNamedIndividual> objects = instances.getFlattened();
    return objects.stream().map(o -> new MessageObject(o)).collect(Collectors.toSet());
  }

  /**
   * Initialize resources and base ontology
   * 
   * @throws Exception if any resource cannot be initialized
   */
  public void init() throws Exception {
    this.ontologyManager = OWLManager.createOWLOntologyManager();
    this.dataFactory = OWLManager.getOWLDataFactory();

    InputStream in = ClassLoader.class.getResourceAsStream("/fix-repository-messages" + ".rdf");
    this.baseModel = loadOntologyModel(in);
    Optional<IRI> optional = this.baseModel.getOntologyID().getOntologyIRI();
    if (optional.isPresent()) {
      this.baseIRI = optional.get();
      this.prefixManager = new DefaultPrefixManager(null, null, baseIRI.toString());
    } else {
      throw new RuntimeException("No ontoloty IRI found");
    }

    messageClass = getDataFactory().getOWLClass(":Message", getPrefixManager());
    fieldClass = getDataFactory().getOWLClass(":Field", getPrefixManager());
    dataTypeClass = getDataFactory().getOWLClass(":DataType", getPrefixManager());
    stateClass = getDataFactory().getOWLClass(":State", getPrefixManager());
    componentClass = getDataFactory().getOWLClass(":Component", getPrefixManager());
    repeatingGroupClass = getDataFactory().getOWLClass(":RepeatingGroup", getPrefixManager());

  }


  /**
   * Load an ontology model from an input stream
   * 
   * @param in stream
   * @throws Exception if an ontology cannot be read or parsed
   */
  public void loadModel(InputStream in) throws Exception {
    this.derivedModel = loadOntologyModel(in);
    Optional<IRI> optional = this.derivedModel.getOntologyID().getOntologyIRI();
    if (optional.isPresent()) {
      this.derivedIRI = optional.get();
      StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
      this.reasoner = reasonerFactory.createReasoner(getDerivedModel());
      this.reasoner.precomputeInferences();
    } else {
      throw new RuntimeException("No ontoloty IRI found");
    }
  }

  /**
   * Store the current ontology model to an output stream
   * 
   * @param out stream
   * @throws Exception if the model cannot be written
   */
  public void storeModel(OutputStream out) throws Exception {
    write(derivedModel, out);
  }


  /**
   * @return the dataFactory
   */
  protected OWLDataFactory getDataFactory() {
    return dataFactory;
  }

  /**
   * @return the derivedIRI
   */
  protected IRI getDerivedIRI() {
    return derivedIRI;
  }



  /**
   * @return the derivedModel
   */
  protected OWLOntology getDerivedModel() {
    return derivedModel;
  }

  protected OWLClass getMessageClass() {
    OWLClass sessionClass = getDataFactory().getOWLClass(":Message", getPrefixManager());
    return sessionClass;
  }

  /**
   * @return the ontologyManager
   */
  protected OWLOntologyManager getOntologyManager() {
    return ontologyManager;
  }

  /**
   * @return the prefixManager
   */
  protected PrefixManager getPrefixManager() {
    return prefixManager;
  }

  OWLNamedIndividual getInstance(String abbreviatedIRI) {
    return getDataFactory().getOWLNamedIndividual(abbreviatedIRI, prefixManager);
  }

  /**
   * @return the prefix
   */
  String getPrefix() {
    return prefix;
  }

  /**
   * @return the reasoner
   */
  OWLReasoner getReasoner() {
    return reasoner;
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
    removeOntology();
    return ontologyManager.loadOntologyFromOntologyDocument(in);
  }

  void removeOntology() {
    if (reasoner != null) {
      reasoner.dispose();
      reasoner = null;
    }
    if (derivedModel != null) {
      ontologyManager.removeOntology(derivedModel);
      derivedModel = null;
      derivedIRI = null;
    }
  }

  /**
   * Save an ontology to an output stream as OWL XML format
   * 
   * @param ontology a populated ontology
   * @param out output stream
   * @throws OWLOntologyStorageException If there was a problem saving this ontology to the
   *         specified output stream
   */
  void write(OWLOntology ontology, OutputStream out) throws OWLOntologyStorageException {
    ontologyManager.saveOntology(ontology, new RDFXMLDocumentFormat(), out);
  }

  private IRI createComponentIRI(String name) {
    return IRI.create(getDerivedIRI().toString(), "component/" + name);
  }

  private IRI createDatatypeIRI(String name) {
    return IRI.create(getDerivedIRI().toString(), "datatype/" + name);
  }

  private IRI createFieldIRI(String name) {
    return IRI.create(getDerivedIRI().toString(), "field/" + name);
  }

  private IRI createMessageIRI(String name) {
    return IRI.create(getDerivedIRI().toString(), "message/" + name);
  }

  private IRI createStateIRI(String name, String fieldName) {
    return IRI.create(getDerivedIRI().toString(), "state/" + fieldName + "/" + name);
  }

  /**
   * @param id
   * @param name
   * @return
   */
  public MessageEntity createRepeatingGroup(BigInteger id, String name) {
    Objects.requireNonNull(id, "ID cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    OWLNamedIndividual component = getDataFactory().getOWLNamedIndividual(createComponentIRI(name));
    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(repeatingGroupClass, component);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    MessageObject messageObject = new MessageObject(component);
    messageObject.withId(id.intValue()).withName(name);
    return messageObject;
  }

  /**
   * @param entityType 
   * @param parent
   * @param entityId
   * @param entityName
   * @param b
   */
  public void addField(MessageEntity component, BigInteger entityId, String name, boolean isRequired) {
    Objects.requireNonNull(component, "Component cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageObject messageObject = (MessageObject) component;
    String componentName = messageObject.getName();
    OWLNamedIndividual componentInd = messageObject.getObject();

    OWLObjectProperty hasProperty = null;
    if (isRequired) {
      hasProperty = getDataFactory().getOWLObjectProperty(":requires", getPrefixManager());    
    } else {
      hasProperty = getDataFactory().getOWLObjectProperty(":has", getPrefixManager());
    }
    
    IRI iri  = createFieldIRI(name);
    
    OWLNamedIndividual entity =
        getDataFactory().getOWLNamedIndividual(iri);

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, componentInd, entity);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);
  }

  public void addNumInGroupField(MessageEntity component, BigInteger entityId, String name, boolean isRequired) {
    Objects.requireNonNull(component, "Component cannot be null");

    MessageObject messageObject = (MessageObject) component;
    String componentName = messageObject.getName();
    OWLNamedIndividual componentInd = messageObject.getObject();

    OWLObjectProperty hasSizeProperty = getDataFactory().getOWLObjectProperty(":hasSize", getPrefixManager());    

    OWLDataProperty hasIdProperty =
        dataFactory.getOWLDataProperty(":hasId", prefixManager);
    OWLDataProperty hasNameProperty =
        dataFactory.getOWLDataProperty(":hasName", prefixManager);

    String fieldName = null;
    Set<OWLNamedIndividual> fields = getReasoner().getInstances(fieldClass, true).getFlattened();
    for (OWLNamedIndividual fieldInd : fields) {     
      Set<OWLLiteral> values = getReasoner().getDataPropertyValues(fieldInd, hasIdProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null && first.parseInteger() == entityId.intValue()) {
        Set<OWLLiteral> names = getReasoner().getDataPropertyValues(fieldInd, hasNameProperty);
        fieldName = names.iterator().next().getLiteral();
        break;
      }
    }

    IRI iri = createFieldIRI(fieldName);
    
    OWLNamedIndividual entity =
        getDataFactory().getOWLNamedIndividual(iri);

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasSizeProperty, componentInd, entity);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);
  }
  
  public void addComponent(MessageEntity component, BigInteger entityId, String name, boolean isRequired) {
    Objects.requireNonNull(component, "Component cannot be null");
    Objects.requireNonNull(name, "Name cannot be null");

    MessageObject messageObject = (MessageObject) component;
    String componentName = messageObject.getName();
    OWLNamedIndividual componentInd = messageObject.getObject();

    OWLObjectProperty hasProperty = null;
    if (isRequired) {
      hasProperty = getDataFactory().getOWLObjectProperty(":requires", getPrefixManager());    
    } else {
      hasProperty = getDataFactory().getOWLObjectProperty(":has", getPrefixManager());
    }
    
    IRI iri = createComponentIRI(name);
    
    OWLNamedIndividual entity =
        getDataFactory().getOWLNamedIndividual(iri);

    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, componentInd, entity);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);
  }

}


