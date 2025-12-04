/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.heading;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.CommonTypeBWorld;
import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.ConfigurationWorld;
import aero.sita.messaging.mercury.e2e.utilities.format.typeb.ControlChars;
import io.cucumber.java.en.Given;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HeadingConstructionSteps {

  private final CommonTypeBWorld common;
  private final ConfigurationWorld configWorld;

  public HeadingConstructionSteps(CommonTypeBWorld world, ConfigurationWorld configWorld) {
    this.common = world;
    this.configWorld = configWorld;
  }

  /**
   * Configures a raw address element at the start of the message (Pre-SOA).
   * <p>
   * - Retrieves address information dynamically from the Background data (ConfigurationWorld).
   * - Uses standard ControlChars (CRLF, SOH, SUB, DOT).
   * - Includes logic to construct complex Pilot Signal structures.
   *
   * @param startsWithSOA       "yes" or "no"
   * @param containsEOA         "yes" or "no"
   * @param soaControlCharacter "SOH", "SUB", or "n/a"
   * @param priority            "QN", "QU", etc., or "n/a"
   * @param addressElement      "NAL" or "PilotSignal"
   */
  @Given("I construct the address element with SOA {string}, EOA {string}, Char {string}, Priority {string}, and Type {string}")
  public void constructAddressElement(String startsWithSOA, String containsEOA, String soaControlCharacter,
                                      String priority, String addressElement) {

    log.info("Constructing raw address element: SOA={}, EOA={}, Char={}, Prio={}, Type={}",
        startsWithSOA, containsEOA, soaControlCharacter, priority, addressElement);

    StringBuilder rawContent = new StringBuilder();

    // 1. Handle "Before" SOA (startsWithSOA = "yes")
    if ("yes".equalsIgnoreCase(startsWithSOA)) {
      rawContent.append(ControlChars.CRLF);
      if ("SUB".equalsIgnoreCase(soaControlCharacter)) {
        rawContent.append(ControlChars.SUB);
      } else {
        rawContent.append(ControlChars.SOH);
      }
    }

    // 2. Resolve Primary Address
    // Based on the 'selected connection' in the ConfigurationWorld
    String primaryAddress = resolveAddressForSelectedConnection();
    log.debug("Resolved primary address: {}", primaryAddress);

    // 3. Build the Address Element Content
    if (!"n/a".equalsIgnoreCase(priority)) {
      rawContent.append(priority).append(" ");
    }
    rawContent.append(primaryAddress);

    // Handle EOA (End of Address)
    // PilotSignal inherently implies EOA before the slash separator
    if ("yes".equalsIgnoreCase(containsEOA) || "PilotSignal".equalsIgnoreCase(addressElement)) {
      rawContent.append(ControlChars.CRLF).append(ControlChars.DOT);
    }

    // 4. Handle Pilot Signal Element
    if ("PilotSignal".equalsIgnoreCase(addressElement)) {
      rawContent.append("/////");

      // Resolve secondary address (e.g., for connectionA) to make the message valid
      String secondaryAddress = resolveAddressForConnectionName("connectionA");
      if (secondaryAddress == null) secondaryAddress = "JFKNYBA"; // Fallback if connectionA not found

      // Append secondary address block with framing
      rawContent.append(ControlChars.CRLF).append(ControlChars.SOH)
          .append("QN ").append(secondaryAddress);

      // FIX: Manually append the EOA (CRLF + Dot) for this secondary address line.
      // This ensures the Originator (appended by the builder later) is separated correctly.
      rawContent.append(ControlChars.CRLF).append(ControlChars.DOT);
    }

    // 5. Inject into Composer
    common.ctx.withAddressOverrideRaw(rawContent.toString());
    common.ctx.withSoa("no");
    common.ctx.withHeading(null);
  }

  /**
   * Finds the address associated with the currently selected connection in the ConfigurationWorld.
   * Logic: Connection -> Destination -> Route -> AddressMatcher
   */
  private String resolveAddressForSelectedConnection() {
    Map<String, String> currentConn = configWorld.getSelectedConnection();
    if (currentConn == null) {
      throw new IllegalStateException("No connection selected. Ensure 'Given I select the connection...' is called first.");
    }
    String connId = currentConn.get("_id");
    return findAddressByConnectionId(connId, "SINSGSQ"); // Default to SINSGSQ if resolution fails
  }

  /**
   * Finds the address associated with a specific connection name (e.g. "connectionA").
   */
  private String resolveAddressForConnectionName(String connectionName) {
    return configWorld.getConnections().stream()
        .filter(c -> connectionName.equals(c.get("_id")))
        .findFirst()
        .map(c -> findAddressByConnectionId(c.get("_id"), "JFKNYBA"))
        .orElse("JFKNYBA");
  }

  private String findAddressByConnectionId(String connectionId, String defaultValue) {
    // 1. Find Destination(s) linked to Connection
    String destId = configWorld.getDestinations().stream()
        .filter(d -> d.get("connectionIds[]") != null && d.get("connectionIds[]").contains(connectionId))
        .map(d -> d.get("_id"))
        .findFirst()
        .orElse(null);

    if (destId == null) return defaultValue;

    // 2. Find Route linked to Destination
    return configWorld.getRoutes().stream()
        .filter(r -> r.get("destinationIds[]") != null && r.get("destinationIds[]").contains(destId))
        .map(r -> r.get("criteria.addressMatcher"))
        .findFirst()
        .orElse(defaultValue);
  }
}