# File: src/test/resources/features/02-diversion_line.feature
Feature: Diversion Line (QSP)
  Scenario: Diversion line is framed with SOA and EOA
    Given SOA is "CRLF+SOH"
    And EOA is "DOT"
    And Diversion routing indicator is "BRAXGRU"
    And add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    When I compose
    Then output contains SOA "CRLF+SOH"
    And output contains EOA "DOT"
    And the visualized output contains "\\r\\n<SOH>QSP BRAXGRU\\r\\n."
