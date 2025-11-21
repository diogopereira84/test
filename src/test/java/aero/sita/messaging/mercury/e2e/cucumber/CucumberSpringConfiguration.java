/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Cucumber Spring configuration class.
 * This class integrates Cucumber with Spring Boot, enabling dependency injection
 * in step definition classes. The @CucumberContextConfiguration annotation marks
 * this as the configuration entry point for Cucumber-Spring integration.
 * The Spring context is shared across all Cucumber scenarios within a test run,
 * improving performance and enabling proper dependency management.
 */
@CucumberContextConfiguration
@SpringBootTest(classes = TestApplication.class)
@ActiveProfiles("${spring.profiles.active:local}")
public class CucumberSpringConfiguration {
  // This class serves as the bridge between Cucumber and Spring Boot
  // No additional code is needed here
}
