# common-module

## Purpose

`common-module` contains shared code used by every DigiBank domain module. It is intentionally small because anything placed here becomes a dependency for the rest of the project.

## What It Contains

- `ApiResponse<T>`: the standard JSON response wrapper used by controllers.
- `ErrorDetails`: a detailed error model for validation and request failures.
- `DigiBankConstants`: shared constants such as the default currency.
- Custom exceptions: safe domain exceptions for not-found, business-rule, invalid-operation, and insufficient-balance failures.

## Security Notes

This module helps keep API errors predictable and safe. Controllers and services can throw domain exceptions without exposing stack traces, SQL messages, or framework internals to clients.

`ErrorDetails` defensively copies validation errors so the response cannot be changed after creation through an old map reference.

## Verification

Run this module alone with:

```bash
mvn -pl common-module test
```
