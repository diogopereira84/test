/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.mongodb;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a status log entry in a Type B message stored in MongoDB.
 * This class maps to the statusLogs array entries in incoming and outgoing messages.
 * Example JSON structure:
 * {
 * "status": "ERRORS_IDENTIFIED",
 * "timestamp": {
 * "$date": "2025-10-13T01:20:00.268Z"
 * }
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusLog {

  /**
   * The status value.
   * Examples for incoming messages: "RECEIVED", "PARSED"
   * Examples for outgoing messages: "ERRORS_IDENTIFIED", "DISPATCHED", "DELIVERED", "PREPARED_TO_DELIVER"
   */
  private String status;

  /**
   * The timestamp when this status was recorded.
   */
  private Instant timestamp;
}