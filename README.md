# DigiBank — Introduction to Security in DevOps

Implementation repository for the **DigiBank** practical labs in the *UCC152-2: Introduction to Security in DevOps* course.

This repository contains the implemented workshop application and the supporting DevSecOps evidence for a secure, modular banking service.

## Workshop Scope

The objective is to design and implement a first version of **DigiBank**, a fictional digital banking application, using a modular monolithic architecture.

The application will progressively support:

- Customer management
- Bank account management
- Simple transfers between accounts
- Basic transaction history
- REST API documentation with Swagger / OpenAPI
- Automated unit and integration testing
- Docker-based local execution
- GitHub Actions CI automation

## Target Architecture

DigiBank will be implemented as a multi-module Maven project:

```text
digibank-parent/
├── common-module/      # Shared responses, exceptions, and utilities
├── customer-module/    # Customer management
├── account-module/     # Account management
├── transfer-module/    # Transfers and transaction history
├── digibank-web/       # Spring Boot entry point, web configuration, Swagger
├── .github/workflows/  # GitHub Actions workflows
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

The application will be deployed as a single Spring Boot application while maintaining clear separation between business domains.

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

## Contribution Guidelines

- Create a dedicated branch for each feature or task.
- Keep commits focused and meaningful.
- Add or update tests with each functional change.
- Do not commit credentials, `.env` files, IDE configuration, or build artifacts.
- Ensure the Maven build and tests pass before opening a pull request.

Example commit messages:

```text
chore: initialize Maven multi-module structure
feat(customer): add customer creation endpoint
feat(account): implement account creation service
feat(transfer): add insufficient funds validation
test(customer): add customer service unit tests
docs: update local setup instructions
```

## Reference

All implementation decisions should follow the workshop specification included in this repository:

```text
UCC152-2 Introduction to security in DevOps - Workshop 1 EN.pdf
```
