/*
 * Copyright 2020 FIX Protocol Ltd
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
 */
/**
 * Generated code to access an Orchestra repository file.
 * <p>
 * To unmarshal an Orchestra XML file into a Java object representing a repository:
 * 
 * <pre>
 * final InputStream is = new FileInputStream("myorchestra.xml");
 * final JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
 * final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
 * Repository = jaxbUnmarshaller.unmarshal(is);
 * </pre>
 * 
 * To marshal a Repository object into an Orchestra XML file:
 * 
 * <pre>
 * Repository jaxbElement = new Repository();
 * // populate the DOM
 * final OutputStream os = new FileOutputStream("myorchestra2.xml");
 * final JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
 * final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
 * jaxbMarshaller.setProperty("jaxb.formatted.output", true);
 * jaxbMarshaller.marshal(jaxbElement, os);
 * </pre>
 *
 * @author Don Mendelson
 *
 */
package io.fixprotocol._2020.orchestra.repository;

