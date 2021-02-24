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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import io.fixprotocol.orchestra.event.EventListener;
import io.fixprotocol.orchestra.repository.RepositoryValidator;
import io.fixprotocol.orchestra.repository.FixRepositoryValidator;


public class Unified2OrchestraTransformerTest {
  
  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  /*
  private EventListener eventLogger;
  private FixRepositoryValidator validator;

  @BeforeEach
  public void setUp() throws Exception {
    final OutputStream jsonOutputStream =
        new FileOutputStream("target/test/transformer.json");
    eventLogger = FixRepositoryValidator.createLogger(jsonOutputStream);
    validator = new FixRepositoryValidator(eventLogger);
  }
  
  @AfterEach
  public void cleanUp() throws Exception {
    eventLogger.close();
  }
  */
  
  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 5.0SP2
   * @throws FileNotFoundException 
   */
  @Test
  public void transformEP() throws Exception {
    File inputXml = new File(Thread.currentThread().getContextClassLoader()
        .getResource("FixRepositoryUnifiedEP247.xml").toURI());
    File outputXml = new File("target/test/OrchestraEP247.xml");
    String sourceDir = inputXml.getParent();
    File phrasesFile = new File(String.format("%s/FIX.5.0SP2_EP247_en_phrases.xml", sourceDir));
    String name = "FIX.Latest";
    String version = "FIX.Latest_EP247";
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml, name, version);
    Assertions.assertTrue(outputXml.exists());
    //validator.validate(new FileInputStream(outputXml));
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
    File outputXml = new File("target/test/Orchestra50SP2.xml");
    String sourceDir = inputXml.getParent();
    File phrasesFile = new File(String.format("%s/FIX.5.0SP2_en_phrases.xml", sourceDir));
    String name = "FIX.5.0SP2";
    String version = "FIX.5.0SP2";
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml, name, version);
    Assertions.assertTrue(outputXml.exists());
    RepositoryValidator validator = RepositoryValidator.builder().inputFile("target/test/FixRepository50SP2.xml").build();
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
        .getResource("FixRepositoryUnified44.xml").toURI());
    File outputXml = new File("target/test/Orchestra44.xml");
    String sourceDir = inputXml.getParent();
    File phrasesFile = new File(String.format("%s/FIX.4.4_en_phrases.xml", sourceDir));
    String name = "FIX.4.4";
    String version = "FIX.4.4";
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml, name, version);
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
    String name = "FIX.4.2";
    String version = "FIX.4.2";
    new Unified2OrchestraTransformer().transform(inputXml, phrasesFile, outputXml, name, version);
    Assertions.assertTrue(outputXml.exists());
  }
}
