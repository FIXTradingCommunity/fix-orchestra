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
package org.fixtrading.orchestra.messages;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
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
 * Populates a message ontology
 * 
 * @author Don Mendelson
 *
 */
public class MessageOntologyManager {

	class CodeObject implements ObjectHolder {

		private final OWLNamedIndividual codeInd;
		private final MessageModel model;

		CodeObject(MessageModel messageModel, OWLNamedIndividual codeInd) {
			this.model = messageModel;
			this.codeInd = codeInd;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
			return result;
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

		public String getName() {
			String name = null;
			OWLDataProperty hasNameProperty = getDataFactory().getOWLDataProperty(":hasName",
					getDefaultPrefixManager());
			Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
			final OWLLiteral first = values.iterator().next();
			if (first != null) {
				name = first.getLiteral();
			}

			return name;
		}

		public OWLNamedIndividual getObject() {
			return codeInd;
		}

		public String getValue() {
			String value = null;
			OWLDataProperty hasValueProperty = getDataFactory().getOWLDataProperty(":hasValue",
					getDefaultPrefixManager());
			Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(getObject(), hasValueProperty);
			final OWLLiteral first = values.iterator().next();
			if (first != null) {
				value = first.getLiteral();
			}

			return value;
		}

		public CodeObject withName(String name) {
			Objects.requireNonNull(name, "Name cannot be null");

			OWLDataProperty hasNameProperty = getDataFactory().getOWLDataProperty(":hasName",
					getDefaultPrefixManager());

			OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

			OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
					.getOWLDataPropertyAssertionAxiom(hasNameProperty, getObject(), dataLiteral);
			ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
			return this;
		}

		public CodeObject withValue(String value) {
			Objects.requireNonNull(value, "Value cannot be null");

			OWLDataProperty hasValueProperty = getDataFactory().getOWLDataProperty(":hasValue",
					getDefaultPrefixManager());

			OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(value);

			OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
					.getOWLDataPropertyAssertionAxiom(hasValueProperty, getObject(), dataLiteral);
			ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
			return this;
		}

		private MessageOntologyManager getOuterType() {
			return MessageOntologyManager.this;
		}

	}

	class CodeSetObject extends DataTypeObject {

		CodeSetObject(MessageModel messageModel, OWLNamedIndividual messageObject) {
			super(messageModel, messageObject);
		}

		public CodeSetObject getDataType() {
			CodeSetObject datatype = null;
			Set<OWLNamedIndividual> values = getModel().getReasoner()
					.getObjectPropertyValues(getObject(), hasDataTypeProperty).getFlattened();
			final OWLNamedIndividual first = values.iterator().next();
			if (first != null) {
				datatype = new CodeSetObject(getModel(), first);
			}

			return datatype;
		}

		public CodeSetObject withDataType(String name) {
			OWLNamedIndividual datatypeInd = getDataFactory().getOWLNamedIndividual("datatypes/" + name,
					getModel().getPrefixManager());

			OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
					.getOWLObjectPropertyAssertionAxiom(hasDataTypeProperty, getObject(), datatypeInd);
			getOntologyManager().addAxiom(getModel().getDerivedModel(), propertyAssertion);

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
			OWLDataProperty hasNameProperty = getDataFactory().getOWLDataProperty(":hasName",
					getDefaultPrefixManager());
			Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
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

			OWLDataProperty hasNameProperty = getDataFactory().getOWLDataProperty(":hasName",
					getDefaultPrefixManager());

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
					.getObjectPropertyValues(getObject(), hasDataTypeProperty).getFlattened();
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
		 * @param name
		 *            of data type
		 */
		public FieldObject withDataType(String name) {
			OWLNamedIndividual datatypeInd = getDataFactory().getOWLNamedIndividual("datatypes/" + name,
					getModel().getPrefixManager());
			OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(dataTypeClass,
					datatypeInd);
			getOntologyManager().addAxiom(getModel().getDerivedModel(), classAssertion);

			OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
					.getOWLObjectPropertyAssertionAxiom(hasDataTypeProperty, getObject(), datatypeInd);
			getOntologyManager().addAxiom(getModel().getDerivedModel(), propertyAssertion);

			return this;
		}
	}

