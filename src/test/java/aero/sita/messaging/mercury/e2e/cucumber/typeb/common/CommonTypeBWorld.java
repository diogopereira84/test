/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.common;

import aero.sita.messaging.mercury.e2e.utilities.format.typeb.CommonTypeB;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.SoaPattern;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessageBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.Before;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Shared world for BDD. Composer stays SAL/NAL-agnostic.
 */
public class CommonTypeBWorld {
  public final Map<String, String> routingDestination = new LinkedHashMap<>();
  public final Map<String, String> routingConnection = new LinkedHashMap<>();
  public CommonTypeB ctx = new CommonTypeB().reset();
  public String output;
  // To be filled by @When (backend)
  public Boolean resultAccepted;
  public String resultReason;
  public List<String> resultRecipients;
  public String resultPriority;

  public static String classifyPriority(String p) {
    String u = (p == null ? "" : p.toUpperCase(Locale.ROOT));
    switch (u) {
      case "SS":
      case "QS":
      case "QC":
        return "EMERGENCY";
      case "QU":
      case "QX":
        return "URGENT";
      case "QD":
        return "DEFERRED";
      // QK and any other/none => Normal per spec summary
      default:
        return "NORMAL";
    }
  }

  @Before
  public void reset() {
    ctx.reset();
    output = null;
    routingDestination.clear();
    routingConnection.clear();
    resultAccepted = null;
    resultReason = null;
    resultRecipients = null;
    resultPriority = null;
  }

  public String escaped() {
    return CommonTypeB.visualize(output);
  }

  public String escapedJson() throws JsonProcessingException {
    return CommonTypeB.escaped(output);
  }

  public String soaSeq() {
    return ctxSoa();
  }

  public String eoaSeq() {
    return ctxEoa();
  }

  // --- address parsing helpers (composer output only) ---
  public List<String> extractAddressElements() {
    if (output == null) {
      return Collections.emptyList();
    }
    String soa = soaSeq(), eoa = eoaSeq();
    List<String> elems = new ArrayList<>();
    int idx = 0;
    while (true) {
      int start = output.indexOf(soa, idx);
      if (start < 0) {
        break;
      }
      int end = output.indexOf(eoa, start + soa.length());
      if (end < 0) {
        break;
      }
      elems.add(output.substring(start + soa.length(), end));
      idx = end + eoa.length();
    }
    if (elems.isEmpty() && output != null) {
      int end = output.indexOf(eoa);
      if (end > 0) {
        elems.add(output.substring(0, end));
      }
    }
    return elems;
  }

  public List<String> extractRecipientsUppercased() {
    List<String> res = new ArrayList<>();
    Pattern ri = Pattern.compile("^(?:[A-Z0-9]{7}|CPY[A-Z0-9]{4})$"); // why: CPY counts as addressee
    for (String elem : extractAddressElements()) {
      for (String tok : elem.trim().split("\\s+")) {
        String t = tok.toUpperCase(Locale.ROOT);
        if (ri.matcher(t).matches()) {
          res.add(t);
        }
      }
    }
    return res;
  }

  public boolean hasDiversionLine(String ri7) {
    String probe = soaSeq() + "QSP " + ri7.toUpperCase(Locale.ROOT) + eoaSeq();
    return output != null && output.contains(probe);
  }

  public boolean originatorHasTrailingSpace(String oi) {
    String probe = oi + " ";
    return output != null && output.contains(probe);
  }

  public String detectPriorityClass() {
    List<String> elems = extractAddressElements();
    if (elems.isEmpty()) {
      return "NORMAL";
    }
    String[] toks = elems.get(0).trim().split("\\s+");
    String p = toks.length == 0 ? "" : toks[0].toUpperCase(Locale.ROOT);
    return classifyPriority(p);
  }

  public int countPilotElements() {
    if (output == null) {
      return 0;
    }
    Pattern pat = Pattern.compile(Pattern.quote(eoaSeq()) + "/////");
    Matcher m = pat.matcher(output);
    int c = 0;
    while (m.find()) {
      c++;
    }
    return c;
  }

  // --- private reflection to current config (no leaks) ---
  private String ctxSoa() {
    SoaPattern s = SoaPattern.CRLF_SOH;
    try {
      var f = CommonTypeB.class.getDeclaredField("soa");
      f.setAccessible(true);
      s = (SoaPattern) f.get(ctx);
    } catch (Exception ignore) {
    }
    return s.sequence();
  }

  private String ctxEoa() {
    TypeBMessageBuilder.AddressEoaToken e = TypeBMessageBuilder.AddressEoaToken.DOT;
    try {
      var f = CommonTypeB.class.getDeclaredField("eoa");
      f.setAccessible(true);
      e = (TypeBMessageBuilder.AddressEoaToken) f.get(ctx);
    } catch (Exception ignore) {
    }
    return e.sequence();
  }
}