/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.client.testharness.impl;

import aero.sita.messaging.mercury.e2e.client.testharness.TestHarnessClient;
import aero.sita.messaging.mercury.e2e.client.testharness.TestHarnessEndpoint;
import aero.sita.messaging.mercury.e2e.exception.TestHarnessException;
import aero.sita.messaging.mercury.e2e.model.testharness.request.LatencyRequest;
import aero.sita.messaging.mercury.e2e.model.testharness.request.SendMessageIbmMqRequest;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ClearResponse;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessage;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessagesResponse;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ResultResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation of TestHarnessClient using Spring RestTemplate.
 * Handles HTTP communication with the test-harness REST API.
 */
@Slf4j
@Component
public class TestHarnessClientImpl implements TestHarnessClient {

  private final RestTemplate restTemplate;
  private final String baseUrl;

  @Autowired
  public TestHarnessClientImpl(
      RestTemplate restTemplate,
      @Value("${test-harness.base.url}") String baseUrl) {
    this.restTemplate = restTemplate;
    this.baseUrl = baseUrl;
  }

  @Override
  public void sendMessage(SendMessageIbmMqRequest request) {
    String url = TestHarnessEndpoint.SEND_MESSAGE.buildUrl(baseUrl);

    log.debug("Request payload: {}", request);

    executeHttpOperation("send message to " + url, () -> {
      HttpEntity<SendMessageIbmMqRequest> entity =
          new HttpEntity<>(request, createJsonHeaders());
      return restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    });
  }

  @Override
  public ReceivedMessagesResponse getReceivedMessages() {
    String url = getReceivedMessagesUrl();
    String operation = String.format("retrieve received messages from %s", url);

    return executeHttpOperation(operation, () -> {
      log.info("Executing: {}", operation);

      // Deserialize directly to List<ReceivedMessage> instead of ReceivedMessagesResponse
      ResponseEntity<List<ReceivedMessage>> response = restTemplate.exchange(
          url,
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<List<ReceivedMessage>>() {
          }
      );

      // Wrap the list in ReceivedMessagesResponse
      List<ReceivedMessage> messages = response.getBody();
      ReceivedMessagesResponse result = ReceivedMessagesResponse.builder()
          .receivedMessages(messages != null ? messages : new ArrayList<>())
          .build();

      logMessageCount(result);
      return result;
    });
  }

  @Override
  public ResultResponse getLatency(LatencyRequest request) {
    String url = TestHarnessEndpoint.GET_LATENCY.buildUrl(baseUrl);

    log.debug("Latency request parameters: {}", request);

    ResultResponse result = executeHttpOperation(
        "retrieve latency for injection ID " + request.getInjectionId(),
        () -> {
          HttpEntity<LatencyRequest> entity =
              new HttpEntity<>(request, createJsonHeaders());
          ResponseEntity<ResultResponse> response =
              restTemplate.exchange(url, HttpMethod.POST, entity, ResultResponse.class);
          return validateAndExtractBody(response, ResultResponse::new);
        }
    );

    logLatency(result);
    return result;
  }

  @Override
  public ClearResponse clearMessages() {
    String url = TestHarnessEndpoint.CLEAR_MESSAGES.buildUrl(baseUrl);

    ClearResponse result = executeHttpOperation(
        "clear messages from " + url,
        () -> {
          HttpEntity<Void> entity = new HttpEntity<>(createJsonHeaders());
          ResponseEntity<ClearResponse> response =
              restTemplate.exchange(url, HttpMethod.POST, entity, ClearResponse.class);
          return validateAndExtractBody(response, ClearResponse::new);
        }
    );

    logClearedCount(result);
    return result;
  }

  @Override
  public ResultResponse getResultById(Long id) {
    String url = TestHarnessEndpoint.GET_RESULT_BY_ID.buildUrl(baseUrl, id);

    return executeHttpOperation(
        "retrieve result by ID " + id,
        () -> {
          ResponseEntity<ResultResponse> response =
              restTemplate.getForEntity(url, ResultResponse.class);
          return validateAndExtractBody(response, ResultResponse::new);
        }
    );
  }

  /**
   * Builds the URL for the received messages endpoint.
   *
   * @return the full URL for the received messages endpoint
   */
  private String getReceivedMessagesUrl() {
    return TestHarnessEndpoint.GET_RECEIVED.buildUrl(baseUrl);
  }

  /**
   * Template method for executing HTTP operations with consistent error handling and logging.
   * Reduces code duplication across all HTTP methods.
   *
   * @param operationName Human-readable description of the operation for logging
   * @param operation     The HTTP operation to execute
   * @param <T>           The return type of the operation
   * @return The result of the operation
   * @throws TestHarnessException if the operation fails
   */
  private <T> T executeHttpOperation(String operationName, Supplier<T> operation) {
    log.info("Executing: {}", operationName);

    try {
      T result = operation.get();
      log.info("Successfully completed: {}", operationName);
      return result;

    } catch (HttpClientErrorException e) {
      log.error("Client error ({}): {} - Response: {}",
          e.getStatusCode(), operationName, e.getResponseBodyAsString());
      throw new TestHarnessException(
          String.format("Client error during %s: %s", operationName, e.getStatusCode()), e);

    } catch (HttpServerErrorException e) {
      log.error("Server error ({}): {} - Response: {}",
          e.getStatusCode(), operationName, e.getResponseBodyAsString());
      throw new TestHarnessException(
          String.format("Server error during %s: %s", operationName, e.getStatusCode()), e);

    } catch (ResourceAccessException e) {
      log.error("Network/timeout error: {} - {}", operationName, e.getMessage());
      throw new TestHarnessException(
          String.format("Network error during %s", operationName), e);

    } catch (RestClientException e) {
      log.error("Unexpected REST client error: {} - {}", operationName, e.getMessage(), e);
      throw new TestHarnessException(
          String.format("Unexpected error during %s", operationName), e);
    }
  }

  /**
   * Creates standard JSON headers for HTTP requests.
   * Centralizes header creation to ensure consistency.
   *
   * @return HttpHeaders configured for JSON content
   */
  private HttpHeaders createJsonHeaders() {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    return headers;
  }

  /**
   * Validates response body and provides default if null.
   * Prevents NullPointerException when response body is unexpectedly null.
   *
   * @param response        The HTTP response entity
   * @param defaultSupplier Supplier for default value if body is null
   * @param <T>             The response body type
   * @return The response body or default value
   */
  private <T> T validateAndExtractBody(ResponseEntity<T> response, Supplier<T> defaultSupplier) {
    T body = response.getBody();

    if (body == null) {
      log.warn("Received null response body, using default value");
      return defaultSupplier.get();
    }

    return body;
  }

  /**
   * Logs the count of received messages in a safe manner.
   */
  private void logMessageCount(ReceivedMessagesResponse result) {
    if (result == null || result.getReceivedMessages() == null) {
      log.info("Retrieved 0 received messages");
    } else {
      log.info("Retrieved {} received messages", result.getReceivedMessages().size());
    }
  }

  /**
   * Logs latency information in a safe manner.
   */
  private void logLatency(ResultResponse result) {
    if (result == null) {
      log.info("Latency result: N/A");
    } else {
      log.info("Latency retrieved: {} seconds", result.getElapsedTimeInSeconds());
    }
  }

  /**
   * Logs the count of cleared messages in a safe manner.
   */
  private void logClearedCount(ClearResponse result) {
    if (result == null) {
      log.info("Cleared 0 messages");
    } else {
      log.info("Cleared {} messages", result.getNumberOfMessagesCleared());
    }
  }
}
