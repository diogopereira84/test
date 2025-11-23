/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb.section.utils;

import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessageBuilder;
import java.util.Map;

public final class HeadingBuilderUtils {

  private HeadingBuilderUtils() {
  }

  public static void applyField(
      TypeBMessageBuilder builder,
      HeadingField field,
      String value
  ) {
    if (builder == null) {
      throw new IllegalArgumentException("builder must not be null");
    }

    switch (field) {
      case PRE_SOA_TYPE -> builder.preSOAType(value);
      case ADDRESS_END_INDICATOR -> builder.addressEndIndicator(value);
      case PILOT_SIGNAL -> builder.pilotSignal(value);
      case PLAIN_HEADING_TEXT -> builder.plainHeadingText(value);
      default -> throw new IllegalArgumentException("Unsupported field: " + field);
    }
  }

  public static void applyAll(
      TypeBMessageBuilder builder,
      Map<String, String> fieldValues
  ) {
    if (fieldValues == null || fieldValues.isEmpty()) {
      return;
    }
    for (Map.Entry<String, String> entry : fieldValues.entrySet()) {
      HeadingField field = HeadingField.fromKey(entry.getKey());
      applyField(builder, field, entry.getValue());
    }
  }

  public static String buildHeading(Map<String, String> fieldValues) {
    TypeBMessageBuilder builder = new TypeBMessageBuilder();
    applyAll(builder, fieldValues);
    return builder.compose();
  }
}