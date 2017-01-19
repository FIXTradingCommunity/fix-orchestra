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

/**
 * Event handler for {@link XmlDiff}
 * @author Don Mendelson
 *
 */
public interface XmlDiffListener extends Consumer<XmlDiffListener.Event> {

  /**
   * XML difference event
   *
   */
  public class Event {
    /**
     * Type of XML difference
     */
    enum Difference {
      ADD, CHANGE, REMOVE, EQUAL
    }

    private final Difference difference;
    private final String name;
    private final String oldValue;
    private final String value;

    /**
     * Constructor for ADD, REMOVE, EQUAL event
     * @param difference
     * @param name node name
     * @param value node value
     */
    public Event(Difference difference, String name, String value) {
      this.difference = difference;
      this.name = name;
      this.value = value;
      this.oldValue = null;
    }

    /**
     * Constructor for CHANGE event
     * @param difference
     * @param name node name
     * @param value node value
     * @param oldValue previous node value
     */
    public Event(Difference difference, String name, String value, String oldValue) {
      this.difference = difference;
      this.name = name;
      this.value = value;
      this.oldValue = oldValue;
    }

    public Difference getDifference() {
      return difference;
    }

    String getName() {
      return name;
    }

    String getOldValue() {
      return oldValue;
    }

    String getValue() {
      return value;
    }

  }

}
