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
 * Represents the destination details for IBM MQ message sending.
 * Contains server connection information and queue names.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationDetails {

  /**
   * The server address for the destination.
   */
  @JsonProperty("server")
  private String server;

  /**
   * The port number for the destination server.
   */
  @JsonProperty("port")
  private Integer port;

  /**
   * The queue names that messages will be sent to.
   */
  @JsonProperty("destinationNames")
  private List<String> destinationNames;
}
