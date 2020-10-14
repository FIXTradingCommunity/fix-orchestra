/*
 * Copyright 2019-2020 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.interfaces;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import io.fixprotocol.orchestra.event.ConsoleEventListener;
import io.fixprotocol.orchestra.event.EventListener;
import io.fixprotocol.orchestra.event.EventListenerFactory;
import io.fixprotocol.orchestra.event.TeeEventListener;


/**
 * Validates an Orchestra file against the interfaces schema
 *
 * @author Don Mendelson
 *
 */
public class InterfacesValidator {

  private final class ErrorListener implements ErrorHandler {


    @Override
    public void error(SAXParseException exception) throws SAXException {
      eventLogger.error("InterfacesValidator: XML error at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      errors++;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      eventLogger.fatal("InterfacesValidator: XML fatal error at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      fatalErrors++;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      eventLogger.warn("InterfacesValidator: XML warning at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      warnings++;
    }

  }

  public static EventListener createLogger(OutputStream jsonOutputStream) {
    final Logger logger = LogManager.getLogger(InterfacesValidator.class);
    final EventListenerFactory factory = new EventListenerFactory();
    TeeEventListener eventListener = null;
    try {
      eventListener = new TeeEventListener();
      final EventListener logEventLogger = factory.getInstance("LOG4J");
      logEventLogger.setResource(logger);
      eventListener.addEventListener(logEventLogger);
      if (jsonOutputStream != null) {
        final EventListener jsonEventLogger = factory.getInstance("JSON");
        jsonEventLogger.setResource(jsonOutputStream);
        eventListener.addEventListener(jsonEventLogger);
      }
    } catch (Exception e) {
      eventListener.addEventListener(new ConsoleEventListener());
      eventListener.error("Error creating event listener; {0}", e.getMessage());
    }
    return eventListener;
  }

  private int errors = 0;
  private EventListener eventLogger;
  private int fatalErrors = 0;
  private int warnings = 0;

  public int getErrors() {
    return errors;
  }

  public EventListener getEventLogger() {
    return eventLogger;
  }

  public int getFatalErrors() {
    return fatalErrors;
  }

  public int getWarnings() {
    return warnings;
  }

  /**
   * Validate an Orchestra interfaces file against the XML schema
   *
   * A interfaces file is invalid if a parser encounters unrecoverable errors, such as for non-well
   * formed XML, or if errors are produced that recoverable for the parser but would lead to an
   * invalid conversion. However, if only warnings are produced, the file is considered valid.
   *
   * @return Returns {@code true} if the file does not have serious errors, {@code false} if it
   *         does.
   */
  public boolean validate(InputStream inputStream, OutputStream jsonOutputStream,
      boolean doNotCloseEventLog) {
    try {
      eventLogger = createLogger(jsonOutputStream);
    } catch (final Exception e) {
      eventLogger.error("Failed to initialize event logger; {0}", e.getMessage());
    }
    final ErrorListener errorHandler = new ErrorListener();
    try {
      validateSchema(inputStream, errorHandler);
    } catch (final Exception e) {
      eventLogger.fatal("Failed to validate interfaces; {0}", e.getMessage());
      fatalErrors++;
    }

    try {
      if (getErrors() + getFatalErrors() > 0) {
        eventLogger.fatal(
            "InterfacesValidator complete; fatal errors={0,number,integer} errors={1,number,integer} warnings={2,number,integer}",
            getFatalErrors(), getErrors(), getWarnings());
        return false;
      } else {
        eventLogger.info(
            "InterfacesValidator complete; fatal errors={0,number,integer} errors={1,number,integer} warnings={2,number,integer}",
            getFatalErrors(), getErrors(), getWarnings());
        return true;
      }
    } finally {
      if (!doNotCloseEventLog) {
        closeLogger();
      }
    }
  }

  private void closeLogger() {
    try {
      eventLogger.close();
    } catch (Exception e) {

    }
  }

  private Document validateSchema(InputStream inputStream, ErrorListener errorHandler)
      throws ParserConfigurationException, SAXException, IOException {
    // parse an XML document into a DOM tree
    final DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
    parserFactory.setNamespaceAware(true);
    parserFactory.setXIncludeAware(true);
    final DocumentBuilder parser = parserFactory.newDocumentBuilder();
    final Document document = parser.parse(inputStream);

    // create a SchemaFactory capable of understanding WXS schemas
    final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final ResourceResolver resourceResolver = new ResourceResolver();
    factory.setResourceResolver(resourceResolver);

    // load a WXS schema, represented by a Schema instance
    final URL resourceUrl = this.getClass().getClassLoader().getResource("xsd/interfaces.xsd");
    final String path = Objects.requireNonNull(resourceUrl).getPath();
    final String parentPath = path.substring(0, path.lastIndexOf('/'));
    final URL baseUrl = new URL(resourceUrl.getProtocol(), null, parentPath);
    resourceResolver.setBaseUrl(baseUrl);

    final Source schemaFile = new StreamSource(resourceUrl.openStream());
    final Schema schema = factory.newSchema(schemaFile);

    // create a Validator instance, which can be used to validate an instance document
    final Validator validator = schema.newValidator();

    validator.setErrorHandler(errorHandler);

    // validate the DOM tree
    validator.validate(new DOMSource(document));
    return document;
  }

}
