package io.fixprotocol.orchestra.event;

/**
 * Displays events on the console (stdout)
 * 
 * @author Don Mendelson
 *
 */
public class ConsoleEventListener implements EventListener {

  @Override
  public void close() throws Exception {
    
  }

  @Override
  public void event(Event event) {
    System.out.format("%s %s%n", event.getSeverity().toString(), event.getMessage());
  }

}
