# transfer-module

## Purpose

`transfer-module` owns money transfer execution and account transaction history. It is the most sensitive domain module because it changes balances and creates audit records.

## Main Responsibilities

- Validate transfer requests.
- Reject same-account transfers.
- Reject missing source or target accounts.
- Reject transfers with insufficient funds.
- Debit the source account and credit the target account.
- Save immutable transfer history.
- Return account transaction history.

## Important Files

- `TransferController`: REST endpoints under `/api/v1/transfers`.
- `TransferService` and `TransferServiceImpl`: transfer business workflow.
- `TransferRepository`: database access for transfer history.
- `Transfer`: immutable JPA audit record.
- `TransferRequestDto` and `TransferResponseDto`: API input and output models.

## Security Notes

Transfer execution is transactional. If any step fails, account balances and transfer history roll back together. This prevents partial debits, partial credits, or audit records that do not match account state.

Failed transfer attempts are rejected through safe domain exceptions and do not expose stack traces or database details.

## Verification

Run this module alone with:

```bash
mvn -pl transfer-module test
```
