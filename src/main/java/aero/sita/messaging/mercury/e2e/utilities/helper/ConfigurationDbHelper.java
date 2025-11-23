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

  public List<String> validateCollectionContent(String collectionNameWithDb, List<Map<String, String>> expectedRows) {
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
        errors.add("Invalid Test Data: Row missing mandatory '_id' field.");
        continue;
      }

      Document actualDoc = collection.find(Filters.eq("_id", id)).first();

      if (actualDoc == null) {
        errors.add(String.format("Document not found: Collection='%s', _id='%s'", collectionName, id));
        continue;
      }

      validateFields(collectionName, id, row, actualDoc, errors);
    }
    return errors;
  }

  private void validateFields(String collectionName, String id, Map<String, String> expectedRow,
                              Document actualDoc, List<String> errors) {
    expectedRow.forEach((key, expectedValueStr) -> {
      if (key.equals("_id")) {
        return;
      }

      String actualKey = key.replace("[]", "");
      Object actualValue = getNestedValue(actualDoc, actualKey);
      Object expectedValueTyped = parseExpectedValue(expectedValueStr, actualValue);

      if (!valuesMatch(expectedValueTyped, actualValue)) {
        errors.add(String.format("Mismatch in '%s' (_id='%s'): Field '%s' -> Expected='%s', Actual='%s'",
            collectionName, id, actualKey, expectedValueTyped, actualValue));
      }
    });
  }

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
        return null;
      }
    }
    return current.get(parts[parts.length - 1]);
  }

  private Object parseExpectedValue(String expectedStr, Object referenceValue) {
    if (referenceValue instanceof Boolean) {
      return Boolean.parseBoolean(expectedStr);
    }
    if (referenceValue instanceof Integer) {
      try {
        return Integer.parseInt(expectedStr);
      } catch (NumberFormatException e) {
        return expectedStr;
      }
    }
    return expectedStr;
  }

  private boolean valuesMatch(Object expected, Object actual) {
    if (actual instanceof List) {
      return ((List<?>) actual).contains(expected) || actual.toString().contains(String.valueOf(expected));
    }
    return Objects.equals(String.valueOf(expected), String.valueOf(actual));
  }
}