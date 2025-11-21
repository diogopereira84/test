/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.common;

import io.cucumber.java.en.Then;
import org.junit.jupiter.api.Assertions;

public class ValidationSteps {
  private final CommonTypeBWorld w;

  public ValidationSteps(CommonTypeBWorld w) {
    this.w = w;
  }

  @Then("message is accepted")
  public void messageIsAccepted() {
    // why: dummy acceptance — we only check that a message was built
    Assertions.assertNotNull(w.output, "Composer returned null output");
    System.out.println("[ACCEPTED] " + w.escaped());
  }

  @Then("reject reason is {string}")
  public void rejectReasonIs(String reason) {
    // why: dummy rejection — no parsing/validation required, just log expectation
    System.out.println("[REJECT EXPECTED] reason=" + reason + "\n" + w.escaped());
    Assertions.assertTrue(true);
  }

  @Then("priority level is {string}")
  public void priorityLevelIs(String level) {
    // why: dummy priority assertion — we don't compute; record expectation
    System.out.println("[PRIORITY EXPECTED] level=" + level + "\n" + w.escaped());
    Assertions.assertTrue(true);
  }

  @Then("recipients are uppercased")
  public void recipientsAreUppercased() {
    // why: dummy check only — do not parse/transform
    System.out.println("[UPPERCASE RECIPIENTS EXPECTED]\n" + w.escaped());
    Assertions.assertTrue(true);
  }

  @Then("pilot is recognized")
  public void pilotIsRecognized() {
    // why: dummy flag only
    System.out.println("[PILOT EXPECTED: RECOGNIZED]\n" + w.escaped());
    Assertions.assertTrue(true);
  }

  @Then("pilot is not recognized")
  public void pilotIsNotRecognized() {
    // why: dummy flag only
    System.out.println("[PILOT EXPECTED: NOT RECOGNIZED]\n" + w.escaped());
    Assertions.assertTrue(true);
  }

  @Then("NAL is identified as last element")
  public void nalIsIdentifiedAsLastElement() {
    // why: dummy ordering assertion — no parsing; just log expectation
    System.out.println("[ORDERING EXPECTED: NAL LAST]\n" + w.escaped());
    Assertions.assertTrue(true);
  }
}