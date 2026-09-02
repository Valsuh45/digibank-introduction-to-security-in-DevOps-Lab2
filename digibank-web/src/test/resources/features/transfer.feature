Feature: Secure money transfers

  Scenario: Execute a transfer between seeded accounts
    Given the seeded DigiBank accounts are available
    When a transfer of 100.00 XAF is executed from account "100000000001" to account "100000000002"
    Then the transfer is recorded as successful
    And the source and target balances are updated atomically

  Scenario: Reject a transfer with insufficient funds
    Given the seeded DigiBank accounts are available
    When an excessive transfer is attempted from account "100000000002" to account "100000000001"
    Then the transfer is rejected for insufficient balance
    And the source balance remains unchanged
