# DigiBank Web and DevSecOps Implementation Plan

**Goal:** Record the implemented secure, reproducible Spring Boot assembly and auditable CI/container path for Workshop 1.

**Architecture:** DigiBank is a modular monolith. Domain code lives in focused Maven modules, while `digibank-web` assembles them into one Spring Boot runtime. PostgreSQL serves dev/container execution, H2 PostgreSQL mode isolates tests, Flyway owns schema state, and web-level tests verify externally visible contracts.

**Tech stack:** Java 17, Spring Boot 3.5, Spring MVC, Springdoc, Flyway, PostgreSQL, H2, JUnit 5, Cucumber 7, Docker Compose, GitHub Actions.

**Spec:** `docs/evidence/IMPLEMENTATION_DESIGN.md`

## Constraints

- Do not edit domain implementation files.
- Do not commit a database password or fallback secret.
- Preserve concurrent changes and re-check domain entities before final verification.
- Return safe API errors without stack traces or internal exception details.

## Completed Tasks

- [x] Add web contract tests for homepage, OpenAPI metadata, exception mapping, and application context.
- [x] Add the application entry point, OpenAPI configuration, homepage, and safe global exception handler.
- [x] Add test/dev configuration and Flyway migrations.
- [x] Add Cucumber transfer scenarios and JUnit suite wiring.
- [x] Add a least-privilege multi-stage Dockerfile and environment-driven Compose stack with health checks.
- [x] Add CI verification, filesystem security scanning, container security scanning, and Docker Compose smoke testing.
- [x] Align migrations with customer, account, and transfer entities.
- [x] Run `mvn clean verify`, configuration checks, Docker build/smoke checks, and GitHub Actions validation.
