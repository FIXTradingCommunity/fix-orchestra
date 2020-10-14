package io.fixprotocol.orchestra.event;

import static io.fixprotocol.orchestra.event.Event.Severity.*;

/**
 * Reports generic events
 * 
 * @author Don Mendelson
 *
 */
public interface EventListener extends AutoCloseable {
  
  /**
   * Set a resource to handle events
   * 
   * @param resource used to handle events
   * @throws Exception if the resource is of an unrecognized type or implementation fails to use it
   */
  default void setResource(Object resource) throws Exception {
    
  }

  /**
   * Reports an event
   * 
   * @param event to report
   */
  void event(Event event);

  default void info(String pattern) {
    event(new Event(INFO, pattern));
  }

  default void info(String pattern, Object... arguments) {
    event(new Event(INFO, pattern, arguments));
  }

  default void warn(String pattern) {
    event(new Event(WARN, pattern));
  }

  default void warn(String pattern, Object... arguments) {
    event(new Event(WARN, pattern, arguments));
  }

  default void error(String pattern) {
    event(new Event(ERROR, pattern));
  }

  default void error(String pattern, Object... arguments) {
    event(new Event(ERROR, pattern, arguments));
  }

  default void fatal(String pattern) {
    event(new Event(FATAL, pattern));
  }

  default void fatal(String pattern, Object... arguments) {
    event(new Event(FATAL, pattern, arguments));
  }

}
