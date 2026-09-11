# DigiBank — Introduction to Security in DevOps
## Complete Workshop Presentation (Labs 1–4)

**Course:** UCC152-2 — Introduction to Security in DevOps
**Project:** DigiBank — a fictional digital banking application
**Architecture:** Modular Monolith (Spring Boot 3.5, Java 17, PostgreSQL, Maven multi-module)
**Repository:** `Valsuh45/digibank-introduction-to-security-in-DevOps-Lab2`

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Lab 1 — Secure Design & Implementation](#lab-1--secure-design--implementation)
3. [Lab 2 — Static Application Security Testing (SAST)](#lab-2--static-application-security-testing-sast)
4. [Lab 3 — Dynamic Application Security Testing (DAST)](#lab-3--dynamic-application-security-testing-dast)
5. [Lab 4 — Container & Dependency Security](#lab-4--container--dependency-security)
6. [The Complete DevSecOps Pipeline](#the-complete-devsecops-pipeline)
7. [Overall Challenges & Solutions](#overall-challenges--solutions)
8. [Conclusion](#conclusion)

---

## Project Overview

DigiBank is a fictional digital banking application built across four progressive DevSecOps
workshops. Each workshop adds a new layer of security analysis to the same codebase, following the
**"Shift Left"** philosophy — moving security earlier and earlier in the software lifecycle.

| Lab | Security Layer | Question it answers |
|---|---|---|
| **Lab 1** | Secure design & implementation | "Can we build a clean, testable, containerized base?" |
| **Lab 2** | SAST (Static) | "Is the *source code* secure?" |
| **Lab 3** | DAST (Dynamic) | "Is the *running application* secure?" |
| **Lab 4** | Container & Dependencies | "Is what we *import, package, and ship* secure?" |

**The application supports:** customer management, bank account management, transfers between
accounts, transaction history, REST API documentation (Swagger/OpenAPI), automated testing, Docker
containerization, and GitHub Actions CI/CD.

### Technology Stack

| Area | Technology |
|---|---|
| Language | Java 17 |
| Build tool | Maven (multi-module) |
| Framework | Spring Boot 3.5 |
| Persistence | Spring Data JPA + PostgreSQL (dev) / H2 (tests) |
| DB migrations | Flyway |
| API docs | Springdoc OpenAPI / Swagger UI |
| Unit tests | JUnit 5 + Mockito |
| Integration tests | Cucumber (BDD) |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |

### Module Structure

```text
digibank-parent/
├── common-module/      # shared responses, exceptions, constants
├── customer-module/    # customer registration & lookup
├── account-module/     # account creation & balance state
├── transfer-module/    # transfers & transaction history
├── digibank-web/       # Spring Boot entry point, config, Swagger, error handling
├── docs/               # architecture, security, operations, evidence
├── dast/               # Lab 3 DAST artifacts (Postman, ZAP)
├── container-security/ # Lab 4 container/dependency notes & scripts
├── .github/workflows/  # CI/CD pipelines
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

---

## Lab 1 — Secure Design & Implementation

### Complete Description

Lab 1 is the foundation. It builds the **first executable version of DigiBank** as a **modular
monolith** — a single Spring Boot application split into clean Maven modules by business domain.
The goal is not a disposable prototype but a **solid, stable, documented, testable base** that the
later security workshops can build on without reconstruction.

The architecture is deliberately a **modular monolith** rather than microservices: it gives clean
domain boundaries while keeping local development, testing, scanning, and deployment simple enough
for a pedagogical setting.

### Learning Objectives

- **A1.1** — Explain DigiBank's role in the course and justify the modular-monolith choice.
- **A1.2** — Create a parent Maven Spring Boot project with clear module separation.
- **A1.3** — Build the fundamental layers: entities, repositories, services, controllers, DTOs,
  validations, exceptions.
- **A1.4** — Expose consistent REST APIs for core operations.
- **A1.5** — Produce an index view with access to Swagger/OpenAPI documentation.
- **A1.6** — Write unit tests (JUnit) and integration scenarios (Cucumber).
- **A1.7** — Prepare the app for Docker execution and GitHub Actions automation.
- **A1.8** — Deliver a sound base for future SAST, DAST, and dependency/container workshops.

### Steps Involved

1. **Project setup** — created the Maven parent POM with Java 17 and Spring Boot 3.5, then added
   the five modules (`common`, `customer`, `account`, `transfer`, `digibank-web`).
2. **Domain implementation** — for each module, implemented the layered structure:
   - **Entities** (JPA) — `Customer`, `BankAccount`, `Transfer` with status enums.
   - **Repositories** (Spring Data JPA).
   - **Services** — business logic (duplicate protection, server-generated account numbers,
     balance rules, transactional transfers).
   - **Controllers** — REST endpoints under `/api/v1/...`.
   - **DTOs** — request/response records with Jakarta Bean Validation.
   - **Exceptions** — domain exceptions (`BusinessException`, `ResourceNotFoundException`,
     `InsufficientBalanceException`, etc.).
3. **Central error handling** — a `GlobalExceptionHandler` (`@RestControllerAdvice`) that returns
   consistent `ApiResponse` envelopes without leaking stack traces.
4. **Database** — Flyway migrations (`V1__init_schema.sql`, `V2__seed_data.sql`) create the schema
   and deterministic demo data; PostgreSQL for dev/container, H2 (PostgreSQL mode) for tests.
5. **Index view + Swagger** — a static homepage and Springdoc OpenAPI/Swagger UI.
6. **Testing** — JUnit 5 + Mockito unit tests per module, plus Cucumber BDD scenarios for the
   transfer workflow.
7. **Containerization** — a multi-stage Dockerfile and a `docker-compose.yml` that runs PostgreSQL
   + the app with health checks.
8. **CI** — a GitHub Actions workflow running Maven verification.
9. **Documentation** — READMEs, architecture, API, module, and operations docs.

### Required Tools / Environment

- Java JDK 17, Maven 3.9+, Git
- IntelliJ IDEA (or another Java IDE)
- PostgreSQL 15+ or Docker with Docker Compose
- Docker / Docker Compose

### Expected Results

- A multi-module Maven project that compiles and runs.
- REST APIs for customers, accounts, and transfers.
- Swagger UI at `/swagger-ui/index.html` and OpenAPI JSON at `/v3/api-docs`.
- Passing JUnit + Cucumber tests.
- A runnable Docker image and a Docker Compose stack.
- A GitHub Actions pipeline that validates the build.

### Challenges Encountered & Solutions

| Challenge | Solution |
|---|---|
| Keeping domain boundaries clean in one runtime | Used a modular monolith: each domain is a Maven module; only `digibank-web` assembles them. |
| Consistent, safe error responses | Centralized `GlobalExceptionHandler` returning a uniform `ApiResponse` envelope. |
| Schema drift between entities and DB | Flyway owns migrations; Hibernate uses `ddl-auto: validate` so drift fails fast. |
| Testing without a local PostgreSQL | H2 in PostgreSQL-compatibility mode for the test profile. |
| Reproducible local environment | Docker Compose with health checks and environment-driven configuration. |

---

## Lab 2 — Static Application Security Testing (SAST)

### Complete Description

Lab 2 applies **Static Application Security Testing** to DigiBank's source code, bytecode,
configuration, and Maven dependencies — **without running the application**. It detects, interprets,
prioritizes, remediates, and verifies weaknesses, following the **Shift Left** principle (find and
fix issues as early as possible).

The scope covers Java code, REST controllers, services, entities, DTOs, exceptions, validations,
application configuration, and Maven dependency declarations. It does **not** cover runtime behavior
(that is Lab 3).

### Learning Objectives

- **A2.1** — Explain SAST's role in DevSecOps and link it to Shift Left.
- **A2.2** — Analyze DigiBank's source code from a security perspective.
- **A2.3** — Identify main categories of static vulnerabilities.
- **A2.4** — Use tools such as SonarQube, OWASP Dependency-Check, and other static quality
  mechanisms.
- **A2.5** — Interpret results and distinguish critical alerts from false positives.
- **A2.6** — Correct detected vulnerabilities in code and configuration.
- **A2.7** — Justify remediations from a technical and security perspective.
- **A2.8** — Deliver a more robust version ready for dynamic testing (Lab 3).

### Tools Used

- **SonarQube** — primary static code analysis (code quality + security).
- **OWASP Dependency-Check** — scans Maven dependencies for known CVEs.
- **PITest** — mutation testing to measure test robustness.
- **SpotBugs + Find Security Bugs** — Java bug patterns + security-sensitive code patterns.
- **PMD** — static code quality gate.
- **JUnit 5** — verify remediations don't break behavior.

### Steps Involved

1. **Environment preparation** — configured the analysis tools in the parent POM (SonarQube
   scanner, OWASP Dependency-Check, PITest, Surefire).
2. **Static vulnerability review** — examined risk categories:
   - Weak input shape validation in customer data.
   - Sensitive data minimization in response DTOs.
   - Excessive error detail / resource enumeration signals.
   - Secrets and unsafe runtime settings in configuration.
   - Dependency versions requiring CVE review.
   - Financial business rules that must be enforced in services.
   - Test gaps allowing mutations to survive on critical rules.
3. **Remediation** — fixed the identified weaknesses (see below).
4. **Revalidation** — reran the build, tests, and analysis tools to confirm improvements.

### Remediations Implemented

- **Secrets in configuration** — database credentials moved to environment variables; no committed
  default password; SQL/error detail exposure disabled.
- **Input validation** — customer first/last names require a bounded Unicode-letter format;
  identity numbers require a bounded alphanumeric/underscore/hyphen format (`@Pattern`).
- **Data minimization** — customer responses omit the sensitive identity number.
- **Error handling** — `GlobalExceptionHandler` returns generic messages; no stack traces or
  internal details leaked.
- **Business rules in services** — transfer validation repeated in the transactional service
  (null checks, positive amount, same-account rejection, description length); account DTOs enforce
  positive customer IDs and non-negative balances.
- **Dependency updates** — versions pinned and reviewed; known Spring CVEs documented in
  `dependency-check-suppressions.xml` (time-bounded).
- **Test strengthening** — added focused tests for malformed input, transfer rejection branches,
  insufficient funds, safe not-found responses, and generic errors (improves PITest mutation score).

### Expected Results

- A passing build and test suite.
- Reduced or justified static-analysis findings.
- Preserved modular boundaries (no architectural regression).
- No committed secrets.
- Documented decisions for retained alerts / false positives.

### Challenges Encountered & Solutions

| Challenge | Solution |
|---|---|
| SonarQube not reachable from a hosted GitHub runner | SonarQube runs locally/externally; CI runs the other gates (SpotBugs, PMD, Dependency-Check, PITest). |
| Dependency-Check flagged transitive Spring Framework 6.2.19 CVEs with no safe newer patch available | Time-bounded suppressions in `dependency-check-suppressions.xml` (until 2026-12-31), kept visible in reports. |
| Mutation testing revealed weak coverage on critical rules | Added explicit tests for debit/transfer edge cases to kill surviving mutants. |
| Distinguishing real alerts from false positives | Narrowly-scoped suppressions with documented justification. |

---

## Lab 3 — Dynamic Application Security Testing (DAST)

### Complete Description

Lab 3 applies **Dynamic Application Security Testing** — observing the **running application** from
the outside, like an attacker, by sending real HTTP requests. It complements Lab 2's static analysis
by confirming at runtime which weaknesses are actually observable and exploitable.

The app is started against PostgreSQL, the REST surface is mapped, targeted scenarios are built in
**Postman**, replayed automatically with **Newman**, and observed with **OWASP ZAP**. Each finding is
traced back to a code/configuration choice, remediated, and revalidated.

### Learning Objectives

- Explain DAST's role and how it extends the DevSecOps logic.
- Build consistent dynamic test scenarios with Postman.
- Supplement observation with OWASP ZAP.
- Link findings to DTOs, services, handlers, and configuration.
- Write corrections and revalidate everything (Postman/Newman + ZAP).

### Tools Used

- **Postman** — build and send HTTP request scenarios.
- **Newman** — run Postman collections from the command line (automation).
- **OWASP ZAP** — automated web scanner / baseline scan.
- **curl** — targeted manual tests.
- **Docker Compose** — reproducible runtime environment.

### Steps Involved

1. **Prepare the DAST environment** — start DigiBank + PostgreSQL, map the API with Swagger/curl.
2. **Build dynamic test scenarios** in Postman (functional, input-validation, error-handling,
   information-exposure, remediation checks).
3. **Observe with OWASP ZAP** — baseline scan to compare the visible surface before/after.
4. **Identify dynamic vulnerabilities** — see table below.
5. **Remediate** the confirmed issues.
6. **Revalidate** — replay the Postman collection with Newman and rerun ZAP.
7. **Automate** — add a DAST CI pipeline (`.github/workflows/digibank-dast.yml`).

### Dynamic Findings & Remediations

| # | Observed weakness (runtime) | Root cause | Remediation |
|---|---|---|---|
| 1 | Error responses echoed the requested id (`Customer not found with id: 99999`) and business state (`A customer with this email already exists`, `Insufficient balance`) — enables enumeration/probing. | `GlobalExceptionHandler` returned `exception.getMessage()`. | Handler returns generic messages (`Resource not found`, `Request could not be processed`); real reason logged server-side. |
| 2 | Not-found message embedded the raw identifier. | `CustomerServiceImpl.getCustomerById` built the message from the id. | Message made generic (`Customer not found`); no identifier in public text. |
| 3 | Duplicate creation revealed whether an email/identity already existed. | Distinct, revealing messages. | Combined into a single generic message. |
| 4 | Identity number had no structural constraint (`???` accepted). | Only `@Size` on the field. | Added `@Pattern` (`^[A-Z0-9][A-Z0-9-]{3,99}$`). |
| 5 | Interactive API documentation exposed in every profile. | `springdoc` had no environment gating. | `dev` keeps Swagger enabled; `prod` and `ci` disable it. |
| 6 | Customer responses reviewed for over-exposure. | Response DTO historically included identity number. | Confirmed/kept `CustomerResponseDto` without `identityNumber`; test asserts it is absent. |

Already-defensive areas were verified rather than changed: transfer amount validation, null/zero/
same-account rejection, and negative-balance rejection.

### Expected Results

- Essential business flows still work (functional regression check).
- Prioritized dynamic weaknesses reduced/removed.
- Remediations consistent with the architecture and linked to code/config.
- Reproducible evidence (Postman collection, environment, ZAP notes, CI artifacts).

### Challenges Encountered & Solutions

| Challenge | Solution |
|---|---|
| Making dynamic tests reproducible | Versioned Postman collection + Newman CLI with HTML/JSON reports. |
| ZAP scans can be noisy / slow | ZAP runs as an observation-level (non-blocking) job in CI; Newman is the blocking gate. |
| Fresh database needed per run | Collection creates its own data; re-run on a clean DB (`docker compose down -v`). |
| Confirming remediation actually changed behavior | Replayed the collection and compared ZAP before/after. |

---

## Lab 4 — Container & Dependency Security

### Complete Description

Lab 4 shifts focus from the code and running app to the **software supply chain** — the third-party
libraries, the Docker image, configuration files, and the CI/CD pipeline. A program can be perfectly
written yet still insecure because of a vulnerable library, a bloated/root-running container, a
leaked secret, or no automated checks.

The workshop detects, analyzes, prioritizes, remediates, and **automates** security risks related to
containers and dependencies.

### Learning Objectives

- **A4.1** — Explain security risks of container images and third-party libraries.
- **A4.2** — Identify files/artifacts/configurations involved in containerized build/execution.
- **A4.3** — Analyze software dependencies for old, sensitive, or vulnerable components.
- **A4.4** — Examine the Dockerfile, image, and execution parameters for attack-surface choices.
- **A4.5** — Correct weaknesses via updates, attack-surface reduction, and hardening.
- **A4.6** — Justify remediations from a security perspective.
- **A4.7** — Implement a CI/CD pipeline to replay dependency and container checks.
- **A4.8** — Produce technical deliverables with analysis, corrections, and evidence.

### Tools Used

- **OWASP Dependency-Check** — Maven dependency vulnerability scan (CVSS ≥ 7 gate).
- **Trivy** — Docker image scan (vulnerabilities, secrets, misconfigurations).
- **Docker / Docker Compose** — build and run the containerized artifact.
- **GitHub Actions** — automate the checks.
- **Dependabot** — weekly automated dependency updates.

### Steps Involved

1. **Prepare the environment** — verify the build, Dockerfile, `.dockerignore`,
   `docker-compose.yml`, and parent POM.
2. **Identify vulnerabilities** — review Maven dependencies (Dependency-Check + `dependency:tree`),
   the Dockerfile/image (size, layers, base image), secrets, and privileges.
3. **Remediate** — see below.
4. **Automate** — add the container & dependency security pipeline.
5. **Final validation** — rerun build, tests, Dependency-Check, Docker build, and Trivy.

### Remediations Implemented

- **Overly large / overexposed Docker image** — multi-stage Dockerfile: a Maven build stage produces
  the jar, and a slim `eclipse-temurin:17-jre-alpine` runtime stage copies only the runnable
  artifact (image reduced to **254MB** vs 754MB single-stage).
- **Container running as root** — added a non-root `digibank` user; the app runs as `uid=100`
  (verified with `docker run --entrypoint id`).
- **Secrets in containerized config** — credentials injected via environment variables
  (`SPRING_DATASOURCE_*`); no committed default password; `.dockerignore` excludes `.env`, logs,
  and local reports from the build context.
- **Swagger exposure** — OpenAPI/Swagger **disabled by default**, enabled only under the `dev`
  (and `test`) profiles; disabled in `prod` and `ci`.
- **Lack of automated dependency/image checking** — added OWASP Dependency-Check to the parent POM
  (CVSS ≥ 7 gate) and a dedicated security pipeline with Trivy image scanning.
- **Verification discipline** — a local script (`container-security/scripts/verify-workshop4.sh`)
  replays the full sequence.

### Expected Results

- A passing build and test suite (no functional regressions).
- Reduced/justified dependency findings (Dependency-Check report).
- A slim, non-root, secret-free Docker image.
- Swagger reachable only in development.
- A CI pipeline that rebuilds, scans dependencies, builds the image, scans it with Trivy, and
  publishes reports as artifacts.

### Challenges Encountered & Solutions

| Challenge | Solution |
|---|---|
| Dependency-Check slow without an NVD API key | Use a free `NVD_API_KEY` (GitHub secret) to speed up the NVD download. |
| Keeping `mvn clean verify` fast/offline | Dependency-Check runs explicitly (not bound to `verify`), consistent with SpotBugs/PMD. |
| Reducing image attack surface | Multi-stage build + Alpine runtime + non-root user + `.dockerignore`. |
| Swagger leaking in production | Environment-gated profiles (dev on, prod/ci off). |
| Making checks reproducible | `container-security/scripts/verify-workshop4.sh` + CI artifacts. |

---

## The Complete DevSecOps Pipeline

The repository contains **three** GitHub Actions workflows that together industrialize security:

| Workflow | Purpose | Key gates |
|---|---|---|
| `ci.yml` | Core CI | Maven verify, SpotBugs/Find Security Bugs, PMD, OWASP Dependency-Check, PITest, Trivy filesystem + container scan, Docker Compose smoke test |
| `digibank-dast.yml` | Lab 3 DAST | Newman (blocking) + OWASP ZAP baseline (observation) |
| `digibank-security-pipeline.yml` | Lab 4 container/deps | Dependency-Check + Docker build + Trivy image scan + artifact publishing |

**Dependabot** monitors Maven and GitHub Actions dependencies weekly, keeping the supply chain
up-to-date.

---

## Overall Challenges & Solutions

| Challenge | Solution |
|---|---|
| Coordinating four progressive workshops on one codebase | Modular monolith + clear per-lab evidence docs and CI workflows. |
| Avoiding regressions while hardening | Every remediation is followed by `mvn clean verify` and focused tests. |
| Reproducible security evidence | Versioned Postman collections, scan reports, scripts, and CI artifacts. |
| No committed secrets anywhere | Environment variables + GitHub secrets (`NVD_API_KEY`) + `.dockerignore`. |
| Balancing security with build speed | Heavy scanners run explicitly / in dedicated CI jobs, not on every local build. |

---

## Conclusion

Across the four labs, DigiBank progressed from a clean, testable modular monolith (Lab 1) to a
system whose **source code** (Lab 2), **runtime behavior** (Lab 3), and **supply chain** (Lab 4) are
all analyzed, hardened, and automated. This demonstrates the full **DevSecOps / Shift Left**
philosophy: security is not a final step but a continuous, integrated property of the software
lifecycle — from the first line of code to the container image and the CI/CD pipeline.

**Key takeaway:** A secure application is not just well-written code. It is code that is statically
and dynamically validated, built from trusted dependencies, shipped in a hardened container, and
continuously re-checked by an automated pipeline.
