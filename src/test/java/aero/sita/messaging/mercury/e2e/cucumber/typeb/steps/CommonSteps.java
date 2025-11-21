/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps;

import aero.sita.messaging.mercury.e2e.client.testharness.TestHarnessClient;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ClearResponse;
import aero.sita.messaging.mercury.e2e.utilities.helper.PollingHelper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Common step definitions shared across multiple feature files.
 * This class contains reusable step definitions for common operations
 * such as setup, teardown, and shared preconditions.
 * Also maintains shared state like correlation ID for cross-step communication.
 */
@Slf4j
public class CommonSteps {

  @Autowired
  private TestHarnessClient testHarnessClient;

  @Autowired
  private PollingHelper pollingHelper;

  /**
   * Message Identity for tracking messages across systems.
   * Set by message injection steps, used by MongoDB validation steps.
   */
  @Getter
  @Setter
  private String messageIdentity;

  /**
   * Hook that runs before each scenario to ensure clean state.
   */
  @Before
  public void beforeScenario() {
    //log.info("Starting new test scenario");
    //messageIdentity = null; // Reset correlation ID for each scenario
    //theTestHarnessIsInACleanState();
  }

  /**
   * Clears all messages from the test-harness to ensure clean state.
   */
  @Given("the test-harness is in a clean state")
  public void theTestHarnessIsInACleanState() {
    log.info("Clearing test-harness state");
    try {
      ClearResponse response = testHarnessClient.clearMessages();
      log.info("Cleared {} messages from test-harness",
          response.getNumberOfMessagesCleared());
    } catch (Exception e) {
      log.warn("Failed to clear messages, continuing anyway: {}", e.getMessage());
    }
  }

  /**
   * Verifies that the test-harness is accessible.
   */
  @Given("the test-harness is accessible")
  public void theTestHarnessIsAccessible() {
    log.info("Verifying test-harness accessibility");
    try {
      testHarnessClient.getReceivedMessages();
      log.info("Test-harness is accessible");
    } catch (Exception e) {
      log.error("Test-harness is not accessible: {}", e.getMessage());
      throw new RuntimeException("Test-harness is not accessible", e);
    }
  }
}
