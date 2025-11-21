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
import aero.sita.messaging.mercury.e2e.model.testharness.request.DestinationDetails;
import aero.sita.messaging.mercury.e2e.model.testharness.request.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessage;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper class for message injection operations.
 * This utility class provides convenient methods for injecting messages
 * into the Mercury system via the test-harness. It encapsulates common
 * message injection patterns and reduces code duplication in test scenarios.
 * Following the Single Responsibility Principle, this class focuses solely
 * on message injection helper operations.
 */
@Slf4j
@Component
public class MessageInjectionHelper {

  private final TestHarnessClient testHarnessClient;
  private final String defaultServer;
  private final Integer defaultPort;
  private final String defaultQueue;

  @Autowired
  public MessageInjectionHelper(
      TestHarnessClient testHarnessClient,
      @Value("${test-harness.default.server:localhost}") String defaultServer,
      @Value("${test-harness.default.port:1414}") Integer defaultPort,
      @Value("${test-harness.default.queue:DEV.QUEUE.1}") String defaultQueue) {
    this.testHarnessClient = testHarnessClient;
    this.defaultServer = defaultServer;
    this.defaultPort = defaultPort;
    this.defaultQueue = defaultQueue;
  }

  /**
   * Injects a Type B message using default destination settings.
   *
   * @param message the Type B message to inject
   */
  public void injectTypeB(TypeBMessage message) {
    log.info("Injecting Type B message");

    // Log the Type B message structure at DEBUG level
    if (log.isDebugEnabled()) {
      logTypeBMessageStructure(message);
    }

    String messageString = message.toMessageString();
    injectRawMessage(messageString);
  }

  /**
   * Injects a Type B message to a specific destination.
   *
   * @param message   the Type B message to inject
   * @param server    the target server address
   * @param port      the target server port
   * @param queueName the target queue name
   */
  public void injectTypeB(TypeBMessage message, String server, Integer port, String queueName) {
    log.info("Injecting Type B message to {}:{} queue: {}", server, port, queueName);

    // Log the Type B message structure at DEBUG level
    if (log.isDebugEnabled()) {
      logTypeBMessageStructure(message);
    }

    String messageString = message.toMessageString();
    injectRawMessage(messageString, server, port, queueName);
  }

  /**
   * Injects a raw message string using default destination settings.
   *
   * @param messageContent the raw message content to inject
   */
  public void injectRawMessage(String messageContent) {
    injectRawMessage(messageContent, defaultServer, defaultPort, defaultQueue);
  }

  /**
   * Injects a raw message string to a specific destination.
   *
   * @param messageContent the raw message content to inject
   * @param server         the target server address
   * @param port           the target server port
   * @param queueName      the target queue name
   */
  public void injectRawMessage(String messageContent, String server, Integer port, String queueName) {
    log.info("Injecting message to {}:{} queue: {}", server, port, queueName);
    log.debug("Message content: {}", messageContent);

    DestinationDetails destination = DestinationDetails.builder()
        .server(server)
        .port(port)
        .destinationNames(Collections.singletonList(queueName))
        .build();

    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .message(messageContent)
        .destinationsDetailsList(Collections.singletonList(destination))
        .build();

    testHarnessClient.sendMessage(request);

    log.info("Message injected successfully");
  }

  /**
   * Injects a message to multiple queues on the same server.
   *
   * @param messageContent the raw message content to inject
   * @param server         the target server address
   * @param port           the target server port
   * @param queueNames     list of target queue names
   */
  public void injectToMultipleQueues(String messageContent, String server,
                                     Integer port, List<String> queueNames) {
    log.info("Injecting message to {}:{} queues: {}", server, port, queueNames);

    DestinationDetails destination = DestinationDetails.builder()
        .server(server)
        .port(port)
        .destinationNames(queueNames)
        .build();

    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .message(messageContent)
        .destinationsDetailsList(Collections.singletonList(destination))
        .build();

    testHarnessClient.sendMessage(request);

    log.info("Message injected to {} queues successfully", queueNames.size());
  }

  /**
   * Injects a message to multiple destinations (different servers/ports).
   *
   * @param messageContent the raw message content to inject
   * @param destinations   list of destination details
   */
  public void injectToMultipleDestinations(String messageContent,
                                           List<DestinationDetails> destinations) {
    log.info("Injecting message to {} destinations", destinations.size());

    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .message(messageContent)
        .destinationsDetailsList(destinations)
        .build();

    testHarnessClient.sendMessage(request);

    log.info("Message injected to multiple destinations successfully");
  }

  /**
   * Injects a message with a specific load profile ID.
   *
   * @param messageContent the raw message content to inject
   * @param loadProfileId  the load profile ID to associate with this message
   */
  public void injectWithLoadProfile(String messageContent, Long loadProfileId) {
    log.info("Injecting message with load profile ID: {}", loadProfileId);

    DestinationDetails destination = DestinationDetails.builder()
        .server(defaultServer)
        .port(defaultPort)
        .destinationNames(Collections.singletonList(defaultQueue))
        .build();

    SendMessageIbmMqRequest request = SendMessageIbmMqRequest.builder()
        .message(messageContent)
        .destinationsDetailsList(Collections.singletonList(destination))
        .loadProfileId(loadProfileId)
        .build();

    testHarnessClient.sendMessage(request);

    log.info("Message injected with load profile ID successfully");
  }

  /**
   * Logs the Type B message structure at DEBUG level.
   * Displays the message in a readable format showing each component.
   *
   * @param message the Type B message to log
   */
  private void logTypeBMessageStructure(TypeBMessage message) {
    log.debug("=========================== Type B Message ===========================");
    log.debug("Heading Line    : {}", message.getHeadingLine());
    log.debug("Address Line    : .{}", message.getNormalAddressLine());
    log.debug("Origin Line     : {}", message.getOriginLine());
    log.debug("Text Content    : {}", message.getText());
    log.debug("======================================================================");

    // Also log the formatted message with control characters visible
    String formatted = message.toMessageString();
    String displayFormat = formatted
        .replace("\u0001", "<SOH>")
        .replace("\u0002", "<STX>")
        .replace("\u0003", "<ETX>")
        .replace("\r\n", "\n");

    log.debug("Formatted Message:\n{}", displayFormat);
  }
}
