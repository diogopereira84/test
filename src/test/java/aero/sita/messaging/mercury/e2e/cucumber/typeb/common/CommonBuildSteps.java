/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.common;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.steps.CommonSteps;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.ControlChars;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.TypeBMessageBuilder;
import aero.sita.messaging.mercury.e2e.utilities.generator.MessageIdentityGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CommonBuildSteps {
  private static final Logger LOG = LoggerFactory.getLogger(CommonBuildSteps.class);
  private final CommonTypeBWorld common;

  @Autowired
  private CommonSteps commonSteps; // Injected to store the generated Identity

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

  @Given("I set originator {string} and identity {string}")
  public void setOriginator(String oi, String id) {
    // FIXED: Generate Unique ID instead of using the static BDD value.
    // This ensures test isolation and allows MongoAssertionSteps to find the specific record.
    String uniqueId = MessageIdentityGenerator.generate();

    LOG.info("Setting Originator: {}. Overriding BDD Identity '{}' with Generated Unique ID: '{}'", oi, id, uniqueId);

    // 1. Apply to Message Builder
    common.ctx.withOriginatorIndicator(oi).withMessageIdentity(uniqueId);

    // 2. Store in CommonSteps context so MongoAssertionSteps can use it for queries
    commonSteps.setMessageIdentity(uniqueId);
  }

  @Given("I add text line {string}")
  public void addTextLine(String t) {
    common.ctx.withTextLine(t);
  }

  @Given("I add address line {string}")
  public void addAddressLine(String line) {
    common.ctx.withAddressLine(line);
  }

  @Given("the message is composed")
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

  @Given("I craft a raw message with heading {string} and first address element {string} without SOA and with EOA {string}")
  public void rawHeadingFirstElemNoSoa(String heading, String firstElem, String eoaToken) {
    String eoa = TypeBMessageBuilder.AddressEoaToken.parse(eoaToken).sequence();
    common.output =
        heading + ControlChars.CRLF + firstElem + eoa + "LKYSOLT 3456700" + ControlChars.CRLF + ControlChars.STX + "UAT Validation" + ControlChars.CRLF +
            ControlChars.ETX;
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