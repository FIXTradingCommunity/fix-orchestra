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

import java.util.Objects;
import java.util.Stack;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * @author Adapted from a post by Mikkel Flindt Heisterberg
 * @see <a
 *      href=http://lekkimworld.com/2007/06/19/building_xpath_expression_from_xml_node.html>Building
 *      XPath expression from XML node</a>

 */
public final class XpathUtil {

  public static String getAttribute(String xpath) {
    Objects.requireNonNull(xpath, "Xpath cannot be null");
    String[] fields = xpath.split("/");
    if (fields == null || fields.length == 0) {
      return "";
    }
    if (fields[fields.length - 1].startsWith("@")) {
      return fields[fields.length - 1].substring(1);
    } else {
      return "";
    }
  }

  public static String getElementLocalName(String xpath) {
    Objects.requireNonNull(xpath, "Xpath cannot be null");
    String[] fields = xpath.split("/");
    if (fields == null || fields.length == 0) {
      return "";
    }
    for (int i = fields.length - 1; i >= 0; i--) {
      if (fields[i].startsWith("@")) {
        continue;
      }
      int colon = fields[i].indexOf(":") + 1;
      int bracket = fields[i].indexOf("[");
      return fields[i].substring(colon, bracket == -1 ? fields[i].length() : bracket);
    }
    return "";
  }
  
  public static String getElementPredicate(String xpath) {
    Objects.requireNonNull(xpath, "Xpath cannot be null");
    String[] fields = xpath.split("/");
    if (fields == null || fields.length == 0) {
      return "";
    }
    for (int i = fields.length - 1; i >= 0; i--) {
      if (fields[i].startsWith("@")) {
        continue;
      }
      int open = fields[i].indexOf("=");
      int close = fields[i].indexOf("]");
      if (open == -1 || close == -1) {
        return "";
      }
      // skip over quote
      return fields[i].substring(open + 2, close - 1);
    }
    return "";
  }

  /**
   * Utility to determine the XPATH of an XML node
   * 
   * @param n an XML node
   * @return XPATH representation
   */
  public static String getFullXPath(Node n) {
    Objects.requireNonNull(n, "Node cannot be null");

    // declarations
    Node parent = null;
    Stack<Node> hierarchy = new Stack<>();
    StringBuilder buffer = new StringBuilder();

    // push element on stack
    hierarchy.push(n);

    switch (n.getNodeType()) {
      case Node.ATTRIBUTE_NODE:
        parent = ((Attr) n).getOwnerElement();
        break;
      case Node.ELEMENT_NODE:
      case Node.DOCUMENT_NODE:
        parent = n.getParentNode();
        break;
      default:
        throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
    }

    while (null != parent) {
      // push on stack
      hierarchy.push(parent);

      // get parent of parent
      parent = parent.getParentNode();
    }

    // construct xpath
    Node node = null;
    while (!hierarchy.isEmpty() && null != (node = hierarchy.pop())) {
      boolean handled = false;

      switch (node.getNodeType()) {
        case Node.DOCUMENT_NODE:
          buffer.append("/");
          break;
        case Node.ELEMENT_NODE:
          Element e = (Element) node;

          // is this the root element?
          if ("/".equals(buffer.toString())) {
            buffer.append(getQualifiedNodeName(node));
          } else {
            // child element - append slash and element name
            buffer.append("/");
            buffer.append(getQualifiedNodeName(node));

            if (node.hasAttributes()) {
              // prioritize name over id as predicate
              Attr key = getAttributeCaseInsensitive(e, "name");
              if (key == null) {
                key = getAttributeCaseInsensitive(e, "id");
              }
              if (key != null) {
                buffer.append("[@").append(key.getNodeName()).append("=\"").append(key.getValue())
                    .append("\"]");
                handled = true;
              }
            }

            if (!handled) {
              // no known attribute we could use - get sibling index
              int prev_siblings = 1;
              Node prev_sibling = node.getPreviousSibling();
              while (null != prev_sibling) {
                if (prev_sibling.getNodeType() == node.getNodeType()) {
                  if (prev_sibling.getNodeName().equalsIgnoreCase(node.getNodeName())) {
                    prev_siblings++;
                  }
                }
                prev_sibling = prev_sibling.getPreviousSibling();
              }
              buffer.append("[").append(prev_siblings).append("]");
            }
          }
          break;
        case Node.ATTRIBUTE_NODE:
          buffer.append("/@");
          buffer.append(node.getNodeName());
          break;
      }
    }
    // return buffer
    return buffer.toString();
  }

  static String getQualifiedNodeName(Node node) {
    String prefix = node.getPrefix();
    String uri = node.getNamespaceURI();
    if (prefix == null && uri != null) {
      StringBuilder buffer = new StringBuilder();
      buffer.append("Q{");
      buffer.append(uri);
      buffer.append("}");
      buffer.append(node.getLocalName());
      return buffer.toString();
    } else {
    return node.getNodeName();
    }
  }

  public static String getParentLocalName(String xpath) {
    Objects.requireNonNull(xpath, "Xpath cannot be null");
    String[] fields = xpath.split("/");
    if (fields == null || fields.length == 0) {
      return "";
    }
    boolean elementFound = false;
    for (int i = fields.length - 1; i >= 0; i--) {
      if (fields[i].startsWith("@")) {
        continue;
      }
      if (!elementFound) {
        elementFound = true;
        continue;
      }
      int colon = fields[i].indexOf(":") + 1;
      int bracket = fields[i].indexOf("[");
      return fields[i].substring(colon, bracket == -1 ? fields[i].length() : bracket);
    }
    return "";
  }

  public static String getParentPredicate(String xpath) {
    Objects.requireNonNull(xpath, "Xpath cannot be null");
    String[] fields = xpath.split("/");
    if (fields == null || fields.length == 0) {
      return "";
    }
    boolean elementFound = false;
    for (int i = fields.length - 1; i >= 0; i--) {
      if (fields[i].startsWith("@")) {
        continue;
      }
      int open = fields[i].indexOf("=");
      int close = fields[i].indexOf("]");
      if (open == -1 || close == -1) {
        continue;
      }
      if (!elementFound) {
        elementFound = true;
        continue;
      }
      // skip over quote
      return fields[i].substring(open + 2, close - 1);
    }
    return "";
  }
  
  public static boolean isAttribute(String xpath) {
    Objects.requireNonNull(xpath, "Xpath cannot be null");
    String[] fields = xpath.split("/");
    return !(fields == null || fields.length == 0) && fields[fields.length - 1].startsWith("@");
  }
  
  
  public static Attr getAttributeCaseInsensitive(Element element, String attributeName) {
    NamedNodeMap attributes = element.getAttributes();
    for (int i =0; i < attributes.getLength(); i++) {
      Node attribute = attributes.item(i);
      if (attribute.getNodeName().equalsIgnoreCase(attributeName)) {
        return (Attr)attribute;
      }
    }
    
    return null;
  }

  public static String getAttributeCaseInsensitiveValue(Element element, String attributeName) {
    Attr attr = getAttributeCaseInsensitive(element, attributeName);
    if (attr != null) {
      return attr.getValue();
    } else {
      return "";
    }
  }

}
