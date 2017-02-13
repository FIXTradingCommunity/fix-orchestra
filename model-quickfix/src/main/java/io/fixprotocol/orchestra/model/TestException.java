package io.fixprotocol.orchestra.model;

import java.util.ArrayList;
import java.util.List;

public class TestException extends Exception {
  
  private class Detail {
    
    private final String actual;
    private final String detailMessage;
    private final String expected;
    
    public Detail(String detailMessage) {
      this(detailMessage, null, null);
    }
    
    public Detail(String detailMessage, String expected, String actual) {
      this.detailMessage = detailMessage;
      this.expected = expected;
      this.actual = actual;
    }
    
    @Override
    public String toString() {
      return "Detail [" + (detailMessage != null ? "detailMessage=" + detailMessage + ", " : "")
          + (expected != null ? "expected=" + expected + ", " : "")
          + (actual != null ? "actual=" + actual : "") + "]";
    }
  }

  private static final long serialVersionUID = 1017825737727657154L;

  private final List<Detail> details = new ArrayList<>();
      
  public TestException() {
  }

  public TestException(String message) {
    super(message);
  }

  public TestException(String message, Throwable cause) {
    super(message, cause);
  }

  public TestException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public TestException(Throwable cause) {
    super(cause);
  }
  
  public void addDetail(String detailMessage) {
    details.add(new Detail(detailMessage));
  }
  
  public void addDetail(String detailMessage, String expected, String actual) {
    details.add(new Detail(detailMessage, expected, actual));
  }
  
  public boolean hasDetails() {
    return !details.isEmpty();
  }

  @Override
  public String getMessage() {
    return super.getMessage() + "; " + details.toString();
  }

  @Override
  public String toString() {
    return "TestException [" + (details != null ? "details=" + details + ", " : "")
        + (super.toString() != null ? "toString()=" + super.toString() : "") + "]";
  }

}
