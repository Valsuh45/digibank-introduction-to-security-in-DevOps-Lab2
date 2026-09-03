# digibank-web

## Purpose

`digibank-web` is the runnable Spring Boot assembly module. It brings the domain modules into one application, exposes HTTP endpoints, applies configuration, runs migrations, serves the homepage, and publishes OpenAPI documentation.

## Main Responsibilities

- Start the DigiBank Spring Boot application.
- Scan all `com.m2ibank` modules for components, entities, and repositories.
- Configure PostgreSQL for local/container runs.
- Configure H2 PostgreSQL mode for tests.
- Run Flyway migrations.
- Expose Actuator health checks.
- Expose Swagger UI and OpenAPI JSON.
- Convert exceptions into safe API responses.

## Important Files

- `DigiBankApplication`: application entry point.
- `GlobalExceptionHandler`: central safe error response boundary.
- `OpenApiConfig`: OpenAPI title, version, description, and contact.
- `application.yml`: runtime configuration and security-safe error settings.
- `db/migration/`: Flyway schema and seed data.
- `static/index.html`: simple landing page with documentation links.

## Security Notes

Runtime database credentials come from environment variables. The application disables public error details such as exception names and stack traces. The global exception handler logs unexpected failures internally and returns a generic message to clients.

## Verification

Run the web module and required dependencies through the full project build:

```bash
mvn clean verify
```
