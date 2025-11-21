/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps;

import aero.sita.messaging.mercury.e2e.service.MessageBuildService;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessage;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessageFactory;
import aero.sita.messaging.mercury.e2e.utilities.generator.MessageIdentityGenerator;
import io.cucumber.java.en.Given;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Cucumber step definitions for building Type B messages.
 * Provides steps to create valid and invalid Type B messages for testing.
 * <p>
 * All messages preserve the messageIdentity for MongoDB tracking and correlation.
 * <p>
 * Key Features:
 * - Table-based syntax for addresses (improved readability)
 * - Backward compatibility with existing hardcoded steps
 * - Automatic validation and warning for Type B standard violations
 * - Proper messageIdentity preservation for MongoDB queries
 */
@Slf4j
public class MessageBuildSteps {

  @Autowired
  private CommonSteps commonSteps;

  @Autowired
  private MessageBuildService messageBuildService;

  /**
   * The last injected message content.
   */
  @Getter
  @Setter
  private String lastInjectedMessage;

  /**
   * The last generated message identity.
   */
  @Getter
  @Setter
  private String lastMessageIdentity;

  /**
   * The last exception that occurred during injection.
   */
  @Getter
  @Setter
  private Exception lastException;

  /**
   * The current Type B message being built.
   */
  @Getter
  @Setter
  private TypeBMessage message;


  // ========================================
  // MESSAGE CREATION STEPS
  // ========================================

