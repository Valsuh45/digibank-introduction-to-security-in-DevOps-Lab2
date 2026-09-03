# DigiBank — Introduction to Security in DevOps

Implementation repository for the **DigiBank** practical labs in the *UCC152-2: Introduction to Security in DevOps* course.

This repository contains the implemented workshop application and the supporting DevSecOps evidence for a secure, modular banking service.

## Architecture

**DigiBank is a modular monolith.**

The code is split into Maven modules so each business area has a clear boundary. Customer, account, transfer, and shared code are packaged together and run as one Spring Boot service with one PostgreSQL database schema.

This design fits Workshop 1 because it gives clean domain separation while keeping local development, testing, scanning, and deployment simple enough to review.

## Workshop Scope

The objective is to design and implement a first version of **DigiBank**, a fictional digital banking application, using a modular monolithic architecture.

The application supports:

- Customer management
- Bank account management
- Simple transfers between accounts
- Basic transaction history
- REST API documentation with Swagger / OpenAPI
- Automated unit and integration testing
- Docker-based local execution
- GitHub Actions CI automation

## Target Architecture

DigiBank is implemented as a multi-module Maven project:

```text
digibank-parent/
├── common-module/      # Shared responses, exceptions, and utilities
├── customer-module/    # Customer management
├── account-module/     # Account management
├── transfer-module/    # Transfers and transaction history
├── digibank-web/       # Spring Boot entry point, web configuration, Swagger
├── docs/               # Architecture, module, API, security, operations, and evidence docs
├── .github/workflows/  # GitHub Actions workflows
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

The application is deployed as a single Spring Boot application while maintaining clear separation between business domains.

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build tool | Maven |
| Framework | Spring Boot 3.x |
| Web API | Spring Web |
| Persistence | Spring Data JPA |
| Database | PostgreSQL |
| Validation | Jakarta / Spring Validation |
| API documentation | Springdoc OpenAPI / Swagger UI |
| Unit testing | JUnit 5 and Mockito |
| Integration testing | Cucumber |
| Containerization | Docker and Docker Compose |
| CI | GitHub Actions |

## Repository Contents

| File | Description |
|---|---|
| `README.md` | Repository overview and implementation plan |
| `LICENSE` | Project license |
| `UCC152-2 Introduction to security in DevOps - Workshop 1 EN.pdf` | Official workshop specification and implementation guide |
| `docs/architecture/` | Architecture explanation and module boundaries |
| `docs/api/` | REST API summary and endpoint map |
| `docs/modules/` | Module-by-module implementation guide |
| `docs/operations/` | Local run, Docker, CI, and future deployment notes |
| `docs/security/` | DevSecOps and security controls |
| `docs/evidence/` | Workshop evidence and implementation notes |

Each Maven module also has its own README:

- `common-module/README.md`
- `customer-module/README.md`
- `account-module/README.md`
- `transfer-module/README.md`
- `digibank-web/README.md`

## Development Status

> **Status: Implemented for Workshop 1**

The repository now includes:

- [x] Multi-module Maven project structure
- [x] PostgreSQL configuration through environment variables
- [x] Customer management
- [x] Account management
- [x] Transfers and transaction history
- [x] Validation and centralized exception handling
- [x] OpenAPI / Swagger documentation
- [x] JUnit and Cucumber tests
- [x] Docker and Docker Compose support
- [x] GitHub Actions CI with Maven verification, filesystem scanning, container scanning, and Compose smoke testing

## Prerequisites

The following tools will be required for local development:

- Java JDK 17
- Maven 3.9+
- Git
- PostgreSQL 15+ or Docker with Docker Compose
- IntelliJ IDEA or another Java IDE

Verify the local environment with:

```bash
java -version
mvn -version
git --version
docker --version
docker compose version
```

## Run Locally With Docker Compose

Start the full application stack with PostgreSQL and the Spring Boot web application:

```bash
export POSTGRES_PASSWORD=change-me-locally
docker compose up --build
```

Useful local URLs:

| URL | Purpose |
|---|---|
| `http://localhost:8080/` | Application root |
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI |
| `http://localhost:8080/v3/api-docs` | OpenAPI JSON |
| `http://localhost:8080/actuator/health` | Application health check |

Stop the stack with:

```bash
docker compose down
```

## Contribution Guidelines

- Create a dedicated branch for each feature or task.
- Keep commits focused and meaningful.
- Add or update tests with each functional change.
- Do not commit credentials, `.env` files, IDE configuration, or build artifacts.
- Ensure the Maven build and tests pass before opening a pull request.

## Reference

All implementation decisions should follow the workshop specification included in this repository:

```text
UCC152-2 Introduction to security in DevOps - Workshop 1 EN.pdf
```

For a deeper explanation of the current implementation, start with:

- `docs/architecture/overview.md`
- `docs/api/endpoints.md`
- `docs/modules/overview.md`
- `docs/security/devsecops.md`
- `docs/operations/local-development.md`
