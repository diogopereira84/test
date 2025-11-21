/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StreamUtils;

/**
 * Enhanced logging interceptor for REST requests and responses.
 * Provides detailed logging of HTTP communication for debugging and monitoring.
 * <p>
 * IMPORTANT: This interceptor buffers the response body to allow multiple reads.
 * The response body can only be read once from the original stream, so we cache it
 * to enable both logging and subsequent deserialization by RestTemplate.
 */
@Slf4j
public class LoggingInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request,
      byte[] body,
      ClientHttpRequestExecution execution) throws IOException {

    logRequest(request, body);

    ClientHttpResponse response = execution.execute(request, body);

    // Wrap the response to enable multiple reads of the body
    ClientHttpResponse bufferedResponse = new BufferingClientHttpResponseWrapper(response);

    logResponse(bufferedResponse);

    return bufferedResponse;
  }

  private void logRequest(HttpRequest request, byte[] body) {
    if (log.isDebugEnabled()) {
      log.debug("===========================Request Begin===========================");
      log.debug("URI         : {}", request.getURI());
      log.debug("Method      : {}", request.getMethod());
      log.debug("Headers     : {}", request.getHeaders());
      log.debug("Request body: {}", new String(body, StandardCharsets.UTF_8));
      log.debug("===========================Request End=============================");
    }
  }

  private void logResponse(ClientHttpResponse response) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("===========================Response Begin==========================");
      log.debug("Status code  : {}", response.getStatusCode());
      log.debug("Status text  : {}", response.getStatusText());
      log.debug("Headers      : {}", response.getHeaders());

      // Safe to read body multiple times due to buffering wrapper
      byte[] bodyBytes = StreamUtils.copyToByteArray(response.getBody());
      String body = new String(bodyBytes, StandardCharsets.UTF_8);
      log.debug("Response body: {}", body);
      log.debug("===========================Response End============================");
    }
  }

  /**
   * Wrapper that buffers the response body to allow multiple reads.
   * The original response body stream can only be read once, but this wrapper
   * caches the content in memory so it can be read multiple times.
   */
  private static class BufferingClientHttpResponseWrapper implements ClientHttpResponse {

    private final ClientHttpResponse response;
    private byte[] body;

    public BufferingClientHttpResponseWrapper(ClientHttpResponse response) {
      this.response = response;
    }

    @Override
    public InputStream getBody() throws IOException {
      if (body == null) {
        body = StreamUtils.copyToByteArray(response.getBody());
      }
      return new ByteArrayInputStream(body);
    }

    @Override
    public org.springframework.http.HttpHeaders getHeaders() {
      return response.getHeaders();
    }

    @Override
    public org.springframework.http.HttpStatusCode getStatusCode() throws IOException {
      return response.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
      return response.getStatusText();
    }

    @Override
    public void close() {
      response.close();
    }
  }
}


