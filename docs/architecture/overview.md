# Architecture Overview

## Short Answer

DigiBank is a **modular monolith**.

The repository has multiple Maven modules, but they are packaged into one Spring Boot application and deployed as one runtime service.

## Why This Matters

A modular monolith gives the team clean code boundaries while keeping the runtime simple. Each module has a focused responsibility, but the application still starts, tests, scans, and deploys as one unit.

This is the right fit for Workshop 1 because the goal is to demonstrate secure DevOps foundations, not distributed service infrastructure.

## Module Boundaries

- `common-module`: shared response models, constants, and exceptions.
- `customer-module`: customer registration and lookup.
- `account-module`: account creation, account lookup, and balance state.
- `transfer-module`: transfers and transaction history.
- `digibank-web`: application startup, web configuration, migrations, API docs, and error handling.

## Runtime Shape

All modules run inside one Spring Boot process. They use one PostgreSQL database schema managed by Flyway. There is no network call between modules, no separate service discovery, and no separate deployment per business domain.

## Security Shape

Security controls are layered:

- DTO validation rejects malformed input early.
- Services enforce business rules.
- Entities and database constraints protect persisted state.
- The global exception handler prevents internal details from leaking.
- CI verifies tests, source scan, container scan, and Docker Compose smoke behavior.

## Follow One Transfer

An HTTP client sends JSON to the transfer controller. Bean Validation checks its DTO; the controller delegates to the transfer service. The service loads accounts, enforces banking rules, changes balances, and saves the transfer record within a Spring transaction. JPA repositories perform persistence and PostgreSQL constraints provide a final integrity boundary. Response DTOs control exposed fields, and the web exception handler maps failures to public HTTP responses.

The transfer implementation persists account changes through the account repository; it does not call `BankAccountService.updateBalance`. All participating repositories use the same application transaction and database. A transaction provides atomicity; it alone does not establish safe behavior for simultaneous balance updates.

Validation checks the shape of a request. Business rules decide whether an operation is allowed by the banking domain. Database constraints protect persisted relationships. None of these identifies the caller: this educational version has no authentication or customer/account ownership authorization. Workshop 1 establishes the local functional base; later security analysis must explicitly examine that trust boundary.
