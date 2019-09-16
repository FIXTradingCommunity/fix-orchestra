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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;
import io.fixprotocol.orchestra.repository.RepositoryValidator;
import io.fixprotocol.orchestra.transformers.RepositoryXslTransformer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;


public class RepositoryXslTransformerTest {

  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 5.0SP2
   * @throws URISyntaxException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  @Test
  public void transformEP() throws IOException, TransformerException, ParserConfigurationException, SAXException, URISyntaxException {
    String[] arr = new String[4];
    arr[0] = Thread.currentThread().getContextClassLoader()
        .getResource("xsl/unified2orchestra.xslt").getFile();
    arr[1] =
        Thread.currentThread().getContextClassLoader().getResource("FixRepositoryUnifiedEP247.xml").getFile();
    String sourceDir = new File(arr[1]).getParent();
    // send output to target so it will get cleaned
    arr[2] = "target/test/FixRepository50SP2EP247.xml";
    // document function in XSLT expects a URI, not a file name (Saxon does not convert)
    arr[3] = String.format("phrases-file=file:///%s/FIX.5.0SP2_EP247_en_phrases.xml",
        sourceDir.replace('\\', '/'));
    RepositoryXslTransformer.main(arr);
    File outFile = new File(arr[2]);
    Assert.assertTrue(outFile.exists());
    //new RepositoryValidator().validate(new FileInputStream(outFile));
  }
  
  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 5.0SP2
   * @throws URISyntaxException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  @Ignore
  @Test
  public void transform50() throws IOException, TransformerException, ParserConfigurationException, SAXException, URISyntaxException {
    String[] arr = new String[4];
    arr[0] = Thread.currentThread().getContextClassLoader()
        .getResource("xsl/unified2orchestra.xslt").getFile();
    arr[1] =
        Thread.currentThread().getContextClassLoader().getResource("FixRepositoryUnified.xml").getFile();
    String sourceDir = new File(arr[1]).getParent();
    // send output to target so it will get cleaned
    arr[2] = "target/test/FixRepository50SP2.xml";
    // document function in XSLT expects a URI, not a file name (Saxon does not convert)
    arr[3] = String.format("phrases-file=file:///%s/FIX.5.0SP2_en_phrases.xml",
        sourceDir.replace('\\', '/'));
    RepositoryXslTransformer.main(arr);
    File outFile = new File(arr[2]);
    Assert.assertTrue(outFile.exists());
    new RepositoryValidator().validate(new FileInputStream(outFile));
  }
  
  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 4.4
   */
  @Test
  public void transform44() throws IOException, TransformerException {
    String[] arr = new String[4];
    arr[0] = Thread.currentThread().getContextClassLoader()
        .getResource("xsl/unified2orchestra.xslt").getFile();
    arr[1] =
        Thread.currentThread().getContextClassLoader().getResource("FixRepositoryUnified.xml").getFile();
    String sourceDir = new File(arr[1]).getParent();
    // send output to target so it will get cleaned
    arr[2] = "target/test/FixRepository44.xml";
    // document function in XSLT expects a URI, not a file name (Saxon does not convert)
    arr[3] = String.format("phrases-file=file:///%s/FIX.4.4_en_phrases.xml",
        sourceDir.replace('\\', '/'));
    RepositoryXslTransformer.main(arr);
    File outFile = new File(arr[2]);
    Assert.assertTrue(outFile.exists());
  }
  
  /**
   * 
   * Transform FIX Repository 2010 Edition unified repositories to Orchestra schema for FIX 4.2
   * @throws URISyntaxException 
   * @throws SAXException 
   * @throws ParserConfigurationException 
   */
  @Test
  public void transform42() throws IOException, TransformerException, ParserConfigurationException, SAXException, URISyntaxException {
    String[] arr = new String[4];
    arr[0] = Thread.currentThread().getContextClassLoader()
        .getResource("xsl/unified2orchestra.xslt").getFile();
    arr[1] =
        Thread.currentThread().getContextClassLoader().getResource("FixRepositoryUnified.xml").getFile();
    String sourceDir = new File(arr[1]).getParent();
    // send output to target so it will get cleaned
    arr[2] = "target/test/FixRepository42.xml";
    // document function in XSLT expects a URI, not a file name (Saxon does not convert)
    arr[3] = String.format("phrases-file=file:///%s/FIX.4.2_en_phrases.xml",
        sourceDir.replace('\\', '/'));
    RepositoryXslTransformer.main(arr);
    File outFile = new File(arr[2]);
    Assert.assertTrue(outFile.exists());
    //new RepositoryValidator().validate(new FileInputStream(outFile));
  }
}
