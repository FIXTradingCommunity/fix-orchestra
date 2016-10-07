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
package io.fixprotocol.orchestra.session;

import java.io.OutputStream;
import java.time.ZonedDateTime;

import io.fixprotocol.orchestra.session.FixSessionTool;
import io.fixprotocol.orchestra.session.FixVersion;
import io.fixprotocol.orchestra.session.FixtSessionRole;
import io.fixprotocol.orchestra.session.Session;
import quickfix.Acceptor;
import quickfix.Dictionary;
import quickfix.FixVersions;
import quickfix.Initiator;
import quickfix.SessionFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

/**
 * @author Don Mendelson
 *
 */
public class QuickFixSessionConfigurer extends FixSessionTool {


  public void configure(OutputStream out) throws Exception {
    SessionSettings quickFixSettings = new SessionSettings();

    for (Session session : getSessions()) {

      FixSessionObject fixSession = (FixSessionObject) session;
      
      ZonedDateTime activationTime = fixSession.getAcivationTime();
      ZonedDateTime deativationTime = fixSession.getDeactivationTime();
      ZonedDateTime now = ZonedDateTime.now();     
      if (activationTime.isAfter(now) || deativationTime.isBefore(now)) {
        continue;
      }

      FixVersion fixVersion = fixSession.getFixVersion();
      String beginString = null;
      String applVersion = null;
      switch (fixVersion) {
        case FIX4_2:
          beginString = FixVersions.BEGINSTRING_FIX42;
          break;
        case FIX4_4:
          beginString = FixVersions.BEGINSTRING_FIX44;
          break;
        case FIX5_0_SP2:
          beginString = FixVersions.BEGINSTRING_FIXT11;
          applVersion = FixVersions.FIX50SP2;
          break;
      }

      String senderCompId = fixSession.getSenderCompId();
      String senderSubId = SessionID.NOT_SET;
      String senderLocationId = SessionID.NOT_SET;
      String targetCompId = fixSession.getTargetCompId();
      String targetSubId = SessionID.NOT_SET;
      String targetLocationId = SessionID.NOT_SET;

      SessionID sessionId = new SessionID(beginString, senderCompId, senderSubId, senderLocationId,
          targetCompId, targetSubId, targetLocationId, SessionID.NOT_SET);

      Dictionary dictionary = new Dictionary();
      dictionary.setString(SessionSettings.BEGINSTRING, beginString);
      dictionary.setString(SessionSettings.SENDERCOMPID, senderCompId);
      dictionary.setString(SessionSettings.SENDERSUBID, senderLocationId);
      dictionary.setString(SessionSettings.SENDERLOCID, senderSubId);
      dictionary.setString(SessionSettings.TARGETCOMPID, targetCompId);
      dictionary.setString(SessionSettings.TARGETSUBID, targetSubId);
      dictionary.setString(SessionSettings.TARGETLOCID, targetLocationId);

      if (FixVersion.FIX5_0_SP2 == fixVersion) {
        dictionary.setString(quickfix.Session.SETTING_DEFAULT_APPL_VER_ID, applVersion);
      }
      
      FixtSessionRole role = fixSession.getSessionRole();
      switch (role) {
        case INITIATOR:
          dictionary.setString(SessionFactory.SETTING_CONNECTION_TYPE,
              SessionFactory.INITIATOR_CONNECTION_TYPE);
          dictionary.setString(Initiator.SETTING_SOCKET_CONNECT_HOST, fixSession.getIpAddress());
          dictionary.setString(Initiator.SETTING_SOCKET_CONNECT_PORT,
              Integer.toString(fixSession.getPort()));
          break;
        case ACCEPTOR:
          dictionary.setString(SessionFactory.SETTING_CONNECTION_TYPE,
              SessionFactory.ACCEPTOR_CONNECTION_TYPE);
          dictionary.setString(Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS, fixSession.getIpAddress());
          dictionary.setString(Acceptor.SETTING_SOCKET_ACCEPT_PORT,
              Integer.toString(fixSession.getPort()));
          break;
      }

      quickFixSettings.set(sessionId, dictionary);
    }

    quickFixSettings.toStream(out);
  }
}
