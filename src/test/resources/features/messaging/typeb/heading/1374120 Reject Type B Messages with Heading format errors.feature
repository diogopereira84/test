# features/messaging/typeb/heading/1374120_reject_type_b_messages_with_heading_format_errors.feature

Feature: Type-B Heading Section handling in Mercury
  Requirement: 1374120
  Ensures correct acceptance, parsing, and rejection of Type-B messages regarding the
  Heading Section (content preceding the SOA).

  Background:
    # --- DATA INTEGRITY CHECK ---
    Given the "configuration.hosts" collection contains the following documents:
      | _id     | type  | name    | enabled |
      | server1 | IBMMQ | server1 | true    |

    And the "configuration.connections" collection contains the following documents:
      | _id         | type  | name         | serviceAddress | format | hostId  | inQueue        | outQueue       | messageConfiguration.acceptMessagesWithAHeadingSection | enabled |
      | connectionA | IBMMQ | connectionA  | LETTTLK        | TYPE_B | server1 | LETTTLK.OUT    | LETTTLK.IN     | true                                                   | true    |
      | connectionC | IBMMQ | connectionC  | LETVVLK        | TYPE_B | server1 | LETVVLK.OUT    | LETVVLK.IN     | false                                                  | true    |

    And the "configuration.destinations" collection contains the following documents:
      | _id          | connectionIds[] | enabled |
      | destinationA | connectionA     | true    |
      | destinationC | connectionC     | true    |

    And the "configuration.routes" collection contains the following documents:
      | _id | type   | criteria.addressMatcher | criteria.type | destinationIds[] | enabled |
      | 175 | DIRECT | JFKNYBA                 | DISCRETE      | destinationA     | true    |
      | 177 | DIRECT | SINSGSQ                 | DISCRETE      | destinationC     | true    |

    # --- TEST SETUP ---
    Given a clean TypeBComposer
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Standard UAT Body Text"

  # ==============================================================================
  # GROUP 1: Heading Support DISABLED (Target: SINSGSQ -> Disabled)
  # ==============================================================================

  @config-disabled @edge @positive
  Scenario Outline: [DISABLED] Accept message when no actual heading content precedes SOA
    # SINSGSQ routes to ConnectionC (Heading Disabled)
    Given the message "<hasSOA>" contains SOA
    And I set the content immediately preceding the SOA to "<preSoaContent>"
    And I add address line "QN SINSGSQ"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the overall disposition is "accepted"
    And the heading detection result is "none"

    Examples:
      | hasSOA | preSoaContent         |
      | no     |                       |
      | yes    | AddressEndIndicator   |
      | yes    | PilotSignal           |

  @config-disabled @negative
  Scenario: [DISABLED] Reject message when actual text content precedes SOA
    Given the message "yes" contains SOA
    And I set the content immediately preceding the SOA to "GENERIC HEADING TEXT"
    And I add address line "QN SINSGSQ"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the overall disposition is "rejected"
    And the reason is "HEADING SECTION NOT ALLOWED"

  # ==============================================================================
  # GROUP 2: Heading Support ENABLED (Target: JFKNYBA -> Enabled)
  # ==============================================================================

  @config-enabled @preservation @positive
  Scenario Outline: [ENABLED] Ignore prefixes for parsing but preserve for forwarding
    # JFKNYBA routes to ConnectionA (Heading Enabled)
    Given the message "yes" contains SOA
    And I set heading prefix <prefix> and content "001 VALID"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
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
    Given the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
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
    Given the message "yes" contains SOA
    And I set heading "SUID 6F1E2D3C-1111-2222 3333-444455556666"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the overall disposition is "accepted"
    And the parsed heading type is "suid"
    And the SUID fields are captured without decoding

  @config-enabled @parsing @custom @edge @positive
  Scenario Outline: [ENABLED] Accept single-line Custom Heading formats
    Given the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the overall disposition is "accepted"
    And the parsed heading type is "custom"

    Examples:
      | headingContent                                   |
      | 123456                                           |
      | A123                                             |
      | VERY LONG CUSTOM HEADING TEXT ON A SINGLE LINE   |

  # ==============================================================================
  # GROUP 3: Heading Support ENABLED - Invalid Formats (Negative)
  # ==============================================================================

  @config-enabled @validation @negative
  Scenario Outline: [ENABLED] Reject Multi-line headings
    Given the message "yes" contains SOA
    And I set heading with internal line break: "<line1>" + CRLF + "<line2>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the overall disposition is "rejected"
    And the reason is "INVALID HEADING SECTION"

    Examples:
      | line1  | line2  |
      | LINE 1 | LINE 2 |

  @terminator @edge @positive
  Scenario Outline: Heading terminates with spacing signal (5 spaces) and/or SOA
    Given the message "yes" contains SOA
    And pre-SOA is a Standard Heading with valid serial and supplemental "<supplemental>"
    And the heading terminator is "<terminator>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the overall disposition is "accepted"
    And the parsed heading type is "standard"

    Examples:
      | supplemental | terminator     |
      | HELLO        | 5SpacesThenSOA |
      | WORLD        | SOA            |

  @length @edge @positive
  Scenario: No maximum size enforced for heading line
    Given the message "hasSOA" contains SOA
    And pre-SOA is a Custom Heading with extremely long content of length "5000"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the overall disposition is "accepted"
    And the parsed heading type is "custom"
    And the forwarded heading equals the received heading (unchanged)

  @length @edge @positive
  Scenario: Heading containing SUID information
