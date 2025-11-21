/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.runners;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.PLUGIN_PROPERTY_NAME;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * JUnit 5 test runner for Cucumber regression tests.
 * <p>
 * This runner executes all scenarios tagged with @regression.
 * Regression tests provide comprehensive validation of all functionality
 * including edge cases and error scenarios.
 * <p>
 * Usage:
 * - Run directly from IDE
 * - Execute via Gradle: ./gradlew regressionTest
 * - Execute via command line: ./gradlew test -Dcucumber.filter.tags="@regression"
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "@regression")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "aero.sita.messaging.mercury.e2e.cucumber")
@ConfigurationParameter(
    key = PLUGIN_PROPERTY_NAME,
    value = "pretty, html:build/reports/cucumber/regression-tests.html, json:build/reports/cucumber/regression-tests.json"
)
public class RegressionTestRunner {
  // This class serves as a test runner for Cucumber regression tests
  // No additional code is needed here
}
