package io.fixprotocol.orchestra.event.json;

import java.io.IOException;
import java.io.OutputStream;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.fixprotocol.orchestra.event.Event;
import io.fixprotocol.orchestra.event.EventListener;

public class JSONEventListener implements EventListener {

  private JsonGenerator generator = null;

  /**
   * Creates a listener object that is not ready until {@link #setOutputStream(OutputStream)} is
   * invoked
   */
  public JSONEventListener() {

  }

  /**
   * Creates a new listener that immediately starts writing to an OutputStream
   * 
   * @param outputStream
   * @throws IOException
   */
  public JSONEventListener(OutputStream outputStream) throws IOException {
    setOutputStream(outputStream);
  }

  @Override
  public void close() throws Exception {
    generator.close();
  }

  @Override
  public void event(Event event) {
    try {
      if (generator == null) {
        throw new IllegalStateException("JSONEventListener not ready; call setOutputStream()");
      }
      generator.writeStartObject();
      generator.writeObjectField("severity", event.getSeverity().name());
      generator.writeObjectField("message", event.getMessage());
      generator.writeEndObject();
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void setOutputStream(OutputStream outputStream) throws IOException {
    final JsonFactory factory = new JsonFactory();
    generator = factory.createGenerator(outputStream);
    generator.writeStartObject();
    generator.writeArrayFieldStart("events");
  }
  
  public void setResource(Object resource) throws Exception {
    if (resource instanceof OutputStream) {
      setOutputStream((OutputStream) resource);
    } else {
      throw new IllegalArgumentException("No implementation for resource class");
    }
  }

}
