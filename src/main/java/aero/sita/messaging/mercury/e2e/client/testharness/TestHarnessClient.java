/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.client.testharness;

import aero.sita.messaging.mercury.e2e.model.testharness.request.LatencyRequest;
import aero.sita.messaging.mercury.e2e.model.testharness.request.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ClearResponse;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessagesResponse;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ResultResponse;

/**
 * Client interface for interacting with the test-harness REST API.
 * Provides methods for message injection, retrieval, and result management.
 * This interface follows the Dependency Inversion Principle (SOLID),
 * allowing for easy mocking and alternative implementations.
 */
public interface TestHarnessClient {

  /**
   * Sends a message to IBM MQ via the test-harness.
   *
   * @param request the message request containing message content and destination details
   * @throws RuntimeException if the request fails
   */
  void sendMessage(SendMessageIbmMqRequest request);

  /**
   * Retrieves all received messages from the test-harness.
   *
   * @return ReceivedMessagesResponse containing the list of received messages
   * @throws RuntimeException if the request fails
   */
  ReceivedMessagesResponse getReceivedMessages();

  /**
   * Retrieves latency information for a specific injection.
   *
   * @param request the latency request containing injection ID and polling parameters
   * @return ResultResponse containing latency information
   * @throws RuntimeException if the request fails
   */
  ResultResponse getLatency(LatencyRequest request);

  /**
   * Clears all delivered messages from the system.
   *
   * @return ClearResponse containing the number of messages cleared
   * @throws RuntimeException if the request fails
   */
  ClearResponse clearMessages();

  /**
   * Retrieves the result of a test run by ID.
   *
   * @param id the unique identifier of the test run
   * @return ResultResponse containing test execution results
   * @throws RuntimeException if the request fails or result not found
   */
  ResultResponse getResultById(Long id);
}
