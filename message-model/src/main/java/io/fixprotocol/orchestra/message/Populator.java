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

import io.fixprotocol._2023.orchestra.repository.MessageType;
import io.fixprotocol.orchestra.model.ModelException;

/**
 * Populates an outbound message from an inbound message and an Orchestra file
 *
 * @author Don Mendelson
 *
 * @param <M> message class
 */
public interface Populator<M> {

  /**
   * Populates a message.
   *
   * @param inboundMessage inbound message
   * @param inboundMessageType Orchestra declaration of inbound message type
   * @param outboundMessage outbound message to populate
   * @param outboundMessageType Orchestra declaration of outbound message type
   * @throws ModelException if the message cannot be populated
   */
  void populate(M inboundMessage, MessageType inboundMessageType, M outboundMessage,
      MessageType outboundMessageType) throws ModelException;
}
