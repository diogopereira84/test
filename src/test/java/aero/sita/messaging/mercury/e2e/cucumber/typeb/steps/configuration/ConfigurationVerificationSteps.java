/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps.configuration;

import aero.sita.messaging.mercury.e2e.utilities.helper.ConfigurationDbHelper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
public class ConfigurationVerificationSteps {

  @Autowired
  private ConfigurationDbHelper configurationDbHelper;

  private static final Set<String> verifiedCollections = ConcurrentHashMap.newKeySet();

  @Given("the configuration data is verified and healthy")
  public void verifyConfigurationIntegrity() {
    String checkKey = "GENERAL_INTEGRITY";
    if (verifiedCollections.contains(checkKey)) return;

    List<String> emptyCollections = configurationDbHelper.getEmptyRequiredCollections();
    assertThat(emptyCollections)
        .withFailMessage("DB Integrity Error: Empty collections in configuration: %s", emptyCollections)
        .isEmpty();

    verifiedCollections.add(checkKey);
  }

  @Given("the {string} collection contains the following documents:")
  public void verifyConfigurationData(String collectionName, DataTable dataTable) {
    if (verifiedCollections.contains(collectionName)) return;

    List<Map<String, String>> expectedRows = dataTable.asMaps(String.class, String.class);
    List<String> errors = configurationDbHelper.validateCollectionContent(collectionName, expectedRows);

    assertThat(errors)
        .withFailMessage("Data Integrity Mismatches in '%s':\n%s", collectionName, String.join("\n", errors))
        .isEmpty();

    verifiedCollections.add(collectionName);
  }
}