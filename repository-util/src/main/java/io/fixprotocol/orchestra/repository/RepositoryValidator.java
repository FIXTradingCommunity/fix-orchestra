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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.util.LogUtil;


/**
 * Validates an Orchestra file against the repository schema
 *
 * @author Don Mendelson
 *
 */
public class RepositoryValidator {

  public static class Builder {
    private InputStream inputStream;
    private String logFile;
    private boolean verbose;

    public RepositoryValidator build() {
      return new RepositoryValidator(this);
    }

    public Builder eventLog(String logFile) {
      this.logFile = logFile;
      return this;
    }

    public Builder inputFile(String inputFilename) throws FileNotFoundException {
      this.inputStream = new FileInputStream(inputFilename);
      return this;
    }

    public Builder inputStream(InputStream inputStream) {
      this.inputStream = inputStream;
      return this;
    }

    public Builder verbose(boolean verbose) {
      this.verbose = verbose;
      return this;
    }
  }

  private final class ErrorListener implements ErrorHandler {


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

      final int filePos = systemId.lastIndexOf('/') + 1;
      final String filePath = baseUrl.getPath() + "/" + systemId.substring(filePos);
      try {
        final URL fileUrl = new URL(baseUrl.getProtocol(), baseUrl.getHost(), filePath);
        resourceAsStream = fileUrl.openStream();
      } catch (final IOException e) {
        // Not a fatal error for schema validation
      }
      return new Input(publicId, systemId, resourceAsStream, baseURI);
    }


    public void setBaseUrl(URL baseUrl) {
      this.baseUrl = baseUrl;
    }

  }


  private static Logger parentLogger = null;

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Execute RepositoryValidator with command line arguments
   * 
   * @param args command line arguments
   * 
   *        <pre>
   * -e &lt;logfile&gt; name of event log
   * -v verbose logging
   * -i &lt;orchestrafile&gt; name of input file; "-i" is optional
   *        </pre>
   * 
   * @throws Exception if the file to validate cannot be found, read, or parsed
   */
  public static void main(String[] args) throws Exception {
    final Builder builder = RepositoryValidator.builder();

    for (int i = 0; i < args.length;) {
      switch (args[i]) {
        case "-e":
          if (i < args.length - 1) {
            builder.eventLog(args[i + 1]);
            i++;
          }
          break;
        case "-v":
          builder.verbose(true);
          break;
        case "-i":
          break;
        default:
          builder.inputFile(args[i]);
      }
      i++;
    }
    final RepositoryValidator validator = builder.build();
    validator.validate();
  }

  private int errors = 0;

  private int fatalErrors = 0;

  private final InputStream inputStream;

  private final File logFile;

  private final boolean verbose;

  private int warnings = 0;

  private RepositoryValidator(Builder builder) {
    this.logFile = builder.logFile != null ? new File(builder.logFile) : null;
    this.verbose = builder.verbose;
    this.inputStream = builder.inputStream;
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

  /**
   * Validate an Orchestra repository file against the XML schema
   *
   * If the validation fails, an exception is thrown. If valid, there is no return.
   *
   * @throws ParserConfigurationException if the XML parser fails due to a configuration error
   * @throws SAXException if XML parser fails or the file is invalid
   * @throws IOException if the XML file cannot be read
   */
  public void validate() throws IOException, ParserConfigurationException, SAXException {

    final Level level = verbose ? Level.DEBUG : Level.ERROR;
    if (logFile != null) {
      parentLogger = LogUtil.initializeFileLogger(logFile.getCanonicalPath(), level, getClass());
    } else {
      parentLogger = LogUtil.initializeDefaultLogger(level, getClass());
    }

    final ErrorListener errorHandler = new ErrorListener();
    final Document xmlDocument = validateSchema(errorHandler);
    validateExpressions(xmlDocument);

    if (getErrors() + getFatalErrors() > 0) {
      parentLogger.fatal("RepositoryValidator complete; fatal errors={} errors={} warnings={}",
          getFatalErrors(), getErrors(), getWarnings());
    } else {
      parentLogger.info("RepositoryValidator complete; fatal errors={} errors={} warnings={}",
          getFatalErrors(), getErrors(), getWarnings());
    }
  }

  private void validateExpressions(Document xmlDocument) {
    XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(new NamespaceContext() {
      @Override
      public String getNamespaceURI(String arg0) {
        if ("fixr".equals(arg0)) {
          return "http://fixprotocol.io/2020/orchestra/repository";
        }
        return null;
      }

      @Override
      public String getPrefix(String arg0) {
        return null;
      }

      @Override
      public Iterator<String> getPrefixes(String arg0) {
        return null;
      }
    });
    String expression = "//fixr:when";
    try {
      NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        Node node = nodeList.item(i);
        short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          String condition = element.getTextContent();
          try {
            Evaluator.validateSyntax(condition);
          } catch (ScoreException exception) {
            parentLogger.error("{}:{} {}", exception.getLineNumber(), exception.getColumnNumber(),
                exception.getMessage());
            errors++;
          }
        }
      }
    } catch (XPathExpressionException e) {
      parentLogger.error(e);
      fatalErrors++;
    }
  }

  private Document validateSchema(ErrorListener errorHandler)
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
    final URL resourceUrl = this.getClass().getClassLoader().getResource("xsd/repository.xsd");
    final String path = resourceUrl.getPath();
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
