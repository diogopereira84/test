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
 * <p>
 * SOLID Principles Applied:
 * - Single Responsibility: Encapsulates all waiting/polling/retry logic.
 * - Open/Closed: Extensible for new polling strategies without modifying clients.
 */
@Slf4j
@Component
public class PollingHelper {

  /**
   * Polls for a result until it is not null or a timeout is reached.
   */
  public <T> T poll(Supplier<T> supplier, int timeoutSeconds, long intervalMillis) {
    return poll(supplier, timeoutSeconds, TimeUnit.SECONDS, intervalMillis, TimeUnit.MILLISECONDS);
  }

  public <T> T poll(Supplier<T> supplier, long timeout, TimeUnit timeoutUnit,
                    long interval, TimeUnit intervalUnit) {
    long timeoutMillis = timeoutUnit.toMillis(timeout);
    long intervalMillis = intervalUnit.toMillis(interval);

    log.debug("Starting polling: timeout={}ms, interval={}ms", timeoutMillis, intervalMillis);

    try {
      return await()
          .atMost(Duration.ofMillis(timeoutMillis))
          .pollInterval(Duration.ofMillis(intervalMillis))
          .pollDelay(Duration.ZERO)
          .ignoreExceptions()
          .until(supplier::get, Objects::nonNull);
    } catch (ConditionTimeoutException e) {
      log.warn("Polling timed out after {}ms. Result not found.", timeoutMillis);
      return null;
    } catch (Exception e) {
      log.error("Polling failed with unexpected error", e);
      return null;
    }
  }

  /**
   * Polls until the provided assertion passes (does not throw AssertionError).
   * This is critical for validating lists that populate asynchronously.
   *
   * @param assertionLogic A Runnable containing the assertion logic (e.g. AssertJ assertions).
   * This block will be re-executed until it succeeds or times out.
   * @param timeoutSeconds Maximum time to wait.
   * @param intervalMillis Interval between attempts.
   */
  public void pollUntilAsserted(Runnable assertionLogic, int timeoutSeconds, long intervalMillis) {
    log.debug("Starting assertion polling: timeout={}s, interval={}ms", timeoutSeconds, intervalMillis);

    try {
      await()
          .atMost(Duration.ofSeconds(timeoutSeconds))
          .pollInterval(Duration.ofMillis(intervalMillis))
          .pollDelay(Duration.ZERO)
          .alias("Waiting for assertion to pass")
          .untilAsserted(assertionLogic::run);

      log.info("Assertion polling successful");
    } catch (ConditionTimeoutException e) {
      log.warn("Assertion polling timed out after {}s", timeoutSeconds);
      throw e;
    }
  }

  /**
   * Polls for a result with exponential backoff strategy.
   */
  public <T> T pollWithExponentialBackoff(Supplier<T> supplier, int timeoutSeconds,
                                          long initialIntervalMillis, double backoffMultiplier,
                                          long maxIntervalMillis) {
    long timeoutMillis = timeoutSeconds * 1000L;

    log.debug("Starting polling with exponential backoff: timeout={}ms", timeoutMillis);

    try {
      return await()
          .atMost(Duration.ofMillis(timeoutMillis))
          .pollDelay(Duration.ofMillis(initialIntervalMillis))
          .pollInterval(new org.awaitility.pollinterval.IterativePollInterval(duration -> {
            long currentMillis = duration.toMillis();
            long nextMillis = (long) (currentMillis * backoffMultiplier);
            long cappedMillis = Math.min(nextMillis, maxIntervalMillis);
            return Duration.ofMillis(cappedMillis);
          }))
          .ignoreExceptions()
          .until(supplier::get, Objects::nonNull);

    } catch (ConditionTimeoutException e) {
      log.warn("Polling with backoff timed out after {}ms.", timeoutMillis);
      return null;
    } catch (Exception e) {
      log.error("Polling with backoff failed with unexpected error", e);
      return null;
    }
  }

  public void waitFor(int seconds) {
    log.info("Waiting for {} seconds", seconds);
    await().pollDelay(Duration.ofSeconds(seconds)).until(() -> true);
  }

  public void waitForMillis(long millis) {
    await().pollDelay(Duration.ofMillis(millis)).until(() -> true);
  }

  public boolean pollUntilTrue(Callable<Boolean> condition, int timeoutSeconds, long intervalMillis) {
    log.debug("Starting boolean polling: timeout={}s", timeoutSeconds);
    try {
      await()
          .atMost(Duration.ofSeconds(timeoutSeconds))
          .pollInterval(Duration.ofMillis(intervalMillis))
          .pollDelay(Duration.ZERO)
          .ignoreExceptions()
          .until(condition);
      return true;
    } catch (ConditionTimeoutException e) {
      return false;
    } catch (Exception e) {
      log.error("Boolean polling failed", e);
      return false;
    }
  }
}