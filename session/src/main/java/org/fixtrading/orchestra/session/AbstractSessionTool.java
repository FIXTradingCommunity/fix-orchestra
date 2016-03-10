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
package org.fixtrading.orchestra.session;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

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
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import com.google.common.base.Optional;

abstract class AbstractSessionTool {
  interface ObjectHolder {
    default String getName() {
      return getObject().getIRI().getRemainder().get();
    }

    OWLNamedIndividual getObject();
  }
  class SessionObject implements Session, ObjectHolder {
    private final OWLNamedIndividual sessionObject;

    SessionObject(OWLNamedIndividual sessionObject) {
      this.sessionObject = sessionObject;
    }

    public OWLNamedIndividual getObject() {
      return sessionObject;
    }

    public ZonedDateTime getAcivationTime() {
      ZonedDateTime activationTime = null;
      
      OWLDataProperty hasActivateTimeProperty =
          dataFactory.getOWLDataProperty(":hasActivation", getDefaultPrefixManager());
      Set<OWLLiteral> values =
          getReasoner().getDataPropertyValues(getObject(), hasActivateTimeProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        Calendar cal = DatatypeConverter.parseDateTime(first.getLiteral());
        activationTime = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.systemDefault());
      }

      return activationTime;
    }
    
    public ZonedDateTime getDeactivationTime() {
      ZonedDateTime deactivationTime = null;
      
      OWLDataProperty hasDeactivateTimeProperty =
          dataFactory.getOWLDataProperty(":hasDeactivation", getDefaultPrefixManager());
      Set<OWLLiteral> values =
          getReasoner().getDataPropertyValues(getObject(), hasDeactivateTimeProperty);
      final OWLLiteral first = values.iterator().next();
      if (first != null) {
        Calendar cal = DatatypeConverter.parseDateTime(first.getLiteral());
        deactivationTime = ZonedDateTime.ofInstant(cal.toInstant(), ZoneId.systemDefault());
      }

      return deactivationTime;
    }
    
    public SessionObject withActivationTime(ZonedDateTime zonedDateTime)
        throws DatatypeConfigurationException {
      Objects.requireNonNull(zonedDateTime, "Time cannot be null");

      GregorianCalendar cal = GregorianCalendar.from(zonedDateTime);
      XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
      String dateTime = xmlCal.toXMLFormat();

      OWLDataProperty hasActivateTimeProperty =
          dataFactory.getOWLDataProperty(":hasActivation", getDefaultPrefixManager());

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(dateTime, OWL2Datatype.XSD_DATE_TIME);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = dataFactory
          .getOWLDataPropertyAssertionAxiom(hasActivateTimeProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);
      return this;
    }

    public SessionObject withDeactivationTime(ZonedDateTime zonedDateTime)
        throws DatatypeConfigurationException {
      Objects.requireNonNull(zonedDateTime, "Time cannot be null");

      GregorianCalendar cal = GregorianCalendar.from(zonedDateTime);
      XMLGregorianCalendar xmlCal = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
      String dateTime = xmlCal.toXMLFormat();

      OWLDataProperty hasDeactivateTimeProperty =
          dataFactory.getOWLDataProperty(":hasDeactivation", getDefaultPrefixManager());

      OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(dateTime, OWL2Datatype.XSD_DATE_TIME);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion = dataFactory
          .getOWLDataPropertyAssertionAxiom(hasDeactivateTimeProperty, getObject(), dataLiteral);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);
      return this;
    }

    /**
     * Add a TCP transport configuration for a session
     * 
     * @param ipAddress IP address
     * @param port port number
     * @return this session object
     */
    public SessionObject withTcpTransport(String ipAddress, int port) {
      Objects.requireNonNull(ipAddress, "Address cannot be null");

      OWLClass tcpTransportClass = dataFactory.getOWLClass(":TcpTransport", getDefaultPrefixManager());
      OWLObjectProperty hasProperty = dataFactory.getOWLObjectProperty(":has", getDefaultPrefixManager());

      OWLDataProperty hasAddressProperty =
          dataFactory.getOWLDataProperty(":hasAddress", getDefaultPrefixManager());
      OWLDataProperty hasPortProperty = dataFactory.getOWLDataProperty(":hasPort", getDefaultPrefixManager());

      String sessionName = getName();

      OWLNamedIndividual transport = dataFactory
          .getOWLNamedIndividual("transports/" + sessionName, getPrefixManager());

      OWLClassAssertionAxiom classAssertion =
          dataFactory.getOWLClassAssertionAxiom(tcpTransportClass, transport);
      ontologyManager.addAxiom(derivedModel, classAssertion);
      OWLObjectPropertyAssertionAxiom propertyAssertion =
          dataFactory.getOWLObjectPropertyAssertionAxiom(hasProperty, getObject(), transport);
      ontologyManager.addAxiom(derivedModel, propertyAssertion);

      OWLDataPropertyAssertionAxiom dataPropertyAssertion =
          dataFactory.getOWLDataPropertyAssertionAxiom(hasAddressProperty, transport, ipAddress);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);

      dataPropertyAssertion =
          dataFactory.getOWLDataPropertyAssertionAxiom(hasPortProperty, transport, port);
      ontologyManager.addAxiom(derivedModel, dataPropertyAssertion);

      return this;
    }

    String getIpAddress() {
      String ipAddresss = null;
      OWLNamedIndividual sessionInd = getObject();
      OWLClass tcpTransportClass = dataFactory.getOWLClass(":TcpTransport", getDefaultPrefixManager());

      OWLObjectProperty hasProperty =
          getDataFactory().getOWLObjectProperty(":has", getDefaultPrefixManager());

      Set<OWLNamedIndividual> objects =
          getReasoner().getObjectPropertyValues(sessionInd, hasProperty).getFlattened();

      for (OWLNamedIndividual sessionChild : objects) {
        Set<OWLClass> classes = getReasoner().getTypes(sessionChild, true).getFlattened();
        if (classes.contains(tcpTransportClass)) {
          OWLDataProperty hasAddressProperty =
              dataFactory.getOWLDataProperty(":hasAddress", getDefaultPrefixManager());
          Set<OWLLiteral> values =
              getReasoner().getDataPropertyValues(sessionChild, hasAddressProperty);
          final OWLLiteral first = values.iterator().next();
          if (first != null) {
            ipAddresss = first.getLiteral();
          }
          break;
        }
      }
      return ipAddresss;
    }

    int getPort() {
      int port = 0;
      OWLNamedIndividual sessionInd = getObject();
      OWLClass tcpTransportClass = dataFactory.getOWLClass(":TcpTransport", getDefaultPrefixManager());

      OWLObjectProperty hasProperty =
          getDataFactory().getOWLObjectProperty(":has", getDefaultPrefixManager());

      Set<OWLNamedIndividual> objects =
          getReasoner().getObjectPropertyValues(sessionInd, hasProperty).getFlattened();

      for (OWLNamedIndividual sessionChild : objects) {
        Set<OWLClass> classes = getReasoner().getTypes(sessionChild, true).getFlattened();
        if (classes.contains(tcpTransportClass)) {
          OWLDataProperty hasPortProperty =
              dataFactory.getOWLDataProperty(":hasPort", getDefaultPrefixManager());
          Set<OWLLiteral> values =
              getReasoner().getDataPropertyValues(sessionChild, hasPortProperty);
          final OWLLiteral first = values.iterator().next();
          if (first != null) {
            port = first.parseInteger();
          }
          break;
        }
      }
      return port;
    }
  }

  private OWLDataFactory dataFactory;
  private IRI derivedIRI;
  private OWLOntology derivedModel;
  private OWLOntologyManager ontologyManager;
  private PrefixManager defaultPrefixManager;
  private PrefixManager prefixManager;
  private OWLReasoner reasoner;

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
    this.prefixManager = new DefaultPrefixManager(null, null, derivedIRI.toString());
    StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    this.reasoner = reasonerFactory.createReasoner(getDerivedModel());
  }


  /**
   * Returns a named session
   * 
   * @param sessionName name of a session
   * @return a session
   */
  public abstract Session getSession(String sessionName);


  /**
   * Returns a Set of session objects
   * 
   * @return set of sessions
   */
  public abstract Set<Session> getSessions();

  /**
   * Initialize resources and base ontology
   * 
   * @throws Exception if any resource cannot be initialized
   */
  public void init() throws Exception {
    this.ontologyManager = OWLManager.createOWLOntologyManager();
    this.dataFactory = OWLManager.getOWLDataFactory();

    InputStream in = ClassLoader.class.getResourceAsStream("/fix-orch-session.rdf");
    OWLOntology baseModel = loadOntologyModel(in);
    Optional<IRI> optional = baseModel.getOntologyID().getOntologyIRI();
    if (optional.isPresent()) {
      IRI baseIRI = optional.get();
      this.defaultPrefixManager = new DefaultPrefixManager(null, null, baseIRI.toString());
    } else {
      throw new RuntimeException("No ontoloty IRI found");
    }
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

  /**
   * @return the ontologyManager
   */
  protected OWLOntologyManager getOntologyManager() {
    return ontologyManager;
  }

  /**
   * @return the prefixManager
   */
  protected PrefixManager getDefaultPrefixManager() {
    return defaultPrefixManager;
  }

  protected PrefixManager getPrefixManager() {
	    return prefixManager;
  }

  protected abstract OWLClass getSessionClass();

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
  private OWLOntology loadOntologyModel(InputStream in) throws OWLOntologyCreationException {
    removeOntology();
    return ontologyManager.loadOntologyFromOntologyDocument(in);
  }

  private void removeOntology() {
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
  private void write(OWLOntology ontology, OutputStream out) throws OWLOntologyStorageException {
    ontologyManager.saveOntology(ontology, new RDFXMLDocumentFormat(), out);
  }

}


