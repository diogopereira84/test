/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps;

import static org.assertj.core.api.Assertions.assertThat;

import aero.sita.messaging.mercury.e2e.cucumber.utilities.parser.RejectMessageBodyParser;
import aero.sita.messaging.mercury.e2e.model.testharness.rejection.RejectMessageBody;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessage;
import aero.sita.messaging.mercury.e2e.utilities.helper.MessageRetrievalHelper;
import io.cucumber.java.en.Then;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * IMPROVED version of MessageVerificationSteps with table-based error verification.
 * <p>
 * This class provides enhanced step definitions for verifying reject messages with:
 * - Table-based error verification (more readable and maintainable)
 * - Backward compatibility with existing step definitions
 * - Detailed error messages showing complete message structure
 * <p>
 * Key improvements:
 * - Use tables instead of multiple "And" statements for error verification
 * - Support verifying multiple errors in one step
 * - Clear, detailed error messages when assertions fail
 */
@Slf4j
public class MessageVerificationSteps {

  @Autowired
  private MessageRetrievalHelper messageRetrievalHelper;

  private ReceivedMessage currentReceivedMessage;

  /**
   * Verifies that the reject message contains all expected errors using a table format.
   * This is more readable and maintainable than multiple "And" statements.
   * <p>
   * Example Gherkin:
   * And the reject message has the following error(s):
   * | UNKNOWN_ORIGIN_INDICATOR      |
   * | TOO_MANY_ADDRESSES_PER_LINE   |
   *
   * @param expectedErrors list of expected error codes
   */
  @Then("the message contains the following error\\(s):")
  public void theMessageContainsTheFollowingErrors(List<String> expectedErrors) {
    // Get the current reject message from MessageVerificationSteps
    currentReceivedMessage = findReceivedMessage();

    assertThat(currentReceivedMessage)
        .as("Reject message must be retrieved before checking errors")
        .isNotNull();

    log.info("Verifying that reject message contains {} error(s): {}", expectedErrors.size(), expectedErrors);

    String messageBody = currentReceivedMessage.getBody();

    // Parse the reject message body
    RejectMessageBody parsedMessage = RejectMessageBodyParser.parse(messageBody);

    // Get actual error codes from parsed message
    List<String> actualErrors = parsedMessage.getErrorCodes();

    // Verify error count matches
    if (actualErrors.size() != expectedErrors.size()) {
      String errorMessage = buildErrorCountMismatchMessage(expectedErrors, actualErrors, parsedMessage);
      log.error("Error count mismatch:\n{}", errorMessage);

      assertThat(actualErrors)
          .as(errorMessage)
          .hasSize(expectedErrors.size());
    }

    // Verify all expected errors are present (order doesn't matter)
    for (String expectedError : expectedErrors) {
      if (!actualErrors.contains(expectedError)) {
        String errorMessage = buildErrorNotFoundMessage(expectedError, actualErrors, parsedMessage);
        log.error("Expected error not found:\n{}", errorMessage);

        assertThat(actualErrors)
            .as(errorMessage)
            .contains(expectedError);
      }
    }

    log.info("Successfully verified reject message contains {} expected error(s)", expectedErrors.size());
  }

  /**
   * Verifies that the reject message contains exactly the expected errors in order.
   * This is stricter than the "contains" version - order matters.
   * <p>
   * Example Gherkin:
   * And the reject message has the following error(s) in order:
   * | UNKNOWN_ORIGIN_INDICATOR      |
   * | TOO_MANY_ADDRESSES_PER_LINE   |
   *
   * @param expectedErrorsInOrder list of expected error codes in exact order
   */
  @Then("the reject message has the following error\\(s) in order:")
  public void theRejectMessageHasTheFollowingErrorsInOrder(List<String> expectedErrorsInOrder) {
    currentReceivedMessage = findReceivedMessage();

    assertThat(currentReceivedMessage)
        .as("Reject message must be retrieved before checking errors")
        .isNotNull();

    log.info("Verifying that reject message contains {} error(s) in order: {}",
        expectedErrorsInOrder.size(), expectedErrorsInOrder);

    String messageBody = currentReceivedMessage.getBody();

    // Parse the reject message body
    RejectMessageBody parsedMessage = RejectMessageBodyParser.parse(messageBody);

    // Get actual error codes from parsed message
    List<String> actualErrors = parsedMessage.getErrorCodes();

    // Verify errors match exactly in order
    if (!actualErrors.equals(expectedErrorsInOrder)) {
      String errorMessage = buildErrorOrderMismatchMessage(expectedErrorsInOrder, actualErrors, parsedMessage);
      log.error("Error order mismatch:\n{}", errorMessage);

      assertThat(actualErrors)
          .as(errorMessage)
          .containsExactlyElementsOf(expectedErrorsInOrder);
    }

    log.info("Successfully verified reject message contains {} expected error(s) in order",
        expectedErrorsInOrder.size());
  }

