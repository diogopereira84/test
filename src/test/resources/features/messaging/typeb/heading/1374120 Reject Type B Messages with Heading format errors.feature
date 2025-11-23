# features/messaging/typeb/heading/1374120_reject_type_b_messages_with_heading_format_errors.feature

Feature: Type-B Heading Section handling in Mercury
  Requirement: 1374120
  Ensures correct acceptance, parsing, and rejection of Type-B messages regarding the
  Heading Section (content preceding the SOA).

  Background:
    # --- DATA INTEGRITY CHECK ---
    # Ensure the configuration database has the required routing data
    Given the "configuration.hosts" collection contains the following documents:
      | _id     | type  | name    | enabled |
      | server1 | IBMMQ | server1 | true    |

    And the "configuration.connections" collection contains the following documents:
      | _id         | type  | name         | serviceAddress | format | hostId  | inQueue        | outQueue      | messageConfiguration.acceptMessagesWithAHeadingSection | enabled |
      | connectionA | IBMMQ | connectionA  | LETTTLK        | TYPE_B | server1 | LETTTLK.OUT    | LETTTLK.IN    | true                                                   | true    |
      | connectionB | IBMMQ | connectionB  | LETRRLK        | TYPE_B | server1 | LETRRLK.OUT    | LETRRLK.IN    | true                                                   | true    |
      | connectionC | IBMMQ | connectionC  | LETVVLK        | TYPE_B | server1 | LETVVLK.OUT    | LETVVLK.IN    | false                                                  | true    |
      | connectionD | IBMMQ | connectionD  | LETCCLK        | TYPE_B | server1 | LETCCLK.OUT    | LETCCLK.IN    | false                                                  | true    |

    And the "configuration.destinations" collection contains the following documents:
      | _id          | connectionIds[] | enabled |
      | destinationA | connectionA     | true    |
      | destinationB | connectionB     | true    |
      | destinationC | connectionC     | true    |
      | destinationD | connectionD     | true    |

    And the "configuration.routes" collection contains the following documents:
      | _id | type   | criteria.addressMatcher | criteria.type | destinationIds[] | enabled |
      | 175 | DIRECT | JFKNYBA                 | DISCRETE      | destinationA     | true    |
      | 176 | DIRECT | CDGFRAF                 | DISCRETE      | destinationB     | true    |
      | 177 | DIRECT | SINSGSQ                 | DISCRETE      | destinationC     | true    |
      | 178 | DIRECT | FRADELH                 | DISCRETE      | destinationD     | true    |

    And the "configuration.routing-indicators" collection contains the following documents:
      | _id     | type     | locationIdentifier | departmentCode | airlineCode |
      | JFKNYBA | DISCRETE | JFK                | NY             | BA          |
      | CDGFRAF | DISCRETE | CDG                | FR             | AF          |
      | SINSGSQ | DISCRETE | SIN                | SG             | SQ          |
      | FRADELH | DISCRETE | FRA                | DE             | LH          |

    # --- TEST SETUP ---
    Given a clean TypeBComposer
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Standard UAT Body Text"

  # ==============================================================================
  # GROUP 1: Heading Support DISABLED (acceptHeading = false)
  # ==============================================================================

  @config-disabled @edge @positive
  Scenario Outline: [DISABLED] Accept message when no actual heading content precedes SOA
    Given connection setting "acceptHeading" is "false"
    When I set the content immediately preceding the SOA to <preSoaContent>
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the heading detection result is "none"

    Examples:
      | description           | preSoaContent         |
      | Empty (SOA is first)  | ""                    |
      | EOA token preceding   | "AddressEndIndicator" |
      | Pilot Signal preceding| "PilotSignal"         |

  @config-disabled @negative
  Scenario: [DISABLED] Reject message when actual text content precedes SOA
    Given connection setting "acceptHeading" is "false"
    And the message "yes" contains SOA
    When I set the content immediately preceding the SOA to "GENERIC HEADING TEXT"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "rejected"
    And the reason is "HEADING SECTION NOT ALLOWED"

  # ==============================================================================
  # GROUP 2: Heading Support ENABLED - Prefixes and Forwarding
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
  # ==============================================================================

  @config-enabled @validation @negative
  Scenario Outline: [ENABLED] Reject Multi-line headings
    Given connection setting "acceptHeading" is "true"
    And the message "yes" contains SOA
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

  @length @edge @positive
  Scenario: No maximum size enforced for heading line
    Given connection setting "acceptHeading" is "true"
    And the message "hasSOA" contains SOA
    And pre-SOA is a Custom Heading with extremely long content of length "5000"
    And I add address line "QN SWIRI1G"
    And I finalize the composed message
    When the message is evaluated
    Then the overall disposition is "accepted"
    And the parsed heading type is "custom"
    And the forwarded heading equals the received heading (unchanged)