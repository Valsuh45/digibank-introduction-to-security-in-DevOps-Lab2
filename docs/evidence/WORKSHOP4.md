# Workshop 4 — Container & Dependency Security Evidence

**Goal:** Record the implemented container and dependency security controls for Workshop 4,
covering dependency analysis, container hardening, secret management, and CI/CD automation.

## What was implemented

### Dependency analysis (OWASP Dependency-Check)

- Added the OWASP Dependency-Check Maven plugin to the parent `pom.xml`
  (`org.owasp:dependency-check-maven`, version `12.1.0`).
- Configuration: HTML report, `failBuildOnCVSS=7`, skip provided/test scopes, NVD API key read
  from the `NVD_API_KEY` environment variable, OSS Index analyzer disabled.
- Added `dependency-check-suppressions.xml` for narrowly-scoped, documented suppressions.
- The plugin runs explicitly (`mvn org.owasp:dependency-check-maven:aggregate`) rather than being
  bound to `verify`, consistent with how SpotBugs and PMD are handled, so a normal build stays
  fast and offline-friendly.

### Container hardening

- Multi-stage Dockerfile: Maven build stage + slim `eclipse-temurin:17-jre-alpine` runtime.
- Runtime image upgrades Alpine packages and runs as an unprivileged `digibank` user.
- `.dockerignore` excludes Git metadata, logs, `.env`, and local reports from the build context.
- `docker-compose.yml` requires `POSTGRES_PASSWORD` and injects credentials via environment
  variables (no committed default password).

### Swagger / OpenAPI exposure

- OpenAPI / Swagger is disabled by default and enabled only under the `dev` profile (and the
  `test` profile for the documentation contract tests).

### CI/CD automation

- Added `.github/workflows/digibank-security-pipeline.yml` which rebuilds the project, runs
  OWASP Dependency-Check, builds the Docker image, scans it with Trivy (HIGH/CRITICAL), and
  publishes the dependency-check report, dependency tree, and image metadata as artifacts.
- `NVD_API_KEY` is injected from GitHub repository secrets.

## Working files

- `container-security/notes/container-review.md` — analysis note and findings log.
- `container-security/reports/` — generated reports (not committed by default).
- `container-security/scripts/verify-workshop4.sh` — local verification sequence.

## Verification commands

```bash
mvn clean install
mvn dependency:tree > dependency-tree.txt
mvn org.owasp:dependency-check-maven:aggregate
docker build -t digibank:local .
docker image inspect digibank:local > docker-image-inspect.json
docker history digibank:local --no-trunc > docker-image-history.txt
trivy image --severity HIGH,CRITICAL --ignore-unfixed --exit-code 1 digibank:local
```

## Acceptance criteria

- [x] `mvn clean install` passes (no functional regressions).
- [ ] OWASP Dependency-Check report is generated and no CVSS >= 7 findings remain (or are
      documented in the suppressions file).
- [x] Docker image builds and runs as a non-root user.
- [x] Swagger is reachable in `dev` and the real OpenAPI/Swagger routes return 404 in `ci` (default disabled; test profile intentionally enabled).
- [ ] The security pipeline runs in GitHub Actions and publishes its artifacts.

The integrated branch and refreshed verification are recorded in [security-gap fixes](security-gap-fixes.md). This integration does not publish or merge the branch into remote main.
