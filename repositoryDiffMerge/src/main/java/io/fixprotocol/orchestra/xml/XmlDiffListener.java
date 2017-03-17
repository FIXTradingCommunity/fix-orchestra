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

import java.util.function.Consumer;

import org.w3c.dom.Node;

/**
 * Event handler for {@link XmlDiff}
 * 
 * @author Don Mendelson
 *
 */
public interface XmlDiffListener extends Consumer<XmlDiffListener.Event>, AutoCloseable {

  /**
   * XML difference event
   *
   */
  class Event {
    /**
     * Type of XML difference
     */
    enum Difference {
      ADD, EQUAL, REMOVE, REPLACE
    }

    private final Difference difference;
    private final Node oldValue;
    private final Node value;
    private final String xpath;

    /**
     * Constructor for REMOVE event
     * 
     * @param difference type of event
     * @param xpath node to remove
     */
    Event(Difference difference, String xpath) {
      this(difference, xpath, null, null);
    }

    /**
     * Constructor for ADD event
     * 
     * @param difference type of event
     * @param xpath parent of the added node
     * @param value node value of element or attribute
     */
    Event(Difference difference, String xpath, Node value) {
      this(difference, xpath, value, null);
    }

    /**
     * Constructor for REPLACE event
     * 
     * @param difference type of event
     * @param xpath node to replace
     * @param value node new value of element or attribute
     * @param oldValue previous node value
     */
    Event(Difference difference, String xpath, Node value, Node oldValue) {
      this.difference = difference;
      this.xpath = xpath;
      this.value = value;
      this.oldValue = oldValue;
    }

    Difference getDifference() {
      return difference;
    }

    Node getOldValue() {
      return oldValue;
    }

    Node getValue() {
      return value;
    }

    String getXpath() {
      return xpath;
    }

  }

}
