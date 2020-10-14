package io.fixprotocol.orchestra.event.log4j2;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.fixprotocol.orchestra.event.Event;
import io.fixprotocol.orchestra.event.EventListener;

/**
 * Serializes events using log4j2
 *
 * @author Don Mendelson
 *
 */
public class Log4jEventLogger implements EventListener {

  private volatile boolean isOpen = true;
  private Logger logger;

  /**
   * Uses a Logger qualified by this class name
   */
  public Log4jEventLogger() {
    logger = LogManager.getLogger(Log4jEventLogger.class);
  }

  /**
   * Uses a supplied Logger
   *
   * @param logger a log4j2 Logger
   */
  public Log4jEventLogger(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void close() throws Exception {
    isOpen = false;
  }

  @Override
  public void event(Event event) {
    if (isOpen) {
      switch (event.getSeverity()) {
        case INFO:
          logger.info(event.getMessage());
          break;
        case ERROR:
          logger.error(event.getMessage());
          break;
        case FATAL:
          logger.fatal(event.getMessage());
          break;
        case WARN:
          logger.warn(event.getMessage());
          break;
      }
    }
  }
  
  /**
   * Overrides default selection of Logger
   * 
   * @param resource an instance of Logger
   * @throws IllegalArgumentException if resource is of a different class
   */
  public void setResource(Object resource) {
    if (resource instanceof Logger) {
      this.logger = (Logger) resource;
    } else {
      throw new IllegalArgumentException("No implementation for resource class");
    }
  }

}
