/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.helper;

import aero.sita.messaging.mercury.e2e.config.PollingProperties;
import aero.sita.messaging.mercury.e2e.model.mongodb.IncomingMessage;
import aero.sita.messaging.mercury.e2e.model.mongodb.OutgoingMessage;
import aero.sita.messaging.mercury.e2e.model.mongodb.StatusLog;
import aero.sita.messaging.mercury.e2e.repository.IncomingMessageRepository;
import aero.sita.messaging.mercury.e2e.repository.OutgoingMessageRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for interacting with the message-store MongoDB collections.
 * Provides methods to search, wait for, and validate incoming and outgoing messages.
 * <p>
 * REFACTORED: Now uses centralized polling configuration and PollingHelper
 * instead of hardcoded timeout and interval values. This follows the
 * Single Responsibility Principle and Dependency Inversion Principle.
 */
@Slf4j
@Component
public class MessageStoreHelper {

  private final IncomingMessageRepository incomingMessageRepository;
  private final OutgoingMessageRepository outgoingMessageRepository;
  private final PollingHelper pollingHelper;
  private final PollingProperties pollingProperties;
  private final MongoDataRefresher mongoDataRefresher;

  @Autowired
  public MessageStoreHelper(IncomingMessageRepository incomingMessageRepository,
                            OutgoingMessageRepository outgoingMessageRepository,
                            PollingHelper pollingHelper,
                            PollingProperties pollingProperties, MongoDataRefresher mongoDataRefresher) {
    this.incomingMessageRepository = incomingMessageRepository;
    this.outgoingMessageRepository = outgoingMessageRepository;
    this.pollingHelper = pollingHelper;
    this.pollingProperties = pollingProperties;
    this.mongoDataRefresher = mongoDataRefresher;
  }

  // ==================== INCOMING MESSAGE METHODS ====================

  /**
   * Search for incoming messages by message identity.
   * Returns all messages matching the given messageIdentity.
   *
   * @param messageIdentity the message identity to search for
   * @return list of incoming messages with matching messageIdentity
   */
  public List<IncomingMessage> searchByMessageIdentity(String messageIdentity) {
    log.debug("Searching for incoming messages with messageIdentity: {}", messageIdentity);
    return incomingMessageRepository.findByMessageIdentity(messageIdentity);
  }

