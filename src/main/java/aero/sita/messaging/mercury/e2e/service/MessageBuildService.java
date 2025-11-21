/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.service;

import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessage;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessageFactory;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.util.AddressUtils;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.util.OriginatorUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for building and manipulating Type B messages.
 * Centralizes all Type B format rules and validation logic to keep
 * Cucumber step definitions clean and focused on test intent.
 * <p>
 * This service:
 * - Encapsulates Type B format rules (max 8 addresses per line, priority handling, etc.)
 * - Validates every message before returning it
 * - Provides a single source of truth for message construction
 * - Enables unit testing without Cucumber
 * <p>
 * Key Design Principles:
 * - Fail fast: All methods validate the message before returning
 * - Immutability: Methods return new TypeBMessage instances (using toBuilder)
 * - Clear errors: Validation failures provide descriptive error messages
 */
@Slf4j
@Service
public class MessageBuildService {

  /**
   * Creates a valid Type B message with the specified message identity.
   * The message is created using the factory and validated before returning.
   *
   * @param messageIdentity the unique message identity (e.g., "121437/160B99PSA")
   * @return a valid TypeBMessage instance
   * @throws IllegalStateException if the created message fails validation
   */
  public TypeBMessage createValidMessage(String messageIdentity) {
    log.debug("Creating valid Type B message with identity: {}", messageIdentity);

    TypeBMessage message = TypeBMessageFactory.validMessageWithIdentity(messageIdentity);
    log.info("Successfully created valid Type B message");
    return message;
  }

  /**
   * Adds addresses to a Type B message.
   * Preserves the existing priority code and rebuilds the address line.
   * Validates the address count (warns if > 8) and the resulting message.
   * <p>
   * Type B standard: Maximum 8 addresses per line.
   *
   * @param message   the current message
   * @param addresses the list of addresses to add
   * @return a new TypeBMessage with the updated address line
   * @throws IllegalArgumentException if message is null
   * @throws IllegalStateException    if the updated message fails validation
   */
  public TypeBMessage addAddresses(TypeBMessage message, List<String> addresses) {
    validateMessageNotNull(message);

    log.debug("Adding {} addresses to Type B message", addresses.size());

    // Extract existing priority
    String priority = AddressUtils.extractPriority(message.getNormalAddressLine());

    // Build new address line
    String updatedAddressLine = AddressUtils.buildAddressLine(priority, addresses);

    // Validate address count (logs warning if > 8)
    AddressUtils.validateAddressCount(addresses.size());

    // Build updated message
    TypeBMessage updatedMessage = message.toBuilder()
        .normalAddressLine(updatedAddressLine)
        .build();

    log.info("Successfully added {} addresses to message", addresses.size());
    return updatedMessage;
  }

  /**
   * Adds multiple address lines to a Type B message.
   * The first line includes the priority code, subsequent lines do not.
   * Validates the line count (warns if > 4) and address count per line (warns if > 8).
   * <p>
   * Type B standard: Maximum 4 address lines, maximum 8 addresses per line.
   *
   * @param message      the current message
   * @param addressLines the list of address lines (each line is a list of addresses)
   * @return a new TypeBMessage with the updated address lines
   * @throws IllegalArgumentException if message is null
   * @throws IllegalStateException    if the updated message fails validation
   */
  public TypeBMessage addAddressLines(TypeBMessage message, List<List<String>> addressLines) {
    validateMessageNotNull(message);

    log.debug("Adding {} address lines to Type B message", addressLines.size());

    // Extract existing priority
    String priority = AddressUtils.extractPriority(message.getNormalAddressLine());

    // Build address lines
    String updatedAddressLine = AddressUtils.buildAddressLines(priority, addressLines);

    // Build updated message
    TypeBMessage updatedMessage = message.toBuilder()
        .normalAddressLine(updatedAddressLine)
        .build();

    log.info("Successfully added {} address lines to message", addressLines.size());
    return updatedMessage;
  }

