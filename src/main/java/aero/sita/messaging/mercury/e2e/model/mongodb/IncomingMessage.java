/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.mongodb;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document model for incoming-messages collection.
 * Represents messages received by the Mercury system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "incoming-messages")
public class IncomingMessage {

  /**
   * Unique identifier of the incoming message.
   */
  @Id
  private String id;

  /**
   * List of addresses associated with this message.
   */
  private List<Address> addresses;

  /**
   * Correlation ID for tracking related messages.
   */
  private String correlationId;

  /**
   * Date when the message was created in the system.
   */
  private Instant createdDate;

  /**
   * List of errors identified in the message.
   * Each error contains an error code.
   */
  @Builder.Default
  private List<MessageError> errors = new ArrayList<>();

  /**
   * ID of the incoming connection.
   */
  private String incomingConnectionId;

  /**
   * Format of the incoming message (e.g., TYPE_B).
   */
  private String incomingFormat;

  /**
   * Service address where the message was received.
   */
  private String incomingServiceAddress;

  /**
   * Last modification date of the message.
   */
  private Instant lastModifiedDate;

  /**
   * Additional metadata associated with the message.
   */
  private Map<String, Object> metadata;

  /**
   * Flag indicating if this might be a duplicate message.
   */
  private Boolean possibleDuplicate;

  /**
   * Raw message data as received.
   */
  private String rawData;

  /**
   * Date and time when the message was received.
   */
  private String receivedDateTime;

  /**
   * Protocol used to receive the message (e.g., IBM_MQ).
   */
  private String receivedProtocol;

  /**
   * Revision number of the document.
   */
  private Long revision;

  /**
   * Source system that generated the message.
   */
  private String source;

  /**
   * List of status logs tracking message processing stages.
   */
  private List<StatusLog> statusLogs;

  /**
   * List of SU IDs associated with the message.
   */
  private List<String> suIds;

  /**
   * Version of the message format.
   */
  private String version;

  /**
   * Identity of the message for tracking.
   */
  private String messageIdentity;

  /**
   * Originator address information.
   */
  private Address originator;

  /**
   * Priority level of the message.
   */
  private String priority;

  /**
   * Priority value code (e.g., QD for deferred).
   */
  private String priorityValue;

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
}

