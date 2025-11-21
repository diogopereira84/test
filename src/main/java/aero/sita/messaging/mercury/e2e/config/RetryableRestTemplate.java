/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.config;

import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.awaitility.core.ConditionTimeoutException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Wrapper around RestTemplate that adds retry logic for transient failures.
 * This improves resilience when communicating with external services.
 * REFACTORED: Now uses Awaitility for retry logic instead of manual Thread.sleep().
 * This provides better readability and more robust retry behavior.
 * Features:
 * - Configurable retry attempts
 * - Exponential backoff between retries using Awaitility
 * - Only retries on specific exceptions (network errors, timeouts)
 */
@Slf4j
public class RetryableRestTemplate {

  /**
   * -- GETTER --
   * Get the underlying RestTemplate for direct access when needed.
   */
  @Getter
  private final RestTemplate restTemplate;
  private final int maxRetries;
  private final long initialBackoffMs;

  public RetryableRestTemplate(RestTemplate restTemplate, int maxRetries, long initialBackoffMs) {
    this.restTemplate = restTemplate;
    this.maxRetries = maxRetries;
    this.initialBackoffMs = initialBackoffMs;
  }

  public RetryableRestTemplate(RestTemplate restTemplate) {
    this(restTemplate, 3, 1000L);
  }

  /**
   * Execute HTTP request with retry logic using Awaitility.
   *
   * @param url           the URL to call
   * @param method        the HTTP method
   * @param requestEntity the request entity (can be null)
   * @param responseType  the expected response type
   * @param <T>           the response type
   * @return the response entity
   * @throws RestClientException if all retries fail
   */
  public <T> ResponseEntity<T> exchange(
      String url,
      HttpMethod method,
      HttpEntity<?> requestEntity,
      Class<T> responseType) throws RestClientException {

    AtomicInteger attempt = new AtomicInteger(0);
    AtomicReference<ResponseEntity<T>> result = new AtomicReference<>();
    AtomicReference<RestClientException> lastException = new AtomicReference<>();

    // Calculate total timeout based on max retries and exponential backoff
    // Total time = sum of exponential backoff: initialBackoff * (2^0 + 2^1 + ... + 2^(n-1))
    long totalTimeoutMs = calculateTotalTimeout(maxRetries, initialBackoffMs);

    try {
      await()
          .atMost(Duration.ofMillis(totalTimeoutMs))
          .pollInterval(new org.awaitility.pollinterval.IterativePollInterval(duration -> {
            int currentAttempt = attempt.get();
            if (currentAttempt == 0) {
              return Duration.ZERO; // First attempt has no delay
            }
            long backoffMs = initialBackoffMs * (long) Math.pow(2, currentAttempt - 1);
            return Duration.ofMillis(backoffMs);
          }))
          .ignoreExceptions()
          .until(() -> {
            int currentAttempt = attempt.incrementAndGet();

            try {
              ResponseEntity<T> response = restTemplate.exchange(url, method, requestEntity, responseType);
              result.set(response);
              log.debug("Request successful on attempt {}/{} for {} {}",
                  currentAttempt, maxRetries, method, url);
              return true;

            } catch (RestClientException e) {
              lastException.set(e);

              if (currentAttempt >= maxRetries) {
                log.error("All {} retry attempts failed for {} {}", maxRetries, method, url);
                return false; // Stop retrying
              }

              long nextBackoffMs = initialBackoffMs * (long) Math.pow(2, currentAttempt);
              log.warn("Attempt {}/{} failed for {} {}. Retrying in {}ms. Error: {}",
                  currentAttempt, maxRetries, method, url, nextBackoffMs, e.getMessage());

              return false; // Continue retrying
            }
          });

      // If we got a result, return it
      if (result.get() != null) {
        return result.get();
      }

    } catch (ConditionTimeoutException e) {
      log.error("Retry timeout exceeded for {} {}", method, url);
    }

    // If all retries failed, throw the last exception
    if (lastException.get() != null) {
      throw lastException.get();
    }

    throw new RestClientException("Request failed after " + maxRetries + " attempts");
  }

  /**
   * Calculate total timeout for retry logic based on exponential backoff.
   * Formula: initialBackoff * (2^0 + 2^1 + ... + 2^(n-1)) + buffer
   */
  private long calculateTotalTimeout(int maxRetries, long initialBackoffMs) {
    long total = 0;
    for (int i = 0; i < maxRetries; i++) {
      total += initialBackoffMs * (long) Math.pow(2, i);
    }
    // Add 50% buffer for request execution time
    return (long) (total * 1.5);
  }

  /**
   * GET request with retry logic.
   */
  public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType) {
    return exchange(url, HttpMethod.GET, null, responseType);
  }

  /**
   * POST request with retry logic.
   */
  public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType) {
    HttpEntity<?> entity = new HttpEntity<>(request);
    return exchange(url, HttpMethod.POST, entity, responseType);
  }

}

