package io.fixprotocol.orchestra.message;

import io.fixprotocol._2016.fixrepository.MessageType;

/**
 * Validate a message against an Orchestra file
 * 
 * @author Don Mendelson
 *
 * @param <M> message class
 */
public interface Validator<M> {

  /**
   * Validates a message. A {@code Validator} checks field presence, data range, and
   * membership of a code in a codeSet.
   * @param message to validate
   * @param messageType Orchestra declaration of a message type
   * @throws TestException if a message is invalid
   */
  void validate(M message, MessageType messageType) throws TestException;
}
