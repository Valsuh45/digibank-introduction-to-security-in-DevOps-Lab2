# DigiBank Web and DevSecOps Implementation Plan

**Goal:** Deliver a secure, reproducible Spring Boot assembly and an auditable CI/container path for Workshop 1.

**Architecture:** Keep all runtime assembly and delivery concerns in `digibank-web` and root deployment files. PostgreSQL serves dev/container execution, H2 PostgreSQL mode isolates tests, Flyway owns schema state, and web-level tests verify externally visible contracts.

**Tech stack:** Java 17, Spring Boot 3.2, Spring MVC, Springdoc, Flyway, PostgreSQL, H2, JUnit 5, Cucumber 7, Docker Compose, GitHub Actions.

**Spec:** `docs/evidence/IMPLEMENTATION_DESIGN.md`

## Constraints

- Do not edit domain implementation files.
- Do not commit a database password or fallback secret.
- Preserve concurrent changes and re-check domain entities before final verification.
- Return safe API errors without stack traces or internal exception details.

## Tasks

- [ ] Add web contract tests for homepage, OpenAPI metadata, exception mapping, and application context; run them red.
- [ ] Add the application entry point, OpenAPI configuration, homepage, and safe global exception handler; run focused tests green.
- [ ] Add test/dev configuration and Flyway migrations; verify migration and context startup on H2 PostgreSQL mode.
- [ ] Add Cucumber platform feature, runner, and step definitions; verify it runs through Maven Surefire.
- [ ] Add a least-privilege multi-stage Dockerfile and environment-driven Compose stack with health checks.
- [ ] Add CI verification, Docker build, test-report artifact upload, and audit evidence instructions.
- [ ] Re-scan concurrent domain work and align migrations or add business-flow BDD only if the APIs are available.
- [ ] Run `mvn clean verify`, configuration checks, Docker static validation/build where available, and inspect the final diff.
