/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb;

import lombok.experimental.UtilityClass;

/**
 * Factory class for creating Type B messages for testing purposes.
 * Provides convenient methods to generate valid and invalid Type B messages
 * with configurable message identities.
 * Type B Message Format:
 * SOH + Heading Line + CRLF + . + Address + CRLF + Origin + CRLF + STX + Text + ETX
 * Example Valid Message:
 * \u0001QP SWIRI1G\r\n.HDQRMJU 281440/160B99PSA\r\n\u0002AVS\r\nJU0580L30AUG LA BEGBCN\r\n\u0003
 * Example Invalid Origin (UNKNOWN_ORIGIN_INDICATOR):
 * \u0001QD SWIRI1G\r\n.MILXT 122121/118132DIO\r\n\u0002AVS\r\nJU0580L30AUG LA BEGBCN\r\n \r\n\u0003
 * Originator Indicator Format (7 characters):
 * - 3 characters: location identifier (city/airport code)
 * - 2 characters: office function designator (department code)
 * - 2 characters: SITA Network user designator (company code)
 */
@UtilityClass
public class TypeBMessageFactory {

  // Default Type B message components
  private static final String DEFAULT_PRIORITY = "QP";
  private static final String DEFAULT_ADDRESS = "SWIRI1G";
  private static final String DEFAULT_ORIGIN_CITY = "HDQ";
  private static final String DEFAULT_ORIGIN_DEPT = "RM";
  private static final String DEFAULT_ORIGIN_COMPANY = "JU";
  private static final String DEFAULT_MESSAGE_TYPE = "AVS";
  private static final String DEFAULT_FLIGHT_INFO = "JU0580L30AUG LA BEGBCN";

  /**
   * Creates a valid Type B message with default values and a specified message identity.
   * This is the recommended method for creating test messages as it ensures unique
   * message identities for better test isolation and traceability.
   * Format:
   * \u0001QP SWIRI1G\r\n.HDQRMJU {messageIdentity}\r\n\u0002AVS\r\nJU0580L30AUG LA BEGBCN\r\n\u0003
   *
   * @param messageIdentity the unique message identity (e.g., "121437/160B99PSA")
   * @return a valid TypeBMessage instance with the specified message identity
   */
  public static TypeBMessage validMessageWithIdentity(String messageIdentity) {
    String addressLine = buildAddressLine(DEFAULT_PRIORITY, DEFAULT_ADDRESS);
    String originLine = buildOriginLine(
        messageIdentity);
    String text = buildMessageText(DEFAULT_MESSAGE_TYPE, DEFAULT_FLIGHT_INFO);

    return TypeBMessage.builder()
        .headingLine("") // Optional heading line
        .normalAddressLine(addressLine)
        .originLine(originLine)
        .text(text)
        .ending("")
        .build();
  }

  /**
   * Creates a Type B message with an invalid originator indicator.
   * The originator indicator will have less than 7 characters, triggering UNKNOWN_ORIGIN_INDICATOR error.
   * Example: MILXT (5 characters) instead of HDQRMJU (7 characters)
   * The messageIdentity is preserved in the normal address line for MongoDB tracking.
   *
   * @param invalidOriginatorIndicator the invalid originator (e.g., "MILXT" with 5 chars)
   * @param messageIdentity            the unique message identity to preserve
   * @return a TypeBMessage with invalid originator indicator
   */
  public static TypeBMessage messageWithInvalidOriginatorIndicator(
      String invalidOriginatorIndicator, String messageIdentity) {
    String addressLine = buildAddressLine("QD", DEFAULT_ADDRESS);
    String originLine = buildOriginLineFromOrigin(invalidOriginatorIndicator, messageIdentity);
    String text = buildMessageText(DEFAULT_MESSAGE_TYPE, DEFAULT_FLIGHT_INFO);

    return TypeBMessage.builder()
        .headingLine("") // Optional heading line
        .normalAddressLine(addressLine)
        .originLine(originLine)
        .text(text)
        .ending("")
        .build();
  }

