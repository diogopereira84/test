/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.config;

import java.time.Duration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration class for REST client components.
 * This configuration provides a RestTemplate bean with customized settings
 * including timeouts, interceptors for logging, and error handling.
 * Following the Single Responsibility Principle, this class focuses solely
 * on REST client configuration.
 * Updated to use Spring Boot 3.4+ non-deprecated timeout configuration methods:
 * - connectTimeout(Duration) instead of setConnectTimeout(Duration)
 * - readTimeout(Duration) instead of setReadTimeout(Duration)
 */
@Configuration
public class RestClientConfiguration {

  /**
   * Creates and configures a RestTemplate bean for HTTP communication.
   * The RestTemplate is configured with:
   * - Connection timeout: 10 seconds
   * - Read timeout: 30 seconds
   * - Request/response logging interceptor
   *
   * @param builder the RestTemplateBuilder provided by Spring Boot
   * @return configured RestTemplate instance
   */
  @Bean
  public RestTemplate restTemplate(
      RestTemplateBuilder builder) {
    return builder
        .connectTimeout(Duration.ofSeconds(10))
        .readTimeout(Duration.ofSeconds(30))
        .interceptors(loggingInterceptor())
        .build();
  }

  /**
   * Creates a logging interceptor for REST requests and responses.
   *
   * @return ClientHttpRequestInterceptor for logging
   */
  private ClientHttpRequestInterceptor loggingInterceptor() {
    return new LoggingInterceptor();
  }
}