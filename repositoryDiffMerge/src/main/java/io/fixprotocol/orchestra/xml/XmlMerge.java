/**
 * Copyright 2017 FIX Protocol Ltd
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
 */
package io.fixprotocol.orchestra.xml;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/**
 * Merges a difference file created by {@link XmlDiff} into a baseline XML file to create a new XML file
 * @author Don Mendelson
 *
 */
public class XmlMerge {
  private class CustomNamespaceContext implements NamespaceContext {
    private final Map<String, String> namespaces = new HashMap<>();

    public CustomNamespaceContext() {
      namespaces.put("xmlns", XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
      namespaces.put("xml", XMLConstants.XML_NS_URI);
      namespaces.put("xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
    }

    public String getNamespaceURI(String prefix) {
      if (prefix == null) {
        throw new NullPointerException("Null prefix");
      }
      String uri = namespaces.get(prefix);
      if (uri != null) {
        return uri;
      } else {
        return XMLConstants.NULL_NS_URI;
      }
    }

    // This method isn't necessary for XPath processing.
    public String getPrefix(String uri) {
      throw new UnsupportedOperationException();
    }

    // This method isn't necessary for XPath processing either.
    public Iterator getPrefixes(String uri) {
      throw new UnsupportedOperationException();
    }

    public void register(String prefix, String uri) {
      namespaces.put(prefix, uri);
    }

  }

  /**
   * Merges a baseline XML file with a differences file to produce a second XML file
   * 
   * @param args three file names: baseline XML file, diff file, name of second XML to produce
   * @throws Exception if an IO or parsing error occurs
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      usage();
    } else {
      XmlMerge tool = new XmlMerge();
      tool.merge(new FileInputStream(args[0]), new FileReader(args[1]), new FileOutputStream(args[2]));
    }
  }

  /**
   * Prints application usage
   */
  public static void usage() {
    System.out.println("Usage: XmlMerge <xml-file1> <diff-file> <xml-file2>");
  }

  private final Pattern addPattern;
  private final Pattern addValuePattern;
  private final Pattern changePattern;
  private final Pattern qualifiedNamePattern;
  private final Pattern removePattern;

  /**
   * Constructor
   */
  public XmlMerge() {
    addPattern = Pattern.compile("^\\+ (.*)");
    addValuePattern = Pattern.compile("^\\+ (.*):=(.*)");
    changePattern = Pattern.compile("^! (.*):=(.*)\\((.*)\\)");
    removePattern = Pattern.compile("^- (.*)\\((.*)\\)");
    qualifiedNamePattern = Pattern.compile("(.*)\\[@(.*)=\\\"(.*)\\\"\\]");
  }

  /**
   * Merges differences into an XML file to produce a new XML file
   * @param baseline XML input stream
   * @param diffReader reads difference file produced by {@link XmlDiff}
   * @param xmlStream XML output
   * @throws Exception if an IO or parser error occurs
   */
  public void merge(InputStream baseline, Reader diffReader, OutputStream xmlStream)
      throws Exception {
    Objects.requireNonNull(baseline, "Baseline stream cannot be null");
    Objects.requireNonNull(diffReader, "Difference file reader cannot be null");
    Objects.requireNonNull(xmlStream, "Output stream cannot be null");

    try (BufferedReader in = new BufferedReader(diffReader)) {
      final Document doc = parse(baseline);
      final Element root = doc.getDocumentElement();
      final NamedNodeMap rootAttributes = root.getAttributes();

      // XPath implementation supplied with Java 8 fails so using Saxon
      final XPathFactory factory = new net.sf.saxon.xpath.XPathFactoryImpl();
      final XPath xpath = factory.newXPath();
      final CustomNamespaceContext nsContext = new CustomNamespaceContext();
      // Namespaces are declared in the root element, but this info needs to be passed to the XPath
      // processor through its own API. Go figure.
      for (int i = 0; i < rootAttributes.getLength(); i++) {
        final Attr attr = (Attr) rootAttributes.item(i);
        if (attr.getPrefix().equals("xmlns")) {
          nsContext.register(attr.getLocalName(), attr.getValue());
        }
      }
      xpath.setNamespaceContext(nsContext);

      String line;
      while ((line = in.readLine()) != null) {
        if (line.length() == 0) {
          continue;
        }
        switch (line.charAt(0)) {
          case '+':
            add(doc, xpath, line);
            break;
          case '-':
            remove(doc, xpath, line);
            break;
          case '!':
            change(doc, xpath, line);
            break;
          default:
            System.err.format("Invalid operation '%c'%n", line.charAt(0));
        }
      }

      write(doc, xmlStream);
    }
  }

  private void add(Document doc, XPath xpath, String line) throws XPathExpressionException {
    Matcher matcher = addValuePattern.matcher(line);
    if (matcher.find()) {
      String expression = matcher.group(1);
      String value = matcher.group(2);
      String parentExpression = expression.substring(0, expression.lastIndexOf('/'));
      Node parent = (Node) xpath.compile(parentExpression).evaluate(doc, XPathConstants.NODE);
      String name = expression.substring(expression.lastIndexOf('/') + 1);
      if (name.startsWith("@")) {
        ((Element) parent).setAttribute(name.substring(1), value);
      } else {
        String elementName = name.substring(0, name.indexOf('['));
        Element child = doc.createElement(elementName);
        if (value != null) {
          Text text = doc.createTextNode(value);
          text.setNodeValue(value);
        }
        parent.appendChild(child);
      }
    } else {
      matcher = addPattern.matcher(line);
      if (matcher.find()) {
        String expression = matcher.group(1);
        String parentExpression = expression.substring(0, expression.lastIndexOf('/'));
        Node parent = (Node) xpath.compile(parentExpression).evaluate(doc, XPathConstants.NODE);
        String qualifiedName = expression.substring(expression.lastIndexOf('/') + 1);
        Matcher matcher2 = qualifiedNamePattern.matcher(qualifiedName);
        if (matcher2.find()) {
          String name = matcher2.group(1);
          String attribute = matcher2.group(2);
          String value = matcher2.group(3);
          Element element = doc.createElement(name);
          element.setAttribute(attribute, value);
          parent.appendChild(element);
        } else {
          String elementName = qualifiedName.substring(0, qualifiedName.indexOf('['));
          Element element = doc.createElement(elementName);
          parent.appendChild(element);
        }
      } else {
        System.err.format("Line did not match add pattern: %s%n", line);
      }
    }
  }

  private void change(final Document doc, XPath xpath, String line)
      throws XPathExpressionException, DOMException {
    Matcher matcher = changePattern.matcher(line);
    if (matcher.find()) {
      String expression = matcher.group(1);
      String value = matcher.group(2);
      Node node = (Node) xpath.compile(expression).evaluate(doc, XPathConstants.NODE);
      switch (node.getNodeType()) {
        case Node.ELEMENT_NODE:
          NodeList children = node.getChildNodes();
          for (int i=0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.TEXT_NODE) {
              children.item(i).setNodeValue(value);
              return;
            }
          }
          
          Text text = doc.createTextNode(value);
          text.setNodeValue(value);
          node.appendChild(text);
          break;
        case Node.ATTRIBUTE_NODE:
          node.setNodeValue(value);
          break;
      }
      
    } else {
      System.err.format("Line did not match change pattern: %s%n", line);
    }
  }

  private Document parse(InputStream is)
      throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    final DocumentBuilder db = dbf.newDocumentBuilder();
    return db.parse(is);
  }

  private void remove(final Document doc, XPath xpath, String line)
      throws XPathExpressionException, DOMException {
    Matcher matcher = removePattern.matcher(line);
    if (matcher.find()) {
      String expression = matcher.group(1);
      Node node = (Node) xpath.compile(expression).evaluate(doc, XPathConstants.NODE);
      if (node != null) {
        Node parent = node.getParentNode();
        if (parent != null) {
          NodeList children = parent.getChildNodes();
          for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) == node) {
              parent.removeChild(node);
              break;
            }
          }
        }
      }
    } else {
      System.err.format("Line did not match remove pattern: %s%n", line);
    }
  }

  private void write(Document document, OutputStream outputStream) throws TransformerException {
    DOMSource source = new DOMSource(document);
    StreamResult result = new StreamResult(outputStream);
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty("indent", "yes");
    transformer.transform(source, result);
  }
}
