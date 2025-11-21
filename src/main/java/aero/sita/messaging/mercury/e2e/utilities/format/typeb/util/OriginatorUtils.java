/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling Type B message originator operations.
 * Provides methods for building and manipulating origin lines while preserving
 * message identity suffixes.
 * <p>
 * Type B Origin Line Format:
 * {originator} {messageIdentity}
 * Example: "HDQRMJU 281440/160B99PSA"
 * <p>
 * Originator Indicator Format (7 characters):
 * - 3 characters: location identifier (city/airport code)
 * - 2 characters: office function designator (department code)
 * - 2 characters: SITA Network user designator (company code)
 */
@Slf4j
@UtilityClass
public class OriginatorUtils {

  /**
   * Required length for a valid originator indicator.
   */
  public static final int VALID_ORIGINATOR_LENGTH = 7;

  /**
   * Builds an origin line from an originator and message identity.
   *
   * @param originator      the originator indicator (e.g., "HDQRMJU")
   * @param messageIdentity the message identity (e.g., "281440/160B99PSA")
   * @return the complete origin line (e.g., "HDQRMJU 281440/160B99PSA")
   */
  public static String buildOriginLine(String originator, String messageIdentity) {
    if (originator == null) {
      originator = "";
    }
    if (messageIdentity == null) {
      messageIdentity = "";
    }

    // Avoid dangling spaces if either component is empty
    if (originator.isEmpty() && messageIdentity.isEmpty()) {
      log.warn("Both originator and messageIdentity are empty");
      return "";
    }
    if (originator.isEmpty()) {
      log.warn("Originator is empty, returning only messageIdentity");
      return messageIdentity;
    }
    if (messageIdentity.isEmpty()) {
      log.warn("MessageIdentity is empty, returning only originator");
      return originator;
    }

    return originator + " " + messageIdentity;
  }

  /**
   * Extracts the message identity from an origin line.
   * The message identity is everything after the first space.
   *
   * @param originLine the origin line (e.g., "HDQRMJU 281440/160B99PSA")
   * @return the message identity (e.g., "281440/160B99PSA"), or empty string if not found
   */
  public static String extractMessageIdentity(String originLine) {
    if (originLine == null || originLine.isEmpty()) {
      log.debug("Origin line is null or empty, returning empty messageIdentity");
      return "";
    }

    String[] parts = originLine.split(" ", 2);
    if (parts.length < 2) {
      log.warn("No messageIdentity found in origin line: {}", originLine);
      return "";
    }

    String messageIdentity = parts[1];
    log.debug("Extracted messageIdentity: {}", messageIdentity);
    return messageIdentity;
  }

  /**
   * Extracts the originator from an origin line.
   * The originator is the first token before the first space.
   *
   * @param originLine the origin line (e.g., "HDQRMJU 281440/160B99PSA")
   * @return the originator (e.g., "HDQRMJU"), or empty string if not found
   */
  public static String extractOriginator(String originLine) {
    if (originLine == null || originLine.isEmpty()) {
      log.debug("Origin line is null or empty, returning empty originator");
      return "";
    }

    String[] parts = originLine.split(" ", 2);
    if (parts.length == 0 || parts[0].isEmpty()) {
      log.warn("No originator found in origin line: {}", originLine);
      return "";
    }

    String originator = parts[0];
    log.debug("Extracted originator: {}", originator);
    return originator;
  }

  /**
   * Replaces the originator in an origin line while preserving the message identity.
   *
   * @param originLine    the current origin line (e.g., "HDQRMJU 281440/160B99PSA")
   * @param newOriginator the new originator to use (e.g., "MILXT")
   * @return the updated origin line (e.g., "MILXT 281440/160B99PSA")
   */
  public static String replaceOriginator(String originLine, String newOriginator) {
    String messageIdentity = extractMessageIdentity(originLine);
    String updatedOriginLine = buildOriginLine(newOriginator, messageIdentity);

    log.debug("Replaced originator in origin line. Old: {}, New: {}", originLine, updatedOriginLine);
    return updatedOriginLine;
  }

  /**
   * Validates the length of an originator indicator.
   * Logs a warning if the length is not exactly 7 characters.
   * This method does not throw an exception, as the validation error will be caught
   * by the Type B validator during message validation.
   *
   * @param originator the originator to validate
   */
  public static void validateOriginatorLength(String originator) {
    if (originator == null) {
      log.warn("WARNING: Originator is null. This will trigger UNKNOWN_ORIGIN_INDICATOR error.");
      return;
    }

    int length = originator.length();
    if (length != VALID_ORIGINATOR_LENGTH) {
      log.warn("WARNING: Originator length is {} (expected {}). " + "This will trigger UNKNOWN_ORIGIN_INDICATOR error. Originator: {}",
          length, VALID_ORIGINATOR_LENGTH, originator);
    }
  }
}

