/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.origin;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.CommonTypeBWorld;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

public class OriginSteps {
  private final CommonTypeBWorld common;

  public OriginSteps(CommonTypeBWorld w) {
    this.common = w;
  }

  @Given("Originator Indicator is {string}")
  public void oi(String oi) {
    common.ctx.withOriginatorIndicator(oi);
  }

  @Given("Double Signature is {string}")
  public void ds(String ds) {
    common.ctx.withDoubleSignature(ds);
  }

  @Given("Message Identity is {string}")
  public void mi(String mi) {
    common.ctx.withMessageIdentity(mi);
  }

  @Then("origin contains OI {string}")
  public void originContainsOi(String oi) {
    Assertions.assertTrue(common.escaped().contains(oi + " "), common.escaped());
  }

  @Then("origin contains DS {string}")
  public void originContainsDs(String ds) {
    Assertions.assertTrue(common.escaped().contains(ds), common.escaped());
  }

  @Then("origin contains MI {string}")
  public void originContainsMi(String mi) {
    Assertions.assertTrue(common.escaped().contains(mi), common.escaped());
  }
}
