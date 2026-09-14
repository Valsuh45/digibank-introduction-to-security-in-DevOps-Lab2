# Local Development

## Prerequisites and Build

Use Java 17, Maven 3.9+, Git, Docker with Compose, and Python 3 for the HTTP verification script. Run commands from the repository root. Password-prompt examples below use Bash.

```bash
mvn clean verify
```

This packages the executable JAR and runs unit tests, Spring Boot integration tests, Flyway migration tests, and Cucumber transfer scenarios. Tests use H2 in PostgreSQL compatibility mode; they do not replace execution against PostgreSQL.

## Run With Docker Compose

Supply a local password without placing it in command history:

```bash
read -r -s -p "Local PostgreSQL password: " POSTGRES_PASSWORD; echo
export POSTGRES_PASSWORD
docker compose up --build -d
docker compose ps
docker compose logs digibank-app
```

Compose uses `POSTGRES_DB=digibank` and `POSTGRES_USER=digibank` unless overridden. `POSTGRES_PASSWORD` is required. Compose supplies the application's `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, and `dev` profile automatically. PostgreSQL is reachable inside the Compose network; it is not published to a host port.

Flyway owns schema creation and demonstration data; Hibernate validates the schema. On a new volume, look for successful V1/V2 migration messages. On subsequent starts, Flyway validates its recorded migrations rather than replaying seed inserts. The named PostgreSQL volume persists across `docker compose down` and subsequent startup. Existing volumes retain their initial PostgreSQL credentials; changing the shell password does not change a database role's stored password.

A local `.env` file can supply Compose values and is ignored by Git. Never copy its contents into evidence. Do not use `docker compose down -v` on data you need: it removes the database volume. For a fresh test database, use a separate Compose project name consistently with `docker compose -p <name> ...` and set `DIGIBANK_PORT=18085` if port 8080 is already in use. Compose binds to loopback by default; set `DIGIBANK_BIND_ADDRESS` explicitly only when access from other machines is intended.

## Run the JAR Against an Existing PostgreSQL Database

With a reachable database and credentials provisioned outside the repository:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/digibank
export SPRING_DATASOURCE_USERNAME=digibank
read -r -s -p "PostgreSQL password: " SPRING_DATASOURCE_PASSWORD; echo
export SPRING_DATASOURCE_PASSWORD
java -jar digibank-web/target/digibank-web-1.0.0-SNAPSHOT.jar
```

Use the packaged filename produced by Maven if the project version changes. Spring Boot does not automatically load Compose's `.env` file when the JAR is run directly.

## Application and Verification

- `http://localhost:8080/`: homepage.
- `http://localhost:8080/swagger-ui/index.html`: Swagger UI.
- `http://localhost:8080/v3/api-docs`: OpenAPI JSON.
- `http://localhost:8080/actuator/health`: health endpoint.

If `DIGIBANK_PORT` is overridden, replace port 8080 in the URLs and `--base-url` below. After the application becomes healthy, run the repeatable HTTP journey:

```bash
python3 scripts/verify-workshop1.py --base-url http://localhost:8080 --output /tmp/workshop1-http.json
```

The script creates demonstration data. Use a training database and keep the output with its commit/revision context. See [the evidence index](../evidence/README.md) for the specification mapping and captured results.

## Stop and Restart

```bash
docker compose down
docker compose up -d
```

After restarting, check health and retrieve an account created in the previous run to demonstrate persistence. Stop with `docker compose down` when finished.
