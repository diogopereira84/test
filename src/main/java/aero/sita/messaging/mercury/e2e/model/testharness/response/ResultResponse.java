/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.testharness.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for test execution results.
 * Used for various result endpoints in test-harness.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultResponse {

  /**
   * Unique identifier of the result.
   */
  @JsonProperty("id")
  private Long id;

  /**
   * Load profile identifier associated with this result.
   */
  @JsonProperty("loadProfileId")
  private Long loadProfileId;

  /**
   * Total elapsed time for the test execution in seconds.
   */
  @JsonProperty("elapsedTimeInSeconds")
  private Integer elapsedTimeInSeconds;
}
