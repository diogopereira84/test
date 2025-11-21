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
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class HeadingSteps {
  private final CommonTypeBWorld common;

  public HeadingSteps(CommonTypeBWorld world) {
    this.common = world;
  }

  @Given("Heading line is {string}")
  public void heading(String h) {
    common.ctx.withHeading(h);
  }

  @Then("heading line appears at start")
  public void headingAtStart() {
  }

  @Given("connection setting {string} is {string}")
  public void connectionSettingIsFalse(String paramSetting, String paramValue) {
  }
}