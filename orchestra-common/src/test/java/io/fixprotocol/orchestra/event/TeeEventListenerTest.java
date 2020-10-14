package io.fixprotocol.orchestra.event;

import static io.fixprotocol.orchestra.event.Event.Severity.ERROR;
import static io.fixprotocol.orchestra.event.Event.Severity.WARN;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TeeEventListenerTest {

  private TeeEventListener tee;
  private final EventListenerFactory factory = new EventListenerFactory();

  @BeforeEach
  void setUp() throws Exception {
    tee = new TeeEventListener();
  }

  @Test
  void testEvent() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);
    EventListener listener = factory.getInstance("JSON");
    listener.setResource(outputStream);   
    tee.addEventListener(listener);
    Event event1 = new Event(WARN, "Warning message");
    Event event2 = new Event(ERROR, "Error message {0}", 23);
    tee.event(event1);
    tee.event(event2);
    tee.close();
    assertTrue(outputStream.toString().length() > 0);
  }

}
