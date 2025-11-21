/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.mongodb;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents an outgoing message stored in MongoDB.
 * Contains message content, routing information, and error details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "outgoing-messages")
public class OutgoingMessage {

  /**
   * Unique identifier of the outgoing message.
   * Example: "lab-connection1.94aedd4e-b7c2-46dd-857d-1470bbd74b85.2025-10-12T15:49:06.214120900-03:00"
   */
  @Id
  private String id;

  /**
   * List of destination addresses.
   */
  @Builder.Default
  private List<Address> addresses = new ArrayList<>();

  /**
   * Correlation ID linking to the incoming message.
   */
  private String correlationId;

  /**
   * Date and time when the message was created.
   */
  private LocalDateTime createdDate;

  /**
   * List of errors identified in the message.
   * Each error contains an error code.
   */
  @Builder.Default
  private List<MessageError> errors = new ArrayList<>();

  /**
   * Incoming connection identifier.
   */
  private String incomingConnectionId;

  /**
   * Format of the incoming message (e.g., "TYPE_B").
   */
  private String incomingFormat;

  /**
   * ID of the incoming message.
   */
  private String incomingMessageId;

  /**
   * Protocol of the incoming message (e.g., "IBM_MQ").
   */
  private String incomingProtocol;

  /**
   * Date and time when the message was last modified.
   */
  private LocalDateTime lastModifiedDate;

  /**
   * Message body content (without control characters).
   * Example: "PLS RPT YR ..... DUE TO:\r\n1. UNKNOWN_ORIGIN_INDICATOR\r\n..."
   */
  private String messageBody;

  /**
   * Message identity extracted from the message.
   * Example: "010724"
   */
  private String messageIdentity;

  /**
   * Type of the message (e.g., "AVS", "MVT", "LDM").
   */
  private String messageType;

  /**
   * Metadata associated with the message.
   */
  private Object metadata;

  /**
   * Outgoing connection identifier.
   */
  private String outgoingConnectionId;

  /**
   * Format of the outgoing message (e.g., "TYPE_B").
   */
  private String outgoingFormat;

  /**
   * Protocol of the outgoing message (e.g., "IBM_MQ").
   */
  private String outgoingProtocol;

  /**
   * Indicates if this message is a possible duplicate.
   */
  private Boolean possibleDuplicate;

  /**
   * Priority level (e.g., "NORMAL", "HIGH").
   */
  private String priority;

  /**
   * Priority value code (e.g., "QP", "FF").
   */
  private String priorityValue;

  /**
   * Raw message data with control characters.
   * Example: "\r\n\u0001QP LKYSOLT\r\n.MERXTXS 010724\r\n\u0002PLS RPT YR..."
   */
  private String rawData;

  /**
   * Date and time when the message was received.
   */
  private LocalDateTime receivedDateTime;

  /**
   * Revision number of the message.
   */
  private Long revision;

  /**
   * Source of the message (e.g., "ibm-mq-endpoint").
   */
  private String source;

  /**
   * List of status logs tracking message processing.
   */
  @Builder.Default
  private List<StatusLog> statusLogs = new ArrayList<>();

  /**
   * List of SU IDs.
   */
  @Builder.Default
  private List<String> suIds = new ArrayList<>();

  /**
   * Version of the message format.
   */
  private String version;

  /**
   * Delivery tracking identifier.
   */
  private String deliveryTrackingId;

  /**
   * Originator information.
   */
  private Originator originator;

  /**
   * Represents a destination address.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Address {
    /**
     * Type of address (e.g., "NAL").
     */
    private String type;

    /**
     * Address value (e.g., "LKYSOLT").
     */
    private String address;
  }

  /**
   * Represents a message error.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class MessageError {
    /**
     * Error code (e.g., "UNKNOWN_ORIGIN_INDICATOR").
     */
    private String errorCode;

    /**
     * Optional error message or description.
     */
    private String message;

    /**
     * Optional error details.
     */
    private Object details;
  }

  /**
   * Represents a status log entry.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class StatusLog {
    /**
     * Status value (e.g., "ERRORS_IDENTIFIED", "DISPATCHED", "DELIVERED").
     */
    private String status;

    /**
     * Timestamp when this status was recorded.
     */
    private LocalDateTime timestamp;
  }

  /**
   * Represents the message originator.
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Originator {
    /**
     * Originator address (e.g., "MERXTXS").
     */
    private String address;
  }
}