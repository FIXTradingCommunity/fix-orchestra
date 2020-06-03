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
package io.fixprotocol.orchestra.transformers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import io.fixprotocol.orchestra.repository.RepositoryValidator;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;


public class Unified2OrchestraTransformerTest {

  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 5.0SP2
   */
  @Test
  public void transformEP() throws TransformerException, URISyntaxException {
    File inputXml = new File(Thread.currentThread().getContextClassLoader()
        .getResource("FixRepositoryUnifiedEP247.xml").toURI());
    File outputXml = new File("target/test/FixRepository50SP2EP247.xml");
    String sourceDir = inputXml.getParent();
    File phrasesFile = new File(String.format("%s/FIX.5.0SP2_EP247_en_phrases.xml", sourceDir));
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml);
    Assertions.assertTrue(outputXml.exists());
    // new RepositoryValidator().validate(new FileInputStream(outFile));
  }
  
  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 5.0SP2
   * @throws IOException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   * @throws FileNotFoundException 
   */
  @Disabled
  @Test
  public void transform50() throws TransformerException, URISyntaxException, FileNotFoundException, ParserConfigurationException, SAXException, IOException {
    File inputXml = new File(Thread.currentThread().getContextClassLoader()
        .getResource("FixRepositoryUnified.xml").toURI());
    File outputXml = new File("target/test/FixRepository50SP2.xml");
    String sourceDir = inputXml.getParent();
    File phrasesFile = new File(String.format("%s/FIX.5.0SP2_en_phrases.xml", sourceDir));
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml);
    Assertions.assertTrue(outputXml.exists());
    RepositoryValidator validator = RepositoryValidator.builder().inputStream(new FileInputStream(outputXml)).build();
    validator.validate();
  }
  
  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 4.4
   * @throws URISyntaxException 
   */
  @Disabled
  @Test
  public void transform44() throws TransformerException, URISyntaxException {
    File inputXml = new File(Thread.currentThread().getContextClassLoader()
        .getResource("FixRepositoryUnified.xml").toURI());
    File outputXml = new File("target/test/FixRepository44.xml");
    String sourceDir = inputXml.getParent();
    File phrasesFile = new File(String.format("%s/FIX.4.4_en_phrases.xml", sourceDir));
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml);
    Assertions.assertTrue(outputXml.exists());
  }
  
  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 4.2
   * @throws URISyntaxException 
   */
  @Disabled
  @Test
  public void transform42() throws TransformerException, URISyntaxException {
    File inputXml = new File(Thread.currentThread().getContextClassLoader()
        .getResource("FixRepositoryUnified.xml").toURI());
    File outputXml = new File("target/test/FixRepository42.xml");
    String sourceDir = inputXml.getParent();
    File phrasesFile = new File(String.format("%s/FIX.4.2_en_phrases.xml", sourceDir));
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml);
    Assertions.assertTrue(outputXml.exists());
  }
}
