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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

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
 * Merges a difference file created by {@link XmlDiff} into a baseline XML file to create a new XML
 * file
 * 
 * Reads XML diffs as patch operations specified by IETF RFC 5261
 * 
 * @author Don Mendelson
 * 
 * @see <a href="https://tools.ietf.org/html/rfc5261">An Extensible Markup Language (XML) Patch
 *      Operations Framework Utilizing XML Path Language (XPath) Selectors</a>
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
   * 
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 3) {
      usage();
    } else {
      XmlMerge tool = new XmlMerge();
      tool.merge(new FileInputStream(args[0]), new FileInputStream(args[1]),
          new FileOutputStream(args[2]));
    }
  }

  /**
   * Prints application usage
   */
  public static void usage() {
    System.out.println("Usage: XmlMerge <xml-file1> <diff-file> <xml-file2>");
  }

  /**
   * Merges differences into an XML file to produce a new XML file
   * 
   * @param baseline XML input stream
   * @param diffReader reads difference file produced by {@link XmlDiff}
   * @param out XML output
   * @throws Exception if an IO or parser error occurs
   */
  public void merge(InputStream baseline, InputStream diff, OutputStream out) throws Exception {
    Objects.requireNonNull(baseline, "Baseline stream cannot be null");
    Objects.requireNonNull(diff, "Difference stream cannot be null");
    Objects.requireNonNull(out, "Output stream cannot be null");

    final Document baselineDoc = parse(baseline);
    final Element baselineRoot = baselineDoc.getDocumentElement();
    final NamedNodeMap rootAttributes = baselineRoot.getAttributes();

    // XPath implementation supplied with Java 8 fails so using Saxon
    final XPathFactory factory = new net.sf.saxon.xpath.XPathFactoryImpl();
    final XPath xpathEvaluator = factory.newXPath();
    final CustomNamespaceContext nsContext = new CustomNamespaceContext();
    // Namespaces are declared in the root element, but this info needs to be passed to the XPath
    // processor through its own API. Go figure.
    for (int i = 0; i < rootAttributes.getLength(); i++) {
      final Attr attr = (Attr) rootAttributes.item(i);
      if (attr.getPrefix().equals("xmlns")) {
        nsContext.register(attr.getLocalName(), attr.getValue());
      }
    }
    xpathEvaluator.setNamespaceContext(nsContext);

    final Document diffDoc = parse(diff);
    final Element diffRoot = diffDoc.getDocumentElement();
    NodeList children = diffRoot.getChildNodes();
    for (int index = 0; index < children.getLength(); index++) {
      Node child = children.item(index);
      short type = child.getNodeType();
      if (type != Node.ELEMENT_NODE) {
        continue;
      }
      Element patchOpElement = (Element) child;
      String tag = patchOpElement.getNodeName();

      switch (tag) {
        case "add":
          add(baselineDoc, xpathEvaluator, patchOpElement);
          break;
        case "remove":
          remove(baselineDoc, xpathEvaluator, patchOpElement);
          break;
        case "replace":
          replace(baselineDoc, xpathEvaluator, patchOpElement);
          break;
        default:
          System.err.format("Invalid operation '%s'%n", tag);
      }
    }

    write(baselineDoc, out);
  }

  private void add(Document doc, XPath xpathEvaluator, Element patchOpElement)
      throws XPathExpressionException {
    String xpathExpression = patchOpElement.getAttribute("sel");
    String attribute = patchOpElement.getAttribute("type");

    String parentExpression = xpathExpression.substring(0, xpathExpression.lastIndexOf('/'));
    Node parent =
        (Node) xpathEvaluator.compile(parentExpression).evaluate(doc, XPathConstants.NODE);

    if (attribute.length() > 0) {
      String value = null;
      NodeList children = patchOpElement.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (Node.TEXT_NODE == child.getNodeType()) {
          value = patchOpElement.getNodeValue();
          break;
        }
      }
      ((Element) parent).setAttribute(attribute.substring(1), value);
    } else {
      Element value = null;
      NodeList children = patchOpElement.getChildNodes();
      for (int i = 0; i < children.getLength(); i++) {
        Node child = children.item(i);
        if (Node.ELEMENT_NODE == child.getNodeType()) {
          value = (Element) child;
          break;
        }
      }
      Node imported = doc.importNode(value, true);
      parent.appendChild(imported);
    }

  }

  private Document parse(InputStream is)
      throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    final DocumentBuilder db = dbf.newDocumentBuilder();
    return db.parse(is);
  }

  private void remove(final Document doc, XPath xpathEvaluator, Element patchOpElement)
      throws XPathExpressionException, DOMException {
    String xpathExpression = patchOpElement.getAttribute("sel");
    Node node = (Node) xpathEvaluator.compile(xpathExpression).evaluate(doc, XPathConstants.NODE);
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
  }

  private void replace(final Document doc, XPath xpathEvaluator, Element patchOpElement)
      throws XPathExpressionException, DOMException {
    String xpathExpression = patchOpElement.getAttribute("sel");
    String value = patchOpElement.getFirstChild().getNodeValue();

    Node node = (Node) xpathEvaluator.compile(xpathExpression).evaluate(doc, XPathConstants.NODE);
    switch (node.getNodeType()) {
      case Node.ELEMENT_NODE:
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
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