	private class MessageModel implements Model {

		private final IRI derivedIRI;
		private final OWLOntology derivedModel;
		private final PrefixManager prefixManager;
		private final OWLReasoner reasoner;

		/**
		 * @param derivedIRI2
		 * @param createOntology
		 */
		public MessageModel(IRI derivedIRI, OWLOntology derivedModel, OWLReasoner reasoner) {
			this.derivedIRI = derivedIRI;
			this.derivedModel = derivedModel;
			this.reasoner = reasoner;
			this.prefixManager = new DefaultPrefixManager(null, null, derivedIRI.toString());
		}

		/**
		 * @return the derivedIRI
		 */
		public IRI getDerivedIRI() {
			return derivedIRI;
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
			OWLDataProperty hasIdProperty = getDataFactory().getOWLDataProperty(":hasId", getDefaultPrefixManager());
			Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(getObject(), hasIdProperty);
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
			OWLDataProperty hasNameProperty = getDataFactory().getOWLDataProperty(":hasName",
					getDefaultPrefixManager());
			Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(getObject(), hasNameProperty);
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
			OWLDataProperty hasShortNameProperty = getDataFactory().getOWLDataProperty(":hasShortName",
					getDefaultPrefixManager());
			Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(getObject(), hasShortNameProperty);
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

			OWLDataProperty hasIdProperty = getDataFactory().getOWLDataProperty(":hasId", getDefaultPrefixManager());

			OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(id);

			OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
					.getOWLDataPropertyAssertionAxiom(hasIdProperty, getObject(), dataLiteral);
			ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
			return this;
		}

		public MessageObject withName(String name) {
			Objects.requireNonNull(name, "Name cannot be null");

			OWLDataProperty hasNameProperty = getDataFactory().getOWLDataProperty(":hasName",
					getDefaultPrefixManager());

			OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

			OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
					.getOWLDataPropertyAssertionAxiom(hasNameProperty, getObject(), dataLiteral);
			ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
			return this;
		}

		public MessageObject withShortName(String name) {
			Objects.requireNonNull(name, "Name cannot be null");

			OWLDataProperty hasShortNameProperty = getDataFactory().getOWLDataProperty(":hasShortName",
					getDefaultPrefixManager());

			OWLLiteral dataLiteral = getDataFactory().getOWLLiteral(name);

			OWLDataPropertyAssertionAxiom dataPropertyAssertion = getDataFactory()
					.getOWLDataPropertyAssertionAxiom(hasShortNameProperty, getObject(), dataLiteral);
			ontologyManager.addAxiom(model.getDerivedModel(), dataPropertyAssertion);
			return this;
		}

	}

	interface ObjectHolder {
		default String getName() {
			return getObject().getIRI().getRemainder().get();
		}

		OWLNamedIndividual getObject();
	}

	private OWLClass codeLiteralClass;
	private static OWLClass codeSetClass;
	private OWLClass componentClass;
	private OWLDataFactory dataFactory;
	private OWLClass dataTypeClass;
	private OWLClass fieldClass;
	private OWLObjectProperty hasDataTypeProperty;
	private OWLObjectProperty hasProperty;
	private OWLObjectProperty hasSizeFieldProperty;
	private OWLObjectProperty isSizeOfProperty;
	private OWLObjectProperty memberProperty;
	private OWLClass messageClass;
	private OWLOntologyManager ontologyManager;
	private String prefix;
	private PrefixManager prefixManager;
	private OWLClass repeatingGroupClass;
	private OWLObjectProperty requiresProperty;

	public static boolean isCodeSet(MessageModel model, OWLNamedIndividual dataTypeObject) {
		return model.getReasoner().getTypes(dataTypeObject, true).containsEntity(codeSetClass);
	}
	
