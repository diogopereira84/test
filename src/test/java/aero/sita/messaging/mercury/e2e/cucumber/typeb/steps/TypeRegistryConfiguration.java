/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.steps;

import io.cucumber.java.ParameterType;
import lombok.extern.slf4j.Slf4j;

/**
 * Cucumber Type Registry.
 * Defines custom parameter types to handle specific data formats globally.
 */
@Slf4j
public class TypeRegistryConfiguration {

  /**
   * Defines a {visualString} parameter type.
   * This allows passing values in Examples tables with double quotes to visualize spaces (e.g., "   ").
   * The transformer automatically strips the surrounding quotes before passing the value to the step.
   * <p>
   * Usage in Step: @Given("prefix is {visualString}")
   * Example in Table: | "   " | -> Java receives: "   " (3 spaces)
   * Example in Table: | ZCZC  | -> Java receives: "ZCZC"
   */
  @ParameterType(name = "visualString", value = ".*")
  public String visualString(String value) {
    if (value != null && value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
      return value.substring(1, value.length() - 1);
    }
    return value;
  }
}