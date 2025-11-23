/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.common;

import aero.sita.messaging.mercury.e2e.utilities.format.typeb.ControlChars;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessageBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonBuildSteps {
  private static final Logger LOG = LoggerFactory.getLogger(CommonBuildSteps.class);
  private final CommonTypeBWorld common;

  public CommonBuildSteps(CommonTypeBWorld world) {
    this.common = world;
  }

  @Given("a clean TypeBComposer")
  public void cleanComposer() {
    common.ctx.reset();
    common.output = null;
  }

  @Given("I start composing a Type-B message with SOA {string} and EOA {string}")
  public void startWithSoaEoa(String soa, String eoa) {
    common.ctx.withSoa(soa).withEoa(eoa);
  }

  // NOTE: "I set heading {string}" was moved to HeadingSteps to avoid duplication

  @Given("I set diversion routing indicator {string}")
  public void setDri(String ri7) {
    common.ctx.withDiversionRoutingIndicator(ri7);
  }

  @Given("emit spacing US {string}")
  public void emitUS(String on) {
    common.ctx.emitSpacingUS(Boolean.parseBoolean(on));
  }

  @Given("I set originator {string} and identity {string}")
  public void setOriginator(String oi, String id) {
    common.ctx.withOriginatorIndicator(oi).withMessageIdentity(id);
  }

  @Given("emit text delimiters {string}")
  public void emitText(String on) {
    common.ctx.emitTextDelimiters(Boolean.parseBoolean(on));
  }

  @Given("I add text line {string}")
  public void addTextLine(String t) {
    common.ctx.withTextLine(t);
  }

  @Given("I add address line {string}")
  public void addAddressLine(String line) {
    common.ctx.withAddressLine(line);
  }

  @Given("add SAL line {string}")
  public void addSalCompat(String line) {
    common.ctx.withAddressLine(line);
  }

  @Given("add NAL line {string}")
  public void addNalCompat(String line) {
    common.ctx.withAddressLine(line);
  }

  @Given("I add pilot address line {string} with pilot signal {string}")
  public void addPilotWithSignal(String line, String signal) {
    common.ctx.withPilot(line);
  }

  @Given("I add pilot address line {string} without pilot signal")
  public void addPilotWithoutSignal(String line) {
    common.ctx.withAddressLine(line);
  }

  @Given("raw first address block is")
  public void rawFirstAddress(String docString) {
    common.ctx.withAddressOverrideRaw(docString);
  }

  @Given("I craft a raw message with heading {string} and first address element {string} without SOA and with EOA {string}")
  public void rawHeadingFirstElemNoSoa(String heading, String firstElem, String eoaToken) {
    String eoa = TypeBMessageBuilder.AddressEoaToken.parse(eoaToken).sequence();
    common.output =
        heading + ControlChars.CRLF + firstElem + eoa + "LKYSOLT 3456700" + ControlChars.CRLF + ControlChars.STX + "UAT Validation" + ControlChars.CRLF +
            ControlChars.ETX;
  }

  @Given("I craft a raw message without any Address Section")
  public void rawWithoutAddress() {
    common.output = "";
  }

  @Given("I craft a raw message starting directly with {string} with no heading, no CRLF prefix")
  public void rawStartWithoutSoa(String firstElem) {
    common.output = firstElem + common.eoaSeq();
  }

  @Given("I append the correct EOA for the element")
  public void appendCorrectEoa() {
    if (common.output == null) {
      common.output = "";
    }
    common.output = common.output + TypeBMessageBuilder.AddressEoaToken.DOT.sequence() + "LKYSOLT 3456700" + ControlChars.CRLF + ControlChars.STX + "UAT Validation" +
        ControlChars.CRLF + ControlChars.ETX;
  }

  /**
   * Finalizes the message and logs visual + plaintext output.
   */
  @Given("I finalize the composed message")
  public void finalizeCompose() throws JsonProcessingException {
    boolean hadRaw = common.output != null && !common.output.isEmpty();
    if (!hadRaw) {
      common.output = common.ctx.compose();
    }

    String visual = common.escaped();
    String plaintext = common.escapedJson();

    LOG.info("----------------------------------------------------------------");
    LOG.info("[TypeB] COMPOSED (visual): {}", visual);
    LOG.info("[TypeB] COMPOSED (plaintext): {}", plaintext);
    LOG.info("----------------------------------------------------------------");
  }

  @Given("I craft a raw message with an address element followed by stray text {string} between EOA and the next SOA")
  public void rawWithStrayTextBetweenElements(String stray) {
    String soa = common.soaSeq(), eoa = common.eoaSeq();
    common.output = soa + "QN SWIRI1G" + eoa + stray + soa + "QN LKYEGLT" + eoa;
  }

  @Given("I craft a raw message where the first element uses {string} and omits the EOA terminator")
  public void rawFirstElementOmitEoa(String soaCase) {
    String firstSoa = aero.sita.messaging.mercury.e2e.utilities.format.typeb.SoaPattern.parse(soaCase).sequence();
    String nextSoa = common.soaSeq(), eoa = common.eoaSeq();
    common.output = firstSoa + "QN SWIRI1G" + nextSoa + "QN LKYEGLT" + eoa;
  }

  @Given("I craft a raw message where the NAL ends with SUB instead of CRLF+{string}")
  public void rawNalEndsWithSub(String suffix) {
    String soa = common.soaSeq();
    common.output = soa + "QN SWIRI1G" + ControlChars.SUB;
  }

  @Given("I craft a raw message where one SAL has an invalid RI {string} but NAL contains {string}")
  public void rawWithInvalidSalAndValidNal(String badToken, String validRi) {
    String soa = common.soaSeq(), eoa = common.eoaSeq();
    common.output = soa + "QN " + badToken + eoa + soa + "QN " + validRi + eoa;
  }

  @Given("I ensure EOA and SOA boundaries allow parsing of NAL")
  public void ensureBoundariesForNal() {
    String boundary = common.eoaSeq() + common.soaSeq();
    if (common.output == null) {
      common.output = boundary;
    }
    if (!common.output.contains(boundary)) {
      common.output = common.output + boundary;
    }
  }

  @Then("print visualized output")
  public void printEscaped() {
  }

  @Then("output contains STX and ETX")
  public void containsStxEtx() {
    String esc = common.escaped();
    Assertions.assertTrue(esc.contains("<STX>"), esc);
    Assertions.assertTrue(esc.contains("<ETX>"), esc);
  }

  @Given("NOOP")
  public void noop() {
  }
}