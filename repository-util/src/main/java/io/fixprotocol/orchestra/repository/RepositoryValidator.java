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
package io.fixprotocol.orchestra.repository;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
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
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Validates an Orchestra file against the repository schema
 * 
 * @author Don Mendelson
 *
 */
public class RepositoryValidator {

  private static final class ErrorListener implements ErrorHandler {
    private int errors = 0;
    private int fatalErrors = 0;
    private int warnings = 0;

    @Override
    public void error(SAXParseException exception) throws SAXException {
      parentLogger.error("{}:{} {}", exception.getLineNumber(), exception.getColumnNumber(),
          exception.getMessage());
      errors++;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      parentLogger.fatal("{}:{} {}", exception.getLineNumber(), exception.getColumnNumber(),
          exception.getMessage());
      fatalErrors++;
    }

    public int getErrors() {
      return errors;
    }

    public int getFatalErrors() {
      return fatalErrors;
    }

    public int getWarnings() {
      return warnings;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      parentLogger.warn("{}:{} {}", exception.getLineNumber(), exception.getColumnNumber(),
          exception.getMessage());
      warnings++;
    }

  }

  static class Input implements LSInput {

    private String baseUri;
    private boolean certifiedText = false;
    private String encoding;
    private InputStream inputStream;
    private String publicId;
    private String systemId;

    public Input(String publicId, String systemId, InputStream inputStream, String baseURI) {
      this.publicId = publicId;
      this.systemId = systemId;
      this.inputStream = new BufferedInputStream(inputStream);
      this.baseUri = baseURI;
    }

    @Override
    public String getBaseURI() {
      return this.baseUri;
    }

    @Override
    public InputStream getByteStream() {
      return inputStream;
    }

    @Override
    public boolean getCertifiedText() {
      return this.certifiedText;
    }

    @Override
    public Reader getCharacterStream() {
      return null;
    }

    @Override
    public String getEncoding() {
      return this.encoding;
    }

    @Override
    public String getPublicId() {
      return publicId;
    }

    @Override
    public String getStringData() {
      return null;
    }

    @Override
    public String getSystemId() {
      return systemId;
    }

    @Override
    public void setBaseURI(String baseURI) {
      this.baseUri = baseURI;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
      this.inputStream = byteStream;
    }

    @Override
    public void setCertifiedText(boolean certifiedText) {
      this.certifiedText = certifiedText;
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setEncoding(String encoding) {
      this.encoding = encoding;
    }

    @Override
    public void setPublicId(String publicId) {
      this.publicId = publicId;
    }

    @Override
    public void setStringData(String stringData) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void setSystemId(String systemId) {
      this.systemId = systemId;
    }

  }

  static class ResourceResolver implements LSResourceResolver {

    private URL baseUrl;

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId,
        String systemId, String baseURI) {
      InputStream resourceAsStream = null;

      int filePos = systemId.lastIndexOf('/') + 1;
      String filePath = baseUrl.getPath() + "/" + systemId.substring(filePos);
      try {
        URL fileUrl = new URL(baseUrl.getProtocol(), baseUrl.getHost(), filePath);
        resourceAsStream = fileUrl.openStream();
      } catch (IOException e) {
        // Not a fatal error for schema validation
      }
      return new Input(publicId, systemId, resourceAsStream, baseURI);
    }


    public void setBaseUrl(URL baseUrl) {
      this.baseUrl = baseUrl;
    }

  }

  private static final Logger parentLogger = LogManager.getLogger();

  public static void main(String[] args) throws Exception {
    RepositoryValidator validator = new RepositoryValidator();
    InputStream is = new FileInputStream(new File(args[0]));
    validator.validate(is);
  }

  /**
   * Validate an Orchestra repository file against the XML schema
   * 
   * If the validation fails, an exception is thrown. If valid, there is no return.
   * 
   * @param repositoryInstance an XML instance file as a stream
   * @throws ParserConfigurationException if the XML parser fails due to a configuration error
   * @throws SAXException if XML parser fails or the file is invalid
   * @throws IOException if the XML file cannot be read
   */
  public void validate(InputStream repositoryInstance)
      throws ParserConfigurationException, SAXException, IOException {

    // parse an XML document into a DOM tree
    final DocumentBuilderFactory parserFactory = DocumentBuilderFactory.newInstance();
    parserFactory.setNamespaceAware(true);
    parserFactory.setXIncludeAware(true);
    DocumentBuilder parser = parserFactory.newDocumentBuilder();
    Document document = parser.parse(repositoryInstance);

    // create a SchemaFactory capable of understanding WXS schemas
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final ResourceResolver resourceResolver = new ResourceResolver();
    factory.setResourceResolver(resourceResolver);

    // load a WXS schema, represented by a Schema instance
    URL resourceUrl = this.getClass().getClassLoader().getResource("xsd/repository.xsd");
    String path = resourceUrl.getPath();
    String parentPath = path.substring(0, path.lastIndexOf('/'));
    URL baseUrl = new URL(resourceUrl.getProtocol(), null, parentPath);
    resourceResolver.setBaseUrl(baseUrl);

    Source schemaFile = new StreamSource(resourceUrl.openStream());
    Schema schema = factory.newSchema(schemaFile);

    // create a Validator instance, which can be used to validate an instance document
    Validator validator = schema.newValidator();
    final ErrorListener errorHandler = new ErrorListener();
    validator.setErrorHandler(errorHandler);

    // validate the DOM tree
    validator.validate(new DOMSource(document));

    if (errorHandler.getErrors() + errorHandler.getFatalErrors() > 0) {
      parentLogger.fatal("RepositoryValidator complete; fatal errors={} errors={} warnings={}",
          errorHandler.getFatalErrors(), errorHandler.getErrors(), errorHandler.getWarnings());
      throw new SAXException();
    } else {
      parentLogger.info("RepositoryValidator complete; fatal errors={} errors={} warnings={}",
          errorHandler.getFatalErrors(), errorHandler.getErrors(), errorHandler.getWarnings());
    }
  }

}
