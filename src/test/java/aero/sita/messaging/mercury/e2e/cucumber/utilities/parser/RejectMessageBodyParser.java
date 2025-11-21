/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.utilities.parser;

import aero.sita.messaging.mercury.e2e.model.testharness.rejection.RejectMessageBody;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Parses Type B reject message bodies into a structured {@link RejectMessageBody}.
 * Expected header (illustrative):
 * "PLS RPT YR <serial> DUE TO:"
 * Error lines (examples):
 * "1. UNKNOWN_ORIGIN_INDICATOR"
 * "2. INVALID_ROUTING_INDICATOR_ADDRESS - LETKJLK"
 * "3. UNKNOWN_ADDRESS LETKJLK"
 * "4. EMPTY_DESTINATION LETJPLK 9"
 * Remaining lines after errors are treated as the original message.
 */
@Slf4j
@UtilityClass
public class RejectMessageBodyParser {

  /**
   * Header: "PLS RPT YR <serial> DUE TO:"
   */
  private static final Pattern REJECTION_HEADER_PATTERN =
      Pattern.compile("PLS RPT YR\\s+(.+?)\\s+DUE TO:");

  /**
   * Numbered error line: "1. ERROR_CODE" or "2. ERROR_CODE - params"
   */
  private static final Pattern ERROR_ENTRY_PATTERN =
      Pattern.compile("^(\\d+)\\.\\s+(.+?)(?:\\s+-\\s+(.+))?$");

  /**
   * Non-numbered content to split code/params: "UNKNOWN_ADDRESS LETKJLK" or "EMPTY_DESTINATION LETJPLK 9"
   */
  private static final Pattern ERROR_WITH_PARAMS_PATTERN =
      Pattern.compile("^([A-Z_]+)(?:\\s+(.+))?$");

  public static RejectMessageBody parse(String messageBody) {
    validateInput(messageBody);

    final String cleaned = stripControlChars(messageBody);
    final String[] lines = cleaned.split("\\r?\\n");

    RejectMessageBody.RejectMessageBodyBuilder builder = RejectMessageBody.builder()
        .rawBody(messageBody);

    // 1) Header (single source of truth: REJECTION_HEADER_PATTERN)
    Header header = findHeader(lines);
    builder.rejectionHeader(header.headerLine);
    builder.serialNumber(header.serialOrUnknown);

    // 2) Errors (start after header line, if any)
    int index = header.nextIndex;
    List<RejectMessageBody.ErrorEntry> errors = parseErrors(lines, index);
    builder.errors(errors);
    index += errors.size() + countConsecutiveBlankLines(lines, index);

    // 3) Original message (remaining lines)
    builder.originalMessage(joinRemaining(lines, index));

    RejectMessageBody result = builder.build();
    log.info("Parsed reject message: {} error(s), serial={}", result.getErrorCount(), result.getSerialNumber());
    return result;
  }

  public static String parseSummary(String messageBody) {
    try {
      return parse(messageBody).toFormattedString();
    } catch (Exception e) {
      return "Failed to parse reject message body: " + e.getMessage();
    }
  }

  /**
   * Validates that a reject message body has a header matching {@link #REJECTION_HEADER_PATTERN}.
   */
  public static boolean isValidRejectMessageBody(String messageBody) {
    try {
      validateInput(messageBody);
      String[] lines = stripControlChars(messageBody).split("\\r?\\n");
      return hasHeader(lines);
    } catch (Exception e) {
      log.warn("Invalid reject message body: {}", e.getMessage());
      return false;
    }
  }

  // ---------- Helpers ----------

  private static void validateInput(String messageBody) {
    if (messageBody == null || messageBody.trim().isEmpty()) {
      throw new IllegalArgumentException("Message body cannot be null or empty");
    }
  }

  /**
   * Remove SOH/STX/ETX; keep newlines for parsing.
   */
  private static String stripControlChars(String s) {
    return s.replace("\u0001", "") // SOH
        .replace("\u0002", "") // STX
        .replace("\u0003", ""); // ETX
  }

