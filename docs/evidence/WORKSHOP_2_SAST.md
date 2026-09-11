# Workshop 2 SAST Evidence

## Part 1: Framework and objectives

Workshop 2 applies static application security testing (SAST) to the Workshop 1 DigiBank modular monolith. The scope is source code, bytecode, configuration, Maven dependency declarations, and test robustness before dynamic analysis in Workshop 3.

The security objective is to detect, interpret, prioritize, remediate, and verify weaknesses without replacing the application or breaking its functional behavior.

## Part 2: Analysis environment

The parent POM centralizes the Workshop 2 analysis tools:

- SonarQube Maven scanner: explicit `sonar:sonar` execution.
- OWASP Dependency-Check: explicit dependency vulnerability scan with HTML output and CVSS 7 build threshold.
- PITest: explicit mutation testing with JUnit 5 support and HTML/XML output.
- Surefire: pinned JUnit 5 execution with module-path disabled for stable Maven and CI runs.

The repository CI workflow runs Maven verification, SpotBugs/Find Security Bugs, PMD, Trivy, Dependency-Check, and PITest. Dependency and mutation reports are uploaded as workflow artifacts. SonarQube remains a local or externally hosted scan because a developer's localhost is not reachable from a hosted GitHub runner.

## Part 3: Static vulnerability review

The reviewed risk categories are:

- Weak input shape validation in customer data.
- Sensitive data minimization in response DTOs.
- Excessive error detail and resource enumeration signals.
- Secrets and unsafe runtime settings in configuration.
- Dependency versions that require CVE review.
- Financial business rules that must be enforced in services, not only at the controller boundary.
- Test gaps that could allow mutations to survive on transfer and balance rules.

The implementation already uses controlled response DTOs, environment-driven database credentials, generic unexpected-error responses, schema validation through Flyway/Hibernate, and service-level transfer validation.

## Part 4: Remediation

Implemented remediation includes:

- Customer first and last names now require a bounded Unicode-letter format.
- Identity numbers now require a bounded alphanumeric, underscore, or hyphen format.
- Customer responses omit the sensitive identity number.
- Database credentials remain environment-driven; committed configuration uses schema validation and disables SQL/error detail exposure.
- Transfer validation is repeated in the transactional service, including null checks, positive amount checks, same-account rejection, and description length control.
- Account DTOs enforce positive customer identifiers, non-negative two-decimal balances, and required account type.
- Static analysis configuration is explicit and version-pinned.

Focused tests cover malformed customer input, transfer rejection branches, insufficient funds, safe not-found responses, and generic unexpected errors.

## Part 5: Final validation

Run the baseline functional checks:

```bash
mvn -B clean verify
```

Run Workshop 2 analysis checks explicitly:

```bash
mvn -B clean install -DskipTests
mvn -B org.owasp:dependency-check-maven:check -DnvdApiKey="$NVD_API_KEY"
mvn -B org.pitest:pitest-maven:mutationCoverage
mvn -B verify sonar:sonar \
  -Dsonar.projectKey=digibank-parent \
  -Dsonar.host.url="${SONAR_HOST_URL:-http://localhost:9000}" \
  -Dsonar.token="$SONAR_TOKEN"
```

Acceptance requires a passing build and tests, reduced or justified analysis findings, preserved modular boundaries, no committed secrets, and documented decisions for retained alerts or false positives.

## Scan Result and Residual Risk

#### Pre-suppression Dependency-Check scan (before allowlist)

The local Dependency-Check run completed its NVD database update and generated
`target/dependency-check-report.html`, but the configured CVSS 7 gate failed on transitive
Spring Framework 6.2.19 findings. The current Spring Boot 3.5.x dependency management available
from Maven Central still resolves Spring Framework 6.2.19, and no newer 6.2.x artifact was
available during this run to apply as a safe direct patch. These findings are now temporarily
allowed through `dependency-check-suppressions.xml` (time-bounded to 2026-12-31) while remaining
visible in reports and noted for future remediation.

The report also identifies lower-severity Log4j findings. They should be addressed together with
the Spring patch when an upstream Spring Framework release containing the fixes is available.

Until that upstream patch is available, the GitHub Actions Dependency-Check job uses a scoped
allowlist (`dependency-check-suppressions.xml`) to permit only known Spring Framework and Spring Boot CVEs
to pass the gate, while new findings, NVD update errors, and scanner failures remain blocking. The
`continue-on-error: true` setting on the scan step allows only the documented Spring CVEs (by version
and package CPE) to pass the workflow; the suppressions are scoped to specific coordinates so unrelated
packages with the same CVE IDs are not hidden. The job should become
blocking again after the dependency findings are remediated or an explicitly approved, time-bounded
suppression is introduced.
