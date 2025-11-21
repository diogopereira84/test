/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.generator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for generating unique message identities for Type B messages.
 * Message Identity Format: DDHHMM/RRRRRRXXX
 * Where:
 * - DD = Day of month (01-31)
 * - HH = Hour (00-23)
 * - MM = Minute (00-59)
 * - RRRRRR = Random 6-digit number (100000-999999)
 * - XXX = Random 3-character alphanumeric string (A-Z, 0-9)
 * Example: 121437/160B99PSA
 * This class uses ThreadLocalRandom for thread-safe random number generation
 * and ensures uniqueness through timestamp + random components.
 */
@Slf4j
@UtilityClass
public class MessageIdentityGenerator {

  /**
   * Date format for the timestamp part of the message identity.
   * Format: ddHHmm (day, hour, minute)
   */
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("ddHHmm");

  /**
   * Separator between timestamp and random parts.
   */
  private static final String SEPARATOR = "/";

  /**
   * Minimum value for the random number component (inclusive).
   */
  private static final int RANDOM_NUMBER_MIN = 100000;

  /**
   * Maximum value for the random number component (exclusive).
   */
  private static final int RANDOM_NUMBER_MAX = 1000000;

  /**
   * Length of the random alphanumeric suffix.
   */
  private static final int SUFFIX_LENGTH = 3;

  /**
   * Characters used for generating the random alphanumeric suffix.
   * Uses uppercase letters and digits only.
   */
  private static final String ALPHANUMERIC_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

  /**
   * Generates a unique message identity using timestamp and random components.
   * Format: DDHHMM/RRRRRRXXX
   * Example: 121437/160B99PSA
   * - Generated on day 12 at 14:37
   * - Random number: 160B99
   * - Random suffix: PSA
   *
   * @return a unique message identity string
   */
  public static String generate() {
    String timestamp = generateTimestampPart();
    String randomPart = generateRandomPart();
    String messageIdentity = timestamp + SEPARATOR + randomPart;

    log.debug("Generated message identity: {}", messageIdentity);
    return messageIdentity;
  }

  /**
   * Generates a unique message identity with a custom separator.
   *
   * @param separator the separator to use between timestamp and random parts
   * @return a unique message identity string
   */
  public static String generate(String separator) {
    String timestamp = generateTimestampPart();
    String randomPart = generateRandomPart();
    String messageIdentity = timestamp + separator + randomPart;

    log.debug("Generated message identity with custom separator: {}", messageIdentity);
    return messageIdentity;
  }

  /**
   * Generates a UUID-based message identity for scenarios requiring
   * globally unique identifiers.
   * Format: DDHHMM/UUID-SHORT
   * Example: 121437/A3F8E9C2D
   *
   * @return a UUID-based unique message identity string
   */
  public static String generateUuidBased() {
    String timestamp = generateTimestampPart();
    String uuidPart = generateUuidPart();
    String messageIdentity = timestamp + SEPARATOR + uuidPart;

    log.debug("Generated UUID-based message identity: {}", messageIdentity);
    return messageIdentity;
  }

  /**
   * Generates a custom message identity with specified random number and suffix lengths.
   *
   * @param randomNumberLength the length of the random number part
   * @param suffixLength       the length of the random alphanumeric suffix
   * @return a unique message identity string
   */
  public static String generateCustom(int randomNumberLength, int suffixLength) {
    String timestamp = generateTimestampPart();
    String randomNumber = generateRandomNumber(randomNumberLength);
    String randomSuffix = generateRandomAlphanumeric(suffixLength);
    String messageIdentity = timestamp + SEPARATOR + randomNumber + randomSuffix;

    log.debug("Generated custom message identity: {}", messageIdentity);
    return messageIdentity;
  }

  /**
   * Generates the timestamp part of the message identity.
   * Format: DDHHMM (day, hour, minute)
   *
   * @return the timestamp string
   */
  private static String generateTimestampPart() {
    LocalDateTime now = LocalDateTime.now();
    return now.format(DATE_FORMAT);
  }

