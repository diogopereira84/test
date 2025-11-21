# File: src/test/resources/features/07-originator_indicator.feature
Feature: Originator Indicator
  Scenario: OI prints with trailing space
    Given add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    When I compose
    Then the visualized output contains "RIOAA11 "
