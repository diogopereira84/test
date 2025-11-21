/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.testharness.rejection;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the structured content of a Type B reject message body.
 * A reject message body has three main parts:
 * 1. Rejection Header (constant):
 * "PLS RPT YR ..... DUE TO:" or "PLS RPT YR {serialNumber} DUE TO:"
 * 2. Error List (array of errors):
 * "1. UNKNOWN_ORIGIN_INDICATOR"
 * "2. TOO_MANY_ADDRESSES_PER_LINE"
 * etc.
 * 3. Original Message (the message that was rejected):
 * The original Type B message content without control characters
 * Example reject message body:
 * <pre>
 * PLS RPT YR ..... DUE TO:
 * 1. UNKNOWN_ORIGIN_INDICATOR
 *
 *
 * QD SWIRI1G
 *  130015/778418HKC
 * AVS
 * JU0580L30AUG LA BEGBCN
 *
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RejectMessageBody {

  /**
   * The rejection header line.
   * Format: "PLS RPT YR ..... DUE TO:" (no serial number)
   * or "PLS RPT YR {serialNumber} DUE TO:" (with serial number)
   */
  private String rejectionHeader;

  /**
   * The serial number extracted from the rejection header.
   * Will be "....." if no serial number is present.
   */
  private String serialNumber;

  /**
   * List of error entries in the reject message.
   * Each entry contains the error number and error code.
   * -- GETTER --
   * Gets all error entries in the reject message.
   */
  @Builder.Default
  private List<ErrorEntry> errors = new ArrayList<>();

  /**
   * The original message content that was rejected.
   * This is the Type B message without control characters (SOH, STX, ETX).
   */
  private String originalMessage;

  /**
   * The raw message body as received.
   */
  private String rawBody;

  /**
   * Checks if the reject message contains a specific error code.
   *
   * @param errorCode the error code to check
   * @return true if the error code is present in the error list
   */
  public boolean hasErrorCode(String errorCode) {
    return errors.stream()
        .anyMatch(error -> errorCode.equals(error.getErrorCode()));
  }

  /**
   * Gets the number of errors in the reject message.
   *
   * @return the count of errors
   */
  public int getErrorCount() {
    return errors.size();
  }

  /**
   * Gets all error codes as a list of strings.
   *
   * @return list of error codes
   */
  public List<String> getErrorCodes() {
    return errors.stream()
        .map(ErrorEntry::getErrorCode)
        .toList();
  }

  /**
   * Returns a formatted string representation of the reject message body.
   * Useful for debugging and logging.
   *
   * @return formatted string
   */
  public String toFormattedString() {
    StringBuilder sb = new StringBuilder();
    sb.append("=== Reject Message Body ===\n");
    sb.append("Rejection Header: ").append(rejectionHeader).append("\n");
    sb.append("Serial Number: ").append(serialNumber).append("\n");
    sb.append("Error Count: ").append(getErrorCount()).append("\n");
    sb.append("Errors:\n");
    for (ErrorEntry error : errors) {
      sb.append("  ").append(error.getNumber()).append(". ")
          .append(error.getErrorCode()).append("\n");
    }
    sb.append("Original Message:\n");
    sb.append(originalMessage);
    sb.append("\n===========================");
    return sb.toString();
  }

  /**
   * Represents a single error entry in the reject message.
   * Format: "{number}. {errorCode}"
   * Example: "1. UNKNOWN_ORIGIN_INDICATOR"
   */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ErrorEntry {

    /**
     * The error number (position in the list).
     * Example: 1, 2, 3
     */
    private int number;


    /**
     * The error code/reason.
     * Example: "UNKNOWN_ORIGIN_INDICATOR", "TOO_MANY_ADDRESSES_PER_LINE"
     */
    private String errorCode;

    /**
     * The original line as it appears in the message.
     * Example: "1. UNKNOWN_ORIGIN_INDICATOR"
     */
    private String originalLine;

    private String parameters;

  }
}