	/**
	 * Adds a FIX message component to its parent component
	 * 
	 * @param parent
	 *            component to which the new component will be added
	 * @param entityId
	 *            unique ID of the added component
	 * @param name
	 *            name of the added component
	 * @param isRequired
	 *            the component is required if {@code true}
	 */
	public void addComponent(MessageEntity parent, int entityId, String name, boolean isRequired) {
		Objects.requireNonNull(parent, "Parent component cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageObject parentObject = (MessageObject) parent;
		OWLNamedIndividual parentInd = parentObject.getObject();

		MessageModel model = parentObject.getModel();

		OWLNamedIndividual entity = getDataFactory().getOWLNamedIndividual("components/" + name,
				model.getPrefixManager());

		OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
				.getOWLObjectPropertyAssertionAxiom(isRequired ? requiresProperty : hasProperty, parentInd, entity);
		getOntologyManager().addAxiom(model.getDerivedModel(), propertyAssertion);
	}

	/**
	 * 
	 * Adds a FIX message field to its parent component
	 * 
	 * @param parent
	 *            component to which the new field will be added
	 * @param entityId
	 *            unique ID of the added field
	 * @param name
	 *            name of the added field
	 * @param isRequired
	 *            the field is required if {@code true}
	 */
	public void addField(MessageEntity parent, int entityId, String name, boolean isRequired) {
		Objects.requireNonNull(parent, "Parent component cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageObject parentObject = (MessageObject) parent;
		OWLNamedIndividual parentInd = parentObject.getObject();

		MessageModel model = parentObject.getModel();

		OWLNamedIndividual entity = getDataFactory().getOWLNamedIndividual("fields/" + name, model.getPrefixManager());

		OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
				.getOWLObjectPropertyAssertionAxiom(isRequired ? requiresProperty : hasProperty, parentInd, entity);
		getOntologyManager().addAxiom(model.getDerivedModel(), propertyAssertion);
	}

	public void addNumInGroupField(MessageEntity parent, int entityId, String name, boolean isRequired) {
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

		OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
				.getOWLObjectPropertyAssertionAxiom(hasDataTypeProperty, field, refFieldObj.getDataType().getObject());
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
	 * This is known as an 'enum' in FIX repository, but it is not truly an
	 * enumeration since codes have no inherent order or ordinal. An Alt, by
	 * contrast, has no order.
	 * 
	 * @param model
	 *            ontology to update
	 * @param codeSetName
	 *            name of the code set
	 * @param name
	 *            symbolic name of the new value
	 * @param valueAsString
	 *            the valid value in string format. It may need to be cast or
	 *            converted to the true data type of the field.
	 */
	public void createCode(Model model, String codeSetName, String name, String valueAsString) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(codeSetName, "Code set name cannot be null");
		Objects.requireNonNull(name, "Symbolic name cannot be null");
		Objects.requireNonNull(valueAsString, "Value cannot be null");

		MessageModel messageModel = (MessageModel) model;

		OWLNamedIndividual codeInd = getDataFactory().getOWLNamedIndividual("datatypes/" + codeSetName + "/" + name,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(codeLiteralClass, codeInd);
		final OWLOntology derivedModel = messageModel.getDerivedModel();
		getOntologyManager().addAxiom(derivedModel, classAssertion);

		OWLNamedIndividual codeSetInd = getDataFactory().getOWLNamedIndividual("datatypes/" + codeSetName,
				messageModel.getPrefixManager());

		OWLObjectPropertyAssertionAxiom propertyAssertion = getDataFactory()
				.getOWLObjectPropertyAssertionAxiom(memberProperty, codeSetInd, codeInd);
		getOntologyManager().addAxiom(derivedModel, propertyAssertion);

		OWLDataProperty hasNameProperty = getDataFactory().getOWLDataProperty(":hasName", getDefaultPrefixManager());
		OWLLiteral nameLiteral = getDataFactory().getOWLLiteral(name);

		OWLDataPropertyAssertionAxiom namePropertyAssertion = getDataFactory()
				.getOWLDataPropertyAssertionAxiom(hasNameProperty, codeInd, nameLiteral);
		getOntologyManager().addAxiom(derivedModel, namePropertyAssertion);

		OWLDataProperty hasValueProperty = getDataFactory().getOWLDataProperty(":hasValue", getDefaultPrefixManager());
		OWLLiteral valueLiteral = getDataFactory().getOWLLiteral(valueAsString);

		OWLDataPropertyAssertionAxiom valuePropertyAssertion = getDataFactory()
				.getOWLDataPropertyAssertionAxiom(hasValueProperty, codeInd, valueLiteral);
		getOntologyManager().addAxiom(derivedModel, valuePropertyAssertion);
	}

	public CodeSetObject createCodeSet(Model model, String codeSetName, String primitiveDatatypeName) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(codeSetName, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;
		OWLNamedIndividual datatype = getDataFactory().getOWLNamedIndividual("datatypes/" + codeSetName,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(codeSetClass, datatype);
		getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);

		CodeSetObject dataTypeObject = new CodeSetObject(messageModel, datatype);
		dataTypeObject.withDataType(primitiveDatatypeName).withName(codeSetName);
		return dataTypeObject;
	}

	/**
	 * Create a new component in the model
	 * 
	 * @param model
	 *            ontology to update
	 * @param id
	 *            unique identifier of the new component
	 * @param name
	 *            name of the new component
	 * @return a wrapper for the new component
	 */
	public MessageObject createComponent(Model model, int id, String name) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(id, "ID cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;

		OWLNamedIndividual component = getDataFactory().getOWLNamedIndividual("components/" + name,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(componentClass, component);
		getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);

		MessageObject messageObject = new MessageObject(messageModel, component);
		messageObject.withId(id).withName(name);
		return messageObject;
	}

	/**
	 * Create a new data type in the model
	 * 
	 * @param model
	 *            ontology to update
	 * @param name
	 *            name of the new data type
	 * @return a wrapper for the new data type
	 */
	public DataTypeObject createDataType(Model model, String name) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;
		OWLNamedIndividual datatype = getDataFactory().getOWLNamedIndividual("datatypes/" + name,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(dataTypeClass, datatype);
		getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);

		DataTypeObject dataTypeObject = new DataTypeObject(messageModel, datatype);
		dataTypeObject.withName(name);
		return dataTypeObject;
	}

	public FieldObject createField(Model model, int id, String name) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(id, "ID cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;

		OWLNamedIndividual field = getDataFactory().getOWLNamedIndividual("fields/" + name,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(fieldClass, field);
		getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);

		FieldObject messageObject = new FieldObject(messageModel, field);
		messageObject.withId(id).withName(name);
		return messageObject;
	}

	/**
	 * Create a new field in the model
	 * 
	 * @param model
	 *            ontology to update
	 * @param id
	 *            unique identifier of the new field
	 * @param name
	 *            name of the new field
	 * @param dataType
	 *            data type of the field
	 * @return a wrapper for the new field
	 */
	public FieldObject createField(Model model, int id, String name, String dataType) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(id, "ID cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");
		Objects.requireNonNull(dataType, "Data type cannot be null");

		MessageModel messageModel = (MessageModel) model;

		OWLNamedIndividual field = getDataFactory().getOWLNamedIndividual("fields/" + name,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(fieldClass, field);
		getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);

		FieldObject messageObject = new FieldObject(messageModel, field);
		messageObject.withDataType(dataType).withId(id).withName(name);
		return messageObject;
	}

	/**
	 * Create a new message in the model
	 * 
	 * @param model
	 *            ontology to update
	 * @param id
	 *            unique identifier of the new message
	 * @param name
	 *            name of the new field
	 * @param shortName
	 *            alternative name of the new field
	 * @return a wrapper for the new field
	 */
	public MessageObject createMessage(Model model, int id, String name, String shortName) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(id, "ID cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;

		OWLNamedIndividual message = getDataFactory().getOWLNamedIndividual("messages/" + name,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(messageClass, message);
		getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);

		MessageObject messageObject = new MessageObject(messageModel, message);
		messageObject.withId(id).withName(name).withShortName(shortName);
		return messageObject;
	}

	/**
	 * Create a new ontology model
	 * 
	 * @param prefix
	 *            prefix for the identifier
	 * @param uri
	 *            identifier of the model
	 * @throws Exception
	 *             if an ontology cannot be created
	 */
	public Model createNewModel(String prefix, URI uri) throws Exception {
		IRI derivedIRI = IRI.create(uri);
		this.prefixManager.setPrefix(prefix, derivedIRI.toString());
		this.prefix = prefix;
		StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
		final OWLOntology derivedModel = ontologyManager.createOntology(derivedIRI);
		final OWLReasoner reasoner = reasonerFactory.createReasoner(derivedModel);
		return new MessageModel(derivedIRI, derivedModel, reasoner);
	}

	/**
	 * Create a new repeating group component in the model
	 * 
	 * @param model
	 *            ontology to update
	 * @param id
	 *            unique identifier of the new component
	 * @param name
	 *            name of the new component
	 * @return a wrapper for the new component
	 */

	public MessageEntity createRepeatingGroup(Model model, int id, String name) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(id, "ID cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;

		OWLNamedIndividual component = getDataFactory().getOWLNamedIndividual("components/" + name,
				messageModel.getPrefixManager());
		OWLClassAssertionAxiom classAssertion = getDataFactory().getOWLClassAssertionAxiom(repeatingGroupClass,
				component);
		getOntologyManager().addAxiom(messageModel.getDerivedModel(), classAssertion);

		MessageObject messageObject = new MessageObject(messageModel, component);
		messageObject.withId(id).withName(name);
		return messageObject;
	}

	public Set<CodeObject> getCodes(Model model, String codeSetName) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(codeSetName, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;
		OWLNamedIndividual codeSetInd = getDataFactory().getOWLNamedIndividual("datatypes/" + codeSetName,
				messageModel.getPrefixManager());

		Set<OWLNamedIndividual> members = messageModel.getReasoner().getObjectPropertyValues(codeSetInd, memberProperty)
				.getFlattened();
		return members.stream().map(m -> new CodeObject(messageModel, m)).collect(Collectors.toSet());
	}

	/**
	 * @return the dataFactory
	 */
	protected OWLDataFactory getDataFactory() {
		return dataFactory;
	}

	/**
	 * @return the prefixManager
	 */
	protected PrefixManager getDefaultPrefixManager() {
		return prefixManager;
	}

	/**
	 * Returns a named field
	 * 
	 * @param name
	 *            name of a field
	 * @return a wrapped field object
	 */
	public FieldObject getField(Model model, String name) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;
		OWLNamedIndividual field = getDataFactory().getOWLNamedIndividual("fields/" + name,
				messageModel.getPrefixManager());
		return new FieldObject(messageModel, field);
	}

