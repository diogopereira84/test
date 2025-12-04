/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.common;

import io.cucumber.spring.ScenarioScope;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * Holds the "mini world" of configuration data defined in the Feature file Background.
 * This allows steps to query the test scenario's source of truth instead of the actual database.
 */
@Component
@ScenarioScope
@Data
public class ConfigurationWorld {

  /**
   * Stores the content of the 'configuration.connections' collection defined in Background.
   */
  private List<Map<String, String>> connections = new ArrayList<>();

  /**
   * Stores the content of the 'configuration.routes' collection defined in Background.
   */
  private List<Map<String, String>> routes = new ArrayList<>();

  /**
   * Stores the content of the 'configuration.destinations' collection defined in Background.
   */
  private List<Map<String, String>> destinations = new ArrayList<>();

  /**
   * The connection currently selected by the test step "Given I select the connection where...".
   * This allows subsequent steps to know which connection (and thus which addresses) are in scope.
   */
  private Map<String, String> selectedConnection;

  /**
   * Clears the data.
   */
  public void reset() {
    connections.clear();
    routes.clear();
    destinations.clear();
    selectedConnection = null;
  }
}