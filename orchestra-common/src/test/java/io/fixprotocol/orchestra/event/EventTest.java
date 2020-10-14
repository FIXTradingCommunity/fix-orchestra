package io.fixprotocol.orchestra.event;

import static io.fixprotocol.orchestra.event.Event.Severity.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.fixprotocol.orchestra.event.Event;

class EventTest {

  @BeforeEach
  void setUp() throws Exception {}

  @Test
  void testToString() {
    Event event1 = new Event(ERROR, "The sky is {0}", "blue");
    assertEquals("The sky is blue", event1.toString());
    
    Event event2 = new Event(WARN, "Goody {0} {1}", 2, "shoes");
    assertEquals("Goody 2 shoes", event2.toString());
    
    Event event3 = new Event(INFO, "The grass is green");
    assertEquals("The grass is green", event3.toString());
    assertEquals("INFO", event3.getSeverity().toString());
  }

}
