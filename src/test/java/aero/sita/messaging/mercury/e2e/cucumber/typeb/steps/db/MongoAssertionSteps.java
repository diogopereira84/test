/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps.db;

import static org.assertj.core.api.Assertions.assertThat;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.steps.CommonSteps;
import aero.sita.messaging.mercury.e2e.utilities.helper.MongoGenericHelper;
import aero.sita.messaging.mercury.e2e.utilities.helper.PollingHelper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
public class MongoAssertionSteps {

  @Autowired
  private MongoGenericHelper mongoGenericHelper;

  @Autowired
  private CommonSteps commonSteps;

  @Autowired
  private PollingHelper pollingHelper;

  @Value("${mongodb.query.default-filter-field:messageIdentity}")
  private String defaultFilterField;

  @Value("${polling.message-store.timeout.seconds:30}")
  private int pollTimeout;

  @Value("${polling.message-store.interval.millis:1000}")
  private long pollInterval;

  /**
   * Property to control execution of detailed validations.
   * Steps will only execute if mod=UAT.
   */
  @Value("${mod:false}")
  private boolean isUatMode;

  // --- SINGLE VALUE ASSERTION ---
  @Then("the value of {string} is {string} {string}")
  public void verifyMongoField(String path, String operator, String expectedValue) {
    if (!isUatMode) {
      log.info("Skipping MongoDB validation (Single) for path '{}' because mode '{}' is not 'UAT'", path, isUatMode);
      return;
    }

    Object actualValue = pollingHelper.poll(
        () -> fetchValue(path),
        pollTimeout,
        pollInterval
    );

    log.info("Assertion (Single): Path='{}', Op='{}', Expected='{}', Actual='{}'",
        path, operator, expectedValue, actualValue);

    performAssertion(actualValue, operator, expectedValue);
  }

  // --- LIST/TABLE ASSERTION ---
  @Then("the value of {string} is {string}:")
  public void verifyMongoFieldList(String path, String operator, DataTable dataTable) {
    if (!isUatMode) {
      log.info("Skipping MongoDB validation (List) for path '{}' because mode '{}' is not 'UAT'", path, isUatMode);
      return;
    }

    // 1. Prepare Expected Data with quote stripping
    List<String> expectedList = parseExpectedList(dataTable);

    log.info("Assertion (List): Path='{}', Op='{}', Expected={}", path, operator, expectedList);

    // 2. Delegate "Wait & Retry" logic to PollingHelper
    pollingHelper.pollUntilAsserted(
        () -> executeListAssertion(path, operator, expectedList),
        pollTimeout,
        pollInterval
    );
  }

  /**
   * Helper to parse DataTable list, handling CSV splitting and quote stripping.
   */
  private List<String> parseExpectedList(DataTable dataTable) {
    List<String> raw = dataTable.asList(String.class);
    if (raw.size() == 1 && raw.get(0).contains(",")) {
      // Split by comma, trim whitespace, and then strip quotes if present
      // This allows handling " " (space) vs "" (empty) correctly
      return Arrays.stream(raw.get(0).split(","))
          .map(String::trim)
          .map(this::stripQuotes)
          .collect(Collectors.toList());
    }
    return raw;
  }

  /**
   * Removes surrounding double quotes from a string if present.
   */
  private String stripQuotes(String value) {
    if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }

  private void executeListAssertion(String path, String operator, List<String> expectedList) {
    Object actualValue = fetchValue(path);

    if (actualValue == null) {
      throw new AssertionError("Value for path '" + path + "' is null (waiting for data...)");
    }

    if (!(actualValue instanceof List<?>)) {
      throw new AssertionError("Expected a List from MongoDB for path '" + path +
          "' but got: " + actualValue.getClass().getSimpleName());
    }

    List<String> actualStrings = ((List<?>) actualValue).stream()
        .map(String::valueOf)
        .collect(Collectors.toList());

    log.debug("Comparing lists: Expected={} vs Actual={}", expectedList, actualStrings);

    switch (operator.toLowerCase().replace(" ", "_")) {
      case "equal_to":
      case "equals":
        assertThat(actualStrings).isEqualTo(expectedList);
        break;
      case "contains_in_order":
        assertThat(actualStrings).containsSubsequence(expectedList);
        break;
      case "contains":
      case "contains_any":
        assertThat(actualStrings).containsAll(expectedList);
        break;
      default:
        throw new IllegalArgumentException("Unsupported list operator: " + operator);
    }
  }

  // --- Helpers ---

  private Object fetchValue(String path) {
    String[] parts = path.split("\\.", 3);
    if (parts.length < 3) return null;

    String dbName = parts[0];
    String collectionName = parts[1];
    String targetFieldPath = parts[2];
    String filterValue = commonSteps.getMessageIdentity();

    if (filterValue == null || filterValue.isEmpty()) {
      throw new IllegalStateException("No Message Identity found in context.");
    }

    return mongoGenericHelper.getField(dbName, collectionName, defaultFilterField, filterValue, targetFieldPath);
  }

  private void performAssertion(Object actual, String operator, String expected) {
    String op = operator.toLowerCase().replace(" ", "_");

    if (op.equals("is_null")) {
      assertThat(actual).isNull();
      return;
    }

    boolean checkEmpty = op.equals("is_empty") || op.equals("empty") ||
        ((op.equals("equal_to") || op.equals("equals")) &&
            (expected.equalsIgnoreCase("empty") || expected.equals("[]") || expected.isEmpty()));

    if (checkEmpty) {
      if (actual == null) return;
      if (actual instanceof List) assertThat((List<?>) actual).isEmpty();
      else if (actual instanceof String) assertThat((String) actual).isEmpty();
      return;
    }

    if (actual == null) {
      throw new AssertionError("Field is null/missing in MongoDB after polling.");
    }

    if (op.equals("is_not_null")) {
      assertThat(actual).isNotNull();
      return;
    }
    if (op.equals("is_not_empty") || op.equals("not_empty")) {
      if (actual instanceof List) assertThat((List<?>) actual).isNotEmpty();
      else if (actual instanceof String) assertThat((String) actual).isNotEmpty();
      return;
    }

    String actualStr = String.valueOf(actual);

    switch (op) {
      case "equal_to":
      case "equals":
        if (isNumeric(actualStr) && isNumeric(expected)) {
          assertThat(new BigDecimal(actualStr)).isEqualByComparingTo(new BigDecimal(expected));
        } else {
          assertThat(actualStr).isEqualTo(expected);
        }
        break;
      case "not_equal_to":
      case "!=":
        assertThat(actualStr).isNotEqualTo(expected);
        break;
      case "contains":
        if (actual instanceof List<?> list) {
          boolean match = list.stream().map(Object::toString).anyMatch(s -> s.contains(expected));
          assertThat(match)
              .withFailMessage("List %s does not contain matching element '%s'", list, expected)
              .isTrue();
        } else {
          assertThat(actualStr).contains(expected);
        }
        break;
      case "starts_with":
        assertThat(actualStr).startsWith(expected);
        break;
      case "greater_than":
        assertThat(new BigDecimal(actualStr)).isGreaterThan(new BigDecimal(expected));
        break;
      case "less_than":
        assertThat(new BigDecimal(actualStr)).isLessThan(new BigDecimal(expected));
        break;
      default:
        throw new IllegalArgumentException("Unsupported operator: " + operator);
    }
  }

  private boolean isNumeric(String str) {
    try {
      new BigDecimal(str);
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}