  /**
   * Wait for incoming message to appear in MongoDB by searching for messageIdentity.
   * Uses centralized polling configuration.
   * <p>
   * REFACTORED: Now uses PollingHelper and centralized configuration.
   *
   * @param messageIdentity the message identity to search for
   * @return optional containing the most recent message if found
   */
  public Optional<IncomingMessage> waitForIncomingMessageByMessageIdentity(String messageIdentity) {
    log.info("Waiting for incoming message with messageIdentity: {}", messageIdentity);

    int timeout = pollingProperties.getMessageStore().getTimeoutSeconds();
    long interval = pollingProperties.getMessageStore().getIntervalMillis();

    IncomingMessage message = pollingHelper.poll(
        () -> findMostRecentIncomingMessage(messageIdentity),
        timeout,
        interval
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for incoming message to appear in MongoDB by searching for messageIdentity.
   * Allows custom timeout and interval for specific test scenarios.
   *
   * @param messageIdentity    the message identity to search for
   * @param maxWaitSeconds     maximum time to wait in seconds
   * @param pollIntervalMillis polling interval in milliseconds
   * @return optional containing the most recent message if found
   */
  public Optional<IncomingMessage> waitForIncomingMessageByMessageIdentity(
      String messageIdentity, int maxWaitSeconds, long pollIntervalMillis) {
    log.info("Waiting for incoming message with messageIdentity: {} (max {} seconds)",
        messageIdentity, maxWaitSeconds);

    IncomingMessage message = pollingHelper.poll(
        () -> findMostRecentIncomingMessage(messageIdentity),
        maxWaitSeconds,
        pollIntervalMillis
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for incoming message to appear in MongoDB by searching for message content.
   * Uses centralized polling configuration.
   * <p>
   * REFACTORED: Now uses PollingHelper and centralized configuration.
   *
   * @param messageContent the message content to search for (will search in rawData)
   * @return optional containing the message if found
   */
  public Optional<IncomingMessage> waitForIncomingMessageByContent(String messageContent) {
    log.info("Waiting for incoming message containing content");

    int timeout = pollingProperties.getMessageStore().getTimeoutSeconds();
    long interval = pollingProperties.getMessageStore().getIntervalMillis();

    IncomingMessage message = pollingHelper.poll(
        () -> findIncomingMessageByContent(messageContent),
        timeout,
        interval
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for incoming message to appear in MongoDB by searching for message content.
   * Allows custom timeout and interval for specific test scenarios.
   *
   * @param messageContent     the message content to search for (will search in rawData)
   * @param maxWaitSeconds     maximum time to wait in seconds
   * @param pollIntervalMillis polling interval in milliseconds
   * @return optional containing the message if found
   */
  public Optional<IncomingMessage> waitForIncomingMessageByContent(
      String messageContent, int maxWaitSeconds, long pollIntervalMillis) {
    log.info("Waiting for incoming message containing content (max {} seconds)", maxWaitSeconds);

    IncomingMessage message = pollingHelper.poll(
        () -> findIncomingMessageByContent(messageContent),
        maxWaitSeconds,
        pollIntervalMillis
    );

    return Optional.ofNullable(message);
  }

  /**
   * Get the number of status logs in an incoming message.
   *
   * @param message the incoming message
   * @return the count of status logs
   */
  public int getIncomingMessageStatusCount(IncomingMessage message) {
    if (message.getStatusLogs() == null) {
      return 0;
    }
    return message.getStatusLogs().size();
  }

  /**
   * Check if an incoming message has a specific status.
   *
   * @param message the incoming message
   * @param status  the status to check for
   * @return true if the message has the status, false otherwise
   */
  public boolean incomingMessageHasStatus(IncomingMessage message, String status) {
    if (message.getStatusLogs() == null) {
      return false;
    }
    return message.getStatusLogs().stream()
        .anyMatch(log -> status.equals(log.getStatus()));
  }

  /**
   * Get all statuses from an incoming message.
   *
   * @param message the incoming message
   * @return list of status strings
   */
  public List<String> getIncomingMessageStatuses(IncomingMessage message) {
    if (message.getStatusLogs() == null) {
      return List.of();
    }
    return message.getStatusLogs().stream()
        .map(StatusLog::getStatus)
        .collect(Collectors.toList());
  }

  // ==================== OUTGOING MESSAGE METHODS ====================

  /**
   * Search for outgoing messages by message identity.
   * Returns all messages matching the given messageIdentity.
   *
   * @param messageIdentity the message identity to search for
   * @return list of outgoing messages with matching messageIdentity
   */
  public List<OutgoingMessage> searchOutgoingByMessageIdentity(String messageIdentity) {
    log.debug("Searching for outgoing messages with messageIdentity: {}", messageIdentity);
    return outgoingMessageRepository.findByMessageIdentity(messageIdentity);
  }

  /**
   * Wait for outgoing message to appear in MongoDB by searching for messageIdentity.
   * Uses centralized polling configuration.
   * <p>
   * REFACTORED: Now uses PollingHelper and centralized configuration.
   *
   * @param messageIdentity the message identity to search for
   * @return optional containing the most recent message if found
   */
  public Optional<OutgoingMessage> waitForOutgoingMessageByMessageIdentity(String messageIdentity) {
    log.info("Waiting for outgoing message with messageIdentity: {}", messageIdentity);

    int timeout = pollingProperties.getMessageStore().getTimeoutSeconds();
    long interval = pollingProperties.getMessageStore().getIntervalMillis();

    OutgoingMessage message = pollingHelper.poll(
        () -> findMostRecentOutgoingMessage(messageIdentity),
        timeout,
        interval
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for outgoing message to appear in MongoDB by searching for messageIdentity.
   * Allows custom timeout and interval for specific test scenarios.
   *
   * @param messageIdentity    the message identity to search for
   * @param maxWaitSeconds     maximum time to wait in seconds
   * @param pollIntervalMillis polling interval in milliseconds
   * @return optional containing the most recent message if found
   */
  public Optional<OutgoingMessage> waitForOutgoingMessageByMessageIdentity(
      String messageIdentity, int maxWaitSeconds, long pollIntervalMillis) {
    log.info("Waiting for outgoing message with messageIdentity: {} (max {} seconds)",
        messageIdentity, maxWaitSeconds);

    OutgoingMessage message = pollingHelper.poll(
        () -> findMostRecentOutgoingMessage(messageIdentity),
        maxWaitSeconds,
        pollIntervalMillis
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for outgoing message to appear in MongoDB by searching for message content.
   * Uses centralized polling configuration.
   * <p>
   * REFACTORED: Now uses PollingHelper and centralized configuration.
   *
   * @param messageContent the message content to search for (will search in rawData)
   * @return optional containing the message if found
   */
  public Optional<OutgoingMessage> waitForOutgoingMessageByContent(String messageContent) {
    log.info("Waiting for outgoing message containing content");

    int timeout = pollingProperties.getMessageStore().getTimeoutSeconds();
    long interval = pollingProperties.getMessageStore().getIntervalMillis();

    OutgoingMessage message = pollingHelper.poll(
        () -> findOutgoingMessageByContent(messageContent),
        timeout,
        interval
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for outgoing message to appear in MongoDB by searching for message content.
   * Allows custom timeout and interval for specific test scenarios.
   *
   * @param messageContent     the message content to search for (will search in rawData)
   * @param maxWaitSeconds     maximum time to wait in seconds
   * @param pollIntervalMillis polling interval in milliseconds
   * @return optional containing the message if found
   */
  public Optional<OutgoingMessage> waitForOutgoingMessageByContent(
      String messageContent, int maxWaitSeconds, long pollIntervalMillis) {
    log.info("Waiting for outgoing message containing content (max {} seconds)", maxWaitSeconds);

    OutgoingMessage message = pollingHelper.poll(
        () -> findOutgoingMessageByContent(messageContent),
        maxWaitSeconds,
        pollIntervalMillis
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for outgoing message by incoming message ID.
   * Uses centralized polling configuration.
   * <p>
   * REFACTORED: Now uses PollingHelper and centralized configuration.
   * Removed hardcoded 500ms interval.
   *
   * @param incomingMessageId the incoming message ID
   * @return optional containing the outgoing message if found
   */
  public Optional<OutgoingMessage> waitForOutgoingMessageByIncomingMessageId(String incomingMessageId) {
    log.info("Waiting for outgoing message with incomingMessageId: {}", incomingMessageId);

    int timeout = pollingProperties.getMessageStore().getTimeoutSeconds();
    long interval = pollingProperties.getMessageStore().getIntervalMillis();

    OutgoingMessage message = pollingHelper.poll(
        () -> findOutgoingMessageByIncomingMessageId(incomingMessageId),
        timeout,
        interval
    );

    return Optional.ofNullable(message);
  }

  /**
   * Wait for outgoing message by incoming message ID.
   * Allows custom timeout for specific test scenarios.
   *
   * @param incomingMessageId the incoming message ID
   * @param maxWaitSeconds    maximum time to wait in seconds
   * @return optional containing the outgoing message if found
   */
  public Optional<OutgoingMessage> waitForOutgoingMessageByIncomingMessageId(
      String incomingMessageId, int maxWaitSeconds) {
    log.info("Waiting for outgoing message with incomingMessageId: {} (max {} seconds)",
        incomingMessageId, maxWaitSeconds);

    long interval = pollingProperties.getMessageStore().getIntervalMillis();

    OutgoingMessage message = pollingHelper.poll(
        () -> findOutgoingMessageByIncomingMessageId(incomingMessageId),
        maxWaitSeconds,
        interval
    );

    return Optional.ofNullable(message);
  }

  /**
   * Get the number of status logs in an outgoing message.
   *
   * @param message the outgoing message
   * @return the count of status logs
   */
  public int getOutgoingMessageStatusCount(OutgoingMessage message) {
    if (message.getStatusLogs() == null) {
      return 0;
    }
    return message.getStatusLogs().size();
  }

  /**
   * Check if an outgoing message has a specific status.
   *
   * @param message the outgoing message
   * @param status  the status to check for
   * @return true if the message has the status, false otherwise
   */
  public boolean outgoingMessageHasStatus(OutgoingMessage message, String status) {
    if (message.getStatusLogs() == null) {
      return false;
    }
    return message.getStatusLogs().stream()
        .anyMatch(log -> status.equals(log.getStatus()));
  }

  /**
   * Get all statuses from an outgoing message in timestamp order (chronological order).
   *
   * @param message the outgoing message
   * @return list of status strings sorted by timestamp
   */
  public List<String> getOutgoingMessageStatuses(OutgoingMessage message) {
    if (message.getStatusLogs() == null) {
      return List.of();
    }

    // Sort by timestamp first, then extract status strings
    return message.getStatusLogs().stream()
        .sorted(Comparator.comparing(OutgoingMessage.StatusLog::getTimestamp))
        .map(OutgoingMessage.StatusLog::getStatus)
        .collect(Collectors.toList());
  }

  /**
   * Get all error codes from an outgoing message.
   *
   * @param message the outgoing message
   * @return list of error code strings
   */
  public List<String> getOutgoingMessageErrorCode(OutgoingMessage message) {
    if (message.getErrors() == null) {
      return List.of();
    }
    return message.getErrors().stream()
        .map(OutgoingMessage.MessageError::getErrorCode)
        .collect(Collectors.toList());
  }

  /**
   * Get all error codes from an incoming message.
   *
   * @param message the incoming message
   * @return list of error code strings
   */
  public List<String> getInComingMessageErrorCode(IncomingMessage message) {
    if (message.getErrors() == null) {
      return List.of();
    }
    return message.getErrors().stream()
        .map(IncomingMessage.MessageError::getErrorCode)
        .collect(Collectors.toList());
  }

  /**
   * Get all statuses from an outgoing message, sorted by timestamp in ascending order.
   * This returns the statuses in chronological order (earliest to latest).
   *
   * @param message the outgoing message
   * @return list of status strings sorted by timestamp (ascending)
   */
  public List<String> getOutgoingMessageStatusesSortedByTimestamp(OutgoingMessage message) {
    if (message.getStatusLogs() == null) {
      return List.of();
    }
    return message.getStatusLogs().stream()
        .sorted(Comparator.comparing(OutgoingMessage.StatusLog::getTimestamp))
        .map(OutgoingMessage.StatusLog::getStatus)
        .collect(Collectors.toList());
  }

  // ==================== PRIVATE HELPER METHODS (POLLING LOGIC) ====================

  /**
   * Finds the most recent incoming message by messageIdentity.
   * This is the core logic called by the polling helper.
   *
   * @param messageIdentity the message identity to search for
   * @return the most recent message, or null if not found
   */
  private IncomingMessage findMostRecentIncomingMessage(String messageIdentity) {
    List<IncomingMessage> messages = mongoDataRefresher.findIncomingByMessageIdentity(messageIdentity);

    if (messages.isEmpty()) {
      return null;
    }

    // Return the most recent message if multiple exist
    return messages.stream()
        .max(Comparator.comparing(IncomingMessage::getCreatedDate))
        .orElse(messages.getFirst());
  }

  /**
   * Finds an incoming message by content.
   * This is the core logic called by the polling helper.
   *
   * @param messageContent the message content to search for
   * @return the message if found, or null
   */
  private IncomingMessage findIncomingMessageByContent(String messageContent) {
    List<IncomingMessage> allMessages = incomingMessageRepository.findAll();

    return allMessages.stream()
        .filter(msg -> msg.getRawData() != null && msg.getRawData().contains(messageContent))
        .max(Comparator.comparing(IncomingMessage::getCreatedDate))
        .orElse(null);
  }

  /**
   * Finds the most recent outgoing message by messageIdentity.
   * This is the core logic called by the polling helper.
   *
   * @param messageIdentity the message identity to search for
   * @return the most recent message, or null if not found
   */
  private OutgoingMessage findMostRecentOutgoingMessage(String messageIdentity) {
    List<OutgoingMessage> messages = mongoDataRefresher.findOutgoingByMessageIdentity(messageIdentity);

    if (messages.isEmpty()) {
      return null;
    }

    // Return the most recent message if multiple exist
    return messages.stream()
        .max(Comparator.comparing(OutgoingMessage::getCreatedDate))
        .orElse(messages.getFirst());
  }

  /**
   * Finds an outgoing message by content.
   * This is the core logic called by the polling helper.
   *
   * @param messageContent the message content to search for
   * @return the message if found, or null
   */
  private OutgoingMessage findOutgoingMessageByContent(String messageContent) {
    List<OutgoingMessage> allMessages = outgoingMessageRepository.findAll();

    return allMessages.stream()
        .filter(msg -> msg.getRawData() != null && msg.getRawData().contains(messageContent))
        .max(Comparator.comparing(OutgoingMessage::getCreatedDate))
        .orElse(null);
  }

  /**
   * Finds an outgoing message by incoming message ID.
   * This is the core logic called by the polling helper.
   *
   * @param incomingMessageId the incoming message ID
   * @return the outgoing message if found, or null
   */
  private OutgoingMessage findOutgoingMessageByIncomingMessageId(String incomingMessageId) {
    List<OutgoingMessage> messages = mongoDataRefresher.findByIncomingMessageId(incomingMessageId);

    if (messages.isEmpty()) {
      return null;
    }

    return messages.getFirst();
  }

  // ==================== STATUS VALIDATION METHODS ====================

  /**
   * Waits for an incoming message to have all expected statuses.
   * Polls MongoDB until all statuses are present or timeout is reached.
   *
   * @param message          the incoming message to check
   * @param expectedStatuses list of expected status names
   * @return true if all statuses are found, false if timeout
   */
  public boolean waitForIncomingMessageStatuses(IncomingMessage message, List<String> expectedStatuses) {
    int timeoutSeconds = pollingProperties.getStatusValidation().getTimeoutSeconds();
    long intervalMillis = pollingProperties.getStatusValidation().getIntervalMillis();

    log.info("Waiting for incoming message to have statuses: {} (max {} seconds)",
        expectedStatuses, timeoutSeconds);

    return pollingHelper.poll(
        () -> checkIncomingMessageHasStatuses(message.getId(), expectedStatuses),
        timeoutSeconds,
        intervalMillis
    );
  }

  /**
   * Waits for an outgoing message to have all expected statuses.
   * Polls MongoDB until all statuses are present or timeout is reached.
   *
   * @param message          the outgoing message to check
   * @param expectedStatuses list of expected status names
   * @return true if all statuses are found, false if timeout
   */
  public boolean waitForOutgoingMessageStatuses(OutgoingMessage message, List<String> expectedStatuses) {
    int timeoutSeconds = pollingProperties.getStatusValidation().getTimeoutSeconds();
    long intervalMillis = pollingProperties.getStatusValidation().getIntervalMillis();

    log.info("Waiting for outgoing message to have statuses: {} (max {} seconds)",
        expectedStatuses, timeoutSeconds);

    return pollingHelper.poll(
        () -> checkOutgoingMessageHasStatuses(message.getId(), expectedStatuses),
        timeoutSeconds,
        intervalMillis
    );
  }

  /**
   * Checks if an incoming message has all expected statuses.
   * Fetches fresh data from MongoDB on each call.
   *
   * @param messageId        the message ID
   * @param expectedStatuses list of expected status names
   * @return true if all statuses are present
   */
  private boolean checkIncomingMessageHasStatuses(String messageId, List<String> expectedStatuses) {
    // Fetch fresh message from MongoDB
    Optional<IncomingMessage> freshMessage = incomingMessageRepository.findById(messageId);

    if (freshMessage.isEmpty()) {
      log.debug("Incoming message {} not found", messageId);
      return false;
    }

    List<String> actualStatuses = getIncomingMessageStatuses(freshMessage.get());
    boolean hasAllStatuses = actualStatuses.containsAll(expectedStatuses);

    log.debug("Incoming message has statuses: {} (expected: {})", actualStatuses, expectedStatuses);

    return hasAllStatuses;
  }

  /**
   * Checks if an outgoing message has all expected statuses.
   * Fetches fresh data from MongoDB on each call.
   *
   * @param messageId        the message ID
   * @param expectedStatuses list of expected status names
   * @return true if all statuses are present
   */
  private boolean checkOutgoingMessageHasStatuses(String messageId, List<String> expectedStatuses) {
    // Fetch fresh message from MongoDB
    Optional<OutgoingMessage> freshMessage = outgoingMessageRepository.findById(messageId);

    if (freshMessage.isEmpty()) {
      log.debug("Outgoing message {} not found", messageId);
      return false;
    }

    List<String> actualStatuses = getOutgoingMessageStatuses(freshMessage.get());
    boolean hasAllStatuses = actualStatuses.containsAll(expectedStatuses);

    log.debug("Outgoing message has statuses: {} (expected: {})", actualStatuses, expectedStatuses);

    return hasAllStatuses;
  }
}

