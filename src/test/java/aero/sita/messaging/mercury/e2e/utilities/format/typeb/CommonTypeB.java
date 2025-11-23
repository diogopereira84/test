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
  private String headingPrefix = "";
  private String headingTerminator = "";

  private String diversionRoutingIndicator;
  private String originatorIndicator;
  private String doubleSignature;
  private String messageIdentity;
  private String firstAddressOverrideRaw;

  public CommonTypeB reset() {
    soa = SoaPattern.CRLF_SOH;
    eoa = TypeBMessageBuilder.AddressEoaToken.DOT;
    emitTextDelimiters = false;
    emitSpacingUS = false;
    heading = null;
    headingPrefix = "";
    headingTerminator = "";
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

  public CommonTypeB withSoa(SoaPattern p) {
    this.soa = p;
    return this;
  }

  public CommonTypeB withSoa(String tokenOrSeq) {
    if ("no".equalsIgnoreCase(tokenOrSeq) || "noSOA".equalsIgnoreCase(tokenOrSeq)) {
      this.soa = null;
    } else {
      this.soa = SoaPattern.parse(tokenOrSeq);
    }
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

  public CommonTypeB withHeading(String h) {
    this.heading = h;
    return this;
  }

  public CommonTypeB withHeadingPrefix(String p) {
    this.headingPrefix = p;
    return this;
  }

  public CommonTypeB withHeadingTerminator(String t) {
    this.headingTerminator = t;
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

  public CommonTypeB withAddressLine(String line) {
    if (line != null) {
      addresses.add(new AddressEntry(AddressEntry.Kind.ADDRESS, line));
    }
    return this;
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

  public CommonTypeB withAddressOverrideRaw(String raw) {
    this.firstAddressOverrideRaw = raw;
    return this;
  }

  public CommonTypeB addPreTextRaw(String raw) {
    if (raw != null) preTextRaw.add(raw);
    return this;
  }

  public CommonTypeB addPostTextRaw(String raw) {
    if (raw != null) postTextRaw.add(raw);
    return this;
  }

  public String compose() {
    TypeBMessageBuilder c = new TypeBMessageBuilder().reset();

    if (soa == null) {
      c.withExplicitSoa(null);
    } else {
      c.withSoa(soa);
    }

    c.withEoaToken(eoa)
        .emitTextDelimiters(emitTextDelimiters)
        .emitSpacingUS(emitSpacingUS)
        .withHeading(heading)
        .withHeadingPrefix(headingPrefix)
        .withHeadingTerminator(headingTerminator)
        .withDiversionRoutingIndicator(diversionRoutingIndicator)
        .withOriginatorIndicator(originatorIndicator)
        .withDoubleSignature(doubleSignature)
        .withMessageIdentity(messageIdentity)
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
      try {
        String plaintext = escaped(out);
        LOG.info("[TypeB] (CTX) Composed (tokens)\n{}\n[TypeB] (CTX) Plaintext: {}", visualize(out), plaintext);
      } catch (JsonProcessingException e) {
        LOG.warn("Failed to generate plaintext log", e);
      }
    }
    return out;
  }

  public static String visualize(String s) {
    if (s == null) return "<null>";
    return s.replace(ControlChars.CRLF, "\\r\\n")
        .replace("\u0001", "<SOH>").replace("\u0002", "<STX>").replace("\u0003", "<ETX>")
        .replace("\u001A", "<SUB>")
        .replace("\u001F", "<US>");
  }

  public static String escaped(String s) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(s);
  }

  public String escaped() {
    return visualize(compose());
  }

  public String escapedJson() throws JsonProcessingException {
    return escaped(compose());
  }

  private static final class AddressEntry {
    final Kind kind;
    final String line;
    AddressEntry(Kind kind, String line) { this.kind = kind; this.line = line; }
    enum Kind { PILOT, ADDRESS }
  }
}