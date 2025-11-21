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

import aero.sita.messaging.mercury.e2e.model.mongodb.IncomingMessage;
import aero.sita.messaging.mercury.e2e.model.mongodb.OutgoingMessage;
import aero.sita.messaging.mercury.e2e.utilities.helper.MessageStoreHelper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Step definitions for MongoDB message-store validations.
 * Provides BDD steps to verify message status in incoming-messages
 * and outgoing-messages collections.
 */
@Slf4j
public class MessageStoreSteps {

  @Autowired
  private MessageStoreHelper messageStoreHelper;

  @Autowired
  private CommonSteps commonSteps;

  private IncomingMessage currentIncomingMessage;
  private OutgoingMessage currentOutgoingMessage;

  // ==================== INCOMING MESSAGE STEPS ====================

  /**
   * Wait for and retrieve incoming message from MongoDB by messageIdentity.
   * This is the recommended approach for finding messages.
   * <p>
   * Example Gherkin:
   * Then the incoming message should be found in MongoDB within 30 seconds
   */
  @Then("the incoming message should be found")
  public void theIncomingMessageShouldBeFoundInMongoDBWithinSeconds() {
    String messageIdentity = commonSteps.getMessageIdentity();
    assertThat(messageIdentity)
        .as("Message identity must be set before querying MongoDB")
        .isNotNull();

    log.info("Waiting for incoming message with messageIdentity: {} in MongoDB", messageIdentity);

    // Uses centralized polling configuration for interval
    Optional<IncomingMessage> message = messageStoreHelper.waitForIncomingMessageByMessageIdentity(
        messageIdentity);

    assertThat(message)
        .as("Incoming message with messageIdentity '%s' should be found in MongoDB", messageIdentity)
        .isPresent();

    currentIncomingMessage = message.get();
    log.info("Found incoming message: {} with messageIdentity: {} and correlationId: {}",
        currentIncomingMessage.getId(),
        currentIncomingMessage.getMessageIdentity(),
        currentIncomingMessage.getCorrelationId());
  }

  /**
   * Wait for and retrieve incoming message from MongoDB by content search.
   * This is a fallback approach when messageIdentity is not available.
   * <p>
   * Example Gherkin:
   * Then the incoming message should be found by content in MongoDB within 30 seconds
   */
  @Then("the incoming message should be found by content")
  public void theIncomingMessageShouldBeFoundByContentInMongoDBWithinSeconds() {
    String messageContent = commonSteps.getMessageIdentity();
    assertThat(messageContent)
        .as("Message content must be set before querying MongoDB")
        .isNotNull();

    log.info("Waiting for incoming message containing content in MongoDB");

    // Uses centralized polling configuration for interval
    Optional<IncomingMessage> message = messageStoreHelper.waitForIncomingMessageByContent(
        messageContent);

    assertThat(message)
        .as("Incoming message should be found in MongoDB")
        .isPresent();

    currentIncomingMessage = message.get();
    log.info("Found incoming message: {} with correlationId: {}",
        currentIncomingMessage.getId(), currentIncomingMessage.getCorrelationId());
  }

  // ==================== OUTGOING MESSAGE STEPS ====================

  /**
   * Wait for and retrieve outgoing message from MongoDB by messageIdentity.
   * This is the recommended approach for finding outgoing messages.
   * <p>
   * Example Gherkin:
   * Then the outgoing message should be found in MongoDB within 30 seconds
   */
  @Then("the outgoing message should be found")
  public void theOutgoingMessageShouldBeFoundInMongoDBWithinSeconds() {
    String messageIdentity = commonSteps.getMessageIdentity();
    assertThat(messageIdentity)
        .as("Message identity must be set before querying MongoDB")
        .isNotNull();

    log.info("Waiting for outgoing message with messageIdentity: {} in MongoDB", messageIdentity);

    // Uses centralized polling configuration for interval
    Optional<OutgoingMessage> message = messageStoreHelper.waitForOutgoingMessageByMessageIdentity(
        messageIdentity);

    assertThat(message)
        .as("Outgoing message with messageIdentity '%s' should be found in MongoDB", messageIdentity)
        .isPresent();

    currentOutgoingMessage = message.get();
    log.info("Found outgoing message: {} with messageIdentity: {} and correlationId: {}",
        currentOutgoingMessage.getId(),
        currentOutgoingMessage.getMessageIdentity(),
        currentOutgoingMessage.getCorrelationId());
  }

