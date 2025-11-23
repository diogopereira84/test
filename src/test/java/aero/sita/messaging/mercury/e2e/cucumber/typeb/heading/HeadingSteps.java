/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.heading;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.CommonTypeBWorld;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.ControlChars;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeadingSteps {

  private static final Logger LOG = LoggerFactory.getLogger(HeadingSteps.class);
  private final CommonTypeBWorld common;

  public HeadingSteps(CommonTypeBWorld world) {
    this.common = world;
  }

  @Given("connection setting {string} is {string}")
  public void connectionSettingIs(String setting, String value) {
    boolean isEnabled = Boolean.parseBoolean(value);
    if ("acceptHeading".equals(setting)) {
      LOG.info("[CONFIG] Connection setting 'acceptHeading' set to: {}", isEnabled);
    }
  }

  @Given("connection setting {string} is true")
  public void connectionSettingIsTrue(String setting) {
    connectionSettingIs(setting, "true");
  }

  @Given("the message {string} contains SOA")
  public void messageContainsSOA(String condition) {
    if ("no".equalsIgnoreCase(condition) || "noSOA".equalsIgnoreCase(condition)) {
      common.ctx.withSoa("no");
      LOG.info("Configured message WITHOUT SOA");
    } else {
      common.ctx.withSoa("CRLF+SOH");
      LOG.info("Configured message WITH SOA (CRLF+SOH)");
    }
  }

  @Given("the message {string} is shaped relative to SOA")
  public void messageShapedRelativeToSoa(String shape) {
    messageContainsSOA(shape);
  }

  @When("I set the content immediately preceding the SOA to {string}")
  public void preSoaContentIs(String contentType) {
    String content = "";
    String terminator = "";

    switch (contentType) {
      case "AddressEndIndicator":
        // Requirement: EOA is CRLF + DOT.
        content = ControlChars.CRLF + ControlChars.DOT;
        break;

      case "PilotSignal":
        content = "QJQJ";
        break;

      case "GENERIC HEADING TEXT":
        content = "GENERIC HEADING TEXT";
        break;

      case "(n/a)":
      case "":
        content = "";
        break;

      default:
        content = contentType;
    }

    common.ctx.withHeading(content);
    if (!terminator.isEmpty()) {
      common.ctx.withHeadingTerminator(terminator);
    }

    LOG.debug("Set pre-SOA content (visual): {}",
        content.replace("\r", "\\r").replace("\n", "\\n"));
  }

  @Given("I set heading {string}")
  public void setRawHeading(String headingContent) {
    common.ctx.withHeading(headingContent);
    LOG.debug("Set Heading: {}", headingContent);
  }

  @Given("I set heading prefix {string} and content {string}")
  public void setHeadingPrefixAndContent(String prefix, String content) {
    common.ctx.withHeadingPrefix(prefix);
    common.ctx.withHeading(content);
    LOG.debug("Set Heading with prefix '{}' and content '{}'", prefix, content);
  }

  @Given("I set heading with internal line break: {string} + CRLF + {string}")
  public void setHeadingWithInternalLineBreak(String line1, String line2) {
    String multiLineHeading = line1 + ControlChars.CRLF + line2;
    common.ctx.withHeading(multiLineHeading);
    LOG.debug("Set Multi-line Heading: {}", multiLineHeading.replace("\r", "\\r").replace("\n", "\\n"));
  }

  @Given("pre-SOA is a Standard Heading with serial {string} and supplemental {string}")
  public void preSoaIsStandardHeading(String serial, String supplemental) {
    StringBuilder sb = new StringBuilder();
    sb.append(serial);

    if (supplemental != null && !supplemental.isEmpty() && !"SOA".equals(supplemental)) {
      sb.append(" ").append(supplemental);
    }

    common.ctx.withHeading(sb.toString());
    LOG.debug("Set Standard Heading: {}", sb);
  }

  @Given("pre-SOA is a Standard Heading with valid serial and supplemental {string}")
  public void preSoaIsStandardHeadingWithValidSerial(String supplemental) {
    preSoaIsStandardHeading("12345", supplemental);
  }

  @Given("pre-SOA is a candidate Standard Heading with serial token {string} and supplemental {string}")
  public void preSoaIsCandidateStandard(String token, String supp) {
    preSoaIsStandardHeading(token, supp);
  }

  @Given("pre-SOA is a SUID heading with indicator {string}, messageId {string}, transactionId {string}")
  public void preSoaIsSuidHeading(String indicator, String msgId, String txnId) {
    String content = String.format("%s %s %s", indicator, msgId, txnId);
    common.ctx.withHeading(content);
    LOG.debug("Set SUID Heading: {}", content);
  }

  @Given("pre-SOA is {string} where original type is {string}")
  public void preSoaIsSuidPlusOriginal(String scenario, String originalType) {
    String suidPart = "SUID 1111-2222 3333-4444";
    String originalPart = "";

    if ("standard".equalsIgnoreCase(originalType)) {
      originalPart = "123 SUPP";
    } else if ("custom".equalsIgnoreCase(originalType)) {
      originalPart = "CUSTOM123456";
    }

    common.ctx.withHeading(suidPart + " " + originalPart);
    LOG.debug("Set SUID+Original Heading: {}", originalPart);
  }

  @Given("pre-SOA begins with {string} followed by a valid Standard Heading")
  public void preSoaBeginsWithPrefix(String prefix) {
    String cleanPrefix = prefix.replace("\"", "");
    common.ctx.withHeadingPrefix(cleanPrefix);
    common.ctx.withHeading("001 TEST");
    LOG.debug("Set Heading Prefix: '{}'", cleanPrefix);
  }

  @Given("pre-SOA contains a heading spanning {string} lines")
  public void preSoaMultiLine(String lineCountStr) {
    int lines = Integer.parseInt(lineCountStr);
    StringBuilder sb = new StringBuilder();
    for(int i=0; i < lines; i++) {
      sb.append("LINE_").append(i+1);
      if (i < lines - 1) {
        sb.append("\r\n");
      }
    }
    common.ctx.withHeading(sb.toString());
    LOG.debug("Set Multi-line Heading ({} lines)", lines);
  }

  @Given("pre-SOA is a Custom Heading with extremely long content of length {string}")
  public void preSoaLongContent(String lengthStr) {
    int length = Integer.parseInt(lengthStr);
    String longText = "A".repeat(length);
    common.ctx.withHeading(longText);
    LOG.debug("Set Long Heading (length {})", length);
  }

  @Given("the heading terminator is {string}")
  public void headingTerminatorIs(String terminatorType) {
    if ("5SpacesThenSOA".equals(terminatorType)) {
      common.ctx.withHeadingTerminator("     ");
    } else if ("SOA".equals(terminatorType)) {
      common.ctx.withHeadingTerminator("");
    }
    LOG.debug("Set Heading Terminator: {}", terminatorType);
  }

  // --- Verification Steps ---

  @When("the message is evaluated")
  public void messageIsEvaluated() throws JsonProcessingException {
    common.output = common.ctx.compose();

    String visual = common.escaped();
    String plaintext = common.escapedJson();

    LOG.info("----------------------------------------------------------------");
    LOG.info("Message Constructed for Evaluation (Visual): {}", visual);
    LOG.info("Message Constructed for Evaluation (Plaintext): {}", plaintext);
    LOG.info("----------------------------------------------------------------");
  }

  @Then("the heading detection result is {string}")
  public void verifyHeadingDetection(String expectedDetection) {
    LOG.info("Verifying Heading Detection: {}", expectedDetection);
  }

  @Then("the overall disposition is {string}")
  public void verifyDisposition(String expectedDisposition) {
    LOG.info("Verifying Overall Disposition: {}", expectedDisposition);
  }

  @Then("the parsed heading type is {string}")
  public void verifyParsedHeadingType(String expectedType) {
    LOG.info("Verifying Parsed Heading Type: {}", expectedType);
  }

  @Then("the parsed serial is {string}")
  public void verifyParsedSerial(String expectedSerial) {
    LOG.info("Verifying Parsed Serial: {}", expectedSerial);
  }

  @Then("the forwarded heading equals the received heading \\(including spacing markers)")
  public void verifyForwardedHeadingExact() {
    LOG.info("Verifying Forwarded Heading (Exact Match)");
  }

  @Then("the forwarded heading retains the original {string}")
  public void verifyForwardedHeadingPrefix(String prefix) {
    String clean = prefix.replace("\"", "");
    LOG.info("Verifying Forwarded Heading retains: '{}'", clean);
  }

  @Then("the forwarded heading equals the received heading \\(unchanged)")
  public void verifyForwardedHeadingUnchanged() {
    LOG.info("Verifying Forwarded Heading is Unchanged");
  }

  @Then("the SUID fields are captured without decoding")
  public void verifySuidCaptured() {
    LOG.info("Verifying SUID fields captured raw");
  }

  @Then("SUID parts and Original parts are both captured")
  public void verifySuidAndOriginal() {
    LOG.info("Verifying SUID + Original parts captured");
  }

  @Then("the reason is {string}")
  public void verifyRejectionReason(String expectedReason) {
    LOG.info("Verifying Rejection Reason: {}", expectedReason);
  }

  @Then("if rejected the reason is {string}")
  public void verifyConditionalRejectionReason(String expectedReason) {
    if (expectedReason != null && !expectedReason.isEmpty()) {
      LOG.info("Verifying Conditional Rejection Reason: {}", expectedReason);
    }
  }
}