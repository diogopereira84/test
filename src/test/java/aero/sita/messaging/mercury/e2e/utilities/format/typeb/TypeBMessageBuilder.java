/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Permissive TypeB builder.
 * Refactored to support 'Assumed Address' scenarios and prevent Double-CRLF issues.
 */
public class TypeBMessageBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(TypeBMessageBuilder.class);
  private final List<AddressEntry> addresses = new ArrayList<>();
  private final List<String> textLines = new ArrayList<>();
  private final List<String> preTextRaw = new ArrayList<>();
  private final List<String> postTextRaw = new ArrayList<>();

  private SoaPattern soa = SoaPattern.CRLF_SOH;
  private AddressEoaToken eoa = AddressEoaToken.DOT;
  private boolean emitTextDelimiters = false;
  private boolean emitSpacingUS = false;

  // Heading fields
  private String heading;
  private String headingPrefix = "";
  private String headingTerminator = "";

  // Discrete components
  private String preSOAType;
  private String plainHeadingText;
  private String addressEndIndicator;
  private String pilotSignal;

  private String diversionRoutingIndicator;
  private String originatorIndicator;
  private String doubleSignature;
  private String messageIdentity;
  private String firstAddressOverrideRaw;

  // --- Public Setters ---

  public static String visualizeTokens(String s) {
    if (s == null) {
      return "null";
    }
    StringBuilder b = new StringBuilder();
    for (char c : s.toCharArray()) {
      if (c == '\r') {
        b.append("<CR>");
      } else if (c == '\n') {
        b.append("<LF>");
      } else if (c == 0x01) {
        b.append("<SOH>");
      } else if (c == 0x02) {
        b.append("<STX>");
      } else if (c == 0x03) {
        b.append("<ETX>");
      } else if (c == 0x1F) {
        b.append("<US>");
      } else if (c == 0x1A) {
        b.append("<SUB>");
      } else {
        b.append(c);
      }
    }
    return b.toString();
  }

  public static String visualizeEscaped(String s) {
    if (s == null) {
      return "null";
    }
    return s.replace("\r", "\\r").replace("\n", "\\n");
  }

  public TypeBMessageBuilder preSOAType(String value) {
    this.preSOAType = value;
    return this;
  }

  public TypeBMessageBuilder plainHeadingText(String value) {
    this.plainHeadingText = value;
    return this;
  }

  public TypeBMessageBuilder addressEndIndicator(String value) {
    this.addressEndIndicator = value;
    return this;
  }

  public TypeBMessageBuilder pilotSignal(String value) {
    this.pilotSignal = value;
    return this;
  }

  public TypeBMessageBuilder withHeading(String h) {
    this.heading = h;
    return this;
  }

  public TypeBMessageBuilder withHeadingPrefix(String prefix) {
    this.headingPrefix = prefix == null ? "" : prefix;
    return this;
  }

  public TypeBMessageBuilder withHeadingTerminator(String terminator) {
    this.headingTerminator = terminator == null ? "" : terminator;
    return this;
  }

  public TypeBMessageBuilder reset() {
    soa = SoaPattern.CRLF_SOH;
    eoa = AddressEoaToken.DOT;
    emitTextDelimiters = false;
    emitSpacingUS = false;
    heading = null;
    headingPrefix = "";
    headingTerminator = "";
    preSOAType = null;
    plainHeadingText = null;
    addressEndIndicator = null;
    pilotSignal = null;
    diversionRoutingIndicator = null;
    addresses.clear();
    originatorIndicator = null;
    doubleSignature = null;
    messageIdentity = null;
    textLines.clear();
    firstAddressOverrideRaw = null;
    preTextRaw.clear();
    postTextRaw.clear();
    return this;
  }

  public TypeBMessageBuilder withSoa(SoaPattern p) {
    this.soa = (p == null ? SoaPattern.CRLF_SOH : p);
    return this;
  }

  public TypeBMessageBuilder withExplicitSoa(SoaPattern p) {
    this.soa = p;
    return this;
  }

  public TypeBMessageBuilder withSoa(String tokenOrSeq) {
    if ("no".equalsIgnoreCase(tokenOrSeq) || "noSOA".equalsIgnoreCase(tokenOrSeq)) {
      this.soa = null;
    } else {
      this.soa = SoaPattern.parse(tokenOrSeq);
    }
    return this;
  }

  public TypeBMessageBuilder withEoaToken(AddressEoaToken t) {
    this.eoa = (t == null ? AddressEoaToken.DOT : t);
    return this;
  }

  public TypeBMessageBuilder withEoaToken(String tokenOrSeq) {
    this.eoa = AddressEoaToken.parse(tokenOrSeq);
    return this;
  }

  public TypeBMessageBuilder emitTextDelimiters(boolean on) {
    this.emitTextDelimiters = on;
    return this;
  }

  public TypeBMessageBuilder emitSpacingUS(boolean on) {
    this.emitSpacingUS = on;
    return this;
  }

  public TypeBMessageBuilder withDiversionRoutingIndicator(String ri7) {
    this.diversionRoutingIndicator = (ri7 == null ? null : ri7.trim());
    return this;
  }

  public TypeBMessageBuilder withPilot(String line) {
    if (line != null) {
      addresses.add(new AddressEntry(AddressEntry.Kind.PILOT, line));
    }
    return this;
  }

  public TypeBMessageBuilder withAddressLine(String line) {
    if (line != null) {
      addresses.add(new AddressEntry(AddressEntry.Kind.ADDRESS, line));
    }
    return this;
  }

  @Deprecated
  public TypeBMessageBuilder withSal(String line) {
    return withAddressLine(line);
  }

  @Deprecated
  public TypeBMessageBuilder withNal(String line) {
    return withAddressLine(line);
  }

  public TypeBMessageBuilder withOriginatorIndicator(String oi) {
    this.originatorIndicator = (oi == null ? null : oi.trim());
    return this;
  }

  public TypeBMessageBuilder withDoubleSignature(String ds) {
    this.doubleSignature = (ds == null || ds.isBlank()) ? null : ds.trim();
    return this;
  }

  public TypeBMessageBuilder withMessageIdentity(String mi) {
    this.messageIdentity = (mi == null || mi.isBlank()) ? null : mi;
    return this;
  }

  public TypeBMessageBuilder withTextLine(String t) {
    if (t != null) {
      textLines.add(t);
    }
    return this;
  }

  public TypeBMessageBuilder withTextLines(List<String> t) {
    if (t != null) {
      t.forEach(this::withTextLine);
    }
    return this;
  }

  public TypeBMessageBuilder withAddressOverrideRaw(String raw) {
    this.firstAddressOverrideRaw = raw;
    return this;
  }

  public TypeBMessageBuilder addPreTextRawSection(String raw) {
    if (raw != null) {
      preTextRaw.add(raw);
    }
    return this;
  }

  public TypeBMessageBuilder addPostTextRawSection(String raw) {
    if (raw != null) {
      postTextRaw.add(raw);
    }
    return this;
  }

  private String buildInternalHeading() {
    if (heading != null) {
      return heading;
    }

    StringBuilder sb = new StringBuilder();
    if (preSOAType != null) {
      sb.append(preSOAType).append(" ");
    }
    if (plainHeadingText != null) {
      sb.append(plainHeadingText);
    }
    if (addressEndIndicator != null) {
      sb.append(addressEndIndicator);
    }
    if (pilotSignal != null) {
      sb.append(pilotSignal);
    }

    return sb.toString();
  }

  public String compose() {
    StringBuilder sb = new StringBuilder();

    String effectiveHeading = buildInternalHeading();

    // 1. Heading Section
    if (effectiveHeading != null && !effectiveHeading.isEmpty()) {
      sb.append(headingPrefix);
      sb.append(effectiveHeading);
      sb.append(headingTerminator);

      // Logic to prevent Double-CRLF:
      // Only add default CRLF if:
      // a) No explicit terminator was set
      // b) AND the SOA does NOT already start with CRLF
      if (headingTerminator.isEmpty()) {
        boolean soaHasCrlf = (soa != null && soa.sequence().startsWith(ControlChars.CRLF));
        if (!soaHasCrlf) {
          sb.append(ControlChars.CRLF);
        }
      }
    }

    if (emitSpacingUS) {
      sb.append(ControlChars.US);
    }

    // 2. SOA (Start of Address)
    if (soa != null) {
      sb.append(soa.sequence());
    }

    if (firstAddressOverrideRaw != null && !firstAddressOverrideRaw.isEmpty()) {
      sb.append(firstAddressOverrideRaw);
    }

    if (diversionRoutingIndicator != null && soa != null) {
      sb.append("QSP ").append(diversionRoutingIndicator).append(eoa.sequence());
    }

    // 3. Addresses
    for (AddressEntry e : addresses) {
      if (e.kind == AddressEntry.Kind.PILOT) {
        sb.append(e.line).append(eoa.sequence()).append("/////");
      } else {
        sb.append(e.line).append(eoa.sequence());
      }
    }

    // 4. Originator
    if (originatorIndicator != null) {
      sb.append(originatorIndicator).append(' ');
      if (doubleSignature != null) {
        sb.append(doubleSignature);
      }
      if (messageIdentity != null) {
        sb.append(messageIdentity);
      }
    }

    for (String raw : preTextRaw) {
      sb.append(raw);
    }

    // 5. Text Body
    if (emitTextDelimiters || !textLines.isEmpty()) {
      sb.append(ControlChars.CRLF).append(ControlChars.STX);
      if (textLines.isEmpty()) {
        sb.append(ControlChars.CRLF).append(ControlChars.ETX);
      } else {
        for (String t : textLines) {
          sb.append(t == null ? "" : t).append(ControlChars.CRLF);
        }
        sb.append(ControlChars.ETX);
      }
    }

    for (String raw : postTextRaw) {
      sb.append(raw);
    }

    return sb.toString();
  }

  public enum AddressEoaToken {
    SUB {
      public String sequence() {
        return ControlChars.SUB;
      }
    },
    DOT {
      public String sequence() {
        return ControlChars.CRLF + ControlChars.DOT;
      }
    };

    public static AddressEoaToken parse(String value) {
      if (value == null || value.isBlank()) {
        return DOT;
      }
      String v = value.trim().toUpperCase(Locale.ROOT);
      if (v.contains("SUB")) {
        return SUB;
      }
      return DOT;
    }

    public abstract String sequence();
  }

  private static final class AddressEntry {
    final Kind kind;
    final String line;

    AddressEntry(Kind kind, String line) {
      this.kind = kind;
      this.line = line;
    }

    enum Kind { PILOT, ADDRESS }
  }
}