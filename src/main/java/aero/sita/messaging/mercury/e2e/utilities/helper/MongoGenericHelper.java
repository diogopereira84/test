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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Generic helper for retrieving specific data nodes from MongoDB.
 * Designed to be decoupled and flexible for any Database/Collection/Field lookup.
 */
@Slf4j
@Component
public class MongoGenericHelper {

  private final MongoClient mongoClient;

  @Autowired
  public MongoGenericHelper(MongoClient mongoClient) {
    this.mongoClient = mongoClient;
  }

  /**
   * Retrieves a specific field value from a MongoDB document.
   * Supports dot notation for nested fields (e.g., "errors.0.errorCode").
   *
   * @param databaseName   The name of the database (e.g., "configuration")
   * @param collectionName The name of the collection (e.g., "routes")
   * @param filterField    The field to filter by (e.g., "criteria.addressMatcher")
   * @param filterValue    The value to match in the filter field
   * @param targetField    The field to retrieve (e.g., "destinationIds")
   * @return The value of the target field, or null if not found
   */
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
   * Helper to traverse nested documents or lists using dot notation.
   */
  private Object extractField(Document doc, String path) {
    if (!path.contains(".")) {
      return doc.get(path);
    }

    String[] parts = path.split("\\.");
    Object current = doc;

    for (String part : parts) {
      if (current instanceof Document) {
        current = ((Document) current).get(part);
      } else if (current instanceof List) {
        try {
          int index = Integer.parseInt(part);
          current = ((List<?>) current).get(index);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
          return null;
        }
      } else {
        return null;
      }

      if (current == null) {
        return null;
      }
    }

    return current;
  }
}