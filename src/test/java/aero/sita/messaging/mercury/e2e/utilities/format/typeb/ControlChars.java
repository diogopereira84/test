/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb;

public final class ControlChars {
  public static final String CRLF = "\r\n";
  public static final String SOH = "\u0001";
  public static final String STX = "\u0002";
  public static final String ETX = "\u0003";
  public static final String SUB = "\u001A"; // legacy
  public static final String US = "\u001F"; // spacing signal (optional)  // why: spec allows optional US
  public static final String DOT = ".";

  private ControlChars() {
  }
}