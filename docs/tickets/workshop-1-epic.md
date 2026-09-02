# Workshop 1 Epic: Complete Secure DigiBank Foundation

## Purpose
Build the full Workshop 1 DigiBank deliverable as a clean, modular, testable Spring Boot banking application that is ready for later SAST, DAST, dependency, and container security workshops.

## Team Split
This epic is split across five parallel workstreams:

1. Foundation and shared security baseline
2. Customer management domain
3. Bank account management domain
4. Transfer engine, transaction history, and transaction security
5. Web bootstrap, tests, Docker, CI, and audit evidence

## Definition of Done
- `mvn clean verify` succeeds from the repository root.
- The application starts locally and exposes the DigiBank homepage.
- Swagger UI exposes customer, account, and transfer APIs.
- PostgreSQL schema is managed through Flyway migrations.
- Docker Compose starts PostgreSQL and the Spring Boot application.
- GitHub Actions validates the project on push and pull request.
- Unit tests and Cucumber/integration tests cover the core banking flows.
- Error handling is centralized and does not expose stack traces or internal implementation details.
- Configuration uses environment variables for credentials and runtime-specific settings.
- No secrets, `.env` files, IDE files, build artifacts, or credentials are committed.

## Required Audit Proofs
- Root `mvn clean verify` output showing success.
- `docker compose up --build` output showing PostgreSQL health, Flyway migrations, and application startup.
- Screenshot of `http://localhost:8080/`.
- Screenshot of Swagger UI.
- Sample API call evidence for customer creation, account creation, and transfer execution.
- Screenshot of successful GitHub Actions workflow.
