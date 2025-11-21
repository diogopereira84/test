@TID60305REV0.5.0
#noinspection CucumberTableInspection
Feature: [REJECT] Deliver Reject Messages back to the Originator when allowed at Input Connection

  Background:
    Given the test-harness is accessible
    And the test-harness is in a clean state

  # ==================== COMPREHENSIVE UNKNOWN_ORIGIN_INDICATOR TESTS ====================

  @regression
  Scenario Outline: Send a reject message for invalid originator indicator - <description>
    Given a type-b message
    And this type-b message has an invalid originator address "<invalidOriginator>"
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
    And the message contains the error "<errorCode>"
    # Verify incoming message in MongoDB
    And the incoming message should be found
    And the incoming message should have error code "<errorCode>"
    And the incoming message should have 1 error
    And the incoming message raw data should match the sent type-b message
    # Verify outgoing message in MongoDB
    And the outgoing message should be found
    And the outgoing message should have error code "<errorCode>"
    And the outgoing message should have 1 error
    And the outgoing message should have 4 status logs
    And the outgoing message status logs should be in order:
      | ERRORS_IDENTIFIED    |
      | PREPARED_TO_DELIVER  |
      | DISPATCHED           |
      | DELIVERED            |
    And the outgoing message body should contain "PLS RPT YR ..... DUE TO:"
    And the outgoing message body should contain error "<errorCode>" in the list

    Examples:
      | invalidOriginator | errorCode                      | description                                      |
      | MILXT             | UNKNOWN_ORIGIN_INDICATOR       | Too short - 5 characters instead of 7            |
      | MIL               | UNKNOWN_ORIGIN_INDICATOR       | Too short - 3 characters (only city code)        |
      | M                 | UNKNOWN_ORIGIN_INDICATOR       | Too short - 1 character                          |
      |.                   | UNKNOWN_ORIGIN_INDICATOR      | Empty originator indicator                       |
      | HDQRMJUXX         | ORIGIN_INDICATOR_FORMAT_ERROR  | Too long - 9 characters instead of 7             |
   #   | HDQ@M#U           | ORIGIN_INDICATOR_FORMAT_ERROR  | Special characters in originator                 |
   #   | hdqrmju           | ORIGIN_INDICATOR_FORMAT_ERROR  | Lowercase letters instead of uppercase           |
   #   | H1QRM2U           | ORIGIN_INDICATOR_FORMAT_ERROR  | Numbers in wrong positions                       |

  # ==================== SINGLE ERROR TESTS ====================

  @regression
  Scenario: Send a reject message with 1 error - Invalid originator (MILXT)
    Given a type-b message
    And this type-b message has an invalid originator address "MILXT"
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
    And the message contains the error "UNKNOWN_ORIGIN_INDICATOR"
    # Comprehensive MongoDB validations
    And the incoming message should be found
    And the incoming message should have error code "UNKNOWN_ORIGIN_INDICATOR"
    And the incoming message should have 1 error
    And the incoming message raw data should match the sent type-b message
    And the outgoing message should be found
    And the outgoing message should have error code "UNKNOWN_ORIGIN_INDICATOR"
    And the outgoing message should have 1 error
    And the outgoing message should have 4 status logs
    And the outgoing message status logs should be in order:
      | ERRORS_IDENTIFIED    |
      | PREPARED_TO_DELIVER  |
      | DISPATCHED           |
      | DELIVERED            |
    And the outgoing message body should contain "PLS RPT YR ..... DUE TO:"
    And the outgoing message body should contain error "UNKNOWN_ORIGIN_INDICATOR" in the list


  # ==================== MULTIPLE ERROR TESTS ====================
  @regression
  Scenario: Send a reject message with 2 errors
    Given a type-b message
    And this type-b message has an invalid originator address "MILXT"
    #And this type-b message has 9 addresses in one line
    And this type-b message has the following addresses:
      | BARXSXT LETBCLK SWIRI1G LKYSOLT LKYEDLT LKYEGLT LETKJLK LETJPLK LETBCLK |
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
    And the message contains the following error(s):
      | TOO_MANY_ADDRESSES_PER_LINE                 |
      | UNKNOWN_ORIGIN_INDICATOR                    |
      | UNKNOWN_ADDRESS                             |
      | EMPTY_DESTINATION                           |
      | EMPTY_DESTINATION                           |
    And the incoming message should be found
    And the incoming message should have 2 errors
    And the outgoing message should be found
    And the outgoing message should have 5 errors

  @regression
  Scenario: Send a reject message with 3 errors
    Given a type-b message
    And this type-b message has an invalid originator address
    And this type-b message has 9 addresses in one line and 5 lines of addresses
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
    And the message contains the following error(s):
      | TOO_MANY_ADDRESSES_LINES    |
      |TOO_MANY_ADDRESSES_PER_LINE  |
      |UNKNOWN_ORIGIN_INDICATOR     |
      |UNKNOWN_ADDRESS              |
      |EMPTY_DESTINATION            |
      |EMPTY_DESTINATION            |
    And the incoming message should be found
    And the incoming message should have 3 errors
    And The incoming message should have error code:
      | TOO_MANY_ADDRESSES_LINES      |
      | TOO_MANY_ADDRESSES_PER_LINE   |
      | UNKNOWN_ORIGIN_INDICATOR      |
    And the outgoing message should be found
    And the outgoing message should have 6 errors
    And The outgoing message should have error code:
      | TOO_MANY_ADDRESSES_LINES      |
      | TOO_MANY_ADDRESSES_PER_LINE   |
      | UNKNOWN_ORIGIN_INDICATOR      |
      | UNKNOWN_ADDRESS               |
      | EMPTY_DESTINATION             |
      | EMPTY_DESTINATION             |


  # ==================== SERIAL NUMBER TESTS ====================

  @regression
  Scenario: Send a reject message with serial number
    Given a type-b message
    And this type-b message has the serial number "ABC123"
    And this type-b message has an invalid originator address "MILXT"
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
    And the reject has the message text "PLS RPT YR ABC123 DUE TO:"
    And the outgoing message should be found
    And the outgoing message body should contain "PLS RPT YR ABC123 DUE TO:"

  Scenario: Send a reject message with no serial number
    Given a type-b message
    And this type-b message has an invalid originator address "MILXT"
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
    And the reject has the message text "PLS RPT YR ..... DUE TO:"
    And the outgoing message should be found
    And the outgoing message body should contain "PLS RPT YR ..... DUE TO:"


  # ==================== ORIGINATOR ADDRESS TESTS ====================

  @regression
  Scenario: Send a reject message with the system service address as the originator
    Given a type-b message
    And this type-b message has an invalid originator address
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
  #  And the reject message has a new origin according to the system service address config


  # ==================== MESSAGE CONTENT TESTS ====================

  @regression
  Scenario: Send a reject message keeping the content of the original message
    Given a type-b message
    And this type-b message has an invalid originator address
    When this type-b message is sent through mercury
    Then I request the test harness to retrieve all received messages
 #   And the reject message content has the original message with no control characters
 #   And the outgoing message should be found
 #   And the outgoing message raw data should contain "AVS"
 #   And the outgoing message raw data should contain "JU0580L30AUG LA BEGBCN"


  # ==================== PRIORITY TESTS ====================

  @regression
  Scenario: Send a reject message with the same priority of the original message
    Given a type-b message
 #   And the message has a valid priority code
 #   And this type-b message has an invalid originator address
 #   When this type-b message is sent through mercury
 #   Then I request the test harness to retrieve all received messages
 #   And mercury should assign to the reject message the same priority from the original message

  @regression
  Scenario: Send a reject message with an invalid priority of the original message
    Given a type-b message
 #   And the message has a valid priority code
 #   And the message has an invalid priority code
 #   And this type-b message has an invalid originator address
 #   When this type-b message is sent through mercury
 #   Then I request the test harness to retrieve all received messages
 #   And mercury should assign to the reject message the priority "NORMAL"


  # ==================== ROUTING TESTS ====================

  @regression
  Scenario: Identify a reject message with one error
    Given a type-b message
    And this type-b message has an invalid originator address
    When this type-b message is sent through mercury
