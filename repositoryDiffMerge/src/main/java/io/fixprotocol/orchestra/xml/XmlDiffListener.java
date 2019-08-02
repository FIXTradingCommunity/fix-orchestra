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
     * @param difference type of event; must not be null
     * @param xpath node to remove; must not be null
     */
    Event(Difference difference, String xpath) {
      this(difference, xpath, null, null);
    }

    /**
     * Constructor for ADD event
     * 
     * @param difference type of event; must not be null
     * @param xpath parent of the added node; must not be null
     * @param value node value of element or attribute; must not be null
     */
    Event(Difference difference, String xpath, Node value) {
      this(difference, xpath, value, null);
      Objects.requireNonNull(value, "Node to add missing");
    }

    /**
     * Constructor for REPLACE event
     * 
     * @param difference type of event; must not be null
     * @param xpath node target of change; must not be null
     * @param value node new value of element or attribute
     * @param oldValue previous node value
     */
    Event(Difference difference, String xpath, Node value, Node oldValue) {
      Objects.requireNonNull(difference, "Difference type missing");
      Objects.requireNonNull(xpath, "Xpath missing");
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
