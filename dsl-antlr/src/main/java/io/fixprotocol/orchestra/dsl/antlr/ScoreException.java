/*
 * Copyright 2017 FIX Protocol Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *
 */
package io.fixprotocol.orchestra.dsl.antlr;

/**
 * An Exception that occurs in the evaluation of a Score DSL expression
 *
 * @author Don Mendelson
 *
 */
public class ScoreException extends Exception {

  private static final long serialVersionUID = 6950517934100277304L;

  private final int columnNumber;
  private final int lineNumber;

  /**
   *
   */
  public ScoreException() {
    this.lineNumber = -1;
    this.columnNumber = -1;
  }


  /**
   * @param message error text
   */
  public ScoreException(String message) {
    super(message);
    this.lineNumber = -1;
    this.columnNumber = -1;
  }

  /**
   * @param message error text
   */
  public ScoreException(String message, int lineNumber, int columnNumber) {
    super(message);
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }

  /**
   * @param message error text
   * @param cause nested exception
   */
  public ScoreException(String message, int lineNumber, int columnNumber, Throwable cause) {
    super(message, cause);
    this.lineNumber = lineNumber;
    this.columnNumber = columnNumber;
  }

  /**
   * @param message error text
   * @param cause nested exception
   */
  public ScoreException(String message, Throwable cause) {
    super(message, cause);
    this.lineNumber = -1;
    this.columnNumber = -1;
  }

  /**
   * @param message error text
   * @param cause nested exception
   * @param enableSuppression whether or not suppression is enabled or disabled
   * @param writableStackTrace whether or not the stack trace should be writable
   */
  public ScoreException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
    this.lineNumber = -1;
    this.columnNumber = -1;
  }

  /**
   * @param cause nested exception
   */
  public ScoreException(Throwable cause) {
    super(cause);
    this.lineNumber = -1;
    this.columnNumber = -1;
  }

  /**
   * The column number of the end of the text where the exception occurred.
   *
   * <p>
   * The first column in a line is position 1.
   * </p>
   *
   * @return An integer representing the column number, or -1 if none is available.
   */
  public int getColumnNumber() {
    return this.columnNumber;
  }


  /**
   * The line number of the end of the text where the exception occurred.
   *
   * <p>
   * The first line is line 1.
   * </p>
   *
   * @return An integer representing the line number, or -1 if none is available.
   */
  public int getLineNumber() {
    return this.lineNumber;
  }

}
