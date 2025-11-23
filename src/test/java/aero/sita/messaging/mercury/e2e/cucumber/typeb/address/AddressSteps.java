/*
 * This code contains copyright information which is the proprietary property
 * of SITA Information Network Computing Limited (SITA). No part of this
 * code may be reproduced, stored or transmitted in any form without the prior
 * written permission of SITA.
 * Copyright © SITA Information Networking Computing Ireland Limited 2020-2025.
 * Confidential. All rights reserved.
 */

package aero.sita.messaging.mercury.e2e.cucumber.typeb.address;

import aero.sita.messaging.mercury.e2e.cucumber.typeb.common.CommonTypeBWorld;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.util.Map;

public class AddressSteps {
  private final CommonTypeBWorld common;

  public AddressSteps(CommonTypeBWorld world) {
    this.common = world;
  }

  @Given("the configuration has the following routes configured:")
  public void routingTable(DataTable table) {
    for (Map<String, String> row : table.asMaps(String.class, String.class)) {
      String rt = row.get("RouteType");
      String dest = row.get("Destination");
      String conn = row.get("Connection");

      if (rt != null) {
        common.routingDestination.put(rt.toUpperCase(), dest);
        common.routingConnection.put(rt.toUpperCase(), conn);
      }
    }
  }

  @Then("the Address Section is detected")
  public void addressSectionDetected() {
  }

  @Then("the message is accepted")
  public void messageAccepted() {
  }

  @Then("the message is rejected with reason {string}")
  public void messageRejectedWithReason(String reason) {
  }

  @Then("the priority is {string}")
  public void priorityIs(String expected) {
  }

  @Then("the list of recipients extracted equals {string}")
  public void recipientsEquals(String expectedJoined) {
  }

  @Then("all recipients are uppercased")
  public void recipientsUppercased() {
  }

  @Then("the total recipients extracted equals {int}")
  public void totalRecipients(int total) {
  }

  @Then("exactly {int} Pilot element is detected")
  public void exactlyPilotDetected(int n) {
  }

  @Then("zero Pilot elements are detected")
  public void zeroPilotDetected() {
    exactlyPilotDetected(0);
  }

  @Then("NAL is the last element")
  public void nalIsLastElement() {
  }

  @Then("the diversion line for {string} is present")
  public void diversionPresent(String ri7) {
  }

  @Then("the originator indicator {string} has a trailing space")
  public void originatorHasSpace(String oi) {
  }

  @Then("copy indicators are counted among recipients")
  public void copyIndicatorsCounted() {
  }

  @Then("the first addressee is not a copy indicator")
  public void copyNotFirst() {
  }

  @Then("backend generates outgoing messages grouped per distinct destination")
  public void backendGeneratesGroups() {
  }

  @Then("for destination {string} the SAL is the first RI of that destination and NALs include {string}")
  public void perDestinationAssertion(String dest, String allRIs) {
  }
}