package io.fixprotocol.orchestra.message;

import io.fixprotocol._2016.fixrepository.MessageType;
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
