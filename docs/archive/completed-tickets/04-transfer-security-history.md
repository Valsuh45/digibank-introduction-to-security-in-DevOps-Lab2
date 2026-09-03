# Ticket 4: Transfer Engine, Transaction History, and Transaction Security

## Owner
Team member 4

## Scope
Own `transfer-module` and coordinate with account-domain interfaces.

## Tasks
- Create the `Transfer` JPA entity with:
  - `id`
  - `transferReference`
  - `sourceAccountNumber`
  - `targetAccountNumber`
  - `amount`
  - `status`
  - `executionDate`
  - `description`
- Create transfer status enum.
- Create `TransferRequestDto` with validation for source account, target account, positive amount, and bounded description length.
- Create `TransferResponseDto` with safe output fields.
- Create `TransferRepository` with `findBySourceAccountNumberOrTargetAccountNumber`.
- Create `TransferService` and `TransferServiceImpl`.
- Implement `@Transactional executeTransfer(...)`:
  - verify source account exists
  - verify target account exists
  - reject missing accounts safely
  - reject insufficient balance
  - reject zero, negative, or ambiguous same-account transfers
  - debit source account
  - credit target account
  - generate unique transfer reference
  - persist transaction record with status
- Implement `getAccountTransactionHistory(accountNumber)`.
- Create `TransferController` endpoints:
  - `POST /api/v1/transfers`
  - `GET /api/v1/transfers/{id}`
  - `GET /api/v1/transfers/account/{accountNumber}`
- Add unit tests for success, insufficient funds, missing accounts, and invalid same-account transfer.

## Security Acceptance Criteria
- Transfer execution is transactional.
- Money uses `BigDecimal`, never floating-point types.
- Invalid transfer amounts are rejected before business logic runs.
- Failed transfers cannot partially debit or credit accounts.
- Transfer references are unique and auditable.
- Transaction history only exposes safe transfer fields.
- Errors do not expose stack traces, SQL details, or implementation internals.

## Verification
- Run `mvn -pl transfer-module test`.
- Run `mvn clean verify` from the root after integration.

## Evidence
- Unit test output for transfer service behavior.
- Sample API call showing a successful transfer.
- Sample API call showing insufficient balance handled safely.
