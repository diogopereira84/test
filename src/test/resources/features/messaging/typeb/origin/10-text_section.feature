# File: src/test/resources/features/10-text_section.feature
Feature: Text Section
  Scenario: Text is framed with CRLF+STX and CRLF+ETX
    Given add NAL line "QU RIOAA11"
    And Originator Indicator is "RIOAA11"
    And text delimiters is "true"
    And add Text line "HELLO"
    And add Text line "WORLD"
    When I compose
    Then output contains STX and ETX
    And the visualized output contains "<STX>HELLO\\r\\nWORLD\\r\\n<ETX>"
