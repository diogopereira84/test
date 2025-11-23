/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb.section.utils;

import lombok.Getter;

@Getter
public enum HeadingField {

  PRE_SOA_TYPE("preSOAType"),
  ADDRESS_END_INDICATOR("AddressEndIndicator"),
  PILOT_SIGNAL("PilotSignal"),
  PLAIN_HEADING_TEXT("PlainHeadingText");

  private final String key;

  HeadingField(String key) {
    this.key = key;
  }

  public static HeadingField fromKey(String key) {
    for (HeadingField field : values()) {
      if (field.key.equalsIgnoreCase(key)) {
        return field;
      }
    }
    throw new IllegalArgumentException("Unknown HeadingField key: " + key);
  }
}
