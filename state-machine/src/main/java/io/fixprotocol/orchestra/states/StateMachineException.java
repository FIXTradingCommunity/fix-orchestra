package io.fixprotocol.orchestra.states;

public class StateMachineException extends Exception {

  private static final long serialVersionUID = 3789612339273048280L;

  public StateMachineException() {

  }

  public StateMachineException(String arg0) {
    super(arg0);

  }

  public StateMachineException(Throwable cause) {
    super(cause);

  }

  public StateMachineException(String message, Throwable cause) {
    super(message, cause);

  }

  public StateMachineException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);

  }

}