  /**
   * Creates a Type B message with an invalid originator indicator (too short).
   * Uses a 5-character originator instead of the required 7 characters.
   * Example: MILXT (5 chars) - Missing department and company codes
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with too short originator indicator
   */
  public static TypeBMessage messageWithTooShortOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("MILXT", messageIdentity);
  }

  /**
   * Creates a Type B message with an invalid originator indicator (too long).
   * Uses a 9-character originator instead of the required 7 characters.
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with too long originator indicator
   */
  public static TypeBMessage messageWithTooLongOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("HDQRMJUXX", messageIdentity);
  }

  /**
   * Creates a Type B message with an empty originator indicator.
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with empty originator indicator
   */
  public static TypeBMessage messageWithEmptyOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("", messageIdentity);
  }

  /**
   * Creates a Type B message with an originator indicator containing only 3 characters.
   * Example: MIL (3 chars) - Only city code, missing department and company codes
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with 3-character originator indicator
   */
  public static TypeBMessage messageWith3CharOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("MIL", messageIdentity);
  }

  /**
   * Creates a Type B message with an originator indicator containing only 1 character.
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with 1-character originator indicator
   */
  public static TypeBMessage messageWith1CharOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("M", messageIdentity);
  }

  /**
   * Creates a Type B message with an originator indicator containing special characters.
   * Example: HDQ@M#U (7 chars but with invalid characters)
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with special characters in originator indicator
   */
  public static TypeBMessage messageWithSpecialCharsOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("HDQ@M#U", messageIdentity);
  }

  /**
   * Creates a Type B message with an originator indicator containing spaces.
   * Example: "HDQ RM " (7 chars but with spaces)
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with spaces in originator indicator
   */
  public static TypeBMessage messageWithSpacesOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("HDQ RM ", messageIdentity);
  }

  /**
   * Creates a Type B message with an originator indicator containing lowercase letters.
   * Example: hdqrmju (7 chars but lowercase)
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with lowercase originator indicator
   */
  public static TypeBMessage messageWithLowercaseOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("hdqrmju", messageIdentity);
  }

  /**
   * Creates a Type B message with an originator indicator containing numbers in wrong positions.
   * Example: H1QRM2U (7 chars but with numbers in city/dept codes)
   *
   * @param messageIdentity the unique message identity to preserve
   * @return a TypeBMessage with numbers in originator indicator
   */
  public static TypeBMessage messageWithNumbersOriginatorIndicator(String messageIdentity) {
    return messageWithInvalidOriginatorIndicator("H1QRM2U", messageIdentity);
  }

  /**
   * Creates a custom Type B message with specified fields.
   * Uses a placeholder message identity.
   *
   * @param priority the priority indicator (e.g., "QP", "FF", "QU")
   * @param address  the destination address (e.g., "SWIRI1G")
   * @param origin   the origin address (e.g., "HDQRMJU")
   * @param text     the message text content
   * @return a custom TypeBMessage instance
   * @deprecated Use {@link #customMessageWithIdentity(String, String, String, String, String)} instead
   */
  @Deprecated
  public static TypeBMessage customMessage(String priority, String address, String origin, String text) {
    return customMessageWithIdentity(priority, address, origin, text, "000000/000AAA");
  }

  /**
   * Creates a custom Type B message with specified fields and message identity.
   * This is the recommended method for creating custom test messages.
   *
   * @param priority        the priority indicator (e.g., "QP", "FF", "QU")
   * @param address         the destination address (e.g., "SWIRI1G")
   * @param origin          the origin address (e.g., "HDQRMJU")
   * @param text            the message text content
   * @param messageIdentity the unique message identity
   * @return a custom TypeBMessage instance with the specified message identity
   */
  public static TypeBMessage customMessageWithIdentity(
      String priority, String address, String origin, String text, String messageIdentity) {
    String addressLine = buildAddressLine(priority, address);
    String originLine = buildOriginLineFromOrigin(origin, messageIdentity);

    return TypeBMessage.builder()
        .headingLine("") // Optional heading line
        .normalAddressLine(addressLine)
        .originLine(originLine)
        .text(text)
        .ending("")
        .build();
  }

  /**
   * Creates a Type B AVS (Availability Status) message with specified message identity.
   *
   * @param messageIdentity the unique message identity
   * @param flightNumber    the flight number (e.g., "JU0580L")
   * @param flightDate      the flight date (e.g., "30AUG")
   * @param route           the flight route (e.g., "LA BEGBCN")
   * @return a TypeBMessage instance for AVS message type
   */
  public static TypeBMessage avsMessage(
      String messageIdentity, String flightNumber, String flightDate, String route) {
    String addressLine = buildAddressLine(DEFAULT_PRIORITY, DEFAULT_ADDRESS);
    String originLine = buildOriginLine(
        messageIdentity);
    String messageText = buildMessageText("AVS", flightNumber + flightDate + " " + route);

    return TypeBMessage.builder()
        .headingLine("") // Optional heading line
        .normalAddressLine(addressLine)
        .originLine(originLine)
        .text(messageText)
        .ending("")
        .build();
  }

  /**
   * Creates a Type B MVT (Movement) message with specified message identity.
   *
   * @param messageIdentity  the unique message identity
   * @param flightNumber     the flight number
   * @param flightDate       the flight date
   * @param departureAirport the departure airport code
   * @param arrivalTime      the arrival time
   * @return a TypeBMessage instance for MVT message type
   */
  public static TypeBMessage mvtMessage(
      String messageIdentity, String flightNumber, String flightDate,
      String departureAirport, String arrivalTime) {
    String addressLine = buildAddressLine("FF", DEFAULT_ADDRESS);
    String originLine = buildOriginLine(
        messageIdentity);
    String messageText = buildMessageText("MVT",
        String.format("%s/%s %s AD%s", flightNumber, flightDate, departureAirport, arrivalTime));

    return TypeBMessage.builder()
        .headingLine("") // Optional heading line
        .normalAddressLine(addressLine)
        .originLine(originLine)
        .text(messageText)
        .ending("")
        .build();
  }

  /**
   * Creates a Type B LDM (Load Message) message with specified message identity.
   *
   * @param messageIdentity the unique message identity
   * @param flightNumber    the flight number
   * @param flightDate      the flight date
   * @param route           the flight route
   * @param loadDetails     the load details
   * @return a TypeBMessage instance for LDM message type
   */
  public static TypeBMessage ldmMessage(
      String messageIdentity, String flightNumber, String flightDate,
      String route, String loadDetails) {
    String addressLine = buildAddressLine("QU", DEFAULT_ADDRESS);
    String originLine = buildOriginLine(
        messageIdentity);
    String messageText = buildMessageText("LDM",
        String.format("%s/%s %s\r\n%s", flightNumber, flightDate, route, loadDetails));

    return TypeBMessage.builder()
        .headingLine("") // Optional heading line
        .normalAddressLine(addressLine)
        .originLine(originLine)
        .text(messageText)
        .ending("")
        .build();
  }

  /**
   * Builds the address line in the format: "{priority} {address}"
   * Example: "QP SWIRI1G"
   *
   * @param priority the priority indicator
   * @param address  the destination address
   * @return the formatted address line
   */
  private static String buildAddressLine(String priority, String address) {
    return priority + " " + address;
  }

  /**
   * Builds the origin line in the format: "{origin} {messageIdentity}"
   * Example: "HDQRMJU 281440/160B99PSA"
   *
   * @param messageIdentity the message identity
   * @return the formatted origin line
   */
  private static String buildOriginLine(
      String messageIdentity) {
    String origin = TypeBMessageFactory.DEFAULT_ORIGIN_CITY + TypeBMessageFactory.DEFAULT_ORIGIN_DEPT + TypeBMessageFactory.DEFAULT_ORIGIN_COMPANY;
    return origin + " " + messageIdentity;
  }

  /**
   * Builds the origin line from a complete origin string.
   * Format: "{origin} {messageIdentity}"
   * Examples:
   * - Valid: "HDQRMJU 281440/160B99PSA" → becomes ".HDQRMJU 281440/160B99PSA"
   * - Invalid (5 chars): "MILXT 122121/118132DIO" → becomes ".MILXT 122121/118132DIO"
   * - Empty origin: " 122121/118132DIO" → becomes ". 122121/118132DIO"
   * Note: The dot "." is prepended by TypeBMessage.toMessageString(), not here.
   * When origin is empty, we add a space before messageIdentity to ensure
   * the final format is ". {messageIdentity}" (dot + space + messageIdentity).
   *
   * @param origin          the complete origin string (e.g., "HDQRMJU", "MILXT", or "")
   * @param messageIdentity the message identity
   * @return the formatted origin line (without the leading dot)
   */
  private static String buildOriginLineFromOrigin(String origin, String messageIdentity) {
    if (origin == null || origin.isEmpty()) {
      // When origin is empty, add space before messageIdentity
      // so the final format becomes ". {messageIdentity}"
      return " " + messageIdentity;
    }
    return origin + " " + messageIdentity;
  }

  /**
   * Builds the message text in the format: "{messageType}\r\n{content}"
   * Example: "AVS\r\nJU0580L30AUG LA BEGBCN\r\n"
   *
   * @param messageType the message type (e.g., "AVS", "MVT", "LDM")
   * @param content     the message content
   * @return the formatted message text
   */
  private static String buildMessageText(String messageType, String content) {
    return messageType + "\r\n" + content + "\r\n";
  }

  /**
   * Extracts the message type from the message text.
   * Assumes the message type is the first line before CRLF.
   *
   * @param text the message text
   * @return the extracted message type, or "UNK" if text is invalid
   */
  private static String extractMessageType(String text) {
    if (text == null || text.isEmpty()) {
      return "UNK";
    }
    String[] lines = text.split("\r\n");
    return lines.length > 0 ? lines[0] : "UNK";
  }
}