  /**
   * Wait for and retrieve outgoing message from MongoDB by content search.
   * This is a fallback approach when messageIdentity is not available.
   * <p>
   * Example Gherkin:
   * Then the outgoing message should be found by content in MongoDB within 30 seconds
   */
  @Then("the outgoing message should be found by content")
  public void theOutgoingMessageShouldBeFoundByContentInMongoDBWithinSeconds() {
    String messageIdentity = commonSteps.getMessageIdentity();
    assertThat(messageIdentity)
        .as("Message content must be set before querying MongoDB")
        .isNotNull();

    log.info("Waiting for outgoing message containing content in MongoDB");

    // Uses centralized polling configuration for interval
    Optional<OutgoingMessage> message = messageStoreHelper.waitForOutgoingMessageByContent(
        messageIdentity);

    assertThat(message)
        .as("Outgoing message should be found in MongoDB")
        .isPresent();

    currentOutgoingMessage = message.get();
    log.info("Found outgoing message: {} with correlationId: {}",
        currentOutgoingMessage.getId(), currentOutgoingMessage.getCorrelationId());
  }

  /**
   * Wait for and retrieve outgoing message from MongoDB by incoming message ID.
   * Useful when you want to verify that an incoming message generated an outgoing message.
   * <p>
   * Example Gherkin:
   * Then the outgoing message should be found by incoming message ID in MongoDB within 30 seconds
   */
  @Then("the outgoing message should be found by incoming message ID")
  public void theOutgoingMessageShouldBeFoundByIncomingMessageIdInMongoDBWithinSeconds() {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved first before searching for outgoing message")
        .isNotNull();

    String incomingMessageId = currentIncomingMessage.getId();
    log.info("Waiting for outgoing message with incomingMessageId: {} in MongoDB", incomingMessageId);

    Optional<OutgoingMessage> message = messageStoreHelper.waitForOutgoingMessageByIncomingMessageId(
        incomingMessageId);

    assertThat(message)
        .as("Outgoing message should be found for incoming message ID '%s'", incomingMessageId)
        .isPresent();

    currentOutgoingMessage = message.get();
    log.info("Found outgoing message: {} linked to incoming message: {}",
        currentOutgoingMessage.getId(), incomingMessageId);
  }

  // ==================== INCOMING MESSAGE VALIDATION STEPS ====================

  /**
   * Verify incoming message has specific number of status logs.
   * <p>
   * Example Gherkin:
   * And the incoming message should have 2 status logs
   */
  @And("the incoming message should have {int} status log(s)")
  public void theIncomingMessageShouldHaveStatusLogs(int expectedCount) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking status logs")
        .isNotNull();

    int actualCount = messageStoreHelper.getIncomingMessageStatusCount(currentIncomingMessage);

    assertThat(actualCount)
        .as("Incoming message should have %d status logs", expectedCount)
        .isEqualTo(expectedCount);

