/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.helper;

import aero.sita.messaging.mercury.e2e.client.testharness.TestHarnessClient;
import aero.sita.messaging.mercury.e2e.config.PollingProperties;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessage;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessagesResponse;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Helper class for message retrieval operations.
 * This utility class provides convenient methods for retrieving messages
 * from the test-harness. It encapsulates common message retrieval patterns
 * and reduces code duplication in test scenarios.
 * <p>
 * Following the Single Responsibility Principle, this class focuses solely
 * on message retrieval helper operations.
 * <p>
 * REFACTORED: Now includes polling support for asynchronous message arrival.
 * All find methods now use polling to handle race conditions where messages
 * may take milliseconds to seconds to arrive.
 */
@Slf4j
@Component
public class MessageRetrievalHelper {

  private final TestHarnessClient testHarnessClient;
  private final PollingHelper pollingHelper;
  private final PollingProperties pollingProperties;

  @Autowired
  private PollingProperties pollingProps;

  @Autowired
  public MessageRetrievalHelper(TestHarnessClient testHarnessClient,
                                PollingHelper pollingHelper,
                                PollingProperties pollingProperties) {
    this.testHarnessClient = testHarnessClient;
    this.pollingHelper = pollingHelper;
    this.pollingProperties = pollingProperties;
  }

  /**
   * Retrieves all received messages from the test harness.
   *
   * @return list of received messages
   */
  public List<ReceivedMessage> getAllReceivedMessages() {
    log.info("Retrieving all received messages from test harness");

    ReceivedMessagesResponse response = testHarnessClient.getReceivedMessages();
    List<ReceivedMessage> messages = response.getReceivedMessages();

    log.info("Retrieved {} received message(s)", messages.size());

    return messages;
  }

  /**
   * Finds a reject message from the received messages.
   * A reject message is identified by the presence of "PLS RPT YR" in the body.
   * <p>
   * REFACTORED: Now uses polling to wait for the reject message to arrive.
   *
   * @return the reject message, or null if not found within timeout
   */
  public ReceivedMessage findRejectMessage() {
    log.info("Looking for reject message with polling");

    int timeout = pollingProperties.getRejectMessage().getTimeoutSeconds();
    long interval = pollingProperties.getRejectMessage().getIntervalMillis();

    ReceivedMessage message = pollingHelper.poll(
        this::findRejectMessageLogic,
        timeout,
        interval
    );

    if (message != null) {
      log.info("Found reject message: id={}, protocol={}, queueName={}",
          message.getId(), message.getProtocol(), message.getQueueName());

      if (log.isDebugEnabled()) {
        logRejectMessageStructure(message);
      }
    } else {
      log.warn("No reject message found after polling for {} seconds", timeout);
    }

    return message;
  }

  /**
   * Finds a reject message related to a specific message identity.
   * <p>
   * REFACTORED: Now uses polling to wait for the reject message to arrive.
   *
   * @param messageIdentity the message identity to search for
   * @return the reject message, or null if not found within timeout
   */
  public ReceivedMessage findRejectMessageByIdentity(String messageIdentity) {
    log.info("Looking for reject message related to messageIdentity: {} with polling", messageIdentity);

    int timeout = pollingProperties.getRejectMessage().getTimeoutSeconds();
    long interval = pollingProperties.getRejectMessage().getIntervalMillis();

    ReceivedMessage message = pollingHelper.poll(
        () -> findRejectMessageByIdentityLogic(messageIdentity),
        timeout,
        interval
    );

    if (message != null) {
      log.info("Found reject message for messageIdentity {}: id={}, protocol={}, queueName={}",
          messageIdentity, message.getId(), message.getProtocol(), message.getQueueName());

      if (log.isDebugEnabled()) {
        logRejectMessageStructure(message);
      }
    } else {
      log.warn("No reject message found for messageIdentity: {} after polling for {} seconds",
          messageIdentity, timeout);
    }

    return message;
  }


