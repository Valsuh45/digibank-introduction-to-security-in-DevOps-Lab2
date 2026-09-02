# Ticket 5: Web Bootstrap, QA, Docker, CI, and Audit Evidence

## Owner
Team member 5

## Scope
Own `digibank-web`, `Dockerfile`, `docker-compose.yml`, `.github/workflows`, integration tests, and final evidence collection.

## Tasks
- Create `DigiBankApplication` as the Spring Boot entry point.
- Configure package scanning across all `com.m2ibank` modules.
- Add `application.yml` with environment-variable-based configuration and profiles.
- Add centralized `GlobalExceptionHandler`.
- Return safe `ApiResponse.error(...)` responses for validation, business exceptions, not-found errors, and generic failures.
- Add OpenAPI/Swagger configuration.
- Add a simple homepage that presents DigiBank and links to Swagger UI and OpenAPI JSON.
- Add Flyway migrations:
  - `V1__init_schema.sql`
  - `V2__seed_data.sql`
- Add Cucumber feature(s), runner, and step definitions for core flows.
- Add `Dockerfile` for the Spring Boot application.
- Add `docker-compose.yml` for PostgreSQL and DigiBank app.
- Add GitHub Actions workflow that runs `mvn clean verify` and builds the Docker image.
- Collect the final audit evidence required by the workshop.

## Security Acceptance Criteria
- No database password or secret is hard-coded in committed configuration.
- Runtime configuration is supplied through environment variables.
- Generic exception responses do not leak stack traces or internal details.
- Flyway gives a reproducible database state for testing and grading.
- Docker Compose uses explicit service names, health checks, and least-surprising ports.
- CI runs tests automatically on push and pull request.

## Verification
- Run `mvn clean verify`.
- Run `docker compose up --build`.
- Visit `http://localhost:8080/`.
- Visit Swagger UI.
- Execute sample API calls for customer creation, account creation, and transfer execution.

## Evidence
- Root Maven success output.
- Docker Compose startup output.
- Homepage screenshot.
- Swagger UI screenshot.
- Sample API call responses.
- Successful GitHub Actions screenshot.
