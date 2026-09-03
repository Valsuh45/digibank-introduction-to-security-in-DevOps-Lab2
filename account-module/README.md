# account-module

## Purpose

`account-module` owns bank account creation, lookup, generated account numbers, and balance updates. It is a separate Maven module so account rules remain easy to find and test.

## Main Responsibilities

- Create accounts for existing customer ids.
- Generate server-side 12-digit account numbers.
- Store account balances using `BigDecimal`.
- Reject negative balances.
- Retrieve accounts by id, account number, or customer id.
- Provide controlled balance updates for transfer workflows.

## Important Files

- `BankAccountController`: REST endpoints under `/api/v1/accounts`.
- `BankAccountService` and `BankAccountServiceImpl`: account business rules.
- `AccountNumberGenerator`: secure random account-number candidate generation.
- `BankAccountRepository`: database access for account records.
- `BankAccount`: JPA entity mapped to the `bank_accounts` table.

## Security Notes

Clients never choose account numbers, account status, currency, or creation time. Account numbers are generated with `SecureRandom`, and the service checks candidates for uniqueness before saving.

Money uses `BigDecimal`, not floating-point types, to avoid rounding errors in balances and transfers.

## Verification

Run this module alone with:

```bash
mvn -pl account-module test
```
