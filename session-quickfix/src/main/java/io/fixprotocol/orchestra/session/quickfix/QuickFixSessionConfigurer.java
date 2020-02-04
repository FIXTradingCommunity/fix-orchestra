/*
 * Copyright 2017-2020 FIX Protocol Ltd
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.XMLGregorianCalendar;
import org.w3c.dom.Node;
import io.fixprotocol._2020.orchestra.interfaces.IdentifierType;
import io.fixprotocol._2020.orchestra.interfaces.InterfaceType;
import io.fixprotocol._2020.orchestra.interfaces.InterfaceType.Sessions;
import io.fixprotocol._2020.orchestra.interfaces.Interfaces;
import io.fixprotocol._2020.orchestra.interfaces.RoleT;
import io.fixprotocol._2020.orchestra.interfaces.SessionProtocolType;
import io.fixprotocol._2020.orchestra.interfaces.SessionType;
import io.fixprotocol._2020.orchestra.interfaces.TransportProtocolType;
import io.fixprotocol._2020.orchestra.interfaces.TransportUseEnum;
import quickfix.Acceptor;
import quickfix.ConfigError;
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
public class QuickFixSessionConfigurer {

  private ZonedDateTime effectiveTime = ZonedDateTime.now();

  public static void main(String[] args) throws JAXBException, ConfigError, IOException {
    if (args.length < 2) {
      useage();
    } else {
      QuickFixSessionConfigurer configurer = new QuickFixSessionConfigurer();
      try (FileOutputStream out = new FileOutputStream(args[1]);
          FileInputStream in = new FileInputStream(args[0])) {
        configurer.configure(in, out);
      }
    }
  }


  public static void useage() {
    System.err.println(
        "Usage: java io.fixprotocol.orchestra.session.quickfix.QuickFixConfigurer <interfaces-file> <quickfix-file>");
  }

  /**
   * @return the effectiveTime
   */
  public ZonedDateTime getEffectiveTime() {
    return effectiveTime;
  }

  /**
   * @param effectiveTime the effectiveTime to set
   */
  public void setEffectiveTime(ZonedDateTime effectiveTime) {
    this.effectiveTime = effectiveTime;
  }

  public void configure(InputStream in, OutputStream out) throws JAXBException, ConfigError {
    SessionSettings quickFixSettings = new SessionSettings();

    Interfaces interfaces = unmarshal(in);
    List<InterfaceType> interfaceList = interfaces.getInterface();
    for (InterfaceType interfaceType : interfaceList) {
      boolean isFixSession = false;
      String interfaceVersion = null;
      List<SessionProtocolType> sessionProtocols = interfaceType.getSessionProtocol();
      for (SessionProtocolType sessionProtocolType : sessionProtocols) {
        String protocolName = sessionProtocolType.getName();
        if (protocolName.contains("FIX4") || protocolName.contains("FIXT")) {
          interfaceVersion = sessionProtocolType.getVersion();
          isFixSession = true;
          break;
        }
      }

      Sessions sessions = interfaceType.getSessions();
      List<SessionType> sessionList = sessions.getSession();
      for (SessionType sessionType : sessionList) {

        String version = null;
        sessionProtocols = sessionType.getSessionProtocol();
        for (SessionProtocolType sessionProtocolType : sessionProtocols) {
          String protocolName = sessionProtocolType.getName();
          if (protocolName.contains("FIX4") || protocolName.contains("FIXT")) {
            version = sessionProtocolType.getVersion();
            isFixSession = true;
            break;
          }
        }

        if (!isFixSession) {
          continue;
        }

        if (version == null) {
          version = interfaceVersion;
        }
        if (version == null) {
          System.err.println("FIX version unknown; skipping session");
          break;
        }

        final XMLGregorianCalendar activationTimeXml = sessionType.getActivationTime();
        if (activationTimeXml != null) {
          ZonedDateTime activationTime = activationTimeXml.toGregorianCalendar().toZonedDateTime();
          if (activationTime.isAfter(effectiveTime)) {
            continue;
          }
        }

        final XMLGregorianCalendar deactivationTimeXml = sessionType.getDeactivationTime();
        if (deactivationTimeXml != null) {
          ZonedDateTime deativationTime =
              deactivationTimeXml.toGregorianCalendar().toZonedDateTime();
          if (deativationTime.isBefore(effectiveTime)) {
            continue;
          }
        }

        String beginString = null;
        String applVersion = null;
        switch (version) {
          case FixVersions.BEGINSTRING_FIX42:
            beginString = FixVersions.BEGINSTRING_FIX42;
            break;
          case FixVersions.BEGINSTRING_FIX44:
            beginString = FixVersions.BEGINSTRING_FIX44;
            break;
          case FixVersions.BEGINSTRING_FIXT11:
          case FixVersions.FIX50SP2:
            beginString = FixVersions.BEGINSTRING_FIXT11;
            applVersion = FixVersions.FIX50SP2;
            break;
        }

        Map<String, String> identifierMap = new HashMap<>();
        List<IdentifierType> identifierList = sessionType.getIdentifier();
        for (IdentifierType identifierType : identifierList) {
          String name = identifierType.getName();
          Node value = (Node) identifierType.getValue();
          String text = value.getFirstChild().getTextContent();
          identifierMap.put(name, text);
        }

        Dictionary dictionary = new Dictionary();
        dictionary.setString(SessionSettings.BEGINSTRING, beginString);
        dictionary.setString(SessionSettings.SENDERCOMPID,
            identifierMap.getOrDefault(SessionSettings.SENDERCOMPID, SessionID.NOT_SET));
        dictionary.setString(SessionSettings.SENDERSUBID,
            identifierMap.getOrDefault(SessionSettings.SENDERSUBID, SessionID.NOT_SET));
        dictionary.setString(SessionSettings.SENDERLOCID,
            identifierMap.getOrDefault(SessionSettings.SENDERLOCID, SessionID.NOT_SET));
        dictionary.setString(SessionSettings.TARGETCOMPID,
            identifierMap.getOrDefault(SessionSettings.TARGETCOMPID, SessionID.NOT_SET));
        dictionary.setString(SessionSettings.TARGETSUBID,
            identifierMap.getOrDefault(SessionSettings.TARGETSUBID, SessionID.NOT_SET));
        dictionary.setString(SessionSettings.TARGETLOCID,
            identifierMap.getOrDefault(SessionSettings.TARGETLOCID, SessionID.NOT_SET));

        if (FixVersions.FIX50SP2.equals(version)) {
          dictionary.setString(quickfix.Session.SETTING_DEFAULT_APPL_VER_ID, applVersion);
        }

        RoleT role = sessionType.getRole();
        List<TransportProtocolType> transportList = sessionType.getTransport();
        TransportProtocolType transport = null;
        int transportCount = transportList.size();
        if (transportCount == 1) {
          transport = transportList.get(0);
        } else {
          for (TransportProtocolType aTransport : transportList) {
            if (aTransport.getUse().equalsIgnoreCase(TransportUseEnum.PRIMARY.toString())) {
              transport = aTransport;
            }
          }
          if (transport == null) {
            System.err.println("Transport not configured; skipping session");
            continue;
          }

          String address = transport.getAddress();
          String[] addressParts = address.split(":");

          switch (role) {
            case INITIATOR:
              dictionary.setString(SessionFactory.SETTING_CONNECTION_TYPE,
                  SessionFactory.INITIATOR_CONNECTION_TYPE);
              dictionary.setString(Initiator.SETTING_SOCKET_CONNECT_HOST, addressParts[0]);
              dictionary.setString(Initiator.SETTING_SOCKET_CONNECT_PORT, addressParts[1]);
              break;
            case ACCEPTOR:
              dictionary.setString(SessionFactory.SETTING_CONNECTION_TYPE,
                  SessionFactory.ACCEPTOR_CONNECTION_TYPE);
              dictionary.setString(Acceptor.SETTING_SOCKET_ACCEPT_ADDRESS, addressParts[0]);
              dictionary.setString(Acceptor.SETTING_SOCKET_ACCEPT_PORT, addressParts[1]);
              break;
          }

          SessionID sessionId =
              new SessionID(beginString, identifierMap.get(SessionSettings.SENDERCOMPID),
                  identifierMap.get(SessionSettings.SENDERSUBID),
                  identifierMap.get(SessionSettings.SENDERLOCID),
                  identifierMap.get(SessionSettings.TARGETCOMPID),
                  identifierMap.get(SessionSettings.TARGETSUBID),
                  identifierMap.get(SessionSettings.TARGETLOCID), SessionID.NOT_SET);
          quickFixSettings.set(sessionId, dictionary);
        }

        quickFixSettings.toStream(out);

      }
    }
  }

  private Interfaces unmarshal(InputStream in) throws JAXBException {
    JAXBContext jaxbContext = JAXBContext.newInstance(Interfaces.class);
    Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Interfaces) jaxbUnmarshaller.unmarshal(in);
  }
}
