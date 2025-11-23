@ALM432432432434324
Feature: Type-B Address Section parsing & validation (Req 1374121)

  Background:
    Given a clean TypeBComposer
    And the configuration has the following routes configured:
      | RouteType | Address | Destination  | Connection      | OutQueue   |
      | DIRECT    | SWIRI1G | destination4 | lab-connection2 | LKYEDLT.IN |
      | DIRECT    | LKYEGLT | destination7 | lab-connection5 | LETJPLK.IN |
      | DIRECT    | MILXTXS | destination3 | lab-connection1 | LKYSOLT.IN |
      | DIRECT    | LETKJLK | destination8 | lab-connection6 | LETBCLK.IN |

  @positive @overview_matrix
  Scenario Outline: Address Section present — NAL mandatory; Pilot/SAL optional; allowed orders
    Given a clean TypeBComposer
    And I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And <pilotStep>
    And <salStep>
    And <nalStep>
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the Address Section is detected
    And <pilotThen>
    And NAL is the last element
    And the message is accepted

    Examples:
      | pilotStep                                                       | salStep                                                         | nalStep                                 | pilotThen                           | Notes                       |
      | NOOP                                                            | NOOP                                                            | I add address line "QN MILXTXS"         | zero Pilot elements are detected    | N only                      |
      | NOOP                                                            | I add address line "QU SWIRI1G"                                 | I add address line "QN SWIRI1G MILXTXS" | zero Pilot elements are detected    | SAL + NAL                   |
      | I add pilot address line "QN LKYEGLT" with pilot signal "/////" | NOOP                                                            | I add address line "QN MILXTXS"         | exactly 1 Pilot element is detected | Pilot + NAL                 |
      | I add pilot address line "QN LKYEGLT" with pilot signal "/////" | I add address line "QU SWIRI1G"                                 | I add address line "QN SWIRI1G MILXTXS" | exactly 1 Pilot element is detected | Pilot + SAL + NAL (typical) |
      | I add address line "QU SWIRI1G"                                 | I add pilot address line "QN LKYEGLT" with pilot signal "/////" | I add address line "QN SWIRI1G MILXTXS" | exactly 1 Pilot element is detected | SAL may precede Pilot (ok)  |

  @negative @pilot_after_nal
  Scenario Outline: Reject when Pilot appears after NAL
    Given a clean TypeBComposer
    And I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add address line "<nal>"
    And I add pilot address line "<pilot>" with pilot signal "/////"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "INVALID PILOT ADDRESS LINE"

    Examples:
      | nal                | pilot      |
      | QN SWIRI1G         | QN LKYEGLT |
      | QN SWIRI1G MILXTXS | QN LKYEGLT |

  @positive
  Scenario Outline: Accept minimal valid message with a single NAL (various first-element SOA tolerances)
    Given I start composing a Type-B message with SOA "<soaForm>" and EOA "DOT"
    And I add address line "<priority> <riList>"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the Address Section is detected
    And the message is accepted
    And the priority is "<expectedPriority>"

    Examples:
      | soaForm  | priority | riList          | expectedPriority |
      | CRLF+SOH | QN       | SWIRI1G         | NORMAL           |
      | CRLF+SUB | QU       | SWIRI1G LKYEGLT | URGENT           |
      | SOH only | QD       | SWIRI1G         | DEFERRED         |
      | SUB only | SS       | SWIRI1G         | EMERGENCY        |
      | (no SOA) | (none)   | SWIRI1G         | NORMAL           |

  @positive
  Scenario: Accept SAL + NAL (multiple SALs allowed; NAL last)
    Given I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add address line "QU SWIRI1G"
      # SAL
    And I add address line "QX SWIRI1G LKYEGLT"
      # SAL
    And I add address line "QN SWIRI1G LKYEGLT MILXTXS"
      # NAL last
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the Address Section is detected
    And zero Pilot elements are detected
    And NAL is the last element
    And the message is accepted

  @positive
  Scenario Outline: Priority classification (two-letter codes; one-letter defaults to Normal; others → Normal)
    Given I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add address line "<priority> SWIRI1G"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is accepted
    And the priority is "<expected>"

    Examples:
      | priority | expected  |                                         |
      | SS       | EMERGENCY |                                         |
      | QS       | EMERGENCY |                                         |
      | QC       | EMERGENCY |                                         |
      | QU       | URGENT    |                                         |
      | QX       | URGENT    |                                         |
      | QD       | DEFERRED  |                                         |
      | QN       | NORMAL    |                                         |
      | Q        | NORMAL    | # one character - Normal                |
      | ZZ       | NORMAL    | # any code other than SS/QS/QC/QU/QX/QD |

  @negative
  Scenario: ADDRESS SECTION NOT FOUND (no SOA/EOA/addresses)
    Given I craft a raw message without any Address Section
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "ADDRESS SECTION NOT FOUND"

  @negative
  Scenario: END OF ADDRESS NOT FOUND (EOA missing for an element)
    Given I craft a raw message where the first element uses "CRLF+SOH" and omits the EOA terminator
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "END OF ADDRESS NOT FOUND"

  @negative
  Scenario: INVALID ADDRESS SECTION (text found between EOA and next SOA)
    Given I craft a raw message with an address element followed by stray text "XYZ" between EOA and the next SOA
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "INVALID ADDRESS SECTION"

  @negative
  Scenario: Too many Pilot lines (>1)
    Given I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add pilot address line "QN SWIRI1G" with pilot signal "/////"
    And I add pilot address line "QN LKYEGLT" with pilot signal "/////"
    And I add address line "QN SWIRI1G"
      # NAL last
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "TOO MANY PILOT ADDRESS LINE"

  @negative
  Scenario: Pilot has more than one RI
    Given I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add pilot address line "QN SWIRI1G LKYEGLT" with pilot signal "/////"
    And I add address line "QN SWIRI1G"
      # NAL last (to allow parsing)
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "PILOT ADDRESS LINE - TOO MANY ADDRESSES"

  @negative
  Scenario Outline: INVALID ROUTING INDICATOR ADDRESS (bad RI token)
    Given I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add address line "QN <badToken> SWIRI1G"
      # NAL
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "INVALID ROUTING INDICATOR ADDRESS - <badToken>"

    Examples:
      | badToken |                                                        |
      | ABCDEF   | # too short (<7)                                       |
      | ABCDEFGH | # too long (>7)                                        |
      | ABCD#12  | # non-alphanumeric                                     |
      | QSS      | # treated as RI (not a 2-letter priority) - invalid RI |

  @negative
  Scenario: TOO MANY ADDRESSESS PER LINE (>8)
    Given I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add address line "QN AAAAAAA BBBBBBB CCCCCCC DDDDDDD EEEEEEE FFFFFFF GGGGGGG HHHHHHH IIIIIII"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "TOO MANY ADDRESSESS PER LINE"

  @negative
  Scenario: TOO MANY ADDRESS LINES (>4 for NAL span)
    Given I start composing a Type-B message with SOA "CRLF+SOH" and EOA "DOT"
    And I add address line "QN AAAAAAA BBBBBBB CCCCCCC DDDDDDD EEEEEEE FFFFFFF GGGGGGG HHHHHHH"
    And I add address line "AAAAAAA BBBBBBB CCCCCCC DDDDDDD EEEEEEE FFFFFFF GGGGGGG HHHHHHH"
    And I add address line "AAAAAAA BBBBBBB CCCCCCC DDDDDDD EEEEEEE FFFFFFF GGGGGGG HHHHHHH"
    And I add address line "AAAAAAA BBBBBBB CCCCCCC DDDDDDD EEEEEEE FFFFFFF GGGGGGG HHHHHHH"
    And I add address line "AAAAAAA BBBBBBB"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "Test-UAT"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "TOO MANY ADDRESS LINES"

  @positive @heading_sample
  Scenario Outline: SOA tolerated (CRLF+SOH or CRLF+SUB)
    Given a clean TypeBComposer
    And I start composing a Type-B message with SOA "<soa>" and EOA "DOT"
    And I add address line "QD SWIRI1G"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "FSU/15"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the Address Section is detected

    Examples:
      | soa      |
      | CRLF+SUB |
      | CRLF+SOH |

  @positive
  Scenario: First element without complete SOA when no heading (accepted)
    Given I craft a raw message starting directly with "QN SWIRI1G" with no heading, no CRLF prefix
    And I append the correct EOA for the element
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "FSU/15"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the Address Section is detected
    And the message is accepted

  @positive
  Scenario: Recipients are uppercased at parse time
    Given a clean TypeBComposer
    And I add address line "qn swiri1g lkyeglt"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "FSU/15"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the list of recipients extracted equals "SWIRI1G LKYEGLT"
    And the message is accepted

  @negative @heading_missing_soa
  Scenario: Address Section with heading — first element missing SOA is rejected
    Given a clean TypeBComposer
    Given Heading line is "719"
    And I add address line "QD SWIRI1G MILXTXS"
    Given I craft a raw message with heading "719" and first address element "QD MSPFMPO BRUACER" without SOA and with EOA "DOT"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "FSU/15"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "ADDRESS SECTION NOT FOUND"

  @positive
  Scenario: Routing resolution still executes even if unrelated Address Section errors exist
    Given a clean TypeBComposer
    And I add address line "QD SWIRI1G ASAFX"
    And I set originator "LKYSOLT" and identity "3456700"
    And I add text line "FSU/15"
    And the message is composed
    When I send the composed message via the Test Harness
    Then the message is rejected with reason "INVALID ROUTING INDICATOR ADDRESS - ASAFX"
    And the list of recipients extracted equals "SWIRI1G"