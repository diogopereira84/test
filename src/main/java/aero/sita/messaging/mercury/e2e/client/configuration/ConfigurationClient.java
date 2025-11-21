/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.client.configuration;

import aero.sita.messaging.mercury.e2e.model.testharness.response.ResultResponse;

/**
 * Client interface for interacting with the test-harness REST API.
 * Provides methods for message injection, retrieval, and result management.
 * This interface follows the Dependency Inversion Principle (SOLID),
 * allowing for easy mocking and alternative implementations.
 */
public interface ConfigurationClient {

  /**
   * Retrieves the result of a test run by ID.
   *
   * @param id the unique identifier of the test run
   * @return ResultResponse containing test execution results
   * @throws RuntimeException if the request fails or result not found
   */
  Object getExpandedGroupCodeById(String id);

//  ResponseModelConfigExp getExpandedGroupCodeById(String id);
}
