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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

/**
 * @author donme
 *
 */
public class XmlDiffTest {

  /**
   * 
   */
  private static final String DIFF_FILENAME = "testdiff.txt";
  private XmlDiff xmlDiff;
  private XmlMerge xmlMerge;

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {
    xmlDiff = new XmlDiff();
    xmlMerge = new XmlMerge();
  }


  @Test
  public void diffAndMerge() throws Exception {
    xmlDiff.diff(
        new FileInputStream(Thread.currentThread().getContextClassLoader()
            .getResource("FixRepository2016EP215.xml").getFile()),
        new FileInputStream(Thread.currentThread().getContextClassLoader()
            .getResource("FixRepository2016EP216.xml").getFile()),
        new PrintStream(DIFF_FILENAME));
    xmlMerge.merge(
        new FileInputStream(Thread.currentThread().getContextClassLoader()
            .getResource("FixRepository2016EP215.xml").getFile()),
        new FileReader(DIFF_FILENAME), new FileOutputStream("testmerged.xml"));
  }

}
