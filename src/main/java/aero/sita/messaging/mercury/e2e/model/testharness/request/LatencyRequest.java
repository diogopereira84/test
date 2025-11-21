/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.testharness.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for retrieving latency information.
 * Used for the POST /test-harness/api/v1/results/latency endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LatencyRequest {

  /**
   * The injection ID to measure latency for.
   */
  @JsonProperty("injectionId")
  private String injectionId;

  /**
   * Maximum time to wait for results in seconds.
   * Default: 20 seconds.
   */
  @JsonProperty("atMostInSeconds")
  @Builder.Default
  private Integer atMostInSeconds = 20;

  /**
   * Initial delay before starting to poll in seconds.
   * Default: 2 seconds.
   */
  @JsonProperty("pollDelayInSeconds")
  @Builder.Default
  private Integer pollDelayInSeconds = 2;

  /**
   * Interval between polls in seconds.
   * Default: 5 seconds.
   */
  @JsonProperty("pollIntervalInSeconds")
  @Builder.Default
  private Integer pollIntervalInSeconds = 5;
}
