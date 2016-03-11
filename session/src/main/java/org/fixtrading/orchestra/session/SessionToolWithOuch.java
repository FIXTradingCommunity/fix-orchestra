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

import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

/**
 * Demonstration of an extension to FIX Orchestra to edit non-FIX session configurations
 * 
 * @author Don Mendelson
 *
 */
public class SessionToolWithOuch extends AbstractSessionTool {

  class OuchSessionObject extends AbstractSessionTool.SessionObject {

    /**
     * @param sessionObject an OWL individual for this session
     */
    OuchSessionObject(OWLNamedIndividual sessionObject) {
      super(sessionObject);
    }
  }

  public Session getSession(String sessionName) {
    OWLNamedIndividual session = getDataFactory().getOWLNamedIndividual("sessions/" + sessionName, getPrefixManager());
    return new OuchSessionObject(session);
  }

  public Set<Session> getSessions() {
    StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    OWLReasoner reasoner = reasonerFactory.createReasoner(getDerivedModel());
    OWLClass sessionClass = getSessionClass();
    NodeSet<OWLNamedIndividual> instances = reasoner.getInstances(sessionClass, true);
    Set<OWLNamedIndividual> objects = instances.getFlattened();
    return objects.stream().map(OuchSessionObject::new).collect(Collectors.toSet());
  }

  protected OWLClass getSessionClass() {
    OWLClass subClass = getDataFactory().getOWLClass(":OuchSession", getDefaultPrefixManager());
    OWLClass superClass = getDataFactory().getOWLClass(":Session", getDefaultPrefixManager());
    OWLSubClassOfAxiom axiom = getDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
    getOntologyManager().addAxiom(getDerivedModel(), axiom);
    return subClass;
  }

  protected OWLClass getSessionProtocolClass() {
    OWLClass subClass = getDataFactory().getOWLClass(":SoupBinTcp", getDefaultPrefixManager());
    OWLClass superClass = getDataFactory().getOWLClass(":SessionProtocol", getDefaultPrefixManager());
    OWLSubClassOfAxiom axiom = getDataFactory().getOWLSubClassOfAxiom(subClass, superClass);
    getOntologyManager().addAxiom(getDerivedModel(), axiom);
    return subClass;
  }

  /**
   * Configure an OUCH session
   * 
   * @param sessionName a 10 character session identifier
   * @return a new session object
   */
  SessionObject createOuchSession(String sessionName) {
    OWLClass sessionClass = getSessionClass();

    OWLObjectProperty hasProperty =
        getDataFactory().getOWLObjectProperty(":has", getDefaultPrefixManager());

    OWLDataProperty hasTextIdentifierProperty =
        getDataFactory().getOWLDataProperty(":hasTextIdentifer", getDefaultPrefixManager());

    OWLNamedIndividual session =
      	getDataFactory().getOWLNamedIndividual("sessions/" + sessionName, getPrefixManager());

    OWLClassAssertionAxiom classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(sessionClass, session);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);

    OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
        .getOWLDataPropertyAssertionAxiom(hasTextIdentifierProperty, session, sessionName);
    getOntologyManager().addAxiom(getDerivedModel(), dataPropertyAssertion);

    OWLClass sessionProtocolClass = getSessionProtocolClass();

    OWLNamedIndividual sessionProtocol = getDataFactory()
    		.getOWLNamedIndividual("sessionProtocols/" + sessionName, getPrefixManager());

    classAssertion =
        getDataFactory().getOWLClassAssertionAxiom(sessionProtocolClass, sessionProtocol);
    getOntologyManager().addAxiom(getDerivedModel(), classAssertion);
    OWLObjectPropertyAssertionAxiom propertyAssertion =
        getDataFactory().getOWLObjectPropertyAssertionAxiom(hasProperty, session, sessionProtocol);
    getOntologyManager().addAxiom(getDerivedModel(), propertyAssertion);

    return new OuchSessionObject(session);
  }

}
