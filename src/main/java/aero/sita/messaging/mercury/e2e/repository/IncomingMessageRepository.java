/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.repository;

import aero.sita.messaging.mercury.e2e.model.mongodb.IncomingMessage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for accessing incoming-messages collection in MongoDB.
 */
@Repository
public interface IncomingMessageRepository extends MongoRepository<IncomingMessage, String> {

  /**
   * Find all incoming messages by message identity.
   * The messageIdentity field is extracted from the message rawData (e.g., "281440/160B99PSA").
   * Since messageIdentity is not unique, this method returns all matching messages.
   *
   * @param messageIdentity the message identity to search for
   * @return list of incoming messages matching the message identity
   */
  List<IncomingMessage> findByMessageIdentity(String messageIdentity);

  /**
   * Find incoming message by correlation ID.
   *
   * @param correlationId the correlation ID to search for
   * @return optional containing the message if found
   */
  Optional<IncomingMessage> findByCorrelationId(String correlationId);

  /**
   * Find all incoming messages by correlation ID.
   *
   * @param correlationId the correlation ID to search for
   * @return list of messages with matching correlation ID
   */
  List<IncomingMessage> findAllByCorrelationId(String correlationId);

  /**
   * Find incoming message by incoming connection ID and raw data.
   *
   * @param incomingConnectionId the connection ID
   * @param rawData              the raw message data
   * @return optional containing the message if found
   */
  Optional<IncomingMessage> findByIncomingConnectionIdAndRawData(String incomingConnectionId, String rawData);

  /**
   * Find incoming messages by incoming connection ID.
   *
   * @param incomingConnectionId the connection ID
   * @return list of messages from this connection
   */
  List<IncomingMessage> findByIncomingConnectionId(String incomingConnectionId);
}

