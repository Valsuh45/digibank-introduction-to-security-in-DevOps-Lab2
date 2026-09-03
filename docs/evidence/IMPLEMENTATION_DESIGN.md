# DigiBank Web and DevSecOps Design

## Scope

This document explains the implemented DigiBank modular monolith. It covers the application entry point, runtime configuration, domain modules, HTTP error boundary, API documentation, database migrations, container packaging, CI, integration tests, and audit-evidence workflow.

## Runtime Assembly

`digibank-web` is the executable module. `DigiBankApplication` scans `com.m2ibank`, allowing controllers, services, repositories, and entities supplied by all domain modules to join one Spring context. The application exposes a static homepage, Springdoc OpenAPI endpoints, and an Actuator health endpoint.

PostgreSQL is the development and container database. Credentials are supplied through environment variables and the password has no committed default. Tests activate a dedicated profile using in-memory H2 in PostgreSQL compatibility mode. Hibernate validates rather than creates the schema, while Flyway owns schema creation and seed data.

## Error Boundary

`GlobalExceptionHandler` maps resource-not-found errors to 404, validation and business errors to 400, and unexpected exceptions to a generic 500 response. Responses use `ApiResponse`, never include exception class names or stack traces, and unexpected exceptions are logged only on the server.

## Database

Flyway V1 creates `customers`, `bank_accounts`, and `transfers` with primary keys, uniqueness constraints, foreign keys, non-negative monetary constraints, status fields, and lookup indexes. V2 inserts deterministic demonstration customers, accounts, and one successful transfer without embedding credentials. SQL remains compatible with PostgreSQL and H2 PostgreSQL mode.

Migrations are kept aligned with the customer, account, and transfer entities. Hibernate validates the schema at startup so schema drift is caught during tests, local runs, and CI.

## Delivery and Evidence

The Docker image uses a Maven build stage and a Java 17 runtime stage, runs as an unprivileged user, and exposes an application health check. Docker Compose requires `POSTGRES_PASSWORD`, waits for PostgreSQL health, and configures the application exclusively through environment variables.

GitHub Actions runs `mvn clean verify`, scans the repository filesystem, scans the built Docker image, and starts the Docker Compose stack for a smoke test. Cucumber exercises transfer behavior through the Spring service layer, while the Compose smoke test verifies real HTTP health, OpenAPI, and transfer endpoints.

Audit commands and expected evidence are recorded in `docs/evidence/README.md`; generated logs and screenshots are intentionally not committed by default.
