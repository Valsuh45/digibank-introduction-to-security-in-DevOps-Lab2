# Container and CI

## Docker Image

The Dockerfile uses a multi-stage build:

1. A Maven build image compiles the project and packages the Spring Boot jar.
2. A smaller Java 17 Alpine runtime image runs only the packaged application.

The runtime image upgrades Alpine packages and runs the app as an unprivileged `digibank` user.

## Docker Compose

Compose starts PostgreSQL and the DigiBank application together. PostgreSQL must be healthy before the app starts. Both health checks are defined in `docker-compose.yml`; the Dockerfile does not define an application `HEALTHCHECK`.

Database credentials come from environment variables. `POSTGRES_PASSWORD` is required so the repository does not contain a default database password.

## GitHub Actions

The CI pipeline runs on pushes and pull requests to `main`.

Current gates:

- Maven verification (`verify`).
- PMD aggregate quality gate (`pmd`).
- SpotBugs / Find Security Bugs (`static-analysis`).
- Filesystem security scan.
- Container security scan.
- Docker Compose smoke test.
- Container and dependency security pipeline (OWASP Dependency-Check + Docker build + Trivy image scan + artifact publishing).

## Dependency and Container Security Pipeline

`.github/workflows/digibank-security-pipeline.yml` industrializes the Workshop 4 controls. It:

1. rebuilds and tests DigiBank (`mvn clean verify`);
2. exports the Maven dependency tree;
3. runs OWASP Dependency-Check on the dependencies (fails on CVSS >= 7);
4. builds the Docker image;
5. scans the image with Trivy (HIGH/CRITICAL, fails on findings);
6. publishes the dependency-check report, dependency tree, and image metadata as artifacts.

The `NVD_API_KEY` is injected from GitHub repository secrets and is never committed.

## Local Verification

The Workshop 4 local verification sequence is captured in
`container-security/scripts/verify-workshop4.sh`. It runs the Maven build, exports the
dependency tree, runs OWASP Dependency-Check, builds the image, exports image metadata, and
scans the image with Trivy.

A configured job is not evidence of a successful run. Record the Actions run URL and commit in the [evidence index](../evidence/README.md) after inspecting its results.

## Next Deployment Work

The next useful improvement is publishing a scanned Docker image to GitHub Container Registry. After that, the team can add environment-specific deployment documentation or a deployment workflow.
