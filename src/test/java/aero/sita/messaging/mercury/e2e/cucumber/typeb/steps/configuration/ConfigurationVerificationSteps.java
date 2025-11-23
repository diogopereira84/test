/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import aero.sita.messaging.mercury.e2e.utilities.helper.ConfigurationDbHelper;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class ConfigurationVerificationSteps {

  @Autowired
  private ConfigurationDbHelper configurationDbHelper;

  @Given("the configuration data is verified and healthy")
  public void verifyConfigurationIntegrity() {
    List<String> emptyCollections = configurationDbHelper.getEmptyRequiredCollections();

    if (!emptyCollections.isEmpty()) {
      log.error("Found empty required collections: {}", emptyCollections);
    }

    assertThat(emptyCollections)
        .withFailMessage("Data Integrity Failure: The following collections in 'configuration' DB are empty: %s", emptyCollections)
        .isEmpty();

    log.info("Sanity Check Passed: All required configuration collections contain data.");
  }

  @Given("the {string} collection contains the following documents:")
  public void verifyConfigurationData(String collectionName, DataTable dataTable) {
    List<Map<String, String>> expectedRows = dataTable.asMaps(String.class, String.class);

    // Delegate validation logic to helper (which returns a list of all errors found)
    List<String> validationErrors = configurationDbHelper.validateCollectionContent(collectionName, expectedRows);

    if (!validationErrors.isEmpty()) {
      log.error("Data Integrity Check FAILED for collection '{}'. Found {} discrepancies.", collectionName, validationErrors.size());
      validationErrors.forEach(log::error);
    }

    // Fail the test only after checking all rows/fields (Soft Assertion)
    assertThat(validationErrors)
        .withFailMessage("Data Integrity Mismatches in '%s':\n%s", collectionName, String.join("\n", validationErrors))
        .isEmpty();

    log.info("SUCCESS: Verified {} documents in '{}'.", expectedRows.size(), collectionName);
  }
}