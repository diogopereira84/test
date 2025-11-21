# File: src/test/resources/features/03-sal.feature
Feature: Short Address Line (SAL)
  Scenario: SAL line is emitted with correct framing
    Given SOA is "CRLF+SOH"
    And EOA is "DOT"
    And add SAL line "QU ABCDE12 FGHIJ34"
    And Originator Indicator is "RIOAA11"
    When I compose
    Then the visualized output contains "\\r\\n<SOH>QU ABCDE12 FGHIJ34\\r\\n."
