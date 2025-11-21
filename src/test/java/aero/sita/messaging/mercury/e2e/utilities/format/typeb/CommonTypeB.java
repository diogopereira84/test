/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.utilities.format.typeb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single common context across sections (permissive).
 */
public class CommonTypeB {
  private static final Logger LOG = LoggerFactory.getLogger(CommonTypeB.class);
  private final List<AddressEntry> addresses = new ArrayList<>();
  private final List<String> textLines = new ArrayList<>();
  private final List<String> preTextRaw = new ArrayList<>();
  private final List<String> postTextRaw = new ArrayList<>();
  private SoaPattern soa = SoaPattern.CRLF_SOH;
  private TypeBMessageBuilder.AddressEoaToken eoa = TypeBMessageBuilder.AddressEoaToken.DOT;
  private boolean emitTextDelimiters = false;
  private boolean emitSpacingUS = false;
  private String heading;
  private String diversionRoutingIndicator;
  private String originatorIndicator;
  private String doubleSignature;
  private String messageIdentity;
  private String firstAddressOverrideRaw;


  public static String visualize(String s) {
    if (s == null) {
      return "<null>";
    }
    return s.replace(ControlChars.CRLF, "\\r\\n")
        .replace("\u0001", "<SOH>").replace("\u0002", "<STX>").replace("\u0003", "<ETX>").replace("\u001A", "<SUB>");
  }

  public static String escaped(String s) throws JsonProcessingException {
    ObjectMapper om = new ObjectMapper();
    return om.writeValueAsString(s);
  }

  public CommonTypeB reset() {
    soa = SoaPattern.CRLF_SOH;
    eoa = TypeBMessageBuilder.AddressEoaToken.DOT;
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

  // Config
  public CommonTypeB withSoa(SoaPattern p) {
    this.soa = (p == null ? SoaPattern.CRLF_SOH : p);
    return this;
  }

  public CommonTypeB withSoa(String tokenOrSeq) {
    this.soa = SoaPattern.parse(tokenOrSeq);
    return this;
  }

  public CommonTypeB withEoa(TypeBMessageBuilder.AddressEoaToken t) {
    this.eoa = (t == null ? TypeBMessageBuilder.AddressEoaToken.DOT : t);
    return this;
  }

  public CommonTypeB withEoa(String tokenOrSeq) {
    this.eoa = TypeBMessageBuilder.AddressEoaToken.parse(tokenOrSeq);
    return this;
  }

  public CommonTypeB emitTextDelimiters(boolean on) {
    this.emitTextDelimiters = on;
    return this;
  }

  public CommonTypeB emitSpacingUS(boolean on) {
    this.emitSpacingUS = on;
    return this;
  }

  // Sections
  public CommonTypeB withHeading(String h) {
    this.heading = h;
    return this;
  }

  public CommonTypeB withDiversionRoutingIndicator(String ri7) {
    this.diversionRoutingIndicator = (ri7 == null ? null : ri7.trim());
    return this;
  }

  public CommonTypeB withPilot(String line) {
    if (line != null) {
      addresses.add(new AddressEntry(AddressEntry.Kind.PILOT, line));
    }
    return this;
  }

  /**
   * Preferred unified API
   */
  public CommonTypeB withAddressLine(String line) {
    if (line != null) {
      addresses.add(new AddressEntry(AddressEntry.Kind.ADDRESS, line));
    }
    return this;
  }

  /**
   * @deprecated
   */
  @Deprecated
  public CommonTypeB withSal(String line) {
    return withAddressLine(line);
  }

  /**
   * @deprecated
   */
  @Deprecated
  public CommonTypeB withNal(String line) {
    return withAddressLine(line);
  }

  public CommonTypeB withOriginatorIndicator(String oi) {
    this.originatorIndicator = (oi == null ? null : oi.trim());
    return this;
  }

  public CommonTypeB withDoubleSignature(String ds) {
    this.doubleSignature = (ds == null || ds.isBlank()) ? null : ds.trim();
    return this;
  }

  public CommonTypeB withMessageIdentity(String mi) {
    this.messageIdentity = (mi == null || mi.isBlank()) ? null : mi;
    return this;
  }

  public CommonTypeB withTextLine(String t) {
    if (t != null) {
      textLines.add(t);
    }
    return this;
  }

  public CommonTypeB withTextLines(List<String> t) {
    if (t != null) {
      t.forEach(this::withTextLine);
    }
    return this;
  }

  // Escape hatches
  public CommonTypeB withAddressOverrideRaw(String raw) {
    this.firstAddressOverrideRaw = raw;
    return this;
  }

  public CommonTypeB addPreTextRaw(String raw) {
    if (raw != null) {
      preTextRaw.add(raw);
    }
    return this;
  }

  public CommonTypeB addPostTextRaw(String raw) {
    if (raw != null) {
      postTextRaw.add(raw);
    }
    return this;
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
    return sb.toString();
  }

  public String compose() {
    TypeBMessageBuilder c = new TypeBMessageBuilder().reset()
        .withSoa(soa).withEoaToken(eoa).emitTextDelimiters(emitTextDelimiters).emitSpacingUS(emitSpacingUS)
        .withHeading(heading).withDiversionRoutingIndicator(diversionRoutingIndicator)
        .withOriginatorIndicator(originatorIndicator).withDoubleSignature(doubleSignature).withMessageIdentity(messageIdentity)
        .withAddressOverrideRaw(firstAddressOverrideRaw);
    for (AddressEntry e : addresses) {
      if (e.kind == AddressEntry.Kind.PILOT) {
        c.withPilot(e.line);
      } else {
        c.withAddressLine(e.line);
      }
    }
    textLines.forEach(c::withTextLine);
    preTextRaw.forEach(c::addPreTextRawSection);
    postTextRaw.forEach(c::addPostTextRawSection);
    String out = c.compose();
    if (LOG.isInfoEnabled()) {
      LOG.info("[TypeB] (CTX) Composed (tokens)\n{}", visualize(out));
      try {
        LOG.debug("[TypeB] (CTX) Composed (escaped) \"{}\"", escaped(out));
      } catch (Exception ignore) {
      }
    }
    return out;
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
