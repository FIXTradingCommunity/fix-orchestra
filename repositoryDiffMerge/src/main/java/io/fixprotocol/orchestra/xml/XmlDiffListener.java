package io.fixprotocol.orchestra.xml;

import java.util.function.Consumer;

public interface XmlDiffListener extends Consumer<XmlDiffListener.Event> {

  public class Event {
    enum Difference {
      ADD, CHANGE, REMOVE, EQUAL
    }

    private final Difference difference;
    private final String name;
    private final String oldValue;
    private final String value;

    public Event(Difference difference, String name, String value) {
      this.difference = difference;
      this.name = name;
      this.value = value;
      this.oldValue = null;
    }

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