#    Then the message should be routed to the rejection topic
#    And mercury process the message sending a reject message


  # ==================== SERIAL NUMBER VALIDATION TESTS ====================

  @regression
  Scenario: Send a type-b message with a valid serial number
    Given a type-b message
 #   And this type-b message has the header "ZCZC ABC852"
 #   When this type-b message is sent through mercury
 #   Then mercury process the message sending to the correct destination
 #   And the message serial number is saved as "ABC852" in the store

  @regression
  Scenario: Send a type-b message with no serial number
    Given a type-b message
  #  And this type-b message has the header "ZCZC"
  #  When this type-b message is sent through mercury
  #  Then mercury process the message sending to the correct destination
  #  And the message serial number is saved as empty in the store

  @regression
  Scenario: Send a type-b message with a serial number with 4 digits
    Given a type-b message
 #   And this type-b message has the header "ZCZC 1234"
 #   When this type-b message is sent through mercury
 #   Then mercury process the message sending to the correct destination
 #   And the message serial number is saved as "1234" in the store

  @regression
  Scenario: Send a type-b message with a serial number equal 000
    Given a type-b message
  #  And this type-b message has the header "ZCZC ABC000"
  #  When this type-b message is sent through mercury
  #  Then mercury process the message sending to the correct destination
  #  And the message serial number is saved as "ABC000" in the store

  @regression
  Scenario: Send a type-b message with an invalid header
    Given a type-b message
  #  And this type-b message has the header "ABC123"
  #  When this type-b message is sent through mercury
  #  Then mercury process the message sending to the correct destination
  #  And the message serial number is saved as empty in the store

  @regression
  Scenario: Send a type-b message with multiple headers
    Given a type-b message
  #  And this type-b message has the header "ZCZC ABC123 \r\n ZCZC ABC123"
  #  When this type-b message is sent through mercury
  #  Then mercury process the message sending to the correct destination
  #  And the message serial number is saved as "ABC123" in the store