  /**
   * Verifies that Mercury sent a reject message.
   * Example Gherkin:
   * Then mercury process the message sending a reject message
   */
  @Then("I request the test harness to retrieve all received messages")
  public void mercuryProcessTheMessageSendingARejectMessage() {
    log.info("Verifying that Mercury sent a reject message");

    currentReceivedMessage = findReceivedMessage();
    assertThat(currentReceivedMessage)
        .as("A reject message should have been sent by Mercury")
        .isNotNull();
    String messageBody = currentReceivedMessage.getBody();

    // Verify it's a valid reject message structure
    assertThat(RejectMessageBodyParser.isValidRejectMessageBody(messageBody))
        .as("Message should be a valid reject message with 'PLS RPT YR' header")
        .isTrue();

    log.info("Successfully verified reject message with 'PLS RPT YR'");
  }

  /**
   * BACKWARD COMPATIBLE: Verifies that the reject message contains a specific error.
   * This maintains compatibility with existing feature files.
   * <p>
   * Example Gherkin:
   * And the reject message has the error "UNKNOWN_ORIGIN_INDICATOR" listed
   *
   * @param expectedErrorCode the expected error code
   */
  @Then("the message contains the error {string}")
  public void theRejectMessageHasTheErrorListed(String expectedErrorCode) {
    log.info("Verifying reject message has error: {} (backward compatible method)", expectedErrorCode);

    // Delegate to the table-based method with a single error
    theMessageContainsTheFollowingErrors(List.of(expectedErrorCode));
  }

  /**
   * Helper method to find the reject message from received messages.
   *
   * @return the reject message
   */
  private ReceivedMessage findReceivedMessage() {
    return messageRetrievalHelper.findLatestReceivedMessage();
  }

  /**
   * Builds a detailed error message for error count mismatch.
   */
  private String buildErrorCountMismatchMessage(
      List<String> expectedErrors,
      List<String> actualErrors,
      RejectMessageBody parsedMessage) {

    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append("Reject message error count mismatch\n");
    sb.append("\n");
    sb.append(String.format("Expected %d error(s): %s\n", expectedErrors.size(), expectedErrors));
    sb.append(String.format("Actual %d error(s): %s\n", actualErrors.size(), actualErrors));
    sb.append("\n");
    sb.append("Complete reject message structure:\n");
    sb.append(parsedMessage.toFormattedString());

    return sb.toString();
  }

  /**
   * Builds a detailed error message for missing expected error.
   */
  private String buildErrorNotFoundMessage(
      String expectedError,
      List<String> actualErrors,
      RejectMessageBody parsedMessage) {

    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append(String.format("Expected error '%s' not found in reject message\n", expectedError));
    sb.append("\n");
    sb.append(String.format("Expected error: %s\n", expectedError));
    sb.append(String.format("Actual errors: %s\n", actualErrors));
    sb.append("\n");
    sb.append("Complete reject message structure:\n");
    sb.append(parsedMessage.toFormattedString());

    return sb.toString();
  }

  /**
   * Builds a detailed error message for error order mismatch.
   */
  private String buildErrorOrderMismatchMessage(
      List<String> expectedErrors,
      List<String> actualErrors,
      RejectMessageBody parsedMessage) {

    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append("Reject message errors are not in the expected order\n");
    sb.append("\n");
    sb.append("Expected order:\n");
    for (int i = 0; i < expectedErrors.size(); i++) {
      sb.append(String.format("  %d. %s\n", i + 1, expectedErrors.get(i)));
    }
    sb.append("\n");
    sb.append("Actual order:\n");
    for (int i = 0; i < actualErrors.size(); i++) {
      sb.append(String.format("  %d. %s\n", i + 1, actualErrors.get(i)));
    }
    sb.append("\n");
    sb.append("Complete reject message structure:\n");
    sb.append(parsedMessage.toFormattedString());

    return sb.toString();
  }
}