/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.CommonTypeBWorld;
import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.ConfigurationWorld;
import aero.sita.messaging.mercury.e2e.utilities.helper.MessageInjectionHelper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MessageInjectionSteps {

  @Autowired
  private MessageInjectionHelper messageInjectionHelper;

  @Autowired
  private CommonTypeBWorld commonWorld;

  @Autowired
  private ConfigurationWorld configurationWorld;

  @Before
  public void cleanInjectionState() {
    messageInjectionHelper.reset();
  }

  /**
   * Generic step to select a target connection based on the background configuration data.
   * Queries the in-memory ConfigurationWorld instead of the actual DB.
   */
  @Given("I select the connection where {string} is {string}")
  public void selectConnectionByCriteria(String field, String value) {
    log.info("Selecting connection from Background data where '{}' is '{}'", field, value);

    List<Map<String, String>> connections = configurationWorld.getConnections();

    if (connections == null || connections.isEmpty()) {
      throw new IllegalStateException("No background connections data found. Ensure 'Given the \"configuration.connections\" collection contains...' is in the Background.");
    }

    // Find the connection map
    Map<String, String> connectionMap = connections.stream()
        .filter(row -> value.equalsIgnoreCase(row.get(field)))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException(
            String.format("No connection found in Background data where %s = %s", field, value)));

    // Store the selected connection in the World for other steps to use
    configurationWorld.setSelectedConnection(connectionMap);

    // Set the target queue for injection
    String foundQueue = connectionMap.get("inQueue"); // 'inQueue' in config = Test Harness OUT Queue
    messageInjectionHelper.setForcedTargetQueue(foundQueue);
  }

  @When("I send the composed message via the Test Harness")
  public void sendComposedMessage() {
    String messageContent = commonWorld.output;

    if (messageContent == null || messageContent.isEmpty()) {
      throw new IllegalStateException("No message content composed. Ensure 'Given the message is composed' was called.");
    }

    messageInjectionHelper.injectWithSmartRouting(messageContent);
  }
}