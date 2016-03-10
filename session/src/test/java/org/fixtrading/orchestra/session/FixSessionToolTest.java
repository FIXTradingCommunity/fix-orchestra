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
package org.fixtrading.orchestra.session;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.ZonedDateTime;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Don Mendelson
 *
 */
public class FixSessionToolTest {

  private FixSessionTool tool;

  @Before
  public void setUp() throws Exception {
    tool = new FixSessionTool();
    tool.init();
  }

  @After
  public void tearDown() throws Exception {}


  @Test
  public void testCreateFixtSession() throws Exception {
    tool.createNewModel("fix", URI.create("http://www.fixtrading.org/session-test/fixt/"));
    Session session1 = tool
        .createFixtSession(FixVersion.FIX4_4, FixtSessionRole.INITIATOR,
            "session1", "sender1", "senderSub1", null, "target1",
            "targetSub1", null)
        .withTcpTransport("192.168.2.3", 6543)
        .withActivationTime(ZonedDateTime.now());
    assertNotNull(session1);

    assertEquals(1, tool.getSessions().size());

    OutputStream out = new FileOutputStream("FixTestModel.rdf");
    tool.storeModel(out);
    out.close();

    InputStream in = new FileInputStream("FixTestModel.rdf");
    tool.loadModel(in);
    in.close();

    Session ind = tool.getSession("session1");
    assertNotNull(ind);
  }

}
