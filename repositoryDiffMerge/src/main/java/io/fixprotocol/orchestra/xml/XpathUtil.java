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

import java.util.Stack;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public final class XpathUtil {

  /**
   * Utility to determine the XPATH of an XML node
   * @param n an XML node
   * @return XPATH representation
   * @author Adapted from a post by Mikkel Flindt Heisterberg
   * @see <a href=http://lekkimworld.com/2007/06/19/building_xpath_expression_from_xml_node.html>Building XPath expression from XML node</a>
   */
  public static String getFullXPath(Node n) {
    // abort early
    if (null == n)
      return null;

    // declarations
    Node parent = null;
    Stack<Node> hierarchy = new Stack<Node>();
    StringBuffer buffer = new StringBuffer();

    // push element on stack
    hierarchy.push(n);

    switch (n.getNodeType()) {
      case Node.ATTRIBUTE_NODE:
        parent = ((Attr) n).getOwnerElement();
        break;
      case Node.ELEMENT_NODE:
        parent = n.getParentNode();
        break;
      case Node.DOCUMENT_NODE:
        parent = n.getParentNode();
        break;
      default:
        throw new IllegalStateException("Unexpected Node type" + n.getNodeType());
    }

    while (null != parent && parent.getNodeType() != Node.DOCUMENT_NODE) {
      // push on stack
      hierarchy.push(parent);

      // get parent of parent
      parent = parent.getParentNode();
    }

    // construct xpath
    Object obj = null;
    while (!hierarchy.isEmpty() && null != (obj = hierarchy.pop())) {
      Node node = (Node) obj;
      boolean handled = false;

      if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element e = (Element) node;

        // is this the root element?
        if (buffer.length() == 0) {
          // root element - simply append element name
          buffer.append(node.getNodeName());
        } else {
          // child element - append slash and element name
          buffer.append("/");
          buffer.append(node.getNodeName());

          if (node.hasAttributes()) {
            // see if the element has a name or id attribute
            if (e.hasAttribute("id")) {
              // id attribute found - use that
              buffer.append("[@id=\"" + e.getAttribute("id") + "\"]");
              handled = true;
            } else if (e.hasAttribute("name")) {
              // name attribute found - use that
              buffer.append("[@name=\"" + e.getAttribute("name") + "\"]");
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
            buffer.append("[" + prev_siblings + "]");
          }
        }
      } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
        buffer.append("/@");
        buffer.append(node.getNodeName());
      }
    }
    // return buffer
    return buffer.toString();
  }
}
