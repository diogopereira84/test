/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.helper;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.stereotype.Component;

/**
 * Generic polling utility for waiting on asynchronous operations using Awaitility.
 * This helper provides a reusable polling mechanism that can be applied
 * to any operation that may not immediately return a result.
 * REFACTORED: Now uses Awaitility library instead of custom Thread.sleep() polling.
 * This provides better readability, maintainability, and more robust polling capabilities.
 * Following SOLID principles:
 * - Single Responsibility: Focuses solely on polling logic
 * - Open/Closed: Extensible through Supplier pattern without modification
 * - Dependency Inversion: Depends on Supplier abstraction, not concrete implementations
 * This is a framework utility designed for test automation engineers to
 * handle asynchronous message arrival, database queries, and other
 * eventually-consistent operations.
 */
@Slf4j
@Component
public class PollingHelper {

  /**
   * Polls for a result until it is not null or a timeout is reached.
   * This method repeatedly executes the provided supplier function
   * until it returns a non-null value or the timeout expires.
   * Uses Awaitility for robust polling with automatic retry and timeout handling.
   *
   * @param supplier       the supplier function that provides the result
   * @param timeoutSeconds the maximum time to wait in seconds
   * @param intervalMillis the interval between polling attempts in milliseconds
   * @param <T>            the type of the result
   * @return the result if found, or null if the timeout is reached
   */
  public <T> T poll(Supplier<T> supplier, int timeoutSeconds, long intervalMillis) {
    return poll(supplier, timeoutSeconds, TimeUnit.SECONDS, intervalMillis, TimeUnit.MILLISECONDS);
  }

  /**
   * Polls for a result until it is not null or a timeout is reached.
   * This is the core polling method with full TimeUnit support.
   * Uses Awaitility for robust polling with automatic retry and timeout handling.
   *
   * @param supplier     the supplier function that provides the result
   * @param timeout      the timeout value
   * @param timeoutUnit  the time unit for the timeout
   * @param interval     the interval between polling attempts
   * @param intervalUnit the time unit for the interval
   * @param <T>          the type of the result
   * @return the result if found, or null if the timeout is reached
   */
  public <T> T poll(Supplier<T> supplier, long timeout, TimeUnit timeoutUnit,
                    long interval, TimeUnit intervalUnit) {
    long timeoutMillis = timeoutUnit.toMillis(timeout);
    long intervalMillis = intervalUnit.toMillis(interval);

    log.debug("Starting polling with Awaitility: timeout={}ms, interval={}ms",
        timeoutMillis, intervalMillis);

    try {
      // Use Awaitility to poll until the supplier returns a non-null value
      T result = await()
          .atMost(Duration.ofMillis(timeoutMillis))
          .pollInterval(Duration.ofMillis(intervalMillis))
          .pollDelay(Duration.ZERO)
          .ignoreExceptions()
          .until(() -> {
            T value = supplier.get();
            log.debug("Polling attempt: result={}", value != null ? "found" : "not found yet");
            return value;
          }, Objects::nonNull);

      log.info("Polling successful: result found within timeout");
      return result;

    } catch (ConditionTimeoutException e) {
      log.warn("Polling timed out after {}ms. Result not found.", timeoutMillis);
      return null;
    } catch (Exception e) {
      log.error("Polling failed with unexpected error", e);
      return null;
    }
  }

