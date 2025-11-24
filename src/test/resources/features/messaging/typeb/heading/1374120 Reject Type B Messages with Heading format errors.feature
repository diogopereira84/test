Feature: 1374120 - [Type B Format] Reject Type B Messages with Heading format errors

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
  # GROUP 1: Heading Support DISABLED
  # ==============================================================================

  @heading-disabled @positive @bug
  Scenario Outline: When the content that precedes the SOA indicator is Address Element
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "false"
    And the message "<hasSOA>" contains SOA
    And I set the content immediately preceding the SOA to "<addressElement>"
    And I add address line "QN SINSGSQ"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
    #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
    And the value of "message-store.incoming-messages.serialNumber" is "equal to" "<expectedSerial>"
    And the value of "message-store.incoming-messages.header" is "equal to" "<headingContent>"
    #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.serialNumber" is "equal to" "<expectedSerial>"
    And the value of "message-store.outgoing-messages.header" is "equal to" "<headingContent>"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | hasSOA | addressElement        |  composed (plaintext)                         |
      | no     |                       | # "QN SINSGSQ\r\n.LKYSOLT... "                |
      | yes    | AddressEndIndicator   | # "\r\n.\r\n\u0001QN SINSGSQ\r\n.LKYSOLT..."  |
      | yes    | PilotSignal           | # "/////\r\n\u0001QN SINSGSQ\r\n.LKYSOLT..."  |

  @heading-disabled @negative
  Scenario: When the content that precedes the SOA indicator is NOT an Address Element
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "false"
    And I set heading "THIS SHOULD BE REJECTED"
    And I add address line "QN SINSGSQ"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
    #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.errors.errorCode.errorType" is "contains":
      | HEADING_SECTION_NOT_ALLOWED |
    #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors.errorCode.errorType" is "contains":
      | HEADING_SECTION_NOT_ALLOWED |
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | ERRORS_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

  Scenario Outline: Handle legacy ZCZC sequence in Heading Section
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
    #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.headingParts" is "equals":
      | <expectedHeadingParts>  |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
    And the value of "message-store.incoming-messages.serialNumber" is "equal to" "<expectedSerial>"
    And the value of "message-store.incoming-messages.header" is "equal to" "<headingContent>"
    #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.serialNumber" is "equal to" "<expectedSerial>"
    And the value of "message-store.outgoing-messages.header" is "equal to" "<headingContent>"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | headingContent                 |            | expectedHeadingParts        | expectedSerial |
      | ZCZC 158 081926 OCT 25         |            | ZCZC, 158, 081926, OCT, 25  | 158            |
      |  ZCZC 158 081926 OCT 25        | #1 space   | ZCZC, 158, 081926, OCT, 25  | 158            |
      |        ZCZC 158 081926 OCT 25  | #6 spaces  | ZCZC, 158, 081926, OCT, 25  | 158            |

  @heading-enabled @positive
  Scenario Outline: Ignore prefixes for parsing but preserve for forwarding
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And I set heading prefix <prefix> and content "001 VALID"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
    #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
    And the value of "message-store.incoming-messages.header" is "equal to" "<headingContent>"
        #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.header" is "equal to" "<headingContent>"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | prefix    | headingContent  |
      | "   "     | 001 VALID       |
      | "ZCZC "   | ZCZC 001 VALID  |
      | "  ZCZC"  | ZCZC001 VALID   |

  @heading-enabled @positive
  Scenario Outline: Accept valid Standard Heading formats
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
        #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.headingParts" is "equals":
      | <expectedHeadingParts>  |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
    And the value of "message-store.incoming-messages.serialNumber" is "equal to" "<expectedSerial>"
    And the value of "message-store.incoming-messages.header" is "equal to" "<headingContent>"
             #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.headingParts" is "equals":
      | <expectedHeadingParts>  |
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.serialNumber" is "equal to" "<expectedSerial>"
    And the value of "message-store.outgoing-messages.header" is "equal to" "<headingContent>"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | headingContent        | expectedSerial | expectedHeadingParts   |
      | 1                     | 1              | 1                      |
      | 12345                 | 12345          | 12345                  |
      | 001 SUPP INFO 2025    | 001            | 001,SUPP,INFO,2025     |

  @heading-enabled @positive
  Scenario Outline: Heading containing SUID information
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
        #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.headingParts" is "contains_in_order":
      | SUID                    |
      | <expectedSuidMessageId> |
      | <suidTransactionId>     |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
    And the value of "message-store.incoming-messages.suidMessageId" is "equal to" "<expectedSuidMessageId>"
    And the value of "message-store.incoming-messages.suidTransactionId" is "equal to" "<suidTransactionId>"
    And the value of "message-store.incoming-messages.header" is "equal to" "<headingContent>"
             #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.headingParts" is "contains_in_order":
      | SUID                    |
      | <expectedSuidMessageId> |
      | <suidTransactionId>     |
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.suidMessageId" is "equal to" "<expectedSuidMessageId>"
    And the value of "message-store.outgoing-messages.suidTransactionId" is "equal to" "<suidTransactionId>"
    And the value of "message-store.outgoing-messages.header" is "equal to" "<headingContent>"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | headingContent                                      | expectedSuidMessageId   | suidTransactionId      |                              |
      | SUID 6F1E2D3C-1111-2222 3333-444455556666           | 6F1E2D3C-1111-2222      | 3333-444455556666      |                              |
      |  SUID JJBLK344Tna8S6o5ZEzSDg EytNEK2HQvKV0AKTofev1A | JJBLK344Tna8S6o5ZEzSDg  | EytNEK2HQvKV0AKTofev1A |  # POs same example - Normal |

  @heading-enabled @positive
  Scenario Outline: Custom Customer Heading formats
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
        #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.headingParts" is "equals":
      | <expectedHeadingParts>  |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
    And the value of "message-store.incoming-messages.header" is "equal to" "<headingContent>"
         #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.headingParts" is "equals":
      | <expectedHeadingParts>  |
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.header" is "equal to" "<headingContent>"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | headingContent                                   | expectedHeadingParts                            |                                         |
      | 123456                                           | 123456                                          | # Only numbers                          |
      | A123                                             | A123                                            | # Alpha numeric                         |
      |  CUSTOM CUSTOMER HEADING INFO                    | CUSTOM,CUSTOMER,HEADING,INFO                    | # PO's same example for Multiple parts  |
      | VERY LONG CUSTOM HEADING TEXT ON A SINGLE LINE   | VERY,LONG,CUSTOM,HEADING,TEXT,ON,A,SINGLE,LINE  | # Multiple parts                        |

  @heading-enabled @positive
  Scenario Outline: SUID and Original Heading Line
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And I set heading "<headingContent>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
        #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
    And the value of "message-store.incoming-messages.header" is "equal to" "<headingContent>"
     #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.header" is "equal to" "<headingContent>"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | headingContent                                                                    |                                |
      |  SUID JJBLK344Tna8S6o5ZEzSDg EytNEK2HQvKVOAKTofev1A 158 081926 OCT 25             | # SUID with Standard Heading   |
      |  SUID JJBLK344Tna8S6o5ZEzSDg EytNEK2HQvKVOAKTofev1A CUSTOM CUSTOMER HEADING INFO  | # SUID with Custom Heading     |

  # ==============================================================================
  # GROUP 3: Heading Support ENABLED - Invalid Formats (Negative)
  # ==============================================================================

  @heading-enabled @negative
  Scenario Outline: Multi-line headings
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And I set heading with internal line break: "<headingContentLineOne>" + CRLF + "<headingContentLineTwo>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
    #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.errors.errorCode.errorType" is "contains":
      | INVALID_HEADING_SECTION  |
     #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors.errorCode.errorType" is "contains":
      | INVALID_HEADING_SECTION  |
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | ERRORS_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | headingContentLineOne  | headingContentLineTwo      |
      |  THIS WOULD BE         | AN INVALID HEADING SECTION |

  @heading-enabled @positive
  Scenario Outline: Heading terminates with spacing signal (5 spaces) and/or SOA
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "yes" contains SOA
    And pre-SOA is a Standard Heading with valid serial and supplemental "<supplemental>"
    And the heading terminator is "<terminator>"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
    #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
        #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |

    Examples:
      | supplemental | terminator     |
      | HELLO        | 5SpacesThenSOA |
      | WORLD        | SOA            |

  @heading-enabled @positive
  Scenario: No maximum size enforced for heading line
    Given I select the connection where "messageConfiguration.acceptMessagesWithAHeadingSection" is "true"
    And the message "hasSOA" contains SOA
    And pre-SOA is a Custom Heading with extremely long content of length "5000"
    And I add address line "QN JFKNYBA"
    And the message is composed
    When I send the composed message via the Test Harness
    #================ MongoDb validation ================#
    #================ incoming-messages ================#
    Then the value of "message-store.incoming-messages.statusLogs.status" is "contains":
      | RECEIVED  |
      | PARSED    |
    And the value of "message-store.incoming-messages.errors" is "equal to" "empty"
        #================ outgoing-messages ================#
    And the value of "message-store.outgoing-messages.errors" is "equal to" "empty"
    And the value of "message-store.outgoing-messages.statusLogs.status" is "contains":
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |