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
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MongoGenericHelper {

  private final MongoClient mongoClient;

  @Autowired
  public MongoGenericHelper(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  public Object getField(String databaseName, String collectionName, String filterField, Object filterValue, String targetField) {
    try {
      MongoDatabase db = mongoClient.getDatabase(databaseName);
      MongoCollection<Document> collection = db.getCollection(collectionName);

      Document doc = collection.find(Filters.eq(filterField, filterValue)).first();

      if (doc == null) {
        log.warn("Mongo Lookup Failed: No document in {}.{} where {} = {}",
            databaseName, collectionName, filterField, filterValue);
        return null;
      }

      return extractField(doc, targetField);

    } catch (Exception e) {
      log.error("Error executing generic Mongo query", e);
      throw new RuntimeException("Failed to retrieve data from MongoDB: " + e.getMessage(), e);
    }
  }

  /**
   * Helper to traverse nested documents using dot notation.
   * Supports:
   * 1. Standard nesting: "originator.address"
   * 2. Array index: "errors.0.errorCode"
   * 3. Array projection: "statusLogs.status" (Returns list of all statuses)
   */
  private Object extractField(Object current, String path) {
    if (current == null) return null;

    if (!path.contains(".")) {
      return getSingleValue(current, path);
    }

    String[] parts = path.split("\\.", 2);
    String currentPart = parts[0];
    String remainingPart = parts[1];

    Object nextObject = getSingleValue(current, currentPart);

    // Recursive call
    return extractField(nextObject, remainingPart);
  }

  private Object getSingleValue(Object current, String key) {
    if (current instanceof Document) {
      return ((Document) current).get(key);
    }
    else if (current instanceof List) {
      List<?> list = (List<?>) current;

      // Case A: Explicit Index (e.g., "0", "1")
      if (isInteger(key)) {
        int index = Integer.parseInt(key);
        if (index >= 0 && index < list.size()) {
          return list.get(index);
        }
        return null;
      }

      // Case B: Projection (Map over the list)
      // e.g. list of statusLogs, key is "status" -> return list of statuses
      List<Object> projected = new ArrayList<>();
      for (Object item : list) {
        if (item instanceof Document) {
          Object val = ((Document) item).get(key);
          if (val != null) projected.add(val);
        }
      }
      return projected;
    }

    return null;
  }

  private boolean isInteger(String s) {
    try {
      Integer.parseInt(s);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}