  /**
   * Polls for a result with exponential backoff strategy.
   * The interval between attempts increases exponentially up to a maximum value.
   * This is useful for operations that are expected to take longer over time.
   * Uses Awaitility's FibonacciPollInterval for exponential backoff behavior.
   *
   * @param supplier              the supplier function that provides the result
   * @param timeoutSeconds        the maximum time to wait in seconds
   * @param initialIntervalMillis the initial interval in milliseconds
   * @param backoffMultiplier     the multiplier for exponential backoff (e.g., 2.0 for doubling)
   * @param maxIntervalMillis     the maximum interval between attempts in milliseconds
   * @param <T>                   the type of the result
   * @return the result if found, or null if the timeout is reached
   */
  public <T> T pollWithExponentialBackoff(Supplier<T> supplier, int timeoutSeconds,
                                          long initialIntervalMillis, double backoffMultiplier,
                                          long maxIntervalMillis) {
    long timeoutMillis = timeoutSeconds * 1000L;

    log.debug("Starting polling with exponential backoff using Awaitility: timeout={}ms, " + "initialInterval={}ms, backoffMultiplier={}, maxInterval={}ms",
        timeoutMillis, initialIntervalMillis, backoffMultiplier, maxIntervalMillis);

    try {
      // Use Awaitility with exponential backoff
      // Note: Awaitility's FibonacciPollInterval provides exponential-like behavior
      // For custom exponential backoff, we use iterativePollInterval
      T result = await()
          .atMost(Duration.ofMillis(timeoutMillis))
          .pollDelay(Duration.ofMillis(initialIntervalMillis))
          .pollInterval(new org.awaitility.pollinterval.IterativePollInterval(duration -> {
            long currentMillis = duration.toMillis();
            long nextMillis = (long) (currentMillis * backoffMultiplier);
            long cappedMillis = Math.min(nextMillis, maxIntervalMillis);
            return Duration.ofMillis(cappedMillis);
          }))
          .ignoreExceptions()
          .until(() -> {
            T value = supplier.get();
            log.debug("Polling with backoff attempt: result={}",
                value != null ? "found" : "not found yet");
            return value;
          }, Objects::nonNull);

      log.info("Polling with backoff successful: result found within timeout");
      return result;

    } catch (ConditionTimeoutException e) {
      log.warn("Polling with backoff timed out after {}ms. Result not found.", timeoutMillis);
      return null;
    } catch (Exception e) {
      log.error("Polling with backoff failed with unexpected error", e);
      return null;
    }
  }

  /**
   * Waits for a specified duration without polling.
   * This is a simple utility method for explicit waits in test scenarios.
   * Uses Awaitility for consistent waiting behavior.
   *
   * @param seconds the number of seconds to wait
   */
  public void waitFor(int seconds) {
    log.info("Waiting for {} seconds using Awaitility", seconds);
    await()
        .pollDelay(Duration.ofSeconds(seconds))
        .until(() -> true);
  }

  /**
   * Waits for a specified duration in milliseconds without polling.
   * This is a simple utility method for explicit waits in test scenarios.
   * Uses Awaitility for consistent waiting behavior.
   *
   * @param millis the number of milliseconds to wait
   */
  public void waitForMillis(long millis) {
    log.debug("Waiting for {} milliseconds using Awaitility", millis);
    await()
        .pollDelay(Duration.ofMillis(millis))
        .until(() -> true);
  }

  /**
   * Polls for a boolean condition until it returns true or a timeout is reached.
   * This is useful for waiting on conditions that return boolean values.
   *
   * @param condition      the callable that returns a boolean condition
   * @param timeoutSeconds the maximum time to wait in seconds
   * @param intervalMillis the interval between polling attempts in milliseconds
   * @return true if the condition was met, false if timeout occurred
   */
  public boolean pollUntilTrue(Callable<Boolean> condition, int timeoutSeconds, long intervalMillis) {
    log.debug("Starting boolean condition polling with Awaitility: timeout={}s, interval={}ms",
        timeoutSeconds, intervalMillis);

    try {
      await()
          .atMost(Duration.ofSeconds(timeoutSeconds))
          .pollInterval(Duration.ofMillis(intervalMillis))
          .pollDelay(Duration.ZERO)
          .ignoreExceptions()
          .until(condition);

      log.info("Boolean condition polling successful: condition met within timeout");
      return true;

    } catch (ConditionTimeoutException e) {
      log.warn("Boolean condition polling timed out after {}s. Condition not met.", timeoutSeconds);
      return false;
    } catch (Exception e) {
      log.error("Boolean condition polling failed with unexpected error", e);
      return false;
    }
  }
}

