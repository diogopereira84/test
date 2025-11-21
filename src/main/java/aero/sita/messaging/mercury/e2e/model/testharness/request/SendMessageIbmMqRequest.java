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
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for sending messages to IBM MQ via test-harness.
 * Used for the POST /test-harness/api/v1/ibm/send endpoint.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageIbmMqRequest {

  /**
   * The message content to be sent.
   */
  @JsonProperty("message")
  private String message;

  /**
   * Array of destination details where messages will be sent.
   */
  @JsonProperty("destinationsDetailsList")
  private List<DestinationDetails> destinationsDetailsList;

  /**
   * Optional field to specify the load ID for the generated messages.
   */
  @JsonProperty("loadProfileId")
  private Long loadProfileId;

  /**
   * Optional field to indicate if this is a pre-load operation.
   */
  @JsonProperty("preLoad")
  private Boolean preLoad;
}