  /**
   * Finds the most recent reject message using exponential-backoff polling.
   * Polls until *any* latest message appears, then applies the reject filter once.
   *
   * @return the most recent reject message, or null if none/mismatch
   */
  public ReceivedMessage findLatestReceivedMessage() {
    final PollingProperties.ExponentialConfig cfg = pollingProps.getExponential();
    final int timeoutSeconds = cfg.getTimeoutSeconds();
    final long initialIntervalMs = cfg.getInitialIntervalMillis();
    final double multiplier = cfg.getBackoffMultiplier();
    final long maxIntervalMs = cfg.getMaxIntervalMillis();

    if (log.isDebugEnabled()) {
      log.debug("Polling for latest received message: timeout={}s, initialInterval={}ms, multiplier={}, maxInterval={}ms",
          timeoutSeconds, initialIntervalMs, multiplier, maxIntervalMs);
    } else {
      log.info("Looking for the latest received message with polling");
    }

    try {
      // 1) Poll until we have the latest *received* message (no filtering here)
      ReceivedMessage latest = pollingHelper.pollWithExponentialBackoff(
          this::findLatestReceivedMessageLogic,
          timeoutSeconds,
          (int) initialIntervalMs,
          multiplier,
          (int) maxIntervalMs
      );

      if (latest == null) {
        log.warn("No received message found after polling for {} seconds.", timeoutSeconds);
        return null;
      }

      // 2) Apply the reject filter exactly once (no further polling)
      if (isRejectMessage(latest)) {
        log.info("Latest message matches reject criteria: id={}, timestamp={}",
            latest.getId(), latest.getHandOffTimestamp());
        if (log.isDebugEnabled()) {
          logRejectMessageStructure(latest);
        }
        return latest;
      }

      log.info("Latest message does not match reject criteria (id={}, timestamp={}). No further polling performed.",
          latest.getId(), latest.getHandOffTimestamp());
      return null;

    } catch (RuntimeException ex) {
      log.error("Polling for latest received message failed: timeout={}s, initial={}ms, multiplier={}, max={}ms",
          timeoutSeconds, initialIntervalMs, multiplier, maxIntervalMs, ex);
      throw ex;
    }
  }

  /**
   * Reject-message predicate kept separate for clarity & reuse.
   */
  private boolean isRejectMessage(ReceivedMessage msg) {
    return msg != null
        && msg.getBody() != null
        && msg.getBody().contains("PLS RPT YR");
  }

  /**
   * Returns the latest received message (no filtering).
   * This is the lightweight supplier used by the polling helper.
   *
   * @return latest received message, or null if none found yet
   */
  private ReceivedMessage findLatestReceivedMessageLogic() {
    List<ReceivedMessage> received = getAllReceivedMessages();
    if (received == null || received.isEmpty()) {
      return null;
    }

    return received.stream()
        .max((m1, m2) -> {
          // Prefer handoffTimestamp if both present
          if (m1.getHandOffTimestamp() != null && m2.getHandOffTimestamp() != null) {
            return m1.getHandOffTimestamp().compareTo(m2.getHandOffTimestamp());
          }
          // Fallback: compare by ID (assumes lexicographically larger = newer)
          return m1.getId().compareTo(m2.getId());
        })
        .orElse(null);
  }

  /**
   * Finds all reject messages from the received messages.
   *
   * @return list of reject messages
   */
  public List<ReceivedMessage> findAllRejectMessages() {
    log.info("Looking for all reject messages");

    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    List<ReceivedMessage> rejectMessages = receivedMessages.stream()
        .filter(msg -> msg.getBody() != null && msg.getBody().contains("PLS RPT YR"))
        .toList();

    log.info("Found {} reject message(s)", rejectMessages.size());

    return rejectMessages;
  }

  /**
   * Finds a message by queue name.
   * <p>
   * REFACTORED: Now uses polling to wait for the message to arrive.
   *
   * @param queueName the queue name to search for
   * @return the message, or null if not found within timeout
   */
  public ReceivedMessage findMessageByQueueName(String queueName) {
    log.info("Looking for message in queue: {} with polling", queueName);

    int timeout = pollingProperties.getMessageRetrieval().getTimeoutSeconds();
    long interval = pollingProperties.getMessageRetrieval().getIntervalMillis();

    ReceivedMessage message = pollingHelper.poll(
        () -> findMessageByQueueNameLogic(queueName),
        timeout,
        interval
    );

    if (message != null) {
      log.info("Found message in queue {}: id={}", queueName, message.getId());
    } else {
      log.warn("No message found in queue: {} after polling for {} seconds", queueName, timeout);
    }

    return message;
  }

