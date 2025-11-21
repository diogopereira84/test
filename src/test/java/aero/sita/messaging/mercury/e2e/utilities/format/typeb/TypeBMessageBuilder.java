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
 * Permissive TypeB builder. SAL/NAL are just "address lines" here.
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
  private String heading;
  private String diversionRoutingIndicator; // QSP {RI7}
  private String originatorIndicator;       // must be followed by space
  private String doubleSignature;
  private String messageIdentity;
  private String firstAddressOverrideRaw;

  // --- Discrete heading builder inputs (used by HeadingBuilderUtils) ---
  private String preSOAType;
  private String addressEndIndicator;
  private String pilotSignal;
  private String plainHeadingText;


  public TypeBMessageBuilder preSOAType(String value) {
    this.preSOAType = value;
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

  public TypeBMessageBuilder plainHeadingText(String value) {
    this.plainHeadingText = value;
    return this;
  }

  /**
   * Builds a heading line from the discrete fields managed by this builder.
   *
   * Order:
   *   [preSOAType] [plainHeadingText][addressEndIndicator][pilotSignal]
   *
   * All components are optional; null/blank values are simply skipped.
   * Caller is responsible for deciding how this string is used
   * (e.g. passing it into {@link #withHeading(String)}).
   */
  public String build() {
    StringBuilder sb = new StringBuilder();

    if (preSOAType != null && !preSOAType.isBlank()) {
      sb.append(preSOAType.trim()).append(' ');
    }

    if (plainHeadingText != null && !plainHeadingText.isBlank()) {
      sb.append(plainHeadingText.trim());
    }

    if (addressEndIndicator != null && !addressEndIndicator.isBlank()) {
      sb.append(addressEndIndicator.trim());
    }

    if (pilotSignal != null && !pilotSignal.isBlank()) {
      sb.append(pilotSignal.trim());
    }

    return sb.toString();
  }


  // --- Logging helpers ---
  public static String visualizeTokens(String s) {
    if (s == null) {
      return "null";
    }
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\r':
          b.append("<CR>");
          break;
        case '\n':
          b.append("<LF>");
          break;
        case 0x01:
          b.append("<SOH>");
          break;
        case 0x02:
          b.append("<STX>");
          break;
        case 0x03:
          b.append("<ETX>");
          break;
        case 0x1F:
          b.append("<US>");
          break;
        case 0x1A:
          b.append("<SUB>");
          break;
        default:
          b.append(c);
      }
    }
    return b.toString();
  }

  public static String visualizeEscaped(String s) {
    if (s == null) {
      return "null";
    }
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '\\':
          b.append("\\\\");
          break;
        case '"':
          b.append("\\");
          break;
        case '\r':
          b.append("\\r");
          break;
        case '\n':
          b.append("\\n");
          break;
        case '\t':
          b.append("\\t");
          break;
        default:
          if (c < 0x20 || c == 0x7F) {
            b.append(String.format("\\u%04x", (int) c));
          } else {
            b.append(c);
          }
      }
    }
    return b.toString();
  }

  public TypeBMessageBuilder reset() {
    soa = SoaPattern.CRLF_SOH;
    eoa = AddressEoaToken.DOT;
    emitTextDelimiters = false;
    emitSpacingUS = false;
    heading = null;
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

  public TypeBMessageBuilder withSoa(String tokenOrSeq) {
    this.soa = SoaPattern.parse(tokenOrSeq);
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

  public TypeBMessageBuilder withHeading(String h) {
    this.heading = h;
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
  } // why: pilot requires ///// post-EOA

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

  public String compose() {
    StringBuilder sb = new StringBuilder();

    if (heading != null && !heading.isEmpty()) {
      sb.append(heading).append(ControlChars.CRLF);
    }
    if (emitSpacingUS) {
      sb.append(ControlChars.US);
    }

    if (firstAddressOverrideRaw != null && !firstAddressOverrideRaw.isEmpty()) {
      sb.append(firstAddressOverrideRaw);
    }

    if (diversionRoutingIndicator != null) {
      sb.append(soa.sequence()).append("QSP ").append(diversionRoutingIndicator).append(eoa.sequence()); // why: diversion line format
    }

    for (AddressEntry e : addresses) {
      if (e.kind == AddressEntry.Kind.PILOT) {
        sb.append(soa.sequence()).append(e.line).append(eoa.sequence()).append("/////"); // why: pilot signal
      } else {
        sb.append(soa.sequence()).append(e.line).append(eoa.sequence());
      }
    }

    if (originatorIndicator != null) {
      sb.append(originatorIndicator).append(' '); // why: spec requires trailing space after originator
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
    String out = sb.toString();
    if (LOG.isInfoEnabled()) {
      LOG.info("[TypeB] Composed (tokens)\n{}", visualizeTokens(out));
      LOG.debug("[TypeB] Composed (escaped) \"{}\"", visualizeEscaped(out));
    }
    return out;
  }

  private String craftedSummary() {
    StringBuilder sb = new StringBuilder();
    sb.append("SOA=").append(soa == null ? "null" : soa.name())
        .append(", EOA=").append(eoa == null ? "null" : eoa.name())
        .append(", heading=").append(heading)
        .append(", diversionRI=").append(diversionRoutingIndicator)
        .append(", originator=").append(originatorIndicator)
        .append(", doubleSig=").append(doubleSignature)
        .append(", msgId=").append(messageIdentity)
        .append(", emitTextDelims=").append(emitTextDelimiters)
        .append(", emitUS=").append(emitSpacingUS)
        .append(", firstAddressOverride=").append(firstAddressOverrideRaw);
    sb.append("\nAddresses:");
    int i = 0;
    for (AddressEntry e : addresses) {
      sb.append("\n  ").append(++i).append(". ").append(e.kind).append(" -> ").append(e.line);
    }
    sb.append("\nTextLines:");
    i = 0;
    for (String t : textLines) {
      sb.append("\n  ").append(++i).append(". ").append(t);
    }
    sb.append("\npreTextRaw=").append(preTextRaw.size())
        .append(", postTextRaw=").append(postTextRaw.size());
    String out = sb.toString();
    if (LOG.isInfoEnabled()) {
      LOG.info("[TypeB] Composed (tokens)\n{}", visualizeTokens(out));
      LOG.debug("[TypeB] Composed (escaped) \"{}\"", visualizeEscaped(out));
    }
    return out;
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
      String v = value.trim();
      String u = v.toUpperCase(Locale.ROOT);
      if (u.equals("DOT") || u.equals("CRLF+DOT") || v.equals(ControlChars.CRLF + ControlChars.DOT) || v.equals(".")) {
        return DOT;
      }
      if (u.equals("SUB") || u.equals("CRLF+SUB") || v.equals(ControlChars.SUB) || v.equals(ControlChars.CRLF + ControlChars.SUB)) {
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