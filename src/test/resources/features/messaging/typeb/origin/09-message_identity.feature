# File: src/test/resources/features/09-message_identity.feature
Feature: Message Identity
  Scenario: Message identity follows double signature when present
    Given add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    And Double Signature is "DS"
    And Message Identity is "999"
    When I compose
    Then origin contains MI "999"
