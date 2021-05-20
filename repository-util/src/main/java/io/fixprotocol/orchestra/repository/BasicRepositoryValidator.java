package io.fixprotocol.orchestra.repository;

import java.io.ByteArrayInputStream;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import io.fixprotocol.md.event.DocumentParser;
import io.fixprotocol.orchestra.dsl.antlr.Evaluator;
import io.fixprotocol.orchestra.dsl.antlr.ScoreException;
import io.fixprotocol.orchestra.event.EventListener;
import io.fixprotocol.orchestra.event.EventListenerFactory;
import io.fixprotocol.orchestra.event.TeeEventListener;

/**
 * Validates that an Orchestra repository file conforms to the schema but does not apply
 * protocol-specific rules
 * 
 * @author Don Mendelson
 *
 */
public class BasicRepositoryValidator {

  private final class ErrorListener implements ErrorHandler {

    @Override
    public void error(SAXParseException exception) throws SAXException {
      eventLogger.error("RepositoryValidator: XML error at line {0} col {1} {2}",
          intOrUnknown(exception.getLineNumber()), intOrUnknown(exception.getColumnNumber()),
          exception.getMessage());
      errors++;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXException {
      eventLogger.fatal("RepositoryValidator: XML fatal error at line {0} col {1} {2}",
          intOrUnknown(exception.getLineNumber()), intOrUnknown(exception.getColumnNumber()),
          exception.getMessage());
      fatalErrors++;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXException {
      eventLogger.warn("RepositoryValidator: XML warning at line {0} col {1} {2}",
          intOrUnknown(exception.getLineNumber()), intOrUnknown(exception.getColumnNumber()),
          exception.getMessage());
      warnings++;
    }

    private String intOrUnknown(int number) {
      return number == -1 ? "Unk" : Integer.toString(number);
    }

  }


  static final String REPOSITORY_NAMESPACE = "http://fixprotocol.io/2020/orchestra/repository";
  
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

  public static EventListener createLogger(OutputStream jsonOutputStream) {
    final Logger logger = LogManager.getLogger(BasicRepositoryValidator.class);
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

  protected Predicate<String> isValidBoolean = t -> true;
  protected Predicate<String> isValidChar = t -> t.length() == 1;
  protected Predicate<String> isValidInt = t -> t.chars().allMatch(Character::isDigit);
  protected Predicate<String> isValidName = t -> true;
  protected Predicate<String> isValidString = t -> true;

  private int errors = 0;
  private final EventListener eventLogger;
  private int fatalErrors = 0;
  private int warnings = 0;

  public BasicRepositoryValidator(EventListener eventLogger) {
    this.eventLogger = eventLogger;
  }

  public int error() {
    return errors++;
  }

  public int fatalError() {
    return fatalErrors++;
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
   * A repository is invalid if a parser encounters unrecoverable errors, such as for non-well
   * formed XML, or if errors are produced that recoverable for the parser but would lead to an
   * invalid conversion. However, if only warnings are produced, the repository is considered valid.
   *
   * @param inputStream input stream of a repository file
   * @return Returns {@code true} if the repository does not have serious errors, {@code false} if
   *         it does.
   */
  public boolean validate(InputStream inputStream) {
    final ErrorListener errorHandler = new ErrorListener();
    Document xmlDocument;
    try {
      xmlDocument = validateSchema(inputStream, errorHandler);
      validateFields(xmlDocument);
      validateCodesets(xmlDocument);
      validateComponents(xmlDocument);
      validateGroups(xmlDocument);
      validateMessages(xmlDocument);
      validateExpressions(xmlDocument);
      validateDocumentation(xmlDocument);
    } catch (final Exception e) {
      eventLogger.fatal("Failed to validate, {0}", e.getMessage());
      fatalError();
    }

    if (getFatalErrors() > 0) {
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

  public int warning() {
    return warnings++;
  }

  protected void validateCodes(NodeList codeElements, Element codesetElement,
      Predicate<String> isCodeValid) {
    for (int i = 0; i < codeElements.getLength(); i++) {
      final Element codeElement = (Element) codeElements.item(i);
      final String value = codeElement.getAttribute("value");
      final String codeName = codeElement.getAttribute("name");
      final String codesetName = codesetElement.getAttribute("name");
      final String codesetId = codesetElement.getAttribute("id");

      if (!isValidName.test(codeName)) {
        warning();
        eventLogger.warn(
            "RepositoryValidator: code name {0} has invalid case in codeset {1} (id={2})", codeName,
            codesetName, codesetId);

      }
      if (!isCodeValid.test(value)) {
        final String datatype = codesetElement.getAttribute("type");
        error();
        eventLogger.error(
            "RepositoryValidator: code {0} value [{1}] is invalid for datatype {2} in codeset {3} (id={4})",
            codeName, value, datatype, codesetName, codesetId);
      }
    }
  }

  protected void validateCodesets(Document xmlDocument) {
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
          final String codesetName = codesetElement.getAttribute("name");
          final String codesetId = codesetElement.getAttribute("id");
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
              error();
              eventLogger.error(
                  "RepositoryValidator: unexpected datatype {0} for code set {1} (id={2})",
                  datatype, codesetName, codesetId);
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate codesets");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }
  
  protected void validateComponents(Document xmlDocument) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(nsContext);
    final String expression = "//fixr:component";
    try {
      final NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        final short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          final Element element = (Element) node;
          final String name = element.getAttribute("name");
          final String id = element.getAttribute("id");
          final String abbrName = element.getAttribute("abbrName");
          if (!isValidName.test(name)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: component name {0} is invalid (id={1})", name, id);
          }
          if (abbrName != null && !isValidName.test(abbrName)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: component abbrName {0} is invalid (id={1})", abbrName, id);
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate components");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }

  protected void validateDocumentation(Document xmlDocument) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(nsContext);
    final String expression = "//fixr:documentation";
    try {
      final NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        final short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          final Element element = (Element) node;
          final String document = element.getTextContent();
          if (document.isBlank()) {
            Node parent = element.getParentNode();
            if (parent != null) {
              Node grandParent = parent.getParentNode();
              eventLogger.warn("RepositoryValidator: empty documentation at element '{0}'",
                  grandParent.getTextContent());
              warning();
            }
          } else {
            final String contentType = element.getAttribute("contentType");
            if ("text/markdown".equals(contentType)) {
              validateMarkdown(document);
            }
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate markdown documentation");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }
  
  protected void validateExpressions(Document xmlDocument) {
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
            error();
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate Score expressions");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }
  
  protected void validateFields(Document xmlDocument) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(nsContext);
    final String expression = "//fixr:field";
    try {
      final NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        final short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          final Element element = (Element) node;
          final String name = element.getAttribute("name");
          final String id = element.getAttribute("id");
          final String abbrName = element.getAttribute("abbrName");
          if (!isValidName.test(name)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: field name {0} is invalid (id={1})", name, id);

          }
          if (abbrName != null && !isValidName.test(abbrName)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: field abbrName {0} is invalid (id={1})", abbrName, id);
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate fields");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }
  
  protected void validateGroups(Document xmlDocument) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(nsContext);
    final String expression = "//fixr:group";
    try {
      final NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        final short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          final Element element = (Element) node;
          final String name = element.getAttribute("name");
          final String id = element.getAttribute("id");
          final String abbrName = element.getAttribute("abbrName");
          if (!isValidName.test(name)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: group name {0} is invalid (id={1})", name, id);

          }
          if (abbrName != null && !isValidName.test(abbrName)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: group abbrName {0} is invalid (id={1})", abbrName, id);
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate groups");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }

  protected void validateMessages(Document xmlDocument) {
    final XPath xPath = XPathFactory.newInstance().newXPath();
    xPath.setNamespaceContext(nsContext);
    final String expression = "//fixr:message";
    try {
      final NodeList nodeList =
          (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
      for (int i = 0; i < nodeList.getLength(); i++) {
        final Node node = nodeList.item(i);
        final short nodeType = node.getNodeType();
        if (nodeType == Node.ELEMENT_NODE) {
          final Element element = (Element) node;
          final String name = element.getAttribute("name");
          final String id = element.getAttribute("id");
          final String abbrName = element.getAttribute("abbrName");
          if (!isValidName.test(name)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: message name {0} is invalid (id={1})", name, id);
          }
          if (abbrName != null && !isValidName.test(abbrName)) {
            warning();
            eventLogger.warn(
                "RepositoryValidator: message abbrName {0} is invalid (id={1})", abbrName, id);
          }
        }
      }
    } catch (final XPathExpressionException e) {
      eventLogger.error("Failed to locate groups");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }
  
  protected Document validateSchema(InputStream inputStream, ErrorListener errorHandler)
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


  private void validateMarkdown(final String document) {
    try {
      DocumentParser parser = new DocumentParser();
      parser.validate(new ByteArrayInputStream(document.getBytes()),
          (line, charPositionInLine, msg) -> {
            eventLogger.error(
                "RepositoryValidator: invalid markdown documentation '{0}'; {1} at position {2}",
                document, msg, charPositionInLine);
            error();
          });
    } catch (IOException e) {
      eventLogger.error("Failed to parse markdown documentation");
      eventLogger.fatal(e.getMessage());
      fatalError();
    }
  }

}

