package io.fixprotocol.orchestra.event;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Delegates events to multiple listeners
 * 
 * @author Don Mendelson
 *
 */
public class TeeEventListener implements EventListener {

  private final List<EventListener> listeners = new CopyOnWriteArrayList<>();

  /**
   * Register an EventListener for events
   * @param listener
   */
  public void addEventListener(EventListener listener) {
    listeners.add(Objects.requireNonNull(listener, "EventListener missing"));
  }

  @Override
  public void close() throws Exception {
    for (final EventListener listener : listeners) {
      listener.close();
    }
  }

  @Override
  public void event(Event event) {
    for (final EventListener listener : listeners) {
      listener.event(event);
    }
  }

  /**
   * @return an unmodifiable list of EventListener
   */
  public List<EventListener> getListeners() {
    return Collections.unmodifiableList(listeners);
  }

  /**
   * Removes a registered EventListener
   * @param listener
   */
  public void removeEventListener(EventListener listener) {
    listeners.remove(listener);
  }

}
