# DigiBank — Container & Dependency Security Review (Workshop 4)

> Analysis note for the container and dependency security workshop. This file records the
> initial observations, the prioritized findings, and the remediation decisions so the work is
> traceable and reproducible.

## Analysis metadata

- **Date of analysis:** 2026-09-11
- **Project version:** `1.0.0-SNAPSHOT` (digibank-parent)
- **Image name:** `digibank:local` / `digibank:ci`
- **Base image (runtime):** `eclipse-temurin:17-jre-alpine`
- **Build image:** `maven:3.9-eclipse-temurin-17`

## Initial observations

### Image size and layers

- The Dockerfile is a **multi-stage build**: a Maven build stage produces the jar, and a slim
  Java 17 Alpine runtime stage copies only the runnable artifact. Build tools and source code
  are not present in the final image.
- The runtime image upgrades Alpine packages (`apk upgrade --no-cache`) and runs as an
  unprivileged `digibank` user.

### Dependencies

- The parent `pom.xml` centralizes dependency versions (Spring Boot, Springdoc, PostgreSQL
  driver, Tomcat, Jackson, etc.) so updates are applied consistently.
- OWASP Dependency-Check is configured to scan direct and transitive Maven dependencies and
  fails the build on CVSS >= 7.
- Dependabot monitors Maven and GitHub Actions dependencies weekly.

### Secrets

- Database credentials are injected through environment variables
  (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`).
- No default database password is committed. `docker-compose.yml` requires `POSTGRES_PASSWORD`.
- `.dockerignore` excludes `.env`, logs, Git metadata, and local reports from the build context.

### Privileges / attack surface

- The container runs as a non-root user.
- Only port 8080 is exposed.
- Swagger / OpenAPI is disabled by default and enabled only under the `dev` (and `test`) profile.

## Sensitive dependencies to monitor

- Populate this section from the OWASP Dependency-Check report
  (`target/dependency-check-report.html`) and `mvn dependency:tree`.
- For each finding record: component, direct/transitive, CVSS, whether it is used in a
  sensitive path, and the remediation decision (update / exclude / justify).

## Points to address

- [ ] Run `mvn org.owasp:dependency-check-maven:aggregate` and review the HTML report.
- [ ] Confirm no CVSS >= 7 findings remain, or document accepted risks in
      `dependency-check-suppressions.xml`.
- [ ] Rebuild the image and re-scan with Trivy (HIGH/CRITICAL) in CI.
- [ ] Confirm Swagger is reachable only under the `dev` profile.
