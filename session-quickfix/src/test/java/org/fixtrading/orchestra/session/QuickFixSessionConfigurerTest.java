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

import static org.junit.Assert.assertNotNull;

import java.io.FileOutputStream;
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
public class QuickFixSessionConfigurerTest {

  private QuickFixSessionConfigurer tool;

  @Before
  public void setUp() throws Exception {
    tool = new QuickFixSessionConfigurer();
    tool.init();
  }

  @After
  public void tearDown() throws Exception {}


  @Test
  public void testCreateFixSession() throws Exception {
    tool.createNewModel("quickfix", URI.create("http://www.fixtrading.org/session-test/quickfix/"));
    final ZonedDateTime activateTime = ZonedDateTime.now().minusDays(7);
    final ZonedDateTime deactivateTime = ZonedDateTime.now().plusDays(30);
    Session session1 = tool
        .createFixtSession(FixVersion.FIX4_4,  FixtSessionRole.INITIATOR, 
            "session1", "sender1", "senderSub1", null, "target1",
            "targetSub1", null)
        .withTcpTransport("192.168.2.3", 6543)
        .withActivationTime(activateTime).withDeactivationTime(deactivateTime);
    assertNotNull(session1);
  
    Session session2 = tool
        .createFixtSession(FixVersion.FIX5_0_SP2,  FixtSessionRole.INITIATOR, 
            "session2", "sender2", "senderSub2", null, "target1",
            "targetSub1", null)
        .withTcpTransport("192.168.2.3", 6544)
        .withActivationTime(activateTime).withDeactivationTime(deactivateTime);
    assertNotNull(session2);
    
    OutputStream out = new FileOutputStream("QuickFixTestModel.rdf");
    tool.storeModel(out);
    out.close();

    tool.configure(new FileOutputStream("QuickFixConfiguration.ini"));
  }

}
