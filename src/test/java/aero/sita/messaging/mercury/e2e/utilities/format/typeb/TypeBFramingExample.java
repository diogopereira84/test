/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb;

public final class TypeBFramingExample {
  public static void main(String[] args) {
    String dot = new TypeBMessageBuilder().reset()
        .withSoa("CRLF+SOH").withEoaToken("DOT").withHeading("EX/DOT")
        .withAddressLine("QU RIOAA11 GRUAA11")
        .withOriginatorIndicator("RIOAA11").withMessageIdentity("001")
        .emitTextDelimiters(true).withTextLine("HELLO DOT").compose();

    String sub = new TypeBMessageBuilder().reset()
        .withSoa("CRLF+SOH").withEoaToken("SUB").withHeading("EX/SUB")
        .withAddressLine("QU RIOAA11 GRUAA11")
        .withOriginatorIndicator("RIOAA11").withMessageIdentity("002")
        .emitTextDelimiters(true).withTextLine("HELLO SUB").compose();

    System.out.println("--- DOT (CRLF+.) ---\n" + escape(dot) + "\n");
    System.out.println("--- SUB (\\u001A) ---\n" + escape(sub) + "\n");
  }

  // Why: make delimiters visible for quick eyeballing in console logs.
  private static String escape(String s) {
    return s.replace("\r", "\\r").replace("\n", "\\n\n")
        .replace("\u0001", "<SOH>").replace("\u0002", "<STX>").replace("\u0003", "<ETX>").replace("\u001A", "<SUB>");
  }
}