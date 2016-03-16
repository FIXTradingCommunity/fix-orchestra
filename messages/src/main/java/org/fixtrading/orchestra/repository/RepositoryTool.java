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
import org.fixtrading.orchestra.messages.Model;
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
 * Imports FIX Repository 2010 edition
 * 
 * @author Don Mendelson
 *
 */
public class RepositoryTool {

	/**
	 * Translates a FIX Repository file to an ontology file
	 * 
	 * @param args
	 *            command line arguments
	 *            <ol>
	 *            <li>repository-file-name</li>
	 *            <li>ontology-file-name</li>
	 *            </ol>
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
		System.out.println("Usage: RepositoryTool <repository-file-name> [ontology-file-name]");
	}

	private Model model;
	private final MessageOntologyManager ontologyManager = new MessageOntologyManager();

	/**
	 * Add a message element to the ontology
	 * 
	 * @param parent
	 *            containing component
	 * @param entity
	 *            an element to add
	 */
	void addEntity(MessageEntity parent, MessageEntityT entity) {
		BigInteger entityId = entity.getId();
		String name = entity.getName();
		boolean isRequired = entity.getRequired() != 0;
		if (entity instanceof FieldRef) {
			ontologyManager.addField(parent, entityId.intValue(), name, isRequired);
		} else if (entity instanceof ComponentRef) {
			ontologyManager.addComponent(parent, entityId.intValue(), name, isRequired);
		} else if (entity instanceof RepeatingGroup) {
			ontologyManager.addNumInGroupField(parent, entityId.intValue(), name, isRequired);
			RepeatingGroup group = (RepeatingGroup) entity;
			List<JAXBElement<? extends MessageEntityT>> entityList = group.getMessageEntity();
			for (JAXBElement<? extends MessageEntityT> childElement : entityList) {
				MessageEntityT child = childElement.getValue();
				addEntity(parent, child);
			}
		}
	}

	private Field findField(BigInteger referencedTag, List<Field> fieldList) {
		for (Field field : fieldList) {
			if (field.getId().equals(referencedTag)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * Initializes resources
	 * 
	 * @throws Exception
	 *             if resources cannot be initialized
	 */
	public void init() throws Exception {
		ontologyManager.init();
		model = ontologyManager.createNewModel("fix",
				URI.create("http://www.fixtrading.org/fix-orchestra/repository/"));
	}

	/**
	 * Parses a FIX repository
	 * 
	 * @param inputStream
	 *            stream of the repository contents
	 * @throws JAXBException
	 *             if a parsing failure occurs
	 */
	public void parse(InputStream inputStream) throws JAXBException {
		FixRepository fixRepository = Serializer.unmarshal(inputStream);
		List<Fix> fixList = fixRepository.getFix();
		for (Fix fix : fixList) {
			Datatypes datatypes = fix.getDatatypes();
			List<Datatype> datatypeList = datatypes.getDatatype();
			for (Datatype datatype : datatypeList) {
				ontologyManager.createDataType(model, datatype.getName());
			}

			Fields fields = fix.getFields();
			List<Field> fieldList = fields.getField();
			for (Field field : fieldList) {
				List<Enum> enumList = field.getEnum();
				if (!enumList.isEmpty()) {
					ontologyManager.createCodeSet(model, field.getName(), field.getType());

					for (Enum fixEnum : enumList) {
						ontologyManager.createCode(model, field.getName(), fixEnum.getSymbolicName(), fixEnum.getValue());
					}
					
					ontologyManager.createField(model, field.getId().intValue(), field.getName(),
							field.getName());
				} else {
					final BigInteger associatedDataTag = field.getAssociatedDataTag();
					if (associatedDataTag != null) {
						ontologyManager.createField(model, field.getId().intValue(), field.getName(),
							field.getType());
					} else {
						ontologyManager.createField(model, field.getId().intValue(), field.getName());
					}
				}
			}

			// Make a second pass to associate fields after they are all created
			for (Field field : fieldList) {
				final BigInteger associatedDataTag = field.getAssociatedDataTag();
				if (associatedDataTag != null) {
					ontologyManager.associateFields(model, field.getId().intValue(), associatedDataTag.intValue());
				}
				
				final BigInteger referencedTag = field.getEnumDatatype();
				if (referencedTag != null) {
					ontologyManager.associateCodeList(model, field.getName(), referencedTag.intValue());
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
					parent = ontologyManager.createComponent(model, id.intValue(), name);
					break;
				case BLOCK_REPEATING:
				case IMPLICIT_BLOCK_REPEATING:
				case OPTIMISED_IMPLICIT_BLOCK_REPEATING:
					parent = ontologyManager.createRepeatingGroup(model, id.intValue(), name);
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
				MessageEntity parent = ontologyManager.createMessage(model, message.getId().intValue(), message.getName(),
						message.getMsgType());
				List<JAXBElement<? extends MessageEntityT>> messageEntityList = message.getMessageEntity();
				for (JAXBElement<? extends MessageEntityT> messageEntity : messageEntityList) {
					MessageEntityT entity = messageEntity.getValue();
					addEntity(parent, entity);
				}
			}
		}
	}

	/**
	 * Stores a translated ontology
	 * 
	 * @param outputStream
	 *            stream of the ontology contents
	 * @throws Exception
	 *             if the ontology cannot be written
	 */
	public void store(FileOutputStream outputStream) throws Exception {
		ontologyManager.storeModel(model, outputStream);
	}

}