	private OWLNamedIndividual getFieldById(int entityId, MessageModel model) {
		OWLDataProperty hasIdProperty = dataFactory.getOWLDataProperty(":hasId", getDefaultPrefixManager());
		OWLDataProperty hasNameProperty = dataFactory.getOWLDataProperty(":hasName", getDefaultPrefixManager());

		String fieldName = null;
		Set<OWLNamedIndividual> fields = model.getReasoner().getInstances(fieldClass, true).getFlattened();
		for (OWLNamedIndividual fieldInd : fields) {
			Set<OWLLiteral> values = model.getReasoner().getDataPropertyValues(fieldInd, hasIdProperty);
			final OWLLiteral first = values.iterator().next();
			if (first != null && first.parseInteger() == entityId) {
				Set<OWLLiteral> names = model.getReasoner().getDataPropertyValues(fieldInd, hasNameProperty);
				fieldName = names.iterator().next().getLiteral();
				break;
			}
		}

		OWLNamedIndividual entity = getDataFactory().getOWLNamedIndividual("fields/" + fieldName,
				model.getPrefixManager());
		return entity;
	}

	OWLNamedIndividual getInstance(String abbreviatedIRI) {
		return getDataFactory().getOWLNamedIndividual(abbreviatedIRI, prefixManager);
	}

