/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.config;

import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessage;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ReceivedMessagesResponse;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom deserializer for ReceivedMessagesResponse that handles both:
 * 1. Direct array response: []
 * 2. Wrapped object response: {"receivedMessages": []}
 * <p>
 * This provides flexibility when the API returns different response formats.
 */
public class ReceivedMessagesResponseDeserializer extends JsonDeserializer<ReceivedMessagesResponse> {

  @Override
  public ReceivedMessagesResponse deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {

    ObjectMapper mapper = (ObjectMapper) parser.getCodec();
    JsonToken currentToken = parser.getCurrentToken();

    if (currentToken == JsonToken.START_ARRAY) {
      // Handle direct array response: [{...}, {...}]
      JsonNode arrayNode = mapper.readTree(parser);
      List<ReceivedMessage> messages = new ArrayList<>();

      for (JsonNode node : arrayNode) {
        ReceivedMessage message = mapper.treeToValue(node, ReceivedMessage.class);
        messages.add(message);
      }

      return ReceivedMessagesResponse.builder()
          .receivedMessages(messages)
          .build();

    } else if (currentToken == JsonToken.START_OBJECT) {
      // Handle wrapped object response: {"receivedMessages": [...]}
      JsonNode objectNode = mapper.readTree(parser);

      if (objectNode.has("receivedMessages")) {
        JsonNode messagesNode = objectNode.get("receivedMessages");
        List<ReceivedMessage> messages = new ArrayList<>();

        for (JsonNode node : messagesNode) {
          ReceivedMessage message = mapper.treeToValue(node, ReceivedMessage.class);
          messages.add(message);
        }

        return ReceivedMessagesResponse.builder()
            .receivedMessages(messages)
            .build();
      } else {
        throw new IOException("Object does not contain 'receivedMessages' field");
      }

    } else {
      throw new IOException("Unexpected token: " + currentToken + ". Expected START_ARRAY or START_OBJECT");
    }
  }
}

