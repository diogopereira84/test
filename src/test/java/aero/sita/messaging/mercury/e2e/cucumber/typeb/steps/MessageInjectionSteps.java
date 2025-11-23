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
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class MessageInjectionSteps {

  @Autowired
  private MessageInjectionHelper messageInjectionHelper;

  @Autowired
  private CommonTypeBWorld commonWorld;

  @When("I send the composed message via the Test Harness")
  public void sendComposedMessage() {
    String messageContent = commonWorld.output;

    if (messageContent == null || messageContent.isEmpty()) {
      throw new IllegalStateException("No message content composed. Ensure 'Given the message is composed' was called.");
    }

    messageInjectionHelper.injectWithSmartRouting(messageContent);
  }
}