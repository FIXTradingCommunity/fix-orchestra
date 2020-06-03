/**
 * Copyright 2015-2016 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.session.quickfix;

import java.io.File;
import java.io.FileOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Don Mendelson
 *
 */
public class QuickFixSessionConfigurerTest {

  private QuickFixSessionConfigurer tool;

  @BeforeAll
  public static void setupOnce() {
    new File(("target/test")).mkdirs();
  }

  @BeforeEach
  public void setUp() throws Exception {
    tool = new QuickFixSessionConfigurer();
  }

  @Test
  public void testCreateFixSession() throws Exception {
    final String outfile = "target/test/QuickFixConfiguration.ini";
    tool.configure(
        Thread.currentThread().getContextClassLoader().getResourceAsStream("SampleInterfaces.xml"),
        new FileOutputStream(outfile));
    Assertions.assertTrue(new File(outfile).exists());
  }

}