  /**
   * Generates the random part of the message identity.
   * Format: RRRRRRXXX (6-digit number + 3-char alphanumeric)
   *
   * @return the random part string
   */
  private static String generateRandomPart() {
    int randomNumber = ThreadLocalRandom.current().nextInt(RANDOM_NUMBER_MIN, RANDOM_NUMBER_MAX);
    String randomSuffix = generateRandomAlphanumeric(SUFFIX_LENGTH);
    return randomNumber + randomSuffix;
  }

  /**
   * Generates a random number with the specified length.
   *
   * @param length the length of the random number
   * @return the random number as a string
   */
  private static String generateRandomNumber(int length) {
    int min = (int) Math.pow(10, length - 1);
    int max = (int) Math.pow(10, length);
    int randomNumber = ThreadLocalRandom.current().nextInt(min, max);
    return String.valueOf(randomNumber);
  }

  /**
   * Generates a random alphanumeric string of the specified length.
   * Uses uppercase letters (A-Z) and digits (0-9) only.
   *
   * @param length the length of the random string
   * @return a random alphanumeric string
   */
  private static String generateRandomAlphanumeric(int length) {
    StringBuilder result = new StringBuilder(length);

    for (int i = 0; i < length; i++) {
      int index = ThreadLocalRandom.current().nextInt(ALPHANUMERIC_CHARS.length());
      result.append(ALPHANUMERIC_CHARS.charAt(index));
    }

    return result.toString();
  }

  /**
   * Generates a UUID-based random part.
   * Takes the first 9 characters of a UUID (without hyphens).
   *
   * @return a UUID-based random string
   */
  private static String generateUuidPart() {
    return UUID.randomUUID()
        .toString()
        .replace("-", "")
        .substring(0, 9)
        .toUpperCase();
  }

  /**
   * Validates if a message identity matches the expected format.
   *
   * @param messageIdentity the message identity to validate
   * @return true if the format is valid, false otherwise
   */
  public static boolean isValidFormat(String messageIdentity) {
    if (messageIdentity == null || messageIdentity.isEmpty()) {
      return false;
    }

    // Standard format: DDHHMM/RRRRRRXXX
    String standardPattern = "\\d{6}/\\d{6}[A-Z0-9]{3}";
    return messageIdentity.matches(standardPattern);
  }

  /**
   * Validates if a UUID-based message identity matches the expected format.
   *
   * @param messageIdentity the message identity to validate
   * @return true if the format is valid, false otherwise
   */
  public static boolean isValidUuidBasedFormat(String messageIdentity) {
    if (messageIdentity == null || messageIdentity.isEmpty()) {
      return false;
    }

    // UUID-based format: DDHHMM/XXXXXXXXX
    String uuidPattern = "\\d{6}/[A-Z0-9]{9}";
    return messageIdentity.matches(uuidPattern);
  }

  /**
   * Extracts the timestamp part from a message identity.
   *
   * @param messageIdentity the message identity
   * @return the timestamp part (DDHHMM), or null if invalid format
   */
  public static String extractTimestamp(String messageIdentity) {
    if (messageIdentity == null || !messageIdentity.contains(SEPARATOR)) {
      return null;
    }

    String[] parts = messageIdentity.split(SEPARATOR);
    return parts.length > 0 ? parts[0] : null;
  }

  /**
   * Extracts the random part from a message identity.
   *
   * @param messageIdentity the message identity
   * @return the random part (RRRRRRXXX), or null if invalid format
   */
  public static String extractRandomPart(String messageIdentity) {
    if (messageIdentity == null || !messageIdentity.contains(SEPARATOR)) {
      return null;
    }

    String[] parts = messageIdentity.split(SEPARATOR);
    return parts.length > 1 ? parts[1] : null;
  }
}