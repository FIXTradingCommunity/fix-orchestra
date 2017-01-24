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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import io.fixprotocol.orchestra.xml.XmlDiffListener.Event;
import io.fixprotocol.orchestra.xml.XmlDiffListener.Event.Difference;

import static io.fixprotocol.orchestra.xml.XmlDiffListener.Event.Difference.*;

/**
 * Utility to report differences between two XML files
 * <p>
 * It does not require XML schemas. Only one assumption is made: the key identifying attribute of an
 * XML element is named either "id" or "name". This should work for the majority of XML files since
 * that is common practice.
 * 
 * @author Don Mendelson
 *
 */
public class XmlDiff {

  public class DefaultListener implements XmlDiffListener {

    @Override
    public void accept(Event t) {
      switch (t.getDifference()) {
        case ADD:
          out.format("+ %s%s%n", t.getName(),
              t.getValue() != null && t.getValue().length() > 0 ? ":=" + t.getValue() : "");
          break;
        case CHANGE:
          out.format("! %s:=%s(%s)%n", t.getName(), t.getValue(), t.getOldValue());
          break;
        case REMOVE:
          out.format("- %s%s%n", t.getName(), t.getValue() != null ? "(" + t.getValue() + ")" : "");
          break;
        case EQUAL:
          break;
      }
    }


    @Override
    public void close() throws Exception {

    }

  }

  private static final class ElementComparator implements Comparator<Element> {
    @Override
    public int compare(Element n1, Element n2) {
      int retv = n1.getNodeName().compareTo(n2.getNodeName());
      if (retv == 0) {
        String id1 = n1.getAttribute("id");
        String id2 = n2.getAttribute("id");
        if (id1.length() > 0 && id2.length() > 0) {
          retv = id1.compareTo(id2);
        }
        if (retv == 0) {
          String name1 = n1.getAttribute("name");
          String name2 = n2.getAttribute("name");
          if (name1.length() > 0 && name2.length() > 0) {
            retv = name2.compareTo(name2);
          }
        }
      }
      return retv;
    }
  }

  /**
   * Compares two XML files. By default, report is sent to console.
   * 
   * @param args file names of two XML files to compare and optional name of difference file. If
   *        diff file is not provided, then output goes to console.
   * @throws Exception if an IO or parsing error occurs
   */
  public static void main(String[] args) throws Exception {
    if (args.length < 2) {
      usage();
    } else {
      XmlDiff tool = new XmlDiff();
      tool.diff(new FileInputStream(args[0]), new FileInputStream(args[1]),
          args.length > 2 ? new PrintStream(args[2]) : System.out);
    }
  }

  /**
   * Prints application usage
   */
  public static void usage() {
    System.out.println("Usage: XmlDiff <xml-file1> <xml-file2> [diff-file]");
  }

  private final ArrayList<Attr> attributesArray1 = new ArrayList<>(64);
  private final ArrayList<Attr> attributesArray2 = new ArrayList<>(64);
  private final ElementComparator elementComparator = new ElementComparator();
  private XmlDiffListener listener;
  protected PrintStream out;

  public XmlDiff() {
    setListener(new DefaultListener());
  }

  /**
   * Generates differences between two XML files
   * 
   * @param is1 first XML input stream
   * @param is2 second XML input stream
   * @param diffStream output stream for differences
   * @throws Exception if an IO or parsing error occurs
   */
  public void diff(InputStream is1, InputStream is2, PrintStream diffStream) throws Exception {
    Objects.requireNonNull(is1, "First input stream cannot be null");
    Objects.requireNonNull(is2, "Second input stream cannot be null");
    Objects.requireNonNull(diffStream, "Difference stream cannot be null");
    
    try {
      this.out = diffStream;
      final Document doc1 = parse(is1);
      final Element root1 = doc1.getDocumentElement();
      final Document doc2 = parse(is2);
      final Element root2 = doc2.getDocumentElement();

      if (!compareElements(root1, root2)) {
        System.err.format("Not comparing same root nodes; %s %s%n", XpathUtil.getFullXPath(root1),
            XpathUtil.getFullXPath(root2));
        System.exit(1);
      }
    } finally {
      listener.close();
      is1.close();
      is2.close();
      diffStream.close();
    }

  }