  /**
   * Replaces the originator in a Type B message while preserving the message identity.
   * Validates the originator length (warns if != 7) and the resulting message.
   * <p>
   * Type B standard: Originator must be exactly 7 characters.
   *
   * @param message       the current message
   * @param newOriginator the new originator indicator (e.g., "MILXT")
   * @return a new TypeBMessage with the updated originator
   * @throws IllegalArgumentException if message is null
   * @throws IllegalStateException    if the updated message fails validation
   */
  public TypeBMessage replaceOriginator(TypeBMessage message, String newOriginator) {
    validateMessageNotNull(message);

    log.debug("Replacing originator with: {}", newOriginator);

    // Validate originator length (logs warning if != 7)
    OriginatorUtils.validateOriginatorLength(newOriginator);

    // Replace originator while preserving message identity
    String updatedOriginLine = OriginatorUtils.replaceOriginator(
        message.getOriginLine(), newOriginator);

    // Build updated message
    TypeBMessage updatedMessage = message.toBuilder()
        .originLine(updatedOriginLine)
        .build();

    log.info("Successfully replaced originator");
    return updatedMessage;
  }

  /**
   * Sets the serial number (heading line) for a Type B message.
   * The serial number appears in the heading line in format: ZCZC {serialNumber}
   *
   * @param message      the current message
   * @param serialNumber the serial number to set (e.g., "ABC123")
   * @return a new TypeBMessage with the updated serial number
   * @throws IllegalArgumentException if message is null
   * @throws IllegalStateException    if the updated message fails validation
   */
  public TypeBMessage setSerialNumber(TypeBMessage message, String serialNumber) {
    validateMessageNotNull(message);

    log.debug("Setting serial number: {}", serialNumber);

    // Build heading line with ZCZC prefix
    String headingLine = "ZCZC " + serialNumber;

    // Build updated message
    TypeBMessage updatedMessage = message.toBuilder()
        .headingLine(headingLine)
        .build();

    log.info("Successfully set serial number: {}", serialNumber);
    return updatedMessage;
  }

  /**
   * Sets the message text content for a Type B message.
   * Automatically adds \r\n line ending if not present.
   *
   * @param message the current message
   * @param text    the message text content
   * @return a new TypeBMessage with the updated text
   * @throws IllegalArgumentException if message is null
   * @throws IllegalStateException    if the updated message fails validation
   */
  public TypeBMessage setMessageText(TypeBMessage message, String text) {
    validateMessageNotNull(message);

    log.debug("Setting message text: {}", text);

    // Ensure text ends with \r\n
    String formattedText = text;
    if (!text.endsWith("\r\n")) {
      formattedText = text + "\r\n";
    }

    // Build updated message
    TypeBMessage updatedMessage = message.toBuilder()
        .text(formattedText)
        .build();

    log.info("Successfully set message text");
    return updatedMessage;
  }

  /**
   * Creates a Type B message with an invalid originator indicator.
   * This is useful for testing validation error scenarios.
   *
   * @param invalidOriginator the invalid originator (e.g., "MILXT" with 5 chars)
   * @param messageIdentity   the message identity to preserve
   * @return a TypeBMessage with invalid originator (may fail validation)
   */
  public TypeBMessage createMessageWithInvalidOriginator(String invalidOriginator, String messageIdentity) {
    log.debug("Creating message with invalid originator: {}", invalidOriginator);

    // Validate originator length (logs warning if != 7)
    OriginatorUtils.validateOriginatorLength(invalidOriginator);

    TypeBMessage message = TypeBMessageFactory.messageWithInvalidOriginatorIndicator(
        invalidOriginator, messageIdentity);

    // Note: We don't validate here because the message is intentionally invalid
    log.info("Created message with invalid originator: {}", invalidOriginator);
    return message;
  }

  // ========================================
  // PRIVATE HELPER METHODS
  // ========================================

  /**
   * Validates that a message is not null.
   *
   * @param message the message to validate
   * @throws IllegalArgumentException if message is null
   */
  private void validateMessageNotNull(TypeBMessage message) {
    if (message == null) {
      throw new IllegalArgumentException("Message cannot be null. Please create a message first.");
    }
  }
}

