# features/type_b_heading_handling.feature

Feature: Type-B Heading Section handling in Mercury
  Ensures correct acceptance, parsing, and rejection of Type-B messages w.r.t. Heading Section and SOA.

  Background:
    Given Mercury assumes standard CR LF alignment in inbound messages
    And the inbound message under test is provided

  # ─────────────────────────────────────────────────────────────────────────────
  # A) Routing when heading support is DISABLED
  # ─────────────────────────────────────────────────────────────────────────────
  Scenario Outline: Routing when heading support is DISABLED
    Given connection setting "acceptHeading" is "false"
    And the message "<hasSOA>" contains SOA
    And the pre-SOA content is "<preSOAType>"
    And I finalize the composed message
    When I send the composed message via the Test Harness
    Then the overall disposition is "<expectedDisposition>"
    And the heading detection result is "<expectedHeadingDetected>"
    And if rejected the reason is "<expectedRejection>"

    Examples:
      | hasSOA | preSOAType              | expectedDisposition | expectedHeadingDetected | expectedRejection                 |
      | yes    | AddressEndIndicator     | accepted            | none                    |                                   |
      | yes    | PilotSignal             | accepted            | none                    |                                   |
      | yes    | PlainHeadingText        | rejected            | none                    | HEADING SECTION NOT ALLOWED       |
      | no     | (n/a)                   | accepted            | none                    |                                   |

  @config-enabled @routing @positive @edge
  Scenario Outline: Routing when heading support is ENABLED (heading optional)
    Given connection setting "acceptHeading" is true
    And the message "<shape>" is shaped relative to SOA
    And the pre-SOA content is "<preSOAType>"
    When the message is evaluated
    Then the overall disposition is "<expectedDisposition>"
    And the heading detection result is "<expectedHeadingDetected>"

    Examples:
      | shape             | preSOAType          | expectedDisposition | expectedHeadingDetected |
      | noSOA             | (n/a)               | accepted            | none                    |
      | startsWithSOAFull | (ignored)           | accepted            | none                    |
      | startsWithSOAOnly | (ignored)           | accepted            | none                    |
      | hasSOA            | AddressEndIndicator | accepted            | none                    |
      | hasSOA            | PilotSignal         | accepted            | none                    |
      | hasSOA            | PlainHeadingText    | accepted            | heading                 |

  @parsing @standard @positive
  Scenario Outline: Valid Standard heading (serial ≤5 digits, optional supplemental)
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA is a Standard Heading with serial "<serialDigits>" and supplemental "<supplemental>"
    And the heading terminator is "<terminator>"
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "standard"
    And the parsed serial is "<expectedSerialValue>"
    And the forwarded heading equals the received heading (including spacing markers)

    Examples:
      | serialDigits | supplemental           | terminator   | expectedSerialValue |
      | 1            |                        | SOA          | 1                   |
      | 3            | DTG 2025-01-01 10:00Z  | SOA          | 3                   |
      | 5            | ABC                    | 5SpacesThenSOA | 5                   |

  @parsing @custom @edge @positive
  Scenario Outline: Overlength or non-numeric "serial" becomes Custom heading
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA is a candidate Standard Heading with serial token "<serialToken>" and supplemental "<supplemental>"
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "custom"
    And the forwarded heading equals the received heading (unchanged)

    Examples:
      | serialToken | supplemental       |
      | 123456      | ANY TEXT           |
      | XX12        | FREE TEXT          |
      | 0000000     | EXTRA              |

  @parsing @suid @positive
  Scenario Outline: SUID-only heading is parsed and preserved
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA is a SUID heading with indicator "<suidInd>", messageId "<msgId>", transactionId "<txnId>"
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "suid"
    And the SUID fields are captured without decoding
    And the forwarded heading equals the received heading (unchanged)

    Examples:
      | suidInd | msgId                               | txnId                                  |
      | SUID    | 6F1E2D3C-1111-2222-3333-444455556666| A1B2C3D4-E5F6-47A8-99AA-BB00CC11DD22   |

  @parsing @suid @standard @custom @positive
  Scenario Outline: SUID + Original Heading on same line
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA is "SUID + Original" where original type is "<originalType>"
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "<expectedHeadingType>"
    And SUID parts and Original parts are both captured
    And the forwarded heading equals the received heading (unchanged)

    Examples:
      | originalType | expectedHeadingType |
      | standard     | suid+standard       |
      | custom       | suid+custom         |

  @parsing @preservation @edge @positive
  Scenario Outline: Leading spaces and ZCZC are ignored for parsing but preserved for forwarding
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA begins with "<prefix>" followed by a valid Standard Heading
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "standard"
    And the forwarded heading retains the original "<prefix>"

    Examples:
      | prefix   |
      | "   "    |
      | "ZCZC "  |
      | "  ZCZC "|

  @validation @negative
  Scenario Outline: Multi-line heading is rejected
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA contains a heading spanning "<lineCount>" lines
    When the message is evaluated
    Then the overall disposition is "rejected"
    And the reason is "INVALID HEADING SECTION"

    Examples:
      | lineCount |
      | 2         |
      | 3         |

  @terminator @edge @positive
  Scenario Outline: Heading terminates with spacing signal (5 spaces) and/or SOA
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA is a Standard Heading with valid serial and supplemental "<supplemental>"
    And the heading terminator is "<terminator>"
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "standard"

    Examples:
      | supplemental | terminator     |
      | HELLO        | 5SpacesThenSOA |
      | WORLD        | SOA            |

  @length @edge @positive
  Scenario: No maximum size enforced for heading line
    Given connection setting "acceptHeading" is true
    And the message "hasSOA" contains SOA
    And pre-SOA is a Custom Heading with extremely long content of length "5000"
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "custom"
    And the forwarded heading equals the received heading (unchanged)
