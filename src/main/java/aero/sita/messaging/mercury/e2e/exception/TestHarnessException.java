/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.exception;

/**
 * Exception thrown when communication with the test-harness fails.
 * This exception wraps underlying HTTP or network errors and provides
 * a consistent error handling mechanism for test-harness operations.
 * By using a custom exception instead of generic RuntimeException,
 * we provide better error context and allow for more specific
 * exception handling in calling code.
 * Example usage:
 * <pre>
 * try {
 *     testHarnessClient.sendMessage(request);
 * } catch (TestHarnessException e) {
 *     log.error("Test harness communication failed", e);
 *     // Handle test-harness specific error
 * }
 * </pre>
 */
public class TestHarnessException extends RuntimeException {

  /**
   * Constructs a new TestHarnessException with the specified message.
   *
   * @param message the detail message explaining the error
   */
  public TestHarnessException(String message) {
    super(message);
  }

  /**
   * Constructs a new TestHarnessException with the specified message and cause.
   * <p>
   * This is the most commonly used constructor, wrapping underlying
   * exceptions (like RestClientException) with additional context.
   *
   * @param message the detail message explaining the error
   * @param cause   the underlying cause of the exception
   */
  public TestHarnessException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new TestHarnessException with the specified cause.
   *
   * @param cause the underlying cause of the exception
   */
  public TestHarnessException(Throwable cause) {
    super(cause);
  }
}
