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
package org.fixtrading.orchestra.repository.messages;

import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.fixtrading.orchestra.repository.jaxb.FixRepository;

/**
 * @author Donald
 *
 */
public final class Serializer {


  public static void marshal(FixRepository jaxbElement, OutputStream outputStream)
      throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(FixRepository.class);
    javax.xml.bind.Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
    jaxbMarshaller.marshal(jaxbElement, outputStream);
  }

  public static FixRepository unmarshal(InputStream inputStream) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(FixRepository.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (FixRepository) jaxbUnmarshaller.unmarshal(inputStream);
  }
}