  /**
   * Find the first header line using REJECTION_HEADER_PATTERN and extract serial; return index after header.
   */
  private static Header findHeader(String[] lines) {
    for (int i = 0; i < lines.length; i++) {
      final String line = lines[i].trim();
      Matcher m = REJECTION_HEADER_PATTERN.matcher(line);
      if (m.find()) {
        String serial = m.group(1).trim();
        log.debug("Found rejection header with serial: {}", serial);
        return new Header(line, serial, i + 1);
      }
    }
    log.warn("Could not find rejection header in message body");
    return new Header("", "UNKNOWN", 0);
  }

  /**
   * True if any line matches the header regex.
   */
  private static boolean hasHeader(String[] lines) {
    for (String line : lines) {
      if (REJECTION_HEADER_PATTERN.matcher(line.trim()).find()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Parse consecutive numbered error lines starting at startIndex.
   */
  private static List<RejectMessageBody.ErrorEntry> parseErrors(String[] lines, int startIndex) {
    List<RejectMessageBody.ErrorEntry> errors = new ArrayList<>();

    for (int i = startIndex; i < lines.length; i++) {
      final String line = lines[i].trim();
      if (line.isEmpty()) {
        break; // stop at first blank after errors
      }

      Matcher errorMatcher = ERROR_ENTRY_PATTERN.matcher(line);
      if (!errorMatcher.matches()) {
        // First non-error line -> stop collecting errors
        break;
      }

      int number = Integer.parseInt(errorMatcher.group(1));
      String errorContent = errorMatcher.group(2).trim();
      String dashParams = errorMatcher.group(3);

      ParsedError parsed = splitErrorCodeAndParams(errorContent, dashParams);
      RejectMessageBody.ErrorEntry entry = RejectMessageBody.ErrorEntry.builder()
          .number(number)
          .errorCode(parsed.code)
          .parameters(parsed.params)
          .originalLine(line)
          .build();

      log.debug("Error {}: {}{}", number, parsed.code,
          parsed.params != null ? " | params=" + parsed.params : "");
      errors.add(entry);
    }

    return errors;
  }

  /**
   * Count how many consecutive blank lines occur starting at startIndex.
   */
  private static int countConsecutiveBlankLines(String[] lines, int startIndex) {
    int count = 0;
    for (int i = startIndex; i < lines.length; i++) {
      if (lines[i].trim().isEmpty()) {
        count++;
      } else {
        break;
      }
    }
    return count;
  }

  /**
   * Join remaining lines using CRLF to preserve message formatting.
   */
  private static String joinRemaining(String[] lines, int startIndex) {
    if (startIndex >= lines.length) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    for (int i = startIndex; i < lines.length; i++) {
      if (!sb.isEmpty()) {
        sb.append("\r\n");
      }
      sb.append(lines[i]);
    }
    log.debug("Original message length: {} chars", sb.length());
    return sb.toString();
  }

  /**
   * Prefer "code - params" when present; otherwise attempt "CODE params".
   */
  private static ParsedError splitErrorCodeAndParams(String errorContent, String dashParams) {
    if (dashParams != null) {
      return new ParsedError(errorContent, dashParams.trim());
    }
    Matcher m = ERROR_WITH_PARAMS_PATTERN.matcher(errorContent);
    if (m.matches()) {
      String code = m.group(1);
      String params = m.group(2) != null ? m.group(2).trim() : null;
      return new ParsedError(code, params);
    }
    return new ParsedError(errorContent, null);
  }

  // ---------- Small value holders ----------

  private static final class Header {
    final String headerLine;
    final String serialOrUnknown;
    final int nextIndex;

    Header(String headerLine, String serialOrUnknown, int nextIndex) {
      this.headerLine = headerLine;
      this.serialOrUnknown = serialOrUnknown;
      this.nextIndex = nextIndex;
    }
  }

  private static final class ParsedError {
    final String code;
    final String params;

    ParsedError(String code, String params) {
      this.code = code;
      this.params = params;
    }
  }
}