@TID73001REV0.1.0
Feature: Message Store Validation
  As a test engineer
  I want to validate messages in MongoDB message-store
  So that I can verify message processing and delivery

  Background:
    Given the test-harness is accessible
    And the test-harness is in a clean state

  @smoke
  Scenario: Validate complete message flow in MongoDB
    When I inject a valid Type B message
    Then the incoming message should be found
    And the incoming message should have statuses:
      | RECEIVED |
      | PARSED   |
    And the outgoing message should be found
    And the outgoing message should have statuses:
      | TARGET_IDENTIFIED      |
      | PREPARED_TO_DELIVER    |
      | DISPATCHED             |
      | DELIVERED              |