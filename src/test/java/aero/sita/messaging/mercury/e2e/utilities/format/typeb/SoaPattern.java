/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb;

/**
 * Tolerant SOA options; default to CRLF+SOH.
 */
public enum SoaPattern {
  CRLF_SOH(ControlChars.CRLF + ControlChars.SOH, "CRLF+SOH"),
  CRLF_SUB(ControlChars.CRLF + ControlChars.SUB, "CRLF+SUB"),
  CRLF_DOT(ControlChars.CRLF + ControlChars.DOT, "CRLF+DOT");

  public static final SoaPattern DEFAULT = CRLF_SOH;
  private final String sequence;
  private final String token;

  SoaPattern(String sequence, String token) {
    this.sequence = sequence;
    this.token = token;
  }

  public static SoaPattern parse(String value) {
    if (value == null || value.isEmpty()) {
      return DEFAULT;
    }
    if (value.equals(CRLF_SOH.sequence)) {
      return CRLF_SOH;
    }
    if (value.equals(CRLF_SUB.sequence)) {
      return CRLF_SUB;
    }
    if (value.equals(CRLF_DOT.sequence)) {
      return CRLF_DOT;
    }
    String n = value.trim().replace(' ', '+').replace('_', '+').toUpperCase();
    return switch (n) {
      case "CRLF+SOH", "SOH" -> CRLF_SOH;
      case "CRLF+SUB", "SUB" -> CRLF_SUB;
      case "CRLF+DOT", "DOT", "CRLF+." -> CRLF_DOT;
      default -> DEFAULT;
    };
  }

  public static SoaPattern detectIn(String text) {
    if (text == null || text.length() < 3) {
      return DEFAULT;
    }
    for (int i = 0; i < text.length() - 2; i++) {
      if (text.charAt(i) == '\r' && text.charAt(i + 1) == '\n') {
        char c = text.charAt(i + 2);
        if (c == '\u0001') {
          return CRLF_SOH;
        }
        if (c == '\u001A') {
          return CRLF_SUB;
        }
        if (c == '.') {
          return CRLF_DOT;
        }
      }
    }
    return DEFAULT;
  }

  public String sequence() {
    return sequence;
  }

  public String toToken() {
    return token;
  }

  public boolean isSpec() {
    return this == CRLF_SOH;
  }

  @Override
  public String toString() {
    return toToken();
  }
}