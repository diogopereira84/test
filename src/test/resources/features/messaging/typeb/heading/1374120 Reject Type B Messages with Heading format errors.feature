# features/messaging/typeb/heading/1374120_reject_type_b_messages_with_heading_format_errors.feature

Feature: Type-B Heading Section handling in Mercury
  Requirement: 1374120
  Ensures correct acceptance, parsing, and rejection of Type-B messages regarding the
  Heading Section (content preceding the SOA).

  Background:
    # We start with a clean slate for each scenario to allow specific SOA config
    Given a clean TypeBComposer
    # Valid body content to ensure E2E success if heading is valid
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Standard UAT Body Text"

  # ==============================================================================
  # GROUP 1: Heading Support DISABLED (acceptHeading = false)
  # Rule: Mercury assumes message starts with Address.
  # 1. If NO SOA: Accept (Assume Address).
  # 2. If SOA exists: Check content before it. Only EOA or Pilot allowed.
  # ==============================================================================

  @config-disabled @edge @positive
  Scenario Outline: [DISABLED] Accept message when content preceding SOA is valid address element or empty
    Given connection setting "acceptHeading" is "false"
    # Configuration of the message structure
    And the message "<hasSOA>" contains SOA
    And I set the content immediately preceding the SOA to "<preSoaContent>"
    # Body is mandatory for valid Type B
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the heading detection result is "none"

    Examples:
      | hasSOA | preSoaContent         | description                                      |
      | no     | (n/a)                 | No SOA -> Assumes Address (Starts with QN...)    |
      | yes    | AddressEndIndicator   | SOA present, but preceded by \r\n. (Valid flow)  |
      | yes    | PilotSignal           | SOA present, but preceded by Pilot (Valid flow)  |
      | yes    |                       | SOA present, but preceded by nothing (Empty)     |

  @config-disabled @negative
  Scenario: [DISABLED] Reject message when actual text content precedes SOA
    Given connection setting "acceptHeading" is "false"
    And the message "yes" contains SOA
    # Any text that is NOT EOA or Pilot is considered a Heading -> Rejected
    And I set the content immediately preceding the SOA to "GENERIC HEADING TEXT"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "rejected"
    And the reason is "HEADING SECTION NOT ALLOWED"

  # ==============================================================================
  # GROUP 2: Heading Support ENABLED - Valid Formats (Positive)
  # Rule: Accept Standard, SUID, Custom. Preserve Prefixes.
  # ==============================================================================

  @config-enabled @preservation @positive
  Scenario Outline: [ENABLED] Ignore prefixes for parsing but preserve for forwarding
    Given connection setting "acceptHeading" is "true"
    And the message "yes" contains SOA
    And I set heading prefix <prefix> and content "001 VALID"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "standard"
    And the forwarded heading retains the original <prefix>

    Examples:
      | prefix    |
      | "   "     |
      | "ZCZC "   |
      | "  ZCZC"  |

  @config-enabled @parsing @standard @positive
  Scenario Outline: [ENABLED] Accept valid Standard Heading formats
    Given connection setting "acceptHeading" is "true"
    And the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "standard"
    And the parsed serial is "<expectedSerial>"

    Examples:
      | headingContent        | expectedSerial |
      | 1                     | 1              |
      | 12345                 | 12345          |
      | 001 SUPP INFO 2025    | 001            |

  @config-enabled @parsing @suid @positive
  Scenario: [ENABLED] Accept valid SUID Heading format
    Given connection setting "acceptHeading" is "true"
    And the message "yes" contains SOA
    And I set heading "SUID 6F1E2D3C-1111-2222 3333-444455556666"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "suid"
    And the SUID fields are captured without decoding

  @config-enabled @parsing @custom @edge @positive
  Scenario Outline: [ENABLED] Accept single-line Custom Heading formats
    Given connection setting "acceptHeading" is "true"
    And the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "custom"

    Examples:
      | headingContent                                   |
      | 123456                                           |
      | A123                                             |
      | VERY LONG CUSTOM HEADING TEXT ON A SINGLE LINE   |

  # ==============================================================================
  # GROUP 3: Heading Support ENABLED - Invalid Formats (Negative)
  # Rule: Heading must be single line.
  # ==============================================================================

  @config-enabled @validation @negative
  Scenario Outline: [ENABLED] Reject Multi-line headings
    Given connection setting "acceptHeading" is "true"
    And the message "yes" contains SOA
    # Injects explicit CRLF in the middle of the heading content
    And I set heading with internal line break: "<line1>" + CRLF + "<line2>"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "rejected"
    And the reason is "INVALID HEADING SECTION"

    Examples:
      | line1  | line2  |
      | LINE 1 | LINE 2 |

  @terminator @edge @positive
  Scenario Outline: Heading terminates with spacing signal (5 spaces) and/or SOA
    Given connection setting "acceptHeading" is "true"
    And the message "yes" contains SOA
    And pre-SOA is a Standard Heading with valid serial and supplemental "<supplemental>"
    And the heading terminator is "<terminator>"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "standard"

    Examples:
      | supplemental | terminator     |
      | HELLO        | 5SpacesThenSOA |
      | WORLD        | SOA            |