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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;

/**
 * Tool to edit session configurations for FIXT sessions; an aspect of FIX Orchestra
 * 
 * @author Don Mendelson
 *
 */
public class FixSessionTool extends AbstractSessionTool {


  class FixSessionObject extends AbstractSessionTool.SessionObject {

    FixSessionObject(OWLNamedIndividual sessionObject) {
      super(sessionObject);
    }
    
    FixVersion getFixVersion() {
      FixVersion version = null;
      OWLNamedIndividual sessionInd = getObject();

      Set<OWLNamedIndividual> objects = getReasoner().getObjectPropertyValues(
          sessionInd,
          hasProperty).getFlattened();
      
      for (OWLNamedIndividual sessionChild : objects) {
        Set<OWLClass> classes = getReasoner().getTypes(sessionChild, true).getFlattened();
        if (classes.contains(fix42Class)) {
          version = FixVersion.FIX4_2;
          break;
        } else if (classes.contains(fix44Class)) {
          version = FixVersion.FIX4_4;
          break;
        } else if (classes.contains(fix50Class)) {
          version = FixVersion.FIX5_0_SP2;
          break;
        }
      }
      
      return version;  
    }
    
    FixtSessionRole getSessionRole() {
      FixtSessionRole role = null;
      OWLNamedIndividual sessionInd = getObject();

      Set<OWLNamedIndividual> objects = getReasoner().getObjectPropertyValues(
          sessionInd,
          hasProperty).getFlattened();
      
      for (OWLNamedIndividual sessionChild : objects) {
        Set<OWLClass> classes = getReasoner().getTypes(sessionChild, true).getFlattened();
        if (classes.contains(initiatorClass)) {
          role = FixtSessionRole.INITIATOR;
          break;
        } else if (classes.contains(acceptorClass)) {
          role = FixtSessionRole.ACCEPTOR;
          break;
        } 
      }
      
      return role;  
    }
 
    String getSenderCompId() {
      String senderCompId = null;
      OWLNamedIndividual sessionInd = getObject();

      Set<OWLNamedIndividual> objects =
          getReasoner().getObjectPropertyValues(sessionInd, hasProperty).getFlattened();

      for (OWLNamedIndividual sessionChild : objects) {
        Set<OWLClass> classes = getReasoner().getTypes(sessionChild, true).getFlattened();
        if (classes.contains(sessionIdClass)) {
          OWLDataProperty hasSenderCompIdProperty =
              getDataFactory().getOWLDataProperty(":hasSenderCompId", getDefaultPrefixManager());
          Set<OWLLiteral> values =
              getReasoner().getDataPropertyValues(sessionChild, hasSenderCompIdProperty);
          final OWLLiteral first = values.iterator().next();
          if (first != null) {
            senderCompId = first.getLiteral();
          }
          break;
        }
      }
      return senderCompId;
    }

    String getTargetCompId() {
      String targetCompId = null;
      OWLNamedIndividual sessionInd = getObject();

      Set<OWLNamedIndividual> objects =
          getReasoner().getObjectPropertyValues(sessionInd, hasProperty).getFlattened();

      for (OWLNamedIndividual sessionChild : objects) {
        Set<OWLClass> classes = getReasoner().getTypes(sessionChild, true).getFlattened();
        if (classes.contains(sessionIdClass)) {
          OWLDataProperty hastargetCompIdProperty =
              getDataFactory().getOWLDataProperty(":hasTargetCompId", getDefaultPrefixManager());
          Set<OWLLiteral> values =
              getReasoner().getDataPropertyValues(sessionChild, hastargetCompIdProperty);
          final OWLLiteral first = values.iterator().next();
          if (first != null) {
            targetCompId = first.getLiteral();
          }
          break;
        }
      }
      return targetCompId;
    }
  }

  /**
   * @param args command line arguments
   */
  public static void main(String[] args) {
    FixSessionTool tool = new FixSessionTool();
    try {
      tool.init();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private OWLClass fix42Class;
  private OWLClass fix44Class;
  private OWLClass fix50Class;
  private OWLClass sessionIdClass;
  private OWLClass initiatorClass;
  private OWLClass acceptorClass;
private OWLObjectProperty hasProperty;

  /**
   * Create a new FIXT session configuration
   * 
   * @param version FIX version
   * @param sessionName name of the new session
   * @param senderCompId sender firm - must be non-null
   * @param senderSubId sender subfirm unit - may be null
   * @param senderLocation sender location - may be null
   * @param targetCompId target firm - must be non-null
   * @param targetSubId target subfirm unit - may be null
   * @param targetLocation target location - may be null
   * @return a new session object
   */
  public SessionObject createFixtSession(FixVersion version, FixtSessionRole role,
      String sessionName,
      String senderCompId, String senderSubId, String senderLocation, String targetCompId,
      String targetSubId, String targetLocation) {
    Objects.requireNonNull(senderCompId, "SenderCompID cannot be null");
    Objects.requireNonNull(targetCompId, "TargetCompID cannot be null");

    OWLClass sessionClass = getSessionClass();

    OWLDataProperty hasSenderCompIdProperty =
        getDataFactory().getOWLDataProperty(":hasSenderCompId", getDefaultPrefixManager());
    OWLDataProperty hasSenderSubIdProperty =
        getDataFactory().getOWLDataProperty(":hasSenderSubId", getDefaultPrefixManager());
    OWLDataProperty hasSenderLocationProperty =
        getDataFactory().getOWLDataProperty(":hasSenderLocation", getDefaultPrefixManager());
    OWLDataProperty hasTargetCompIdProperty =
        getDataFactory().getOWLDataProperty(":hasTargetCompId", getDefaultPrefixManager());
    OWLDataProperty hasTargetSubIdProperty =
        getDataFactory().getOWLDataProperty(":hasTargetSubId", getDefaultPrefixManager());
    OWLDataProperty hasTargetLocationProperty =
        getDataFactory().getOWLDataProperty(":hasTargetLocation", getDefaultPrefixManager());

    OWLNamedIndividual session =
  		getDataFactory().getOWLNamedIndividual("sessions/" + sessionName, getPrefixManager());

    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(sessionClass, session);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    OWLNamedIndividual sessionId = getDataFactory()
		.getOWLNamedIndividual("sessionIds/" + sessionName, getPrefixManager());

    classAssertion = getDataFactory().getOWLClassAssertionAxiom(sessionIdClass, sessionId);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);
    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, session, sessionId);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);

    OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
        .getOWLDataPropertyAssertionAxiom(hasSenderCompIdProperty, sessionId, senderCompId);
    getOntologyManager().addAxiom(getDerivedModel(), dataPropertyAssertion);

    if (senderSubId != null) {
      dataPropertyAssertion = getDataFactory().getOWLDataPropertyAssertionAxiom(hasSenderSubIdProperty,
          sessionId, senderSubId);
      getOntologyManager().addAxiom(getDerivedModel(), dataPropertyAssertion);
    }

    if (senderLocation != null) {
      dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasSenderLocationProperty, sessionId, senderLocation);
      getOntologyManager().addAxiom(getDerivedModel(), dataPropertyAssertion);
    }

    dataPropertyAssertion = getDataFactory().getOWLDataPropertyAssertionAxiom(hasTargetCompIdProperty,
        sessionId, targetCompId);
    getOntologyManager().addAxiom(getDerivedModel(), dataPropertyAssertion);

    if (targetSubId != null) {
      dataPropertyAssertion = getDataFactory().getOWLDataPropertyAssertionAxiom(hasTargetSubIdProperty,
          sessionId, targetSubId);
      getOntologyManager().addAxiom(getDerivedModel(), dataPropertyAssertion);
    }

    if (targetLocation != null) {
      dataPropertyAssertion = getDataFactory()
          .getOWLDataPropertyAssertionAxiom(hasTargetLocationProperty, sessionId, targetLocation);
      getOntologyManager().addAxiom(getDerivedModel(), dataPropertyAssertion);
    }

    OWLClass encodingClass = getDataFactory().getOWLClass(":TagValue", getDefaultPrefixManager());
    OWLNamedIndividual encoding = getDataFactory()
 		.getOWLNamedIndividual("encodings/" + sessionName, getPrefixManager());

    classAssertion = getDataFactory().getOWLClassAssertionAxiom(encodingClass, encoding);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);
    propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, session, encoding);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);

    OWLClass fixClass = null;
    switch (version) {
      case FIX4_2:
        fixClass = fix42Class;
        break;
      case FIX4_4:
        fixClass = fix44Class;
        break;
      case FIX5_0_SP2:
        fixClass = fix50Class;
        break;
    }
    OWLNamedIndividual fixVersion = getDataFactory()
		.getOWLNamedIndividual("fixVersions/" + sessionName, getPrefixManager());

    classAssertion = getDataFactory().getOWLClassAssertionAxiom(fixClass, fixVersion);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);
    propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, session, fixVersion);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);

    OWLClass connectorClass = null;
    switch (role) {
      case INITIATOR:
        connectorClass = initiatorClass;
        break;
      case ACCEPTOR:
        connectorClass = acceptorClass;
        break;
    }
    OWLNamedIndividual roleInd = getDataFactory()
		.getOWLNamedIndividual("roles/" + sessionName, getPrefixManager());

    classAssertion = getDataFactory().getOWLClassAssertionAxiom(connectorClass, roleInd);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);
    propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, session, roleInd);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);

    return new FixSessionObject(session);
  }

  public Session getSession(String sessionName) {
    OWLNamedIndividual session = getDataFactory().getOWLNamedIndividual("sessions/" + sessionName, getPrefixManager());
    return new FixSessionObject(session);
  }

  public Set<Session> getSessions() {
    OWLClass sessionClass = getSessionClass();
    NodeSet<OWLNamedIndividual> instances = getReasoner().getInstances(sessionClass, true);
    Set<OWLNamedIndividual> objects = instances.getFlattened();
    return objects.stream().map(FixSessionObject::new).collect(Collectors.toSet());
  }

  protected OWLClass getSessionClass() {
    return getDataFactory().getOWLClass(":FixtSession", getDefaultPrefixManager());
  }

  public void init() throws Exception {
    super.init();
    fix42Class = getDataFactory().getOWLClass(":FIX4.2", getDefaultPrefixManager());
    fix44Class = getDataFactory().getOWLClass(":FIX4.4", getDefaultPrefixManager());
    fix50Class = getDataFactory().getOWLClass(":FIX5.0_SP2", getDefaultPrefixManager());
    initiatorClass = getDataFactory().getOWLClass(":INITIATOR", getDefaultPrefixManager());
    acceptorClass = getDataFactory().getOWLClass(":ACCEPTOR", getDefaultPrefixManager());
    sessionIdClass = getDataFactory().getOWLClass(":FixtSessionIdentifier", getDefaultPrefixManager());

    hasProperty = getDataFactory().getOWLObjectProperty(":has", getDefaultPrefixManager());

  }
}
