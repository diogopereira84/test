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
import aero.sita.messaging.mercury.e2e.utilities.helper.MessageInjectionHelper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MessageInjectionSteps {

  @Autowired
  private MessageInjectionHelper messageInjectionHelper;

  @Autowired
  private CommonTypeBWorld commonWorld;

  /**
   * Reset the injection helper state before each scenario.
   * Ensures that a forced queue from one scenario doesn't affect the next.
   */
  @Before
  public void cleanInjectionState() {
    messageInjectionHelper.reset();
  }

  /**
   * Generic step to select a target connection based on configuration properties.
   * Replaces hardcoded queue/address steps.
   * Example: Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
   */
  @Given("I select the connection where {string} is {string}")
  public void selectConnectionByCriteria(String field, String value) {
    messageInjectionHelper.setTargetQueueFromConfig(field, value);
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