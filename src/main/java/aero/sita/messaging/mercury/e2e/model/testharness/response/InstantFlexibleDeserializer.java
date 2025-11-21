/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.model.testharness.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class InstantFlexibleDeserializer extends JsonDeserializer<Instant> {

  @Override
  public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
    if (p.currentToken().isNumeric()) {
      return Instant.ofEpochMilli(p.getLongValue());
    }

    String text = p.getValueAsString();
    if (text == null || text.isBlank()) {
      return null;
    }
    text = text.trim();

    // 1) ISO
    Instant iso = tryParseIso(text);
    if (iso != null) {
      return iso;
    }

    // 2) epoch as string
    Instant epoch = tryParseEpoch(text);
    if (epoch != null) {
      return epoch;
    }

    // 3) give Jackson a nice error
    return (Instant) ctxt.handleWeirdStringValue(
        Instant.class,
        text,
        "Unsupported timestamp format (expected ISO-8601 or epoch millis)"
    );
  }

  private Instant tryParseIso(String text) {
    try {
      return Instant.parse(text);
    } catch (DateTimeParseException e) {
      return null; // intentionally fallback
    }
  }

  private Instant tryParseEpoch(String text) {
    try {
      long v = Long.parseLong(text);
      return Instant.ofEpochMilli(v);
    } catch (NumberFormatException e) {
      return null; // intentionally fallback
    }
  }
}