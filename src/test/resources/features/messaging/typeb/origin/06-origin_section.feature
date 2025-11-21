# File: src/test/resources/features/06-origin_section.feature
Feature: Origin Section
  Scenario: Origin block follows addressing
    Given SOA is "CRLF+SOH"
    And EOA is "DOT"
    And add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    And Double Signature is "DS"
    And Message Identity is "001"
    When I compose
    Then origin contains OI "RIOAA11"
    And origin contains DS "DS"
    And origin contains MI "001"
