package io.fixprotocol.orchestra.event;

import java.text.MessageFormat;

/**
 * Reportable generic event
 *
 * @author Don Mendelson
 * @see java.text.MessageFormat
 */
public class Event {

  /**
   * Severity of an event to report
   *
   * Debug and trace levels are deliberately excluded since this feature should be only used to
   * report events to users, not for development debugging.
   */
  public enum Severity {
    INFO, WARN, ERROR, FATAL
  }

  private static final Object[] NULL_ARGUMENTS = new Object[0];

  private final Object[] arguments;
  private final String pattern;
  private final Severity severity;

  /**
   * An event to report
   *
   * @param severity of an event for reporting
   * @param pattern a message without arguments
   */
  public Event(Severity severity, String pattern) {
    this(severity, pattern, NULL_ARGUMENTS);
  }

  /**
   * An event to report
   *
   * @param severity of an event for reporting
   * @param pattern a format with substitutable arguments as in {@link java.text.MessageFormat}
   * @param arguments arguments to substitute in the pattern
   */
  public Event(Severity severity, String pattern, Object... arguments) {
    this.severity = severity;
    this.pattern = pattern;
    this.arguments = arguments;
  }

  public String getMessage() {
    return MessageFormat.format(pattern, arguments);
  }

  public Severity getSeverity() {
    return severity;
  }

  @Override
  public String toString() {
    return getMessage();
  }
}
