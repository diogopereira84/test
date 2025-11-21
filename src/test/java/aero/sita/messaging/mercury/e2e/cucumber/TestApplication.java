/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Spring Boot test application for E2E tests.
 * This application class serves as the entry point for the Spring context
 * during test execution. It enables component scanning for all test-related
 * beans and configurations.
 */
@SpringBootApplication
@ComponentScan(basePackages = "aero.sita.messaging.mercury.e2e")
@EnableMongoRepositories(basePackages = "aero.sita.messaging.mercury.e2e.repository")
public class TestApplication {
  public static void main(String[] args) {
    SpringApplication.run(TestApplication.class, args);
  }
}
