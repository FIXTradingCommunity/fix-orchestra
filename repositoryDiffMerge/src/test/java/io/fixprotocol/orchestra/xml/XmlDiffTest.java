/**
 * Copyright 2017 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.xml;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class XmlDiffTest {

  /**
   * 
   */
  private static final String DIFF_FILENAME = "testdiff.xml";
  private XmlDiff xmlDiff;
  private XmlMerge xmlMerge;

  /**
   * @throws java.lang.Exception
   */
  @SuppressWarnings("resource")
  @Before
  public void setUp() throws Exception {
    xmlDiff = new XmlDiff();
    xmlDiff.setListener(new PatchOpsListener(new FileOutputStream(DIFF_FILENAME)));
    xmlMerge = new XmlMerge();
  }

  @Test
  public void simpleDiff() throws Exception {
    try (
        final FileInputStream is1 = new FileInputStream(Thread.currentThread()
            .getContextClassLoader().getResource("DiffTest1.xml").getFile());
        final FileInputStream is2 = new FileInputStream(Thread.currentThread()
            .getContextClassLoader().getResource("DiffTest2.xml").getFile())) {
      xmlDiff.diff(is1, is2);
    }
    
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(DIFF_FILENAME);

    assertEquals(2, doc.getElementsByTagName("add").getLength());
    assertEquals(2, doc.getElementsByTagName("replace").getLength());
    assertEquals(2, doc.getElementsByTagName("remove").getLength());
  }

  @Test
  public void diffAndMerge() throws Exception {
    try (
        final FileInputStream is1 = new FileInputStream(Thread.currentThread()
            .getContextClassLoader().getResource("FixRepository2016EP215.xml").getFile());
        final FileInputStream is2 = new FileInputStream(Thread.currentThread()
            .getContextClassLoader().getResource("FixRepository2016EP216.xml").getFile())) {
      xmlDiff.diff(is1, is2);

      try (
          final FileInputStream is1Baseline = new FileInputStream(Thread.currentThread()
              .getContextClassLoader().getResource("FixRepository2016EP215.xml").getFile());
          final FileInputStream isDiff = new FileInputStream(DIFF_FILENAME);
          final FileOutputStream osMerge = new FileOutputStream("testmerged.xml")) {
        xmlMerge.merge(is1Baseline, isDiff, osMerge);
      }
    }
  }

}
