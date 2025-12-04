/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for polling operations.
 * Centralizes all polling-related configuration values.
 * Timeout values are in seconds, intervals in milliseconds.
 * Values can be overridden in environment-specific property files.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "polling")
public class PollingProperties {

  /**
   * Default polling configuration for general use.
   */
  private final DefaultConfig defaultConfig = new DefaultConfig();

  /**
   * Polling configuration for message retrieval operations (test-harness).
   */
  private final MessageRetrievalConfig messageRetrieval = new MessageRetrievalConfig();

  /**
   * Polling configuration for message store operations (MongoDB).
   */
  private final MessageStoreConfig messageStore = new MessageStoreConfig();

  /**
   * Polling configuration for status validation (waiting for statuses to appear).
   */
  private final StatusValidationConfig statusValidation = new StatusValidationConfig();

  /**
   * Polling configuration specific to reject message retrieval.
   */
  private final RejectMessageConfig rejectMessage = new RejectMessageConfig();

  /**
   * Exponential backoff polling configuration.
   */
  private final ExponentialConfig exponential = new ExponentialConfig();

  @Data
  public static class DefaultConfig {
    private int timeoutSeconds = 10;
    private long intervalMillis = 500;
  }

  @Data
  public static class MessageRetrievalConfig {
    private int timeoutSeconds = 10;
    private long intervalMillis = 1500;
  }

  @Data
  public static class MessageStoreConfig {
    private int timeoutSeconds = 30;
    private long intervalMillis = 1000;
  }

  @Data
  public static class StatusValidationConfig {
    private int timeoutSeconds = 30;
    private long intervalMillis = 1000;
  }

  @Data
  public static class RejectMessageConfig {
    private int timeoutSeconds = 10;
    private long intervalMillis = 1500;
  }

  @Data
  public static class ExponentialConfig {
    /**
     * Total timeout (in seconds).
     */
    private int timeoutSeconds = 30;

    /**
     * Initial backoff interval (in milliseconds).
     */
    private long initialIntervalMillis = 100;

    /**
     * Backoff multiplier (e.g., 2.0 doubles the interval).
     */
    private double backoffMultiplier = 2.0;

    /**
     * Maximum backoff interval (in milliseconds).
     */
    private long maxIntervalMillis = 5000;
  }
}