	/**
	 * Returns a named message
	 * 
	 * @param name
	 *            name of a Message
	 * @return a wrapped message object
	 */
	public MessageEntity getMessage(Model model, String name) {
		Objects.requireNonNull(model, "Model cannot be null");
		Objects.requireNonNull(name, "Name cannot be null");

		MessageModel messageModel = (MessageModel) model;
		OWLNamedIndividual message = getDataFactory().getOWLNamedIndividual("messages/" + name,
				messageModel.getPrefixManager());
		return new MessageObject(messageModel, message);
	}

	protected OWLClass getMessageClass() {
		return getDataFactory().getOWLClass(":Message", getDefaultPrefixManager());
	}

	/**
	 * Returns all message objects in the ontology
	 * 
	 * @return collection of messages
	 */
	public Set<MessageEntity> getMessages(Model model) {
		MessageModel messageModel = (MessageModel) model;

		NodeSet<OWLNamedIndividual> instances = messageModel.getReasoner().getInstances(messageClass, true);
		Set<OWLNamedIndividual> objects = instances.getFlattened();
		return objects.stream().map(o -> new MessageObject(messageModel, o)).collect(Collectors.toSet());
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

		Set<OWLNamedIndividual> hasSet = messageModel.getReasoner().getObjectPropertyValues(parentInd, hasProperty)
				.getFlattened();

		for (OWLNamedIndividual ind : hasSet) {
			Set<OWLClass> classes = messageModel.getReasoner().getTypes(ind, true).getFlattened();
			if (classes.contains(fieldClass)) {
				fields.add(new FieldObject(messageModel, ind));
			} else if (classes.contains(componentClass)) {
				// Recursively expand components
				getOptionalFields(new MessageObject(messageModel, ind), fields);
			}
		}
	}

