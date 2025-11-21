/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps.configuration;

import aero.sita.messaging.mercury.e2e.client.configuration.ConfigurationClient;
import aero.sita.messaging.mercury.e2e.model.testharness.response.ResultResponse;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ConfigurationLiveExpandedGroupCodesBuildSteps {

  @Autowired
  private ConfigurationClient configurationClient;

  @When("sending get request for expanded group code by Id {string}")
  public void whenSendGetRequetGroupCode(String codeId) {
    Object response = configurationClient.getExpandedGroupCodeById(codeId);

    //  ResponseForModelConfiExpanden response = configurationClient.getExpandedGroupCodeById(codeId);
  }
}
