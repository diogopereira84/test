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
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper class for message injection operations.
 * Supports both Address-Based Routing and Criteria-Based Routing (Smart Logic).
 */
@Slf4j
@Component
public class MessageInjectionHelper {

  private final TestHarnessClient testHarnessClient;
  private final MongoGenericHelper mongoHelper;

  private final String defaultServer;
  private final Integer defaultPort;
  private final String defaultQueue;
  private final String configDbName;

  // State to hold explicitly selected queue from Gherkin steps
  private String forcedTargetQueue;

  @Autowired
  public MessageInjectionHelper(
      TestHarnessClient testHarnessClient,
      MongoGenericHelper mongoHelper,
      @Value("${test-harness.default.server:localhost}") String defaultServer,
      @Value("${test-harness.default.port:1414}") Integer defaultPort,
      @Value("${test-harness.default.queue:DEV.QUEUE.1}") String defaultQueue,
      @Value("${configuration.database.name:configuration}") String configDbName) {
    this.testHarnessClient = testHarnessClient;
    this.mongoHelper = mongoHelper;
    this.defaultServer = defaultServer;
    this.defaultPort = defaultPort;
    this.defaultQueue = defaultQueue;
    this.configDbName = configDbName;
  }

  /**
   * Resets the state for a new scenario.
   */
  public void reset() {
    this.forcedTargetQueue = null;
  }

  /**
   * Selects a target queue by finding a connection in MongoDB that matches the criteria.
   * Example: field="messageConfiguration.acceptMessagesWithAHeadingSection", value="true"
   */
  public void setTargetQueueFromConfig(String filterField, String filterValue) {
    log.info("Smart Logic: Selecting connection where '{}' is '{}'", filterField, filterValue);

    // Handle type conversion (MongoDB stores booleans, Gherkin sends strings)
    Object typedValue = parseValue(filterValue);

    // Query 'connections' collection to find the 'inQueue' (which corresponds to TestHarness OUT)
    String foundQueue = (String) mongoHelper.getField(
        configDbName,
        "connections",
        filterField,
        typedValue,
        "inQueue" // We want the queue name to inject into
    );

    if (foundQueue != null) {
      this.forcedTargetQueue = foundQueue;
      log.info("Smart Logic: Locked target queue to '{}'", foundQueue);
    } else {
      throw new IllegalStateException(String.format(
          "Smart Logic Failure: No connection found where %s = %s", filterField, filterValue));
    }
  }

  /**
   * Injects the message.
   * Priority 1: Use queue explicitly selected via "Given I select connection..."
   * Priority 2: Resolve queue dynamically based on the destination address in the message.
   */
  public void injectWithSmartRouting(String messageContent) {
    String targetQueue;

    if (this.forcedTargetQueue != null) {
      targetQueue = this.forcedTargetQueue;
      log.info("Routing: Using explicitly selected queue '{}'", targetQueue);
    } else {
      String targetAddress = extractRecipientAddress(messageContent);
      targetQueue = resolveInQueueForAddress(targetAddress);
      log.info("Routing: Resolved queue '{}' from address '{}'", targetQueue, targetAddress);
    }

    injectRawMessage(messageContent, defaultServer, defaultPort, targetQueue);
  }

  public void injectRawMessage(String messageContent) {
    injectRawMessage(messageContent, defaultServer, defaultPort, defaultQueue);
  }

  public void injectRawMessage(String messageContent, String server, Integer port, String queueName) {
    log.info("Injecting message to {}:{} queue: {}", server, port, queueName);

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
  }

  // --- Internal Logic ---

  private Object parseValue(String value) {
    if ("true".equalsIgnoreCase(value)) {
      return true;
    }
    if ("false".equalsIgnoreCase(value)) {
      return false;
    }
    return value;
  }

  private String extractRecipientAddress(String message) {
    Pattern pattern = Pattern.compile("[A-Z]{2}[\\s]+([A-Z0-9]{7})");
    Matcher matcher = pattern.matcher(message);
    return matcher.find() ? matcher.group(1) : "JFKNYBA";
  }

  private String resolveInQueueForAddress(String address) {
    try {
      Object destIdsObj = mongoHelper.getField(configDbName, "routes", "criteria.addressMatcher", address, "destinationIds");
      if (destIdsObj == null) {
        return defaultQueue;
      }

      List<?> destinationIds = (List<?>) destIdsObj;
      if (destinationIds.isEmpty()) {
        return defaultQueue;
      }

      Object connIdsObj = mongoHelper.getField(configDbName, "destinations", "_id", destinationIds.get(0), "connectionIds");
      List<?> connectionIds = (List<?>) connIdsObj;
      if (connectionIds.isEmpty()) {
        return defaultQueue;
      }

      String inQueue = (String) mongoHelper.getField(configDbName, "connections", "_id", connectionIds.get(0), "inQueue");
      return inQueue != null ? inQueue : defaultQueue;
    } catch (Exception e) {
      log.error("Routing resolution failed for address {}. Using default.", address, e);
      return defaultQueue;
    }
  }
}