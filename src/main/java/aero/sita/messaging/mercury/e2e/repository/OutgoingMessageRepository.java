/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.repository;

import aero.sita.messaging.mercury.e2e.model.mongodb.OutgoingMessage;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for OutgoingMessage MongoDB collection.
 * Spring Data MongoDB will automatically implement these methods.
 */
@Repository
public interface OutgoingMessageRepository extends MongoRepository<OutgoingMessage, String> {

  /**
   * Find outgoing message by incoming message ID.
   *
   * @param incomingMessageId the incoming message ID to search for
   * @return optional containing the outgoing message if found
   */
  List<OutgoingMessage> findByIncomingMessageId(String incomingMessageId);

  /**
   * Find all outgoing messages by message identity.
   * The messageIdentity field is extracted from the message content.
   *
   * @param messageIdentity the message identity to search for
   * @return list of outgoing messages with matching messageIdentity
   */
  List<OutgoingMessage> findByMessageIdentity(String messageIdentity);

  /**
   * Find outgoing messages by correlation ID.
   *
   * @param correlationId the correlation ID to search for
   * @return list of outgoing messages with matching correlationId
   */
  List<OutgoingMessage> findByCorrelationId(String correlationId);

  /**
   * Find outgoing messages by outgoing format.
   *
   * @param outgoingFormat the outgoing format (e.g., "TYPE_B", "JSON")
   * @return list of outgoing messages with matching format
   */
  List<OutgoingMessage> findByOutgoingFormat(String outgoingFormat);
}
