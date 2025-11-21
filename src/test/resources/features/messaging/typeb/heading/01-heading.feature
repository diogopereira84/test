# File: src/test/resources/features/01-heading.feature
Feature: Heading Section
  Scenario: Heading line appears at the start of the message
    Given SOA is "CRLF+SOH"
    And EOA is "DOT"
    And Heading line is "HDQ/TEST"
    When I compose
    Then heading line appears at start
    And the visualized output contains "HDQ/TEST\\r\\n"
