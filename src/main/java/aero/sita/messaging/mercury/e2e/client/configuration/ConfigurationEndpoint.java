/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.client.configuration;

import lombok.Getter;

/**
 * Enumeration of test-harness API endpoints.
 * This enum provides type-safe access to all test-harness endpoints
 * and includes helper methods for URL construction. Using an enum instead
 * of string constants provides compile-time safety, better refactoring support,
 * and encapsulates URL building logic.
 * Example usage:
 * <pre>
 * String url = TestHarnessEndpoint.SEND_MESSAGE.buildUrl(baseUrl);
 * String urlWithId = TestHarnessEndpoint.GET_RESULT_BY_ID.buildUrl(baseUrl, 123L);
 * </pre>
 */
@Getter
public enum ConfigurationEndpoint {

  /**
   * Endpoint for get expanded group code by Id
   * GET /api/v1/live/expanded-group-codes/{id}
   */
  GET_EXPANDED_GROUP_CODE_BY_ID("/api/v1/live/expanded-group-codes/{id}");

  /**
   * -- GETTER --
   * Gets the endpoint path.
   */
  private final String path;

  /**
   * Constructor for TestHarnessEndpoint.
   *
   * @param path the endpoint path relative to the base URL
   */
  ConfigurationEndpoint(String path) {
    this.path = path;
  }

  /**
   * Builds the full URL by combining base URL with endpoint path.
   *
   * @param baseUrl the base URL of the test-harness (e.g., "<a href="http://localhost:8082/test-harness">...</a>")
   * @return the full URL
   */
  public String buildUrl(String baseUrl) {
    return baseUrl + path;
  }

  /**
   * Builds the full URL with path parameters.
   * This method replaces placeholders in the path with actual values.
   * <p>
   * Currently, supports:
   * - {id} placeholder for GET_RESULT_BY_ID endpoint
   *
   * @param baseUrl the base URL of the test-harness
   * @param params  path parameters to replace placeholders (order-dependent)
   * @return the full URL with parameters replaced
   * @throws IllegalArgumentException if required parameters are missing
   */
  public String buildUrl(String baseUrl, Object... params) {
    String url = baseUrl + path;

    // Replace path parameters based on the endpoint
    if (this == GET_EXPANDED_GROUP_CODE_BY_ID) {
      if (params.length == 0) {
        throw new IllegalArgumentException("GET_EXPANDED_GROUP_CODE_BY_ID requires an ID parameter");
      }
      url = url.replace("{id}", String.valueOf(params[0]));
    }

    return url;
  }
}