# File: src/test/resources/features/11-common.feature
Feature: Common toggles and framing
  Scenario: EOA DOT with SOA SOH and text framing
    Given SOA is "CRLF+SOH"
    And EOA is "DOT"
    And text delimiters is "true"
    And add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    And add Text line "PING"
    When I compose
    Then output contains SOA "CRLF+SOH"
    And output contains EOA "DOT"
    And output contains STX and ETX

  Scenario: EOA SUB legacy framing
    Given SOA is "CRLF+SOH"
    And EOA is "SUB"
    And add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    When I compose
    Then output contains EOA "SUB"
