/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.helper;

import aero.sita.messaging.mercury.e2e.model.mongodb.IncomingMessage;
import aero.sita.messaging.mercury.e2e.model.mongodb.OutgoingMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * Service to provide fresh data reads from MongoDB, bypassing Spring Data repository caching.
 * <p>
 * This service uses MongoTemplate directly to execute queries, which ensures that each
 * call retrieves fresh data from the database without relying on Spring Data's entity cache.
 * <p>
 * This approach works consistently across all environments:
 * - Local: MongoDB standalone
 * - Dev/QA/Stage/Prod: Cosmos DB with MongoDB API
 * <p>
 * No transaction support required, making it simple, reliable, and environment-agnostic.
 */
@Slf4j
@Service
public class MongoDataRefresher {

  private final MongoTemplate mongoTemplate;

  @Autowired
  public MongoDataRefresher(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
    log.info("MongoDataRefresher initialized - using MongoTemplate for fresh data queries");
  }

  /**
   * Finds incoming messages by message identity with guaranteed fresh data.
   * <p>
   * Uses MongoTemplate directly to bypass Spring Data repository caching.
   * Each call executes a fresh MongoDB query.
   *
   * @param messageIdentity the message identity to search for
   * @return a list of {@link IncomingMessage} objects, fetched directly from MongoDB
   */
  public List<IncomingMessage> findIncomingByMessageIdentity(String messageIdentity) {
    log.debug("Fetching fresh incoming messages for messageIdentity: {}", messageIdentity);

    Query query = new Query(Criteria.where("messageIdentity").is(messageIdentity));
    List<IncomingMessage> messages = mongoTemplate.find(query, IncomingMessage.class);

    log.debug("Found {} incoming message(s)", messages.size());
    return messages;
  }

  /**
   * Finds outgoing messages by message identity with guaranteed fresh data.
   * <p>
   * Uses MongoTemplate directly to bypass Spring Data repository caching.
   * Each call executes a fresh MongoDB query.
   *
   * @param messageIdentity the message identity to search for
   * @return a list of {@link OutgoingMessage} objects, fetched directly from MongoDB
   */
  public List<OutgoingMessage> findOutgoingByMessageIdentity(String messageIdentity) {
    log.debug("Fetching fresh outgoing messages for messageIdentity: {}", messageIdentity);

    Query query = new Query(Criteria.where("messageIdentity").is(messageIdentity));
    List<OutgoingMessage> messages = mongoTemplate.find(query, OutgoingMessage.class);

    log.debug("Found {} outgoing message(s)", messages.size());
    return messages;
  }

  /**
   * Finds outgoing messages by incoming message ID with guaranteed fresh data.
   * <p>
   * Uses MongoTemplate directly to bypass Spring Data repository caching.
   * Each call executes a fresh MongoDB query.
   *
   * @param incomingMessageId the ID of the incoming message
   * @return a list of {@link OutgoingMessage} objects, fetched directly from MongoDB
   */
  public List<OutgoingMessage> findByIncomingMessageId(String incomingMessageId) {
    log.debug("Fetching fresh outgoing messages for incomingMessageId: {}", incomingMessageId);

    Query query = new Query(Criteria.where("incomingMessageId").is(incomingMessageId));
    List<OutgoingMessage> messages = mongoTemplate.find(query, OutgoingMessage.class);

    log.debug("Found {} outgoing message(s)", messages.size());
    return messages;
  }
}

