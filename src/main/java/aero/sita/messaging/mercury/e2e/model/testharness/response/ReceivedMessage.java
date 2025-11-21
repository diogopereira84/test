/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.testharness.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a received message from the test-harness.
 * Contains message metadata and content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceivedMessage {

  /**
   * Unique identifier of the received message.
   * Example: "68ebe9574b1c2a4e9d800255"
   */
  @JsonProperty("id")
  private String id;

  /**
   * Timestamp when the message was handed off to the test-harness.
   * Accepts ISO-8601 string or epoch millis.
   * Example: "2025-10-12T17:45:59.791Z"
   */
  @JsonProperty("handOffTimestamp")  // fixed: capital 'O'
  @JsonDeserialize(using = InstantFlexibleDeserializer.class)
  private Instant handOffTimestamp;

  /**
   * Protocol used for the message.
   * Example: "IBMMQ"
   */
  @JsonProperty("protocol")
  private String protocol;

  /**
   * Message body/content.
   * Example: "\r\n\u0001QP SWIRI1G\r\n.HDQRMJU 121445/86716103D\r\n\u0002AVS\r\nJU0580L30AUG LA BEGBCN\r\n\u0003"
   */
  @JsonProperty("body")
  private String body;

  /**
   * Connection name used for the message.
   * Example: "lab-connection2"
   */
  @JsonProperty("connectionName")
  private String connectionName;

  /**
   * Queue name where the message was received.
   * Example: "LKYEDLT.IN"
   */
  @JsonProperty("queueName")
  private String queueName;

  /**
   * Injection ID if the message was injected via test-harness.
   * May be null for messages received from external sources.
   */
  @JsonProperty("injectionId")
  private String injectionId;

  /**
   * Checks if this message is a reject message (contains "PLS RPT YR").
   *
   * @return true if this is a reject message
   */
  public boolean isRejectMessage() {
    return body != null && body.contains("PLS RPT YR");
  }

  /**
   * Checks if this message contains specific text.
   *
   * @param text the text to search for
   * @return true if the message body contains the text
   */
  public boolean contains(String text) {
    return body != null && body.contains(text);
  }

  /**
   * Gets a cleaned version of the message body (replaces control characters).
   *
   * @return cleaned message body
   */
  public String getCleanedBody() {
    if (body == null) {
      return "";
    }
    return body
        .replace("\u0001", "[SOH]")
        .replace("\u0002", "[STX]")
        .replace("\u0003", "[ETX]")
        .replace("\r", "")
        .replace("\n", " ");
  }
}