/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.testharness.response;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing a list of received messages from the test-harness.
 * This is the response from the GET /received-messages endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedMessagesResponse {

  /**
   * List of received messages.
   * This is a JSON array in the response body.
   */
  @Builder.Default
  private List<ReceivedMessage> receivedMessages = new ArrayList<>();

  /**
   * Gets the total number of received messages.
   *
   * @return count of messages
   */
  public int getMessageCount() {
    return receivedMessages != null ? receivedMessages.size() : 0;
  }

  /**
   * Checks if any messages were received.
   *
   * @return true if at least one message was received
   */
  public boolean hasMessages() {
    return receivedMessages != null && !receivedMessages.isEmpty();
  }

  /**
   * Gets all reject messages (containing "PLS RPT YR").
   *
   * @return list of reject messages
   */
  public List<ReceivedMessage> getRejectMessages() {
    if (receivedMessages == null) {
      return new ArrayList<>();
    }
    return receivedMessages.stream()
        .filter(ReceivedMessage::isRejectMessage)
        .collect(Collectors.toList());
  }

  /**
   * Gets messages containing specific text.
   *
   * @param text the text to search for
   * @return list of messages containing the text
   */
  public List<ReceivedMessage> getMessagesContaining(String text) {
    if (receivedMessages == null) {
      return new ArrayList<>();
    }
    return receivedMessages.stream()
        .filter(msg -> msg.contains(text))
        .collect(Collectors.toList());
  }

  /**
   * Gets messages from a specific queue.
   *
   * @param queueName the queue name
   * @return list of messages from that queue
   */
  public List<ReceivedMessage> getMessagesByQueue(String queueName) {
    if (receivedMessages == null) {
      return new ArrayList<>();
    }
    return receivedMessages.stream()
        .filter(msg -> queueName.equals(msg.getQueueName()))
        .collect(Collectors.toList());
  }

  /**
   * Gets messages using a specific protocol.
   *
   * @param protocol the protocol name (e.g., "IBMMQ")
   * @return list of messages using that protocol
   */
  public List<ReceivedMessage> getMessagesByProtocol(String protocol) {
    if (receivedMessages == null) {
      return new ArrayList<>();
    }
    return receivedMessages.stream()
        .filter(msg -> protocol.equals(msg.getProtocol()))
        .collect(Collectors.toList());
  }
}