# Ticket 3: Bank Account Management Domain

## Owner
Team member 3

## Scope
Own `account-module`.

## Tasks
- Create the `BankAccount` JPA entity with:
  - `id`
  - `accountNumber`
  - `balance`
  - `currency`
  - `accountType`
  - `status`
  - `createdAt`
  - customer reference or `customerId` consistent with module boundaries
- Create account type and account status enums.
- Create `AccountRequestDto` with validation for customer ID, account type, and non-negative initial balance.
- Create `AccountResponseDto` with safe output fields.
- Create `BankAccountRepository` with `findByAccountNumber` and `findByCustomerId`.
- Create `BankAccountService` and `BankAccountServiceImpl`.
- Implement:
  - create account for customer
  - get account details
  - get account balance
  - get account by account number
  - list accounts for a customer
- Create `BankAccountController` endpoints:
  - `POST /api/v1/accounts`
  - `GET /api/v1/accounts/{id}`
  - `GET /api/v1/accounts/number/{accountNumber}`
  - `GET /api/v1/accounts/customer/{customerId}`
- Add unit tests for account creation, generated account numbers, lookup, and not-found behavior.

## Security Acceptance Criteria
- Use `BigDecimal` for money, never floating-point types.
- Reject negative initial balances.
- Avoid exposing internal identifiers beyond required API fields.
- Account numbers are generated consistently and are not user-controlled.
- The service prevents invalid account states and uses shared exceptions for safe errors.

## Verification
- Run `mvn -pl account-module test`.
- Run `mvn clean verify` from the root after integration.

## Evidence
- Unit test output for account service behavior.
- Swagger screenshot showing account endpoints after integration.
- Sample API call showing account creation.