  /**
   * Finds a message by queue name AND content.
   * Useful for ensuring we pick up a specific message (e.g. by Identity)
   * rather than just the first available message in the queue.
   *
   * @param queueName the queue name to search for
   * @param content   the unique content (Identity) to search for
   * @return the message, or null if not found within timeout
   */
  public ReceivedMessage findMessageByQueueAndContent(String queueName, String content) {
    log.info("Looking for message in queue: '{}' containing '{}' with polling", queueName, content);

    int timeout = pollingProperties.getMessageRetrieval().getTimeoutSeconds();
    long interval = pollingProperties.getMessageRetrieval().getIntervalMillis();

    ReceivedMessage message = pollingHelper.poll(
        () -> findMessageByQueueAndContentLogic(queueName, content),
        timeout,
        interval
    );

    if (message != null) {
      log.info("Found matching message in queue {}: id={}", queueName, message.getId());
    } else {
      log.warn("No message found in queue: {} with content: {} after polling for {} seconds",
          queueName, content, timeout);
    }

    return message;
  }

  /**
   * Finds a message by protocol.
   * <p>
   * REFACTORED: Now uses polling to wait for the message to arrive.
   *
   * @param protocol the protocol to search for (e.g., "IBMMQ", "KAFKA")
   * @return the message, or null if not found within timeout
   */
  public ReceivedMessage findMessageByProtocol(String protocol) {
    log.info("Looking for message with protocol: {} with polling", protocol);

    int timeout = pollingProperties.getMessageRetrieval().getTimeoutSeconds();
    long interval = pollingProperties.getMessageRetrieval().getIntervalMillis();

    ReceivedMessage message = pollingHelper.poll(
        () -> findMessageByProtocolLogic(protocol),
        timeout,
        interval
    );

    if (message != null) {
      log.info("Found message with protocol {}: id={}", protocol, message.getId());
    } else {
      log.warn("No message found with protocol: {} after polling for {} seconds", protocol, timeout);
    }

    return message;
  }

  /**
   * Finds a message containing specific content in the body.
   * <p>
   * REFACTORED: Now uses polling to wait for the message to arrive.
   *
   * @param content the content to search for
   * @return the message, or null if not found within timeout
   */
  public ReceivedMessage findMessageContaining(String content) {
    log.info("Looking for message containing: {} with polling", content);

    int timeout = pollingProperties.getMessageRetrieval().getTimeoutSeconds();
    long interval = pollingProperties.getMessageRetrieval().getIntervalMillis();

    ReceivedMessage message = pollingHelper.poll(
        () -> findMessageContainingLogic(content),
        timeout,
        interval
    );

    if (message != null) {
      log.info("Found message containing '{}': id={}", content, message.getId());
    } else {
      log.warn("No message found containing: {} after polling for {} seconds", content, timeout);
    }

    return message;
  }

  // ==================== PRIVATE HELPER METHODS (POLLING LOGIC) ====================

  /**
   * Core logic for finding a reject message (without polling).
   * This method is called by the polling helper.
   *
   * @return the reject message, or null if not found
   */
  private ReceivedMessage findRejectMessageLogic() {
    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    Optional<ReceivedMessage> rejectMessage = receivedMessages.stream()
        .filter(msg -> msg.getBody() != null && msg.getBody().contains("PLS RPT YR"))
        .findFirst();

    return rejectMessage.orElse(null);
  }

  /**
   * Core logic for finding a reject message by identity (without polling).
   * This method is called by the polling helper.
   *
   * @param messageIdentity the message identity to search for
   * @return the reject message, or null if not found
   */
  private ReceivedMessage findRejectMessageByIdentityLogic(String messageIdentity) {
    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    Optional<ReceivedMessage> rejectMessage = receivedMessages.stream()
        .filter(msg -> msg.getBody() != null
            && msg.getBody().contains("PLS RPT YR")
            && msg.getBody().contains(messageIdentity))
        .findFirst();

    return rejectMessage.orElse(null);
  }

