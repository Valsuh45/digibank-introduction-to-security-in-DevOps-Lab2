# Container and CI

## Docker Image

The Dockerfile uses a multi-stage build:

1. A Maven build image compiles the project and packages the Spring Boot jar.
2. A smaller Java 17 Alpine runtime image runs only the packaged application.

The runtime image upgrades Alpine packages and runs the app as an unprivileged `digibank` user.

## Docker Compose

Compose starts PostgreSQL and the DigiBank application together. PostgreSQL must be healthy before the app starts.

Database credentials come from environment variables. `POSTGRES_PASSWORD` is required so the repository does not contain a default database password.

## GitHub Actions

The CI pipeline runs on pushes and pull requests to `main`.

Current gates:

- Maven verification.
- Filesystem security scan.
- Container security scan.
- Docker Compose smoke test.

## Next Deployment Work

The next useful improvement is publishing a scanned Docker image to GitHub Container Registry. After that, the team can add environment-specific deployment documentation or a deployment workflow.
