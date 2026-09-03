# Module Overview

## Purpose

DigiBank uses Maven modules to keep code organized by responsibility. This makes the project easier for a team to divide, review, and test.

## Module Map

| Module | Responsibility |
|---|---|
| `common-module` | Shared API responses, constants, and domain exceptions |
| `customer-module` | Customer registration, customer lookup, and duplicate protection |
| `account-module` | Account creation, generated account numbers, account lookup, and balance updates |
| `transfer-module` | Transfer execution, transaction history, and transfer audit records |
| `digibank-web` | Spring Boot startup, configuration, migrations, API docs, static page, and safe error responses |

## Dependency Direction

The dependency direction is intentionally simple:

- Domain modules depend on `common-module`.
- `transfer-module` depends on `account-module` because transfers update account balances.
- `digibank-web` depends on all modules because it assembles the runnable application.

The domain modules do not depend on `digibank-web`. That keeps business logic separate from application startup and web configuration.

## Module READMEs

Each Maven module has its own README:

- `common-module/README.md`
- `customer-module/README.md`
- `account-module/README.md`
- `transfer-module/README.md`
- `digibank-web/README.md`
