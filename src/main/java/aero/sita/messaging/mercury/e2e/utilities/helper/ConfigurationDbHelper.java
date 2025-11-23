/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.helper;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Helper for accessing and validating data in the 'configuration' database.
 * Performs logic validation but delegates assertions to the test steps.
 */
@Slf4j
@Component
public class ConfigurationDbHelper {

  private static final List<String> REQUIRED_COLLECTIONS = List.of(
      "connections",
      "destinations",
      "hosts",
      "routes",
      "routing-indicators"
  );
  private final MongoClient mongoClient;
  private final String configDbName;

  @Autowired
  public ConfigurationDbHelper(MongoClient mongoClient,
                               @Value("${configuration.database.name:configuration}") String configDbName) {
    this.mongoClient = mongoClient;
    this.configDbName = configDbName;
  }

  /**
   * Checks all required collections and returns a list of those that are empty.
   *
   * @return List of empty collection names.
   */
  public List<String> getEmptyRequiredCollections() {
    log.info("Integrity Check: Verifying existence of required collections in '{}'...", configDbName);
    MongoDatabase db = mongoClient.getDatabase(configDbName);
    List<String> emptyCollections = new ArrayList<>();

    for (String collectionName : REQUIRED_COLLECTIONS) {
      long count = db.getCollection(collectionName).countDocuments();
      log.debug("Collection '{}': {} documents found.", collectionName, count);

      if (count == 0) {
        emptyCollections.add(collectionName);
      }
    }
    return emptyCollections;
  }

  /**
   * Validates a list of expected documents against the MongoDB collection.
   * - Uses `_id` as the mandatory primary key lookup.
   * - Compares all other fields provided in the expected data.
   * - Does NOT stop at the first failure.
   *
   * @param collectionNameWithDb The collection name (e.g. "configuration.hosts")
   * @param expectedRows         List of maps from Gherkin table
   * @return List of mismatch descriptions (empty if all match)
   */
  public List<String> validateCollectionContent(String collectionNameWithDb, List<Map<String, String>> expectedRows) {
    // Strip database prefix if present
    String collectionName = collectionNameWithDb.contains(".")
        ? collectionNameWithDb.substring(collectionNameWithDb.lastIndexOf(".") + 1)
        : collectionNameWithDb;

    log.info("Integrity Check: Validating {} documents in collection '{}'...", expectedRows.size(), collectionName);

    MongoDatabase db = mongoClient.getDatabase(configDbName);
    MongoCollection<Document> collection = db.getCollection(collectionName);
    List<String> errors = new ArrayList<>();

    for (Map<String, String> row : expectedRows) {
      String id = row.get("_id");
      if (id == null) {
        errors.add("Invalid Test Data: Row missing mandatory '_id' field in Gherkin table.");
        continue;
      }

      // 1. Find document by ID
      Document actualDoc = collection.find(Filters.eq("_id", id)).first();

      if (actualDoc == null) {
        String error = String.format("Document not found: Collection='%s', _id='%s'", collectionName, id);
        log.error(error);
        errors.add(error);
        continue; // Cannot compare fields if doc missing
      }

      // 2. Compare fields (Soft Assertion logic)
      validateFields(collectionName, id, row, actualDoc, errors);
    }

    log.info("Integrity Check Finished: Collection '{}'. Found {} discrepancies.", collectionName, errors.size());
    return errors;
  }

  private void validateFields(String collectionName, String id, Map<String, String> expectedRow,
                              Document actualDoc, List<String> errors) {
    expectedRow.forEach((key, expectedValueStr) -> {
      if (key.equals("_id")) {
        return; // Already checked
      }

      // Handle Array Notation: "destinationIds[]" -> "destinationIds"
      String actualKey = key.replace("[]", "");

      Object actualValue = getNestedValue(actualDoc, actualKey);
      Object expectedValueTyped = parseExpectedValue(expectedValueStr, actualValue);

      if (!valuesMatch(expectedValueTyped, actualValue)) {
        String error = String.format("Mismatch in '%s' (_id='%s'): Field '%s' -> Expected='%s' (%s), Actual='%s' (%s)",
            collectionName, id, actualKey,
            expectedValueTyped, (expectedValueTyped != null ? expectedValueTyped.getClass().getSimpleName() : "null"),
            actualValue, (actualValue != null ? actualValue.getClass().getSimpleName() : "null"));

        log.debug("FAIL: {}", error);
        errors.add(error);
      } else {
        log.debug("PASS: Collection='{}', _id='{}', Field='{}', Value='{}'", collectionName, id, actualKey, actualValue);
      }
    });
  }

  /**
   * Safely gets values, supporting nested dot notation (e.g., "criteria.type").
   */
  private Object getNestedValue(Document doc, String key) {
    if (!key.contains(".")) {
      return doc.get(key);
    }
    String[] parts = key.split("\\.");
    Document current = doc;
    for (int i = 0; i < parts.length - 1; i++) {
      Object val = current.get(parts[i]);
      if (val instanceof Document) {
        current = (Document) val;
      } else {
        return null; // Structure mismatch or null
      }
    }
    return current.get(parts[parts.length - 1]);
  }

  /**
   * Attempts to convert the Gherkin string to the type present in the DB.
   * This handles "true" (String) vs true (Boolean) mismatches.
   */
  private Object parseExpectedValue(String expectedStr, Object referenceValue) {
    if (referenceValue instanceof Boolean) {
      return Boolean.parseBoolean(expectedStr);
    }
    if (referenceValue instanceof Integer) {
      try {
        return Integer.parseInt(expectedStr);
      } catch (NumberFormatException e) {
        return expectedStr; // Fallback
      }
    }
    // Arrays/Lists handling - primitive contains check
    if (referenceValue instanceof List) {
      // For arrays, we keep the string to check if the list contains it (simplified logic)
      return expectedStr;
    }
    return expectedStr;
  }

  private boolean valuesMatch(Object expected, Object actual) {
    if (actual instanceof List) {
      // If DB has a list [A, B] and Gherkin says "A", we treat it as "contains"
      return ((List<?>) actual).contains(expected) || actual.toString().contains(String.valueOf(expected));
    }
    return Objects.equals(String.valueOf(expected), String.valueOf(actual));
  }
}