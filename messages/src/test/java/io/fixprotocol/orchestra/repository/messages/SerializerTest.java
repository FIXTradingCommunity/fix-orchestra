/**
 *    Copyright 2016 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package io.fixprotocol.orchestra.repository.messages;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Ignore;
import org.junit.Test;

import io.fixprotocol.orchestra.repository.jaxb.FixRepository;
import io.fixprotocol.orchestra.repository.messages.Serializer;

/**
 * @author Don Mendelson
 *
 */
public class SerializerTest {


  /**
   * Test method for {@link io.fixprotocol.orchestra.repository.messages.Serializer#marshal(io.fixprotocol.orchestra.repository.jaxb.FixRepository, java.io.OutputStream)}.
   */
  @Ignore
  @Test
  public void testMarshal() {
    fail("Not yet implemented");
  }

  /**
   * Test method for {@link io.fixprotocol.orchestra.repository.messages.Serializer#unmarshal(java.io.InputStream)}.
   * @throws JAXBException 
   * @throws IOException 
   */
  @Test
  public void testUnmarshal() throws JAXBException, IOException {
    InputStream inputStream = new FileInputStream(this.getClass().getResource("/FixRepository.xml").getPath());
    FixRepository fixRepository = Serializer.unmarshal(inputStream);
    inputStream.close();
  }

}
