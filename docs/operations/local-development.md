# Local Development

## Prerequisites

- Java 17
- Maven 3.9+
- Docker with Docker Compose
- Git

## Run Tests

```bash
mvn clean verify
```

This runs unit tests, Spring Boot integration tests, Flyway migration tests, and Cucumber transfer scenarios.

## Run With Docker Compose

Set a local database password in your shell before starting Compose:

```bash
export POSTGRES_PASSWORD=change-me-locally
docker compose up --build
```

The password is intentionally not committed to the repository.

## Useful URLs

- `http://localhost:8080/`
- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`
- `http://localhost:8080/actuator/health`

## Stop The Stack

```bash
docker compose down
```