  /**
   * Creates a valid Type B message with a unique messageIdentity.
   * <p>
   * Example Gherkin:
   * Given a type-b message
   */
  @Given("a type-b message")
  public void createTypeBMessage() {
    log.info("Creating a valid Type B message with unique messageIdentity");

    // Generate unique message identity
    this.lastMessageIdentity = MessageIdentityGenerator.generate();
    log.info("Generated unique messageIdentity: {}", this.lastMessageIdentity);

    // Delegate to service
    this.message = messageBuildService.createValidMessage(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    // Store the message identity for MongoDB queries
    commonSteps.setMessageIdentity(this.lastMessageIdentity);
    log.info("Successfully created Type B message");
  }


  // ========================================
  // TABLE-BASED ADDRESS STEPS
  // ========================================

  /**
   * Adds addresses to the Type B message using a table format.
   * This is more readable and maintainable than hardcoded addresses.
   * <p>
   * Type B standard: Maximum 8 addresses per line.
   * If more than 8 addresses are provided, they will trigger TOO_MANY_ADDRESSES_PER_LINE error.
   * <p>
   * Example Gherkin:
   * And this type-b message has the following addresses:
   * | BARXSXT | LETBCLK | SWIRI1G | LKYSOLT | LKYEDLT | LKYEGLT | LETKJLK | LETJPLK | LETBCLK |
   *
   * @param addresses list of address codes (7-character SITA addresses)
   */
  @Given("this type-b message has the following addresses:")
  public void thisTypeBMessageHasTheFollowingAddresses(List<String> addresses) {
    ensureMessageExists();
    log.info("Adding {} addresses to Type B message", addresses.size());

    // Delegate to service
    this.message = messageBuildService.addAddresses(this.message, addresses);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Successfully added {} addresses to message", addresses.size());
  }

  /**
   * Adds addresses to the Type B message with a specific number of addresses per line.
   * This allows testing multiple address lines with explicit count validation.
   * <p>
   * Example Gherkin:
   * And this type-b message has 9 addresses in one line:
   * | BARXSXT | LETBCLK | SWIRI1G | LKYSOLT | LKYEDLT | LKYEGLT | LETKJLK | LETJPLK | LETBCLK |
   *
   * @param addressCount the expected number of addresses (for validation)
   * @param addresses    list of address codes
   */
  @Given("this type-b message has {int} addresses in one line:")
  public void thisTypeBMessageHasAddressesInOneLineTable(int addressCount, List<String> addresses) {
    if (addresses.size() != addressCount) {
      throw new IllegalArgumentException(
          String.format("Expected %d addresses but got %d", addressCount, addresses.size()));
    }

    log.info("Adding {} addresses in one line (table format)", addressCount);
    thisTypeBMessageHasTheFollowingAddresses(addresses);
  }

  /**
   * Adds multiple lines of addresses to the Type B message.
   * Each line can have up to 8 addresses (Type B standard).
   * <p>
   * Example Gherkin:
   * And this type-b message has the following address lines:
   * | BARXSXT | LETBCLK | SWIRI1G | LKYSOLT | LKYEDLT | LKYEGLT | LETKJLK | LETJPLK |
   * | BARXSXT | LETBCLK | SWIRI1G |
   * | LKYSOLT | LKYEDLT |
   *
   * @param addressLines list of address lines (each line is a list of addresses)
   */
  @Given("this type-b message has the following address lines:")
  public void thisTypeBMessageHasTheFollowingAddressLines(List<List<String>> addressLines) {
    ensureMessageExists();
    log.info("Adding {} address lines to Type B message", addressLines.size());

    // Delegate to service
    this.message = messageBuildService.addAddressLines(this.message, addressLines);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Successfully added {} address lines to message", addressLines.size());
  }


  // ========================================
  // BACKWARD COMPATIBLE STEPS (EXISTING)
  // ========================================

  /**
   * BACKWARD COMPATIBLE: Adds 9 addresses in one line and 5 lines of addresses (hardcoded).
   * This maintains compatibility with existing feature files.
   * <p>
   * Example Gherkin:
   * And this type-b message has 9 addresses in one line and 5 lines of addresses
   */
  @Given("this type-b message has 9 addresses in one line and 5 lines of addresses")
  public void thisTypeBMessageHas9AddressesInOneLineAnd5LinesOfAddresses() {
    log.info("Adding 9 addresses in one line and 5 lines of addresses (backward compatible method)");

    List<List<String>> addressLines = List.of(
        List.of("BARXSXT", "LETBCLK", "SWIRI1G", "LKYSOLT", "LKYEDLT", "LKYEGLT", "LETKJLK", "LETJPLK", "LETBCLK"),
        List.of("BARXSXT"),
        List.of("BARXSXT"),
        List.of("BARXSXT"),
        List.of("BARXSXT")
    );

    thisTypeBMessageHasTheFollowingAddressLines(addressLines);
  }

  /**
   * Adds a specific number of addresses in one line (generated).
   * Useful for testing different address count scenarios without hardcoding addresses.
   * <p>
   * Example Gherkin:
   * And this type-b message has 10 addresses in one line
   *
   * @param addressCount the number of addresses to add
   */
  @Given("this type-b message has {int} addresses in one line")
  public void thisTypeBMessageHasAddressesInOneLine(int addressCount) {
    log.info("Adding {} addresses in one line (generated)", addressCount);

    // Generate dummy addresses
    List<String> addresses = new java.util.ArrayList<>();
    for (int i = 0; i < addressCount; i++) {
      addresses.add(String.format("ADDR%03d", i + 1));
    }

    thisTypeBMessageHasTheFollowingAddresses(addresses);
  }

  /**
   * Adds a specific number of address lines with a specific number of addresses per line.
   * <p>
   * Example Gherkin:
   * And this type-b message has 5 address lines with 9 addresses each
   *
   * @param lineCount        the number of address lines
   * @param addressesPerLine the number of addresses per line
   */
  @Given("this type-b message has {int} address lines with {int} addresses each")
  public void thisTypeBMessageHasAddressLinesWithAddressesEach(int lineCount, int addressesPerLine) {
    log.info("Adding {} address lines with {} addresses each", lineCount, addressesPerLine);

    List<List<String>> addressLines = new java.util.ArrayList<>();
    int addressCounter = 1;

    for (int line = 0; line < lineCount; line++) {
      List<String> addresses = new java.util.ArrayList<>();
      for (int addr = 0; addr < addressesPerLine; addr++) {
        addresses.add(String.format("ADDR%03d", addressCounter++));
      }
      addressLines.add(addresses);
    }

    thisTypeBMessageHasTheFollowingAddressLines(addressLines);
  }


  // ========================================
  // INVALID ORIGINATOR INDICATOR STEPS
  // ========================================

  /**
   * Modifies the Type B message to have an invalid originator address.
   * The originator indicator must be exactly 7 characters (3-char city + 2-char dept + 2-char company).
   * Any deviation from this format triggers UNKNOWN_ORIGIN_INDICATOR error.
   * <p>
   * IMPORTANT: The messageIdentity is preserved after the space separator.
   * <p>
   * Example Gherkin:
   * And this type-b message has an invalid originator address "MILXT"
   *
   * @param invalidOriginatorAddress the invalid originator (e.g., "MILXT" with 5 chars)
   */
  @Given("this type-b message has an invalid originator address {string}")
  public void thisTypeBMessageHasAnInvalidOriginatorAddress(String invalidOriginatorAddress) {
    ensureMessageExists();
    log.info("Modifying Type B message to have an invalid originator address: {}", invalidOriginatorAddress);

    // Delegate to service
    this.message = messageBuildService.replaceOriginator(this.message, invalidOriginatorAddress);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Modified message with invalid originator address: {}", invalidOriginatorAddress);
  }

  /**
   * Modifies the Type B message to have an invalid originator address (default).
   * Uses "MILXT" (5 characters) as the default invalid originator.
   * <p>
   * Example Gherkin:
   * And this type-b message has an invalid originator address
   */
  @Given("this type-b message has an invalid originator address")
  public void thisTypeBMessageHasAnInvalidOriginatorAddress() {
    thisTypeBMessageHasAnInvalidOriginatorAddress("MILXT");
  }

  /**
   * Modifies the Type B message to have a too short originator indicator.
   * Uses a 5-character originator instead of the required 7 characters.
   * <p>
   * Example Gherkin:
   * And this type-b message has a too short originator indicator
   */
  @Given("this type-b message has a too short originator indicator")
  public void thisTypeBMessageHasATooShortOriginatorIndicator() {
    ensureMessageExists();
    log.info("Creating message with too short originator indicator (5 chars)");

    this.message = TypeBMessageFactory.messageWithTooShortOriginatorIndicator(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with too short originator indicator");
  }

  /**
   * Modifies the Type B message to have a too long originator indicator.
   * Uses a 9-character originator instead of the required 7 characters.
   * <p>
   * Example Gherkin:
   * And this type-b message has a too long originator indicator
   */
  @Given("this type-b message has a too long originator indicator")
  public void thisTypeBMessageHasATooLongOriginatorIndicator() {
    ensureMessageExists();
    log.info("Creating message with too long originator indicator (9 chars)");

    this.message = TypeBMessageFactory.messageWithTooLongOriginatorIndicator(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with too long originator indicator");
  }

  /**
   * Modifies the Type B message to have an empty originator indicator.
   * <p>
   * Example Gherkin:
   * And this type-b message has an empty originator indicator
   */
  @Given("this type-b message has an empty originator indicator")
  public void thisTypeBMessageHasAnEmptyOriginatorIndicator() {
    ensureMessageExists();
    log.info("Creating message with empty originator indicator");

    this.message = TypeBMessageFactory.messageWithEmptyOriginatorIndicator(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with empty originator indicator");
  }

  /**
   * Modifies the Type B message to have an N-character originator indicator.
   * <p>
   * Example Gherkin:
   * And this type-b message has a 3-character originator indicator
   *
   * @param charCount the number of characters in the originator
   */
  @Given("this type-b message has a {int}-character originator indicator")
  public void thisTypeBMessageHasACharacterOriginatorIndicator(int charCount) {
    ensureMessageExists();
    log.info("Creating message with {}-character originator indicator", charCount);

    // Generate originator with specified length
    String originator = generateOriginatorWithLength(charCount);

    this.message = TypeBMessageFactory.messageWithInvalidOriginatorIndicator(originator, this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with {}-character originator indicator: {}", charCount, originator);
  }

  /**
   * Modifies the Type B message to have an originator indicator with special characters.
   * <p>
   * Example Gherkin:
   * And this type-b message has an originator indicator with special characters
   */
  @Given("this type-b message has an originator indicator with special characters")
  public void thisTypeBMessageHasAnOriginatorIndicatorWithSpecialCharacters() {
    ensureMessageExists();
    log.info("Creating message with originator indicator containing special characters");

    this.message = TypeBMessageFactory.messageWithSpecialCharsOriginatorIndicator(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with originator indicator containing special characters");
  }

  /**
   * Modifies the Type B message to have an originator indicator with spaces.
   * <p>
   * Example Gherkin:
   * And this type-b message has an originator indicator with spaces
   */
  @Given("this type-b message has an originator indicator with spaces")
  public void thisTypeBMessageHasAnOriginatorIndicatorWithSpaces() {
    ensureMessageExists();
    log.info("Creating message with originator indicator containing spaces");

    this.message = TypeBMessageFactory.messageWithSpacesOriginatorIndicator(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with originator indicator containing spaces");
  }

  /**
   * Modifies the Type B message to have an originator indicator with lowercase letters.
   * <p>
   * Example Gherkin:
   * And this type-b message has an originator indicator with lowercase letters
   */
  @Given("this type-b message has an originator indicator with lowercase letters")
  public void thisTypeBMessageHasAnOriginatorIndicatorWithLowercaseLetters() {
    ensureMessageExists();
    log.info("Creating message with originator indicator containing lowercase letters");

    this.message = TypeBMessageFactory.messageWithLowercaseOriginatorIndicator(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with originator indicator containing lowercase letters");
  }

  /**
   * Modifies the Type B message to have an originator indicator with numbers.
   * <p>
   * Example Gherkin:
   * And this type-b message has an originator indicator with numbers
   */
  @Given("this type-b message has an originator indicator with numbers")
  public void thisTypeBMessageHasAnOriginatorIndicatorWithNumbers() {
    ensureMessageExists();
    log.info("Creating message with originator indicator containing numbers");

    this.message = TypeBMessageFactory.messageWithNumbersOriginatorIndicator(this.lastMessageIdentity);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Created message with originator indicator containing numbers");
  }


  // ========================================
  // SERIAL NUMBER AND MESSAGE TEXT STEPS
  // ========================================

  /**
   * Sets the serial number (heading line) for the Type B message.
   * The serial number appears in the heading line in format: ZCZC {serialNumber}
   * <p>
   * Example Gherkin:
   * And this type-b message has the serial number "ABC123"
   *
   * @param serialNumber the serial number to set (e.g., "ABC123")
   */
  @Given("this type-b message has the serial number {string}")
  public void thisTypeBMessageHasTheSerialNumber(String serialNumber) {
    ensureMessageExists();
    log.info("Setting serial number: {}", serialNumber);

    // Delegate to service
    this.message = messageBuildService.setSerialNumber(this.message, serialNumber);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Successfully set serial number: {}", serialNumber);
  }

  /**
   * Sets the reject message text content.
   * This replaces the existing text content with the reject message.
   * <p>
   * Example Gherkin:
   * And the reject has the message text "PLS RPT YR ABC123 DUE TO:"
   *
   * @param messageText the reject message text
   */
  @Given("the reject has the message text {string}")
  public void theRejectHasTheMessageText(String messageText) {
    ensureMessageExists();
    log.info("Setting reject message text: {}", messageText);

    // Delegate to service
    this.message = messageBuildService.setMessageText(this.message, messageText);
    this.lastInjectedMessage = this.message.toMessageString();

    log.info("Successfully set reject message text");
  }


  // ========================================
  // HELPER METHODS
  // ========================================

  /**
   * Ensures that a message exists before performing operations on it.
   * Throws an exception if no message has been created.
   */
  private void ensureMessageExists() {
    if (this.message == null) {
      throw new IllegalStateException("No message has been created. Please use 'Given a type-b message' first.");
    }
  }

  /**
   * Generates an originator string with the specified length.
   * Uses 'X' characters to fill the string.
   *
   * @param length the desired length
   * @return a string of 'X' characters with the specified length
   */
  private String generateOriginatorWithLength(int length) {
    if (length <= 0) {
      return "";
    }
    return "X".repeat(length);
  }
}