# File: src/test/resources/features/08-double_signature.feature
Feature: Double Signature
  Scenario: Double signature concatenates after OI space
    Given add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    And Double Signature is "DS"
    When I compose
    Then origin contains DS "DS"