  /**
   * Core logic for finding the latest reject message (without polling).
   * This method is called by the polling helper.
   *
   * @return the most recent reject message, or null if not found
   */
  private ReceivedMessage findLatestRejectMessageLogic() {
    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    Optional<ReceivedMessage> latestRejectMessage = receivedMessages.stream()
        .filter(msg -> msg.getBody() != null && msg.getBody().contains("PLS RPT YR"))
        .max((msg1, msg2) -> {
          // Compare by handoffTimestamp if available
          if (msg1.getHandOffTimestamp() != null && msg2.getHandOffTimestamp() != null) {
            return msg1.getHandOffTimestamp().compareTo(msg2.getHandOffTimestamp());
          }
          // Otherwise compare by ID (assuming higher ID = more recent)
          return msg1.getId().compareTo(msg2.getId());
        });

    return latestRejectMessage.orElse(null);
  }

  /**
   * Core logic for finding a message by queue name (without polling).
   * This method is called by the polling helper.
   *
   * @param queueName the queue name to search for
   * @return the message, or null if not found
   */
  private ReceivedMessage findMessageByQueueNameLogic(String queueName) {
    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    Optional<ReceivedMessage> message = receivedMessages.stream()
        .filter(msg -> queueName.equals(msg.getQueueName()))
        .findFirst();

    return message.orElse(null);
  }

  /**
   * Core logic for finding a message by queue name AND content (without polling).
   *
   * @param queueName the queue name to search for
   * @param content   the content to search for
   * @return the message, or null if not found
   */
  private ReceivedMessage findMessageByQueueAndContentLogic(String queueName, String content) {
    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    Optional<ReceivedMessage> message = receivedMessages.stream()
        .filter(msg ->
            (msg.getQueueName() != null && msg.getQueueName().equals(queueName)) &&
                (msg.getBody() != null && msg.getBody().contains(content))
        )
        .findFirst();

    return message.orElse(null);
  }

  /**
   * Core logic for finding a message by protocol (without polling).
   * This method is called by the polling helper.
   *
   * @param protocol the protocol to search for
   * @return the message, or null if not found
   */
  private ReceivedMessage findMessageByProtocolLogic(String protocol) {
    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    Optional<ReceivedMessage> message = receivedMessages.stream()
        .filter(msg -> protocol.equals(msg.getProtocol()))
        .findFirst();

    return message.orElse(null);
  }

  /**
   * Core logic for finding a message containing specific content (without polling).
   * This method is called by the polling helper.
   *
   * @param content the content to search for
   * @return the message, or null if not found
   */
  private ReceivedMessage findMessageContainingLogic(String content) {
    List<ReceivedMessage> receivedMessages = getAllReceivedMessages();

    Optional<ReceivedMessage> message = receivedMessages.stream()
        .filter(msg -> msg.getBody() != null && msg.getBody().contains(content))
        .findFirst();

    return message.orElse(null);
  }

  /**
   * Logs the reject message structure at DEBUG level.
   * Displays the message in a readable format showing each component.
   *
   * @param message the received message to log
   */
  private void logRejectMessageStructure(ReceivedMessage message) {
    log.debug("=========================== Reject Message ===========================");
    log.debug("Message ID      : {}", message.getId());
    log.debug("Protocol        : {}", message.getProtocol());
    log.debug("Queue Name      : {}", message.getQueueName());
    log.debug("Connection Name : {}", message.getConnectionName());
    log.debug("Timestamp       : {}", message.getHandOffTimestamp());
    log.debug("======================================================================");

    // Log the message body with control characters visible
    String body = message.getBody();
    if (body != null) {
      String displayFormat = body
          .replace("\u0001", "<SOH>")
          .replace("\u0002", "<STX>")
          .replace("\u0003", "<ETX>");

      log.debug("Reject message body:\n{}", displayFormat);
    }

    log.debug("======================================================================");
  }
}