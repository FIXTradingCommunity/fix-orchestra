package io.fixprotocol.orchestra.repository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
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

import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.event.ConsoleEventListener;
import io.fixprotocol.orchestra.event.EventListenerFactory;
import io.fixprotocol.orchestra.event.TeeEventListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import io.fixprotocol.orchestra.event.EventListener;

public class RepositoryValidatorImpl {
  
  public RepositoryValidatorImpl(EventListener eventLogger) {
    this.eventLogger = eventLogger;
  }

  private final class ErrorListener implements ErrorHandler {

    @Override
    public void error(SAXParseException exception) throws SAXException {
      eventLogger.error("RepositoryValidator: XML error at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      errors++;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      eventLogger.fatal("RepositoryValidator: XML fatal error at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      fatalErrors++;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      eventLogger.warn("RepositoryValidator: XML warning at line {0} col {1} {2}",
          exception.getLineNumber(), exception.getColumnNumber(), exception.getMessage());
      warnings++;
    }

  }

  static final String REPOSITORY_NAMESPACE = "http://fixprotocol.io/2020/orchestra/repository";

  public static EventListener createLogger(OutputStream jsonOutputStream) {
    final Logger logger = LogManager.getLogger(RepositoryValidatorImpl.class);
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
       logger.error("Error creating event listener", e);
    }
    return eventListener;
  }

  private int errors = 0;
  private final EventListener eventLogger;
  private int fatalErrors = 0;
  static final Predicate<String> isValidChar = t -> t.length() == 1;
  static final Predicate<String> isValidInt = t -> t.chars().allMatch(Character::isDigit);
  static final Predicate<String> isValidString = t -> !t.isEmpty();
  static final Predicate<String> isValidBoolean = t -> t.equals("Y") || t.equals("N");
  
  private static final NamespaceContext nsContext = new NamespaceContext() {
    @Override
    public String getNamespaceURI(String arg0) {
      if ("fixr".equals(arg0)) {
        return REPOSITORY_NAMESPACE;
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
  };
  private int warnings = 0;

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
   * A repository is invalid if a parser encounters unrecoverable errors, such as for non-well
   * formed XML, or if errors are produced that recoverable for the parser but would lead to an
   * invalid conversion. However, if only warnings are produced, the repository is considered valid.
   *
   * @param inputStream input stream of a repository file
   * @param jsonOutputStream output stream for events
   * @param doNotCloseEventLog if {@code true}, do not close the event log so another process can
   *        append
   * @return Returns {@code true} if the repository does not have serious errors, {@code false} if
   *         it does.
   */
  public boolean validate(InputStream inputStream) {
    final ErrorListener errorHandler = new ErrorListener();
    Document xmlDocument;
    try {
      xmlDocument = validateSchema(inputStream, errorHandler);
      validateCodesets(xmlDocument);
      validateExpressions(xmlDocument);
    } catch (final Exception e) {
      eventLogger.fatal("Failed to validate Score expressions, {0}", e.getMessage());
      fatalErrors++;
    }

    if (getErrors() + getFatalErrors() > 0) {
      eventLogger.fatal(
          "RepositoryValidator complete; fatal errors={0,number,integer} errors={1,number,integer} warnings={2,number,integer}",
          getFatalErrors(), getErrors(), getWarnings());
      return false;
    } else {
      eventLogger.info(
          "RepositoryValidator complete; fatal errors={0,number,integer} errors={1,number,integer} warnings={2,number,integer}",
          getFatalErrors(), getErrors(), getWarnings());
      return true;
    }
  }

  private void closeLogger() {
    try {
      eventLogger.close();
    } catch (Exception e) {

    }
  }

  private void validateCodes(NodeList codeElements, Element codesetElement,
      Predicate<String> isCodeValid) {
    for (int i = 0; i < codeElements.getLength(); i++) {
      final Element codeElement = (Element) codeElements.item(i);
      final String value = codeElement.getAttribute("value");
      if (!isCodeValid.test(value)) {
        final String codesetName = codesetElement.getAttribute("name");
        final String datatype = codesetElement.getAttribute("type");
        final String codeName = codeElement.getAttribute("name");
        errors++;
        eventLogger.error(
            "RepositoryValidator: code {0} value {1} is invalid for datatype {2} in codeset {3}",
            codeName, value, datatype, codesetName);
      }
    }
  }

  private void validateCodesets(Document xmlDocument) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(nsContext);
    final String expression = "//fixr:codeSet";
    try {
      final NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node codesetNode = nodeList.item(i);
        final short nodeType = codesetNode.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          final Element codesetElement = (Element) codesetNode;
          final String datatype = codesetElement.getAttribute("type");
          NodeList codeElements =
              codesetElement.getElementsByTagNameNS(REPOSITORY_NAMESPACE, "code");
          switch (datatype) {
            case "int":
            case "NumInGroup":
              validateCodes(codeElements, codesetElement, isValidInt);
              break;
            case "char":
            case "MultipleCharValue":
              validateCodes(codeElements, codesetElement, isValidChar);
              break;
            case "String":
            case "MultipleStringValue":
              validateCodes(codeElements, codesetElement, isValidString);
              break;
            case "Boolean":
              validateCodes(codeElements, codesetElement, isValidBoolean);
              break;
            default:
              errors++;
              final String codesetName = codesetElement.getAttribute("name");
              eventLogger.error("RepositoryValidator: unexpected datatype {0} for code set {1}",
                  datatype, codesetName);
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate Score expressions");
      eventLogger.fatal(e.getMessage());
      fatalErrors++;
    }
  }

  private void validateExpressions(Document xmlDocument) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(nsContext);
    final String expression = "//fixr:when";
    try {
      final NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        final short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          final Element element = (Element) node;
          final String condition = element.getTextContent();
          try {
            Evaluator.validateSyntax(condition);
          } catch (final ScoreException exception) {
            eventLogger.error(
                "RepositoryValidator: invalid Score expression '{0}'; {1} at col. {2}", condition,
                exception.getMessage(), exception.getColumnNumber());
            errors++;
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate Score expressions");
      eventLogger.fatal(e.getMessage());
      fatalErrors++;
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
    final URL resourceUrl = this.getClass().getClassLoader().getResource("xsd/repository.xsd");
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
