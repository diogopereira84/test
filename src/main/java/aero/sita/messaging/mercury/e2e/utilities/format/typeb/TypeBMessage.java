/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a Type B message structure used in aviation messaging.
 * Type B messages follow a specific format with distinct sections:
 * - Heading line: Message priority and sequence number
 * - Normal address line: Destination address
 * - Origin line: Source address
 * - Text: Message content
 * - Ending: Message terminator
 * This class uses the Builder pattern for flexible message construction.
 */
@Data
@Builder(toBuilder = true)  // ← FIXED: Added toBuilder = true
@NoArgsConstructor
@AllArgsConstructor
public class TypeBMessage {

  /**
   * The heading line (optional) containing sequence information.
   * Example: "ZCZC ABC123"
   */
  private String headingLine;

  /**
   * The address line containing priority code and destination addresses.
   * Example: "QD BARXSXT LETBCLK SWIRI1G LKYSOLT LKYEDLT LKYEGLT LETKJLK LETJPLK LETBCLK"
   * Format: {priority} {address1} {address2} ... (max 8 addresses per line)
   */
  private String normalAddressLine;

  /**
   * The origin line containing the originator indicator and message identity.
   * Example: "MILXT 131400/784507PDV"
   * Format: {originator} {messageIdentity}
   * Note: The dot (.) is prepended automatically by toMessageString()
   */
  private String originLine;

  /**
   * The message text content.
   * Example: "AVS\r\nJU0580L30AUG LA BEGBCN"
   */
  private String text;

  /**
   * The message ending/terminator.
   */
  private String ending;

  /**
   * Creates a Type B message from a raw message string.
   *
   * @param rawMessage the raw message string to parse
   * @return a TypeBMessage instance
   */
  public static TypeBMessage fromMessageString(String rawMessage) {
    if (rawMessage == null || rawMessage.isEmpty()) {
      return TypeBMessage.builder().build();
    }

    TypeBMessageBuilder builder = TypeBMessage.builder();

    // Remove SOH if present
    String message = rawMessage.replaceFirst("^\u0001", "");

    // Split by STX to separate header from body
    String[] parts = message.split("\u0002", 2);

    if (parts.length > 0) {
      String header = parts[0];
      String[] headerLines = header.split("\r\n");

      if (headerLines.length > 0) {
        builder.headingLine(headerLines[0]);
      }
      if (headerLines.length > 1 && headerLines[1].startsWith(".")) {
        builder.normalAddressLine(headerLines[1].substring(1));
      }
      if (headerLines.length > 2) {
        builder.originLine(headerLines[2]);
      }
    }

    if (parts.length > 1) {
      // Split by ETX to separate text from ending
      String[] bodyParts = parts[1].split("\u0003", 2);

      if (bodyParts.length > 0) {
        builder.text(bodyParts[0]);
      }
      if (bodyParts.length > 1) {
        builder.ending(bodyParts[1]);
      }
    }

    return builder.build();
  }

  /**
   * Converts the Type B message to its string representation
   * suitable for transmission.
   *
   * @return the formatted Type B message string
   */
  public String toMessageString() {
    StringBuilder sb = new StringBuilder();

    // Heading line (ZCZC serial number) - BEFORE SOH
    if (headingLine != null && !headingLine.isEmpty()) {
      sb.append(headingLine).append("\r\n");
    }

    // Start of heading (SOH) character - marks start of addressing section
    sb.append("\u0001");

    // Address line (priority + addresses) - NO dot
    if (normalAddressLine != null && !normalAddressLine.isEmpty()) {
      sb.append(normalAddressLine).append("\r\n");
    }

    // Origin line (originator + messageIdentity) - WITH dot
    if (originLine != null && !originLine.isEmpty()) {
      sb.append(".").append(originLine).append("\r\n");
    }

    // Start of text (STX) character
    sb.append("\u0002");

    // Message text
    if (text != null && !text.isEmpty()) {
      sb.append(text);
    }

    // End of text (ETX) character
    sb.append("\u0003");

    // Ending
    if (ending != null && !ending.isEmpty()) {
      sb.append(ending);
    }

    return sb.toString();
  }
}