  /**
   * Registers a listener for ontology differences. If one is not registered, a default listener
   * sends reports to the console.
   * 
   * @param listener a listener
   */
  public void setListener(XmlDiffListener listener) {
    this.listener = listener;
  }

  private void addElement(Element element) throws DOMException {
    String text = null;
    Node child = element.getFirstChild();
    if (child != null && Node.TEXT_NODE == child.getNodeType()) {
      text = child.getNodeValue().trim();
    }
    listener.accept(new Event(ADD, XpathUtil.getFullXPath(element), text));
    NamedNodeMap attributes = element.getAttributes();
    for (int i = 0; i < attributes.getLength(); i++) {
      listener.accept(new Event(ADD, XpathUtil.getFullXPath(attributes.item(i)),
          attributes.item(i).getNodeValue()));
    }
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (Node.ELEMENT_NODE == children.item(i).getNodeType()) {
        addElement((Element) children.item(i));
      }
    }
  }

  private boolean compareAttributes(NamedNodeMap attributes1, NamedNodeMap attributes2) {
    boolean isEqual = true;
    sortAttributes(attributes1, attributesArray1);
    sortAttributes(attributes2, attributesArray2);

    int index1 = 0;
    int index2 = 0;

    while (index1 < attributesArray1.size() || index2 < attributesArray2.size()) {
      Difference difference = EQUAL;
      if (index1 == attributesArray1.size()) {
        difference = ADD;
      } else if (index2 == attributesArray2.size()) {
        difference = REMOVE;
      } else {
        int nameCompare = attributesArray1.get(index1).getNodeName()
            .compareTo(attributesArray2.get(index2).getNodeName());
        if (nameCompare == 0) {
          int valueCompare = attributesArray1.get(index1).getNodeValue()
              .compareTo(attributesArray2.get(index2).getNodeValue());
          if (valueCompare != 0) {
            difference = CHANGE;
          }
        } else if (nameCompare > 0) {
          difference = ADD;
        } else {
          difference = REMOVE;
        }
      }

      switch (difference) {
        case ADD:
          listener.accept(new Event(ADD, XpathUtil.getFullXPath(attributesArray2.get(index2)),
              attributesArray2.get(index2).getNodeValue()));
          index2 = Math.min(index2 + 1, attributesArray2.size());
          isEqual = false;
          break;
        case CHANGE:
          listener.accept(new Event(CHANGE, XpathUtil.getFullXPath(attributesArray1.get(index1)),
              attributesArray2.get(index2).getNodeValue(),
              attributesArray1.get(index1).getNodeValue()));
          index1 = Math.min(index1 + 1, attributesArray1.size());
          index2 = Math.min(index2 + 1, attributesArray2.size());
          isEqual = false;
          break;
        case EQUAL:
          index1 = Math.min(index1 + 1, attributesArray1.size());
          index2 = Math.min(index2 + 1, attributesArray2.size());
          break;
        case REMOVE:
          listener.accept(new Event(REMOVE, XpathUtil.getFullXPath(attributesArray1.get(index1)),
              attributesArray1.get(index1).getNodeValue()));
          index1 = Math.min(index1 + 1, attributesArray1.size());
          isEqual = false;
          break;
      }
    }
    return isEqual;
  }

  private boolean compareChildElements(Element element1, Element element2) {
    NodeList nodeList1 = element1.getChildNodes();
    NodeList nodeList2 = element2.getChildNodes();

    ArrayList<Element> elementsArray1 = new ArrayList<>(64);
    ArrayList<Element> elementsArray2 = new ArrayList<>(64);
    sortElements(nodeList1, elementsArray1);
    sortElements(nodeList2, elementsArray2);

    int index1 = 0;
    int index2 = 0;

    boolean isEqual = true;
    while (index1 < elementsArray1.size() || index2 < elementsArray2.size()) {
      Difference difference = EQUAL;
      if (index1 == elementsArray1.size()) {
        difference = ADD;
      } else if (index2 == elementsArray2.size()) {
        difference = REMOVE;
      } else {
        final Element child1 = elementsArray1.get(index1);
        final Element child2 = elementsArray2.get(index2);
        int elementCompare = elementComparator.compare(child1, child2);
        if (elementCompare == 0) {
          compareElements(child1, child2);
          index1 = Math.min(elementsArray1.size() - 1, index1 + 1);
          index2 = Math.min(elementsArray2.size() - 1, index2 + 1);
        } else if (elementCompare > 0) {
          difference = ADD;
        } else {
          difference = REMOVE;
        }
      }
      switch (difference) {
        case ADD:
          addElement(elementsArray2.get(index2));
          index2 = Math.min(index2 + 1, elementsArray2.size());
          isEqual = false;
          break;
        case CHANGE:
          listener.accept(new Event(CHANGE, XpathUtil.getFullXPath(elementsArray1.get(index1)),
              elementsArray2.get(index2).getNodeValue(),
              elementsArray1.get(index1).getNodeValue()));
          index1 = Math.min(index1 + 1, elementsArray1.size());
          index2 = Math.min(index2 + 1, elementsArray2.size());
          isEqual = false;
          break;
        case EQUAL:
          index1 = Math.min(index1 + 1, elementsArray1.size());
          index2 = Math.min(index2 + 1, elementsArray2.size());
          break;
        case REMOVE:
          listener.accept(new Event(REMOVE, XpathUtil.getFullXPath(elementsArray1.get(index1)),
              elementsArray1.get(index1).getNodeValue()));
          index1 = Math.min(index1 + 1, elementsArray1.size());
          isEqual = false;
          break;
      }
    }

    return isEqual;
  }

  private boolean compareElements(final Element child1, final Element child2) {
    if (!child1.getNodeName().equals(child2.getNodeName())) {
      return false;
    }
    compareText(child1, child2);
    NamedNodeMap attributes1 = child1.getAttributes();
    NamedNodeMap attributes2 = child2.getAttributes();
    compareAttributes(attributes1, attributes2);
    compareChildElements(child1, child2);
    return true;
  }

  private boolean compareText(Element element1, Element element2) {
    Node child1 = element1.getFirstChild();
    Node child2 = element2.getFirstChild();

    if (child1 != null && Node.TEXT_NODE == child1.getNodeType()) {
      if (child2 == null || Node.TEXT_NODE != child2.getNodeType()) {
        listener.accept(new Event(REMOVE, XpathUtil.getFullXPath(element1), child1.getNodeValue()));
      } else {
        int valueCompare = child1.getNodeValue().trim().compareTo(child2.getNodeValue().trim());

        if (valueCompare != 0) {
          listener.accept(new Event(CHANGE, XpathUtil.getFullXPath(element1), child2.getNodeValue(),
              child1.getNodeValue()));
        } else {
          return true;
        }
      }
    } else if (child2 != null && Node.TEXT_NODE == child2.getNodeType()) {
      listener.accept(new Event(ADD, XpathUtil.getFullXPath(element2), child2.getNodeValue()));
    }
    return false;
  }

  private Document parse(InputStream is)
      throws ParserConfigurationException, SAXException, IOException {
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    final DocumentBuilder db = dbf.newDocumentBuilder();
    return db.parse(is);
  }

  private ArrayList<Attr> sortAttributes(NamedNodeMap attributes, ArrayList<Attr> nodeArray) {
    nodeArray.clear();

    for (int i = 0; i < attributes.getLength(); i++) {
      nodeArray.add((Attr) attributes.item(i));
    }
    nodeArray.sort((n1, n2) -> n1.getNodeName().compareTo(n2.getNodeName()));
    return nodeArray;
  }

  private ArrayList<Element> sortElements(NodeList nodeList, ArrayList<Element> nodeArray) {
    nodeArray.clear();

    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (Node.ELEMENT_NODE == node.getNodeType()) {
        nodeArray.add((Element) node);
      }
    }
    nodeArray.sort(elementComparator);
    return nodeArray;
  }
}
