/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.diversion;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.CommonTypeBWorld;
import io.cucumber.java.en.Given;

public class DiversionSteps {
  private final CommonTypeBWorld common;

  public DiversionSteps(CommonTypeBWorld w) {
    this.common = w;
  }

  @Given("Diversion routing indicator is {string}")
  public void diversion(String ri7) {
    common.ctx.withDiversionRoutingIndicator(ri7);
  }
}
