# DigiBank Web and DevSecOps Design

## Scope

This slice assembles the modular Spring Boot application without implementing the customer, account, or transfer domains. It owns the application entry point, runtime configuration, HTTP error boundary, API documentation, database migrations, container packaging, CI, integration tests, and the audit-evidence workflow.

## Runtime Assembly

`digibank-web` is the executable module. `DigiBankApplication` scans `com.m2ibank`, allowing controllers, services, repositories, and entities supplied by all domain modules to join one Spring context. The application exposes a static homepage, Springdoc OpenAPI endpoints, and an Actuator health endpoint.

PostgreSQL is the development and container database. Credentials are supplied through environment variables and the password has no committed default. Tests activate a dedicated profile using in-memory H2 in PostgreSQL compatibility mode. Hibernate validates rather than creates the schema, while Flyway owns schema creation and seed data.

## Error Boundary

`GlobalExceptionHandler` maps resource-not-found errors to 404, validation and business errors to 400, and unexpected exceptions to a generic 500 response. Responses use `ApiResponse`, never include exception class names or stack traces, and unexpected exceptions are logged only on the server.

## Database

Flyway V1 creates `customers`, `bank_accounts`, and `transfers` with primary keys, uniqueness constraints, foreign keys, non-negative monetary constraints, status fields, and lookup indexes. V2 inserts deterministic demonstration customers, accounts, and one successful transfer without embedding credentials. SQL remains compatible with PostgreSQL and H2 PostgreSQL mode.

Before final verification, migrations are compared with any domain entities added concurrently. Domain source files remain outside this slice's ownership.

## Delivery and Evidence

The Docker image uses a Maven build stage and a Java 17 runtime stage, runs as an unprivileged user, and exposes an application health check. Docker Compose requires `POSTGRES_PASSWORD`, waits for PostgreSQL health, and configures the application exclusively through environment variables.

GitHub Actions runs `mvn clean verify`, uploads test reports as audit artifacts, and builds the Docker image. Cucumber exercises available end-to-end platform behavior. Business lifecycle scenarios are added only when the corresponding domain endpoints exist, so CI does not report false failures for another agent's unfinished module.

Audit commands and expected evidence are recorded in `docs/evidence/README.md`; generated logs and screenshots are intentionally not committed by default.