	/**
	 * @return the prefix
	 */
	String getPrefix() {
		return prefix;
	}

	public void getRequiredFields(MessageEntity parent, Set<FieldObject> fields) {
		Objects.requireNonNull(parent, "Message entity cannot be null");

		MessageObject parentObject = (MessageObject) parent;
		OWLNamedIndividual parentInd = parentObject.getObject();
		MessageModel messageModel = parentObject.getModel();

		Set<OWLNamedIndividual> requiredSet = messageModel.getReasoner()
				.getObjectPropertyValues(parentInd, requiresProperty).getFlattened();

		for (OWLNamedIndividual ind : requiredSet) {
			Set<OWLClass> classes = messageModel.getReasoner().getTypes(ind, true).getFlattened();
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

		InputStream in = ClassLoader.class.getResourceAsStream("/fix-repository-messages" + ".rdf");
		OWLOntology baseModel = loadOntologyModel(in);
		Optional<IRI> optional = baseModel.getOntologyID().getOntologyIRI();
		if (optional.isPresent()) {
			IRI baseIRI = optional.get();
			this.prefixManager = new DefaultPrefixManager(null, null, baseIRI.toString());
		} else {
			throw new RuntimeException("No ontoloty IRI found");
		}

		messageClass = getDataFactory().getOWLClass(":Message", getDefaultPrefixManager());
		fieldClass = getDataFactory().getOWLClass(":Field", getDefaultPrefixManager());
		dataTypeClass = getDataFactory().getOWLClass(":DataType", getDefaultPrefixManager());
		codeSetClass = getDataFactory().getOWLClass(":CodeSet", getDefaultPrefixManager());
		codeLiteralClass = getDataFactory().getOWLClass(":CodeLiteral", getDefaultPrefixManager());
		componentClass = getDataFactory().getOWLClass(":Component", getDefaultPrefixManager());
		repeatingGroupClass = getDataFactory().getOWLClass(":RepeatingGroup", getDefaultPrefixManager());

		hasSizeFieldProperty = getDataFactory().getOWLObjectProperty(":hasSizeField", getDefaultPrefixManager());
		isSizeOfProperty = getDataFactory().getOWLObjectProperty(":isSizeOf", getDefaultPrefixManager());
		hasDataTypeProperty = getDataFactory().getOWLObjectProperty(":hasDataType", getDefaultPrefixManager());
		requiresProperty = getDataFactory().getOWLObjectProperty(":requires", getDefaultPrefixManager());
		hasProperty = getDataFactory().getOWLObjectProperty(":has", getDefaultPrefixManager());
		memberProperty = getDataFactory().getOWLObjectProperty("rdfs:member", getDefaultPrefixManager());
	}

	/**
	 * Load an ontology model from an input stream
	 * 
	 * @param in
	 *            input stream
	 * @return a wrapped reference to the ontology
	 * @throws Exception
	 *             if an ontology cannot be read or parsed
	 */
	public Model loadModel(InputStream in) throws Exception {
		OWLOntology derivedModel = loadOntologyModel(in);
		Optional<IRI> optional = derivedModel.getOntologyID().getOntologyIRI();
		if (optional.isPresent()) {
			StructuralReasonerFactory reasonerFactory = new StructuralReasonerFactory();
			OWLReasoner reasoner = reasonerFactory.createReasoner(derivedModel);
			reasoner.precomputeInferences();
			return new MessageModel(optional.get(), derivedModel, reasoner);
		} else {
			throw new RuntimeException("No ontoloty IRI found");
		}
	}

	/**
	 * Create the base ontology model
	 * 
	 * @param in
	 *            input stream
	 * @return base model
	 * @throws OWLOntologyCreationException
	 *             If there was a problem in creating and loading the ontology.
	 */
	OWLOntology loadOntologyModel(InputStream in) throws OWLOntologyCreationException {
		return ontologyManager.loadOntologyFromOntologyDocument(in);
	}

	void removeOntology(Model model) {
		MessageModel messageModel = (MessageModel) model;

		ontologyManager.removeOntology(messageModel.getDerivedModel());
		messageModel.getReasoner().dispose();

	}

	/**
	 * Store the current ontology model to an output stream
	 * 
	 * @param model
	 *            ontology to update
	 * @param out
	 *            output stream
	 * @throws Exception
	 *             if the model cannot be written
	 */
	public void storeModel(Model model, OutputStream out) throws Exception {
		MessageModel messageModel = (MessageModel) model;

		writeAsRdf(messageModel.getDerivedModel(), out);
	}

	/**
	 * Save an ontology to an output stream as RDF XML format
	 * 
	 * @param ontology
	 *            a populated ontology
	 * @param out
	 *            output stream
	 * @throws OWLOntologyStorageException
	 *             If there was a problem saving this ontology to the specified
	 *             output stream
	 */
	void writeAsRdf(OWLOntology ontology, OutputStream out) throws OWLOntologyStorageException {
		ontologyManager.saveOntology(ontology, new RDFXMLDocumentFormat(), out);
	}

	/**
	 * Save an ontology to an output stream as Turtle format
	 * 
	 * @param ontology
	 *            a populated ontology
	 * @param out
	 *            output stream
	 * @throws OWLOntologyStorageException
	 *             If there was a problem saving this ontology to the specified
	 *             output stream
	 */
	void writeAsTurtle(OWLOntology ontology, OutputStream out) throws OWLOntologyStorageException {
		ontologyManager.saveOntology(ontology, new TurtleDocumentFormat(), out);
	}
}