    log.info("Incoming message has {} status logs as expected", actualCount);
  }

  /**
   * Verify incoming message contains specific status.
   * <p>
   * Example Gherkin:
   * And the incoming message should have status "RECEIVED"
   */
  @And("the incoming message should have status {string}")
  public void theIncomingMessageShouldHaveStatus(String expectedStatus) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking status")
        .isNotNull();

    boolean hasStatus = messageStoreHelper.incomingMessageHasStatus(
        currentIncomingMessage, expectedStatus);

    assertThat(hasStatus)
        .as("Incoming message should have status '%s'", expectedStatus)
        .isTrue();

    log.info("Incoming message has status '{}' as expected", expectedStatus);
  }

  /**
   * Verify incoming message contains all expected statuses.
   * <p>
   * Example Gherkin:
   * And the incoming message should have statuses:
   * | RECEIVED |
   * | PARSED   |
   */
  @And("the incoming message should have statuses:")
  public void theIncomingMessageShouldHaveStatuses(List<String> expectedStatuses) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking statuses")
        .isNotNull();

    // Wait for all expected statuses to appear (polls MongoDB with fresh data)
    boolean hasStatuses = messageStoreHelper.waitForIncomingMessageStatuses(
        currentIncomingMessage, expectedStatuses);

    assertThat(hasStatuses)
        .as("Incoming message should have all expected statuses: %s", expectedStatuses)
        .isTrue();

    log.info("Incoming message has all expected statuses: {}", expectedStatuses);
  }

  /**
   * Verify incoming message raw data contains expected text.
   * <p>
   * Example Gherkin:
   * And the incoming message raw data should contain "AVS"
   */
  @And("the incoming message raw data should contain {string}")
  public void theIncomingMessageRawDataShouldContain(String expectedText) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking raw data")
        .isNotNull();

    assertThat(currentIncomingMessage.getRawData())
        .as("Incoming message raw data should contain '%s'", expectedText)
        .contains(expectedText);

    log.info("Incoming message raw data contains expected text: {}", expectedText);
  }

  /**
   * Verify incoming message raw data matches the exact Type B message sent.
   * This is critical for UNKNOWN_ORIGIN_INDICATOR validation.
   * <p>
   * Example: rawData should be exactly:
   * QD SWIRI1G
   * .MILXT 122121/118132DIO
   * AVS
   * JU0580L30AUG LA BEGBCN
   * <p>
   * Example Gherkin:
   * And the incoming message raw data should match the sent type-b message
   */
  @And("the incoming message raw data should match the sent type-b message")
  public void theIncomingMessageRawDataShouldMatchTheSentTypeBMessage() {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking raw data")
        .isNotNull();

    String rawData = currentIncomingMessage.getRawData();
    assertThat(rawData)
        .as("Incoming message raw data should not be null or empty")
        .isNotNull()
        .isNotEmpty();

    log.info("Incoming message raw data matches the sent Type B message");
    log.debug("Raw data: {}", rawData);
  }

  /**
   * Verify incoming message has specific incoming format.
   * <p>
   * Example Gherkin:
   * And the incoming message format should be "TYPE_B"
   */
  @And("the incoming message format should be {string}")
  public void theIncomingMessageFormatShouldBe(String expectedFormat) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking format")
        .isNotNull();

    assertThat(currentIncomingMessage.getIncomingFormat())
        .as("Incoming message format should be '%s'", expectedFormat)
        .isEqualTo(expectedFormat);

    log.info("Incoming message format is '{}' as expected", expectedFormat);
  }

  /**
   * Verify incoming message has specific message identity.
   * <p>
   * Example Gherkin:
   * And the incoming message identity should be "121437/160B99PSA"
   */
  @And("the incoming message identity should be {string}")
  public void theIncomingMessageIdentityShouldBe(String expectedIdentity) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking message identity")
        .isNotNull();

    assertThat(currentIncomingMessage.getMessageIdentity())
        .as("Incoming message identity should be '%s'", expectedIdentity)
        .isEqualTo(expectedIdentity);

    log.info("Incoming message identity is '{}' as expected", expectedIdentity);
  }

  /**
   * Verify incoming message has specific error code.
   * <p>
   * Example Gherkin:
   * And the incoming message should have error code "UNKNOWN_ORIGIN_INDICATOR"
   */
  @And("the incoming message should have error code {string}")
  public void theIncomingMessageShouldHaveErrorCode(String expectedErrorCode) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking error code")
        .isNotNull();

    assertThat(currentIncomingMessage.getErrors())
        .as("Incoming message should have errors list")
        .isNotNull()
        .isNotEmpty();

    boolean hasError = currentIncomingMessage.getErrors().stream()
        .anyMatch(error -> error != null && expectedErrorCode.equals(error.getErrorCode()));

    assertThat(hasError)
        .as("Incoming message should have error code '%s'", expectedErrorCode)
        .isTrue();

    log.info("Incoming message has error code '{}' as expected", expectedErrorCode);
  }

  /**
   * Verify incoming message has specific number of errors.
   * <p>
   * Example Gherkin:
   * And the incoming message should have 1 error
   */
  @And("the incoming message should have {int} error(s)")
  public void theIncomingMessageShouldHaveErrors(int expectedCount) {
    assertThat(currentIncomingMessage)
        .as("Incoming message must be retrieved before checking errors")
        .isNotNull();

    int actualCount = currentIncomingMessage.getErrors() != null ? currentIncomingMessage.getErrors().size() : 0;

    assertThat(actualCount)
        .as("Incoming message should have %d error(s)", expectedCount)
        .isEqualTo(expectedCount);

    log.info("Incoming message has {} error(s) as expected", actualCount);
  }

  // ==================== OUTGOING MESSAGE VALIDATION STEPS ====================

  /**
   * Verify outgoing message has specific number of status logs.
   * <p>
   * Example Gherkin:
   * And the outgoing message should have 4 status logs
   */
  @And("the outgoing message should have {int} status log(s)")
  public void theOutgoingMessageShouldHaveStatusLogs(int expectedCount) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking status logs")
        .isNotNull();

    int actualCount = messageStoreHelper.getOutgoingMessageStatusCount(currentOutgoingMessage);

    assertThat(actualCount)
        .as("Outgoing message should have %d status logs", expectedCount)
        .isEqualTo(expectedCount);

    log.info("Outgoing message has {} status logs as expected", actualCount);
  }

  /**
   * Verify outgoing message contains specific status.
   * <p>
   * Example Gherkin:
   * And the outgoing message should have status "SENT"
   */
  @And("the outgoing message should have status {string}")
  public void theOutgoingMessageShouldHaveStatus(String expectedStatus) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking status")
        .isNotNull();

    boolean hasStatus = messageStoreHelper.outgoingMessageHasStatus(
        currentOutgoingMessage, expectedStatus);

    assertThat(hasStatus)
        .as("Outgoing message should have status '%s'", expectedStatus)
        .isTrue();

    log.info("Outgoing message has status '{}' as expected", expectedStatus);
  }

  /**
   * Verify outgoing message status logs are in the correct order.
   * For UNKNOWN_ORIGIN_INDICATOR error, the expected order is:
   * 1. ERRORS_IDENTIFIED (first position)
   * 2. DISPATCHED (second position)
   * 3. DELIVERED (third position)
   * 4. PREPARED_TO_DELIVER (fourth position)
   * <p>
   * Example Gherkin:
   * And the outgoing message status logs should be in order:
   * | ERRORS_IDENTIFIED    |
   * | DISPATCHED           |
   * | DELIVERED            |
   * | PREPARED_TO_DELIVER  |
   */
  @And("the outgoing message status logs should be in order:")
  public void theOutgoingMessageStatusLogsShouldBeInOrder(List<String> expectedStatusesInOrder) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking status logs order")
        .isNotNull();

    List<String> actualStatuses = messageStoreHelper.getOutgoingMessageStatuses(currentOutgoingMessage);

    assertThat(actualStatuses)
        .as("Outgoing message status logs should be in the expected order")
        .containsExactlyElementsOf(expectedStatusesInOrder);

    log.info("Outgoing message status logs are in the correct order: {}", expectedStatusesInOrder);
  }

  @And("The outgoing message should have error code:")
  public void theOutgoingMessageErrorCode(List<String> expectedErrorCode) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking error code order")
        .isNotNull();

    List<String> actualErrorCode = messageStoreHelper.getOutgoingMessageErrorCode(currentOutgoingMessage);

    assertThat(actualErrorCode)
        .as("Outgoing message error code should be in the expected order")
        .containsExactlyElementsOf(expectedErrorCode);

    log.info("Outgoing message error code are in the correct order: {}", expectedErrorCode);
  }

  @And("The incoming message should have error code:")
  public void theInComingMessageErrorCode(List<String> expectedErrorCode) {
    assertThat(currentIncomingMessage)
        .as("InComing message must be retrieved before checking error code order")
        .isNotNull();

    List<String> actualErrorCode = messageStoreHelper.getInComingMessageErrorCode(currentIncomingMessage);

    assertThat(actualErrorCode)
        .as("InComing message error code should be in the expected order")
        .containsExactlyElementsOf(expectedErrorCode);

    log.info("InComing message error code are in the correct order: {}", expectedErrorCode);
  }

  /**
   * Verify outgoing message status at specific position.
   * Positions are 1-indexed (first position = 1).
   * <p>
   * Example Gherkin:
   * And the outgoing message should have status "ERRORS_IDENTIFIED" at position 1
   */
  @And("the outgoing message should have status {string} at position {int}")
  public void theOutgoingMessageShouldHaveStatusAtPosition(String expectedStatus, int position) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking status at position")
        .isNotNull();

    List<String> actualStatuses = messageStoreHelper.getOutgoingMessageStatuses(currentOutgoingMessage);

    assertThat(actualStatuses)
        .as("Outgoing message should have at least %d status logs", position)
        .hasSizeGreaterThanOrEqualTo(position);

    String actualStatus = actualStatuses.get(position - 1); // Convert to 0-indexed

    assertThat(actualStatus)
        .as("Outgoing message should have status '%s' at position %d", expectedStatus, position)
        .isEqualTo(expectedStatus);

    log.info("Outgoing message has status '{}' at position {} as expected", expectedStatus, position);
  }

  /**
   * Verify outgoing message contains all expected statuses.
   * <p>
   * Example Gherkin:
   * And the outgoing message should have statuses:
   * | CREATED |
   * | SENT    |
   */
  @And("the outgoing message should have statuses:")
  public void theOutgoingMessageShouldHaveStatuses(List<String> expectedStatuses) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking statuses")
        .isNotNull();

    // Wait for all expected statuses to appear (polls MongoDB with fresh data)
    boolean hasStatuses = messageStoreHelper.waitForOutgoingMessageStatuses(
        currentOutgoingMessage, expectedStatuses);

    assertThat(hasStatuses)
        .as("Outgoing message should have all expected statuses: %s", expectedStatuses)
        .isTrue();

    log.info("Outgoing message has all expected statuses: {}", expectedStatuses);
  }

  /**
   * Verify outgoing message raw data contains expected text.
   * <p>
   * Example Gherkin:
   * And the outgoing message raw data should contain "AVS"
   */
  @And("the outgoing message raw data should contain {string}")
  public void theOutgoingMessageRawDataShouldContain(String expectedText) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking raw data")
        .isNotNull();

    assertThat(currentOutgoingMessage.getRawData())
        .as("Outgoing message raw data should contain '%s'", expectedText)
        .contains(expectedText);

    log.info("Outgoing message raw data contains expected text: {}", expectedText);
  }

  /**
   * Verify outgoing message body contains expected text.
   * For UNKNOWN_ORIGIN_INDICATOR error, the message body should contain:
   * "PLS RPT YR ..... DUE TO:" and "1. UNKNOWN_ORIGIN_INDICATOR"
   * <p>
   * Example Gherkin:
   * And the outgoing message body should contain "PLS RPT YR ..... DUE TO:"
   */
  @And("the outgoing message body should contain {string}")
  public void theOutgoingMessageBodyShouldContain(String expectedText) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking message body")
        .isNotNull();

    assertThat(currentOutgoingMessage.getMessageBody())
        .as("Outgoing message body should contain '%s'", expectedText)
        .contains(expectedText);

    log.info("Outgoing message body contains expected text: {}", expectedText);
  }

  /**
   * Verify outgoing message body contains error in list format.
   * For UNKNOWN_ORIGIN_INDICATOR error, the message body should contain:
   * "1. UNKNOWN_ORIGIN_INDICATOR"
   * <p>
   * Example Gherkin:
   * And the outgoing message body should contain error "UNKNOWN_ORIGIN_INDICATOR" in the list
   */
  @And("the outgoing message body should contain error {string} in the list")
  public void theOutgoingMessageBodyShouldContainErrorInTheList(String errorCode) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking message body")
        .isNotNull();

    String expectedErrorText = "1. " + errorCode;

    assertThat(currentOutgoingMessage.getMessageBody())
        .as("Outgoing message body should contain '%s'", expectedErrorText)
        .contains(expectedErrorText);

    log.info("Outgoing message body contains error '{}' in the list", errorCode);
  }

  /**
   * Verify outgoing message has specific outgoing format.
   * <p>
   * Example Gherkin:
   * And the outgoing message format should be "TYPE_B"
   */
  @And("the outgoing message format should be {string}")
  public void theOutgoingMessageFormatShouldBe(String expectedFormat) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking format")
        .isNotNull();

    assertThat(currentOutgoingMessage.getOutgoingFormat())
        .as("Outgoing message format should be '%s'", expectedFormat)
        .isEqualTo(expectedFormat);

    log.info("Outgoing message format is '{}' as expected", expectedFormat);
  }

  /**
   * Verify outgoing message has specific error code.
   * <p>
   * Example Gherkin:
   * And the outgoing message should have error code "UNKNOWN_ORIGIN_INDICATOR"
   */
  @And("the outgoing message should have error code {string}")
  public void theOutgoingMessageShouldHaveErrorCode(String expectedErrorCode) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking error code")
        .isNotNull();

    List<OutgoingMessage.MessageError> actualErrors = currentOutgoingMessage.getErrors();

    assertThat(actualErrors)
        .as("Outgoing message should have an errors list")
        .isNotNull()
        .isNotEmpty();

    // Collect the codes for a clear assertion + readable failure
    List<String> actualCodes = actualErrors.stream()
        .map(OutgoingMessage.MessageError::getErrorCode)   // <-- adjust to getErrorCode() if needed
        .toList();

    assertThat(actualCodes)
        .as("Expected error code '%s' to be present. Actual codes: %s", expectedErrorCode, actualCodes)
        .contains(expectedErrorCode);

    log.info("Outgoing message has error code '{}' as expected", expectedErrorCode);
  }

  /**
   * Verify outgoing message has specific number of errors.
   * <p>
   * Example Gherkin:
   * And the outgoing message should have 1 error
   */
  @And("the outgoing message should have {int} error(s)")
  public void theOutgoingMessageShouldHaveErrors(int expectedCount) {
    assertThat(currentOutgoingMessage)
        .as("Outgoing message must be retrieved before checking errors")
        .isNotNull();

    int actualCount = currentOutgoingMessage.getErrors() != null ? currentOutgoingMessage.getErrors().size() : 0;

    assertThat(actualCount)
        .as("Outgoing message should have %d error(s)", expectedCount)
        .isEqualTo(expectedCount);

    log.info("Outgoing message has {} error(s) as expected", actualCount);
  }

  /**
   * Logs the incoming message for debugging.
   * <p>
   * Example Gherkin:
   * And I log the incoming message
   */
  @And("I log the incoming message")
  public void logTheIncomingMessage() {
    if (currentIncomingMessage == null) {
      log.warn("No incoming message to log");
      return;
    }

    log.info("=== INCOMING MESSAGE ===");
    log.info("ID: {}", currentIncomingMessage.getId());
    log.info("Message Identity: {}", currentIncomingMessage.getMessageIdentity());
    log.info("Correlation ID: {}", currentIncomingMessage.getCorrelationId());
    log.info("Incoming Format: {}", currentIncomingMessage.getIncomingFormat());
    log.info("Errors: {}", currentIncomingMessage.getErrors());
    log.info("Status Logs: {}", currentIncomingMessage.getStatusLogs());
    log.info("Raw Data:");
    log.info("{}", currentIncomingMessage.getRawData());
    log.info("========================");
  }

  /**
   * Logs the outgoing message for debugging.
   * <p>
   * Example Gherkin:
   * And I log the outgoing message
   */
  @And("I log the outgoing message")
  public void logTheOutgoingMessage() {
    if (currentOutgoingMessage == null) {
      log.warn("No outgoing message to log");
      return;
    }

    log.info("=== OUTGOING MESSAGE ===");
    log.info("ID: {}", currentOutgoingMessage.getId());
    log.info("Message Identity: {}", currentOutgoingMessage.getMessageIdentity());
    log.info("Correlation ID: {}", currentOutgoingMessage.getCorrelationId());
    log.info("Outgoing Format: {}", currentOutgoingMessage.getOutgoingFormat());
    log.info("Errors: {}", currentOutgoingMessage.getErrors());
    log.info("Status Logs: {}", currentOutgoingMessage.getStatusLogs());
    log.info("Raw Data:");
    log.info("{}", currentOutgoingMessage.getRawData());
    log.info("Message Body:");
    log.info("{}", currentOutgoingMessage.getMessageBody());
    log.info("========================");
  }
}

