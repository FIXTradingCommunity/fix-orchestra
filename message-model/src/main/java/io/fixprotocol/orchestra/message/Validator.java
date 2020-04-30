/*
 * Copyright 2017-2020 FIX Protocol Ltd
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
package io.fixprotocol.orchestra.message;

import io.fixprotocol._2020.orchestra.repository.MessageType;

/**
 * Validate a message against an Orchestra file
 *
 * @author Don Mendelson
 *
 * @param <M> message class
 */
public interface Validator<M> {

  /**
   * Validates a message. A {@code Validator} checks field presence, data range, and membership of a
   * code in a codeSet.
   * 
   * @param message to validate
   * @param messageType Orchestra declaration of a message type
   * @throws TestException if a message is invalid
   */
  void validate(M message, MessageType messageType) throws TestException;
}
