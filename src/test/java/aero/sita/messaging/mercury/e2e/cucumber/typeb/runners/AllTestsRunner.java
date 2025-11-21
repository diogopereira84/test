/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.runners;

import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit 5 test runner for all Cucumber tests.
 * <p>
 * This runner executes all scenarios regardless of tags.
 * Use this runner for complete test suite execution.
 * <p>
 * Usage:
 * - Run directly from IDE
 * - Execute via Gradle: ./gradlew test
 * - Execute via command line: ./gradlew allTests
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "aero.sita.messaging.mercury.e2e.cucumber")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber/all-tests.html, json:build/reports/cucumber/all-tests.json"
)
public class AllTestsRunner {
  // This class serves as a test runner for all Cucumber tests
  // No additional code is needed here
}
