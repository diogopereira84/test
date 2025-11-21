# File: src/test/resources/features/04-nal.feature
Feature: Normal Address Line (NAL)
  Scenario: Multiple NAL lines are supported
    Given SOA is "CRLF+SOH"
    And EOA is "DOT"
    And add NAL line "QU SWIRI1G"
    And add NAL line "QN SWIRI1G MILXTXS"
    And Originator Indicator is "LKYSOLT"
    And text delimiters is "true"
    And add Text line "HELLO"
    When I compose
