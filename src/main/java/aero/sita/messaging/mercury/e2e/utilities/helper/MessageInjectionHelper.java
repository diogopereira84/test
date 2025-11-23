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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MessageInjectionHelper {

  private final TestHarnessClient testHarnessClient;
  private final MongoGenericHelper mongoHelper;

  private final String defaultServer;
  private final Integer defaultPort;
  private final String defaultQueue;
  private final String configDbName;

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

  public void injectWithSmartRouting(String messageContent) {
    String targetAddress = extractRecipientAddress(messageContent);
    String targetQueue = resolveInQueueForAddress(targetAddress);

    log.info("Smart Routing: Address '{}' resolved to Queue '{}'", targetAddress, targetQueue);
    injectRawMessage(messageContent, defaultServer, defaultPort, targetQueue);
  }

  public void injectTypeB(TypeBMessage message) {
    injectRawMessage(message.toMessageString());
  }

  public void injectTypeB(TypeBMessage message, String server, Integer port, String queueName) {
    injectRawMessage(message.toMessageString(), server, port, queueName);
  }

  public void injectRawMessage(String messageContent) {
    injectRawMessage(messageContent, defaultServer, defaultPort, defaultQueue);
  }

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

  private String extractRecipientAddress(String message) {
    Pattern pattern = Pattern.compile("[A-Z]{2}[\\s]+([A-Z0-9]{7})");
    Matcher matcher = pattern.matcher(message);

    if (matcher.find()) {
      return matcher.group(1);
    }
    log.warn("Could not extract valid address from message for routing. Defaulting to 'JFKNYBA'.");
    return "JFKNYBA";
  }

  private String resolveInQueueForAddress(String address) {
    try {
      // 1. Route
      Object destIdsObj = mongoHelper.getField(configDbName, "routes",
          "criteria.addressMatcher", address, "destinationIds");
      if (destIdsObj == null) {
        return defaultQueue;
      }
      List<String> destinationIds = (List<String>) destIdsObj;
      if (destinationIds.isEmpty()) {
        return defaultQueue;
      }

      // 2. Destination
      Object connIdsObj = mongoHelper.getField(configDbName, "destinations",
          "_id", destinationIds.get(0), "connectionIds");
      List<String> connectionIds = (List<String>) connIdsObj;
      if (connectionIds.isEmpty()) {
        return defaultQueue;
      }

      // 3. Connection
      String inQueue = (String) mongoHelper.getField(configDbName, "connections",
          "_id", connectionIds.get(0), "inQueue");

      return inQueue != null ? inQueue : defaultQueue;

    } catch (Exception e) {
      log.error("Smart Routing Failed for address {}. Fallback to default.", address, e);
      return defaultQueue;
    }
  }
}