/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb.util;

import java.util.List;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling Type B message address operations.
 * Provides methods for extracting, normalizing, and validating addresses
 * according to Type B format standards.
 * <p>
 * Type B Address Line Format:
 * {priority} {address1} {address2} ... (max 8 addresses per line)
 * Example: "QD BARXSXT LETBCLK SWIRI1G"
 */
@Slf4j
@UtilityClass
public class AddressUtils {

  /**
   * Maximum number of addresses allowed per line according to Type B standard.
   */
  public static final int MAX_ADDRESSES_PER_LINE = 8;

  /**
   * Maximum number of address lines allowed according to Type B standard.
   */
  public static final int MAX_ADDRESS_LINES = 4;

  /**
   * Default priority code when none is specified.
   */
  public static final String DEFAULT_PRIORITY = "QD";

  /**
   * Extracts the priority code from an address line.
   * The priority code is the first token before the first space.
   *
   * @param addressLine the address line (e.g., "QP SWIRI1G" or "QD BARXSXT LETBCLK")
   * @return the priority code (e.g., "QP", "QD"), or DEFAULT_PRIORITY if not found
   */
  public static String extractPriority(String addressLine) {
    if (addressLine == null || addressLine.isEmpty()) {
      log.debug("Address line is null or empty, returning default priority: {}", DEFAULT_PRIORITY);
      return DEFAULT_PRIORITY;
    }

    String[] parts = addressLine.split(" ", 2);
    if (parts.length == 0 || parts[0].isEmpty()) {
      log.debug("No priority found in address line, returning default: {}", DEFAULT_PRIORITY);
      return DEFAULT_PRIORITY;
    }

    String priority = parts[0];
    log.debug("Extracted priority: {}", priority);
    return priority;
  }

  /**
   * Normalizes a priority code, returning the default if null or empty.
   *
   * @param priority the priority code to normalize
   * @return the normalized priority code, or DEFAULT_PRIORITY if null/empty
   */
  public static String normalizePriority(String priority) {
    if (priority == null || priority.isEmpty()) {
      log.debug("Priority is null or empty, using default: {}", DEFAULT_PRIORITY);
      return DEFAULT_PRIORITY;
    }
    return priority;
  }

  /**
   * Joins a list of addresses into a single space-separated string.
   *
   * @param addresses the list of addresses to join
   * @return the joined address string
   */
  public static String joinAddresses(List<String> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      log.warn("Address list is null or empty");
      return "";
    }
    return String.join(" ", addresses);
  }

  /**
   * Validates the number of addresses per line and logs a warning if it exceeds the maximum.
   * This method does not throw an exception, as the validation error will be caught
   * by the Type B validator during message validation.
   *
   * @param addressCount the number of addresses to validate
   */
  public static void validateAddressCount(int addressCount) {
    if (addressCount > MAX_ADDRESSES_PER_LINE) {
      log.warn("WARNING: {} addresses in one line exceeds the maximum of {}. This will trigger TOO_MANY_ADDRESSES_PER_LINE error.",
          addressCount, MAX_ADDRESSES_PER_LINE);
    }
  }

  /**
   * Validates the number of address lines and logs a warning if it exceeds the maximum.
   * This method does not throw an exception, as the validation error will be caught
   * by the Type B validator during message validation.
   *
   * @param lineCount the number of address lines to validate
   */
  public static void validateAddressLineCount(int lineCount) {
    if (lineCount > MAX_ADDRESS_LINES) {
      log.warn("WARNING: {} address lines exceeds the maximum of {}. This will trigger TOO_MANY_ADDRESS_LINES error.",
          lineCount, MAX_ADDRESS_LINES);
    }
  }

  /**
   * Builds a complete address line with priority and addresses.
   *
   * @param priority  the priority code (e.g., "QD", "QP")
   * @param addresses the list of addresses
   * @return the complete address line (e.g., "QD BARXSXT LETBCLK")
   */
  public static String buildAddressLine(String priority, List<String> addresses) {
    String normalizedPriority = normalizePriority(priority);
    String joinedAddresses = joinAddresses(addresses);

    if (joinedAddresses.isEmpty()) {
      log.warn("No addresses provided, returning only priority");
      return normalizedPriority;
    }

    return normalizedPriority + " " + joinedAddresses;
  }

  /**
   * Builds multiple address lines from a list of address line lists.
   * The first line includes the priority, subsequent lines do not.
   *
   * @param priority     the priority code for the first line
   * @param addressLines the list of address lines (each line is a list of addresses)
   * @return the complete address block with lines separated by \r\n
   */
  public static String buildAddressLines(String priority, List<List<String>> addressLines) {
    if (addressLines == null || addressLines.isEmpty()) {
      log.warn("Address lines list is null or empty");
      return normalizePriority(priority);
    }

    String normalizedPriority = normalizePriority(priority);
    StringBuilder addressBlock = new StringBuilder();

    for (int i = 0; i < addressLines.size(); i++) {
      List<String> line = addressLines.get(i);
      String addressLine = joinAddresses(line);

      // First line includes priority
      if (i == 0) {
        addressBlock.append(normalizedPriority).append(" ").append(addressLine);
      } else {
        addressBlock.append(addressLine);
      }

      // Add line separator except for the last line
      if (i < addressLines.size() - 1) {
        addressBlock.append("\r\n");
      }

      log.debug("Address line {}: {} addresses", i + 1, line.size());
      validateAddressCount(line.size());
    }

    validateAddressLineCount(addressLines.size());
    return addressBlock.toString();
  }
}

