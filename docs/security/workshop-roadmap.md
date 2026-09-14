# DigiBank workshop assessment and learning roadmap

> Historical assessment: Workshop 1 implementation and verification work continued after this review. See [the current evidence index](../evidence/README.md) before treating the Workshop 1 findings below as still open.

Assessment date: 11 September 2026. Reviewed local `main` at `84048ed60105c0f852f2abdca3bd10b540a39ea5`.

## Assessment

DigiBank has a substantial Workshop 1 implementation and useful early Workshop 2 and 4 controls. It does not yet demonstrate completion of all four workshops. The largest gap is the repeatable learning and evidence cycle: identify a risk, reproduce or measure it, explain its impact, fix it, rerun the same check, and preserve the result.

This assessment separates checked-in implementation, locally verified execution, and work for which evidence has not been inspected. Remote Actions results, repository rules, and external SonarQube dashboards were not inspected. Four newly supplied PDFs are currently untracked; their presence on disk does not mean they are committed to Git. No application implementation was changed for this review.

Source requirements are the five workshop PDFs in the repository root. Useful reading anchors: Workshop 1 Part 8; Workshop 2 sections 1.8, Parts 2–5; Workshop 3 sections 1.8, 3.9, Parts 4–6; Workshop 4 sections 1.8, 2.14, Parts 5–6; session slides 19–30 for the complete tool set. Treat sample paths and commands as examples: this implementation uses `/api/v1/...` and `/v3/api-docs`, and its classes often end in `Dto` or `Impl`.

## 1. What is already implemented

| Area | Evidence in this repository | What this establishes |
|---|---|---|
| Modular monolith | Root `pom.xml`, five child POMs, `DigiBankApplication` | One runnable application with common, customer, account, transfer, and web responsibilities; these are not independently deployed microservices. |
| API design | Three domain controllers, request/response DTOs, OpenAPI annotations, static homepage | Customer registration/lookups, account creation/lookups, transfer execution/history, and discoverable API contracts. |
| Input and domain safeguards | Bean Validation, service checks, generated account numbers/references | Basic invalid input, negative balances, self-transfers, duplicate customers, and insufficient funds are addressed. |
| Data integrity | Flyway V1/V2, Hibernate `validate` | Schema migrations, constraints, and deterministic demonstration data. |
| Error boundary | `GlobalExceptionHandler`, server error configuration | Generic unexpected errors and explicit handling of several validation/domain failures. |
| Automated tests | JUnit/Mockito, Spring tests, two Cucumber scenarios | Unit and integration checks exist; Cucumber exercises real services against the test database. |
| Containers | Multi-stage Dockerfile, non-root runtime user, Compose health checks | A container execution path with external database credentials and startup sequencing. |
| CI | `.github/workflows/ci.yml` | Maven verification, PMD, SpotBugs/Find Security Bugs, Trivy filesystem/image scans, Compose HTTP smoke test. |
| Dependency maintenance | `.github/dependabot.yml`, parent dependency properties | Scheduled Maven/Actions update configuration and centralized dependency overrides. |

Recent history supports the presence of the PMD, SpotBugs/Find Security Bugs, and Dependabot work. A merge commit establishes that code landed; it does not establish that every current security gate is passing.

## 2. Workshop completion map

| Workshop | Current assessment | Work needed to demonstrate completion |
|---|---|---|
| 1: Build a usable foundation, A1.1–A1.8 | Substantially implemented | Reproduce clean build and Compose execution; retain homepage, Swagger, customer/account/transfer, and Actions evidence; explain architecture and transaction boundaries. |
| 2: Static analysis and justified remediation, A2.1–A2.8 | Partially implemented | Add SonarQube, OWASP Dependency-Check, JaCoCo and PIT evidence; complete the session's Gitleaks and Checkstyle controls; interpret findings and retain before/after results. |
| 3: Runtime security, A3.1–A3.8 | Some preventive fixes exist; dedicated campaign missing | Version a Postman collection/environment, automate with Newman, inspect actual response contracts and state changes, compare ZAP observations, fix findings, and retain CI artifacts. |
| 4: Dependencies and containers, A4.1–A4.8 | Hardening and scanning partly implemented | Produce dependency trees, dependency reports, image metadata/history and Trivy reports; explain changes, residual risks, and reproducibility; complete artifact retention. |

The detailed Workshop 2 concentrates on SonarQube, Dependency-Check, and PIT. The session slides describe a broader pipeline including Gitleaks, Checkstyle, and JaCoCo. To cover **all supplied materials**, include both sets. Workshop 3 treats ZAP inconsistently: introductory text calls it optional, while later sections request before/after observations. Include a local ZAP comparison for comprehensive coverage; its CI gate can start in observation mode as section 6.13 permits.

Registry publishing is a useful later extension, but it should not displace the missing analysis, dynamic tests, and evidence. The session architecture itself labels deployment optional.

## 3. Concrete findings to investigate and address

These are code review observations unless explicitly described as runtime-verified. Proposed HTTP outcomes must be confirmed against a running application.

### Transfer monetary validation is incomplete

`TransferRequestDto` requires a positive amount but does not constrain decimal places or integer digits. `TransferServiceImpl.validateAmount` also only checks positivity. The database uses `NUMERIC(19,2)`, while account opening already uses `@Digits(integer = 17, fraction = 2)`.

Test `0.001`, `1.999`, an oversized amount, and a transfer that overflows the recipient balance. Reject values that cannot be represented under the application's declared money policy before persistence. Do not assume a positive `BigDecimal` is automatically a valid monetary value. Verify both balances and the transfer record remain unchanged after rejection. The current two-decimal schema is an application convention; document the intended currency policy rather than silently changing it.

Transfer account numbers are only checked for blankness; the account lookup API enforces a 12-digit format. Align those contracts, including a deliberate decision about trimming whitespace, and repeat the checks at the service boundary where needed.

### Account state rules are not enforced by transfers

`TransferServiceImpl.executeTransfer` checks account existence and funds but does not inspect account status or currency. `BankAccount` stores both. The controller description claims account state is checked, so documentation currently promises more than the implementation provides.

Specify whether both accounts must be active, then test non-active source and destination fixtures. Define the currency compatibility rule if multiple currencies become possible. Current creation assigns XAF, so a public cross-currency exploit has not been demonstrated.

### Unknown customers are left to a database constraint

`BankAccountServiceImpl.createAccount` validates a positive customer ID but does not establish that the customer exists. The schema foreign key prevents orphan accounts, which is good. However, there is no dedicated account-creation handling for that database failure; the generic handler is likely to produce 500 rather than a deliberate client error.

Reproduce account creation for an unused positive customer ID. Add a customer existence check through an appropriate module boundary, preserve the foreign key, and translate any relevant race-time constraint failure safely. Agree on the API's 4xx response and test it. Avoid adding a cyclic module dependency.

### HTTP error coverage is narrower than the documentation suggests

The global handler catches `Exception` and returns 500. It explicitly handles malformed request bodies but not every MVC client error. Test a nonnumeric path ID, unsupported HTTP method, unsupported content type, missing routes, and invalid transfer path values. Some standard client errors may fall through to the generic handler.

Use explicit mappings or Spring's standard exception-handling support while preserving the API response format. Check status, body, and absence of stack traces/database details. Do not infer that generic error text guarantees correct HTTP semantics.

`CustomerServiceImpl.getCustomerById` still echoes the requested ID. A generic message would align it with the other services, but removing the ID alone does not prevent enumeration: status codes, public lookup endpoints, and timing still matter.

### No application authentication or ownership enforcement is present

The inspected POMs, controllers, and services do not configure application authentication or check that a caller owns an account. This is a major boundary of the educational application. It should not be presented as a secure bank simply because scanners pass.

For Workshop 3, record the actual unauthenticated behavior and the intended trust boundary. A3.4 says to test authentication/authorization/session mechanisms **when exposed**; it does not automatically require building a full identity platform. If protected access is part of the chosen extension, design principals, roles, account ownership, and tests for unauthenticated requests and cross-customer access before implementing it. Hiding Swagger does not supply authorization.

### Transactional code still needs concurrency and rollback evidence

Transfers run inside `@Transactional`, but account reads use ordinary repository lookups. There is no `@Version`, explicit row lock, or conditional atomic update in the reviewed account entity/repository. Concurrent transfers can therefore risk lost balance updates; this is a code-level risk, not a demonstrated concurrency exploit.

As a deeper banking-correctness exercise, test simultaneous transfers against PostgreSQL, then choose a locking/versioning strategy with retry and lock ordering where appropriate. Separately inject a failure after balance changes and prove that balances and audit history roll back. The current successful Cucumber balance assertions do not by themselves prove rollback after a partial failure or concurrency safety. Idempotency for retried requests is another valuable extension after the core workshop gaps.

### Runtime profiles and evidence policies need completion

Swagger/OpenAPI is enabled without a production-specific restriction. Demonstration seed migration V2 runs from the common migration location in every profile. Add and test explicit development/CI versus deployment behavior. Keep demonstration fixtures out of a real deployment path without rewriting migrations already applied to existing databases.

Trivy currently gates HIGH/CRITICAL findings and uses `ignore-unfixed: true`. Passing this gate means passing that filter, not having no vulnerabilities. Preserve a broader inventory report and explain accepted/unfixed findings separately. A filesystem secret scan, even after a full Git fetch, does not automatically inspect every historical commit: add the session's Gitleaks history scan and document its scope.

Review the repository-wide `CT_CONSTRUCTOR_THROW` suppression. Its statement that finalizer attacks only apply when DigiBank itself declares a finalizer is too broad: subclassing can be relevant. Assess the actual flagged classes and scope or justify the exception. A scanner suppression is a security decision that needs evidence.

## 4. Recommended implementation sequence

Each item is suitable for a focused pull request. Finish its acceptance criteria before marking it complete.

### Step 1: Establish a reproducible evidence baseline

Record commit SHA, Java/Maven versions, commands, dates, exit codes, report locations, and CI run links. Preserve a current baseline before further fixes. For already-fixed weaknesses, use Git history and authentic retained reports; do not invent historical measurements or weaken main to manufacture a before state.

Run clean verification, then PMD, then SpotBugs in sequence locally. The goals can touch shared `target` directories; separate CI jobs have isolated workspaces. Start Compose with disposable demonstration data, verify health/OpenAPI, and create a customer, an account, and a transfer through HTTP. Record balances and history, not just a success message.

Acceptance: another student can repeat the steps and locate the evidence. Generated reports can be CI artifacts; they do not all need to be committed, but they need stable references and a retention policy.

### Step 2: Add JaCoCo and durable test reports

Produce HTML and XML coverage for the multi-module project. Account for integration tests in `digibank-web` executing code from domain modules. Upload Surefire and Cucumber reports, and configure Cucumber HTML/JSON output rather than only `pretty` console output.

Acceptance: identify specific untested business branches and write meaningful tests for them. Record the initial coverage and then set a justified non-regression threshold. Do not invent a course-mandated percentage or chase 100% on generated code.

### Step 3: Add Dependency-Check and Gitleaks

Pin a supported Dependency-Check plugin version, provide NVD credentials through environment/CI secrets, scan the reactor with an understood aggregation strategy, and retain HTML/JSON results plus the dependency tree. Distinguish scanner/data-download failure from a clean result. Compare findings with Trivy and document mismatches.

Add redacted Gitleaks reports for the agreed Git history scope. If a real exposed secret is found, revoke/rotate it; deleting the current line is not sufficient. Never place unredacted secret values in course evidence.

Acceptance: explain at least one dependency finding or a genuine clean scan, including direct/transitive dependency path, applicability, remediation, and remaining risk. No real secret appears in the exported reports.

### Step 4: Complete the static quality and test-strength controls

Add SonarQube analysis and retain its dashboard, quality gate, findings, and reviewed hotspots. Import JaCoCo XML. If SonarQube is local, do not configure a hosted runner to reach it as `localhost`; use a reachable service or retain local analysis evidence as Workshop 2 permits. When automating it, distinguish successful report upload from a passing quality gate.

Add Checkstyle with an explicit convention. Keep style-only changes separate from behavioral security fixes. The current PMD gate covers selected quality rules and deliberately omits CPD; if claiming the session's duplication objective, produce CPD or Sonar duplication evidence.

Add PIT with JUnit 5 support, starting with customer/account/transfer service rules. Verify the multi-module test strategy rather than copying a single-module command. Inspect surviving mutants and improve assertions on observable outcomes.

Acceptance: explain the difference between an uncovered branch, a surviving mutant, a code smell, a security hotspot, and a verified vulnerability. Preserve baseline and improved results.

### Step 5: Run the Workshop 3 campaign and fix measured gaps

Create a versioned Postman collection and non-secret environment. Derive routes and request fields from this application's OpenAPI. Create unique fixtures per run and capture IDs/account numbers dynamically; avoid dependence on fixed balances after repeated runs.

Minimum scenario matrix:

| Group | Cases | Assertions |
|---|---|---|
| Customer | Valid creation, blank/oversized fields, malformed email, duplicates after normalization, unknown ID | Intended status and response shape; no identity number or internal details exposed; no duplicate row. |
| Account | Valid creation, negative/overprecise balance, missing customer, invalid type and identifier | Safe 4xx for invalid requests; no orphan account; server controls account number/status/currency. |
| Transfer | Success, zero/negative/null/overprecise amount, same account including whitespace, unknown/malformed accounts, insufficient funds, non-active account fixtures | Correct balances and history on success; all state unchanged on rejection. |
| HTTP boundary | Invalid JSON, wrong method/content type, malformed path, nonexistent route | Deliberate 4xx contract; no internal stack trace, SQL, or infrastructure detail. |
| Exposure | Homepage, OpenAPI, health, headers, public access, customer/account/history lookups | Behavior matches documented environment and access policy. |

For text resembling SQL or HTML, distinguish accepting ordinary text from executing it. Do not call a string an injection vulnerability merely because it was stored; demonstrate the unsafe sink or rendering behavior.

Run Newman against a disposable stack, assert meaningful behavior, upload reports even on failure, and clean up on every outcome. Collect ZAP observations by exercising relevant API traffic or importing API definitions; crawling only the homepage misses many POST operations. Re-run the same campaign after fixes.

Acceptance: every retained finding connects request → observed response/state → source cause → fix → repeat test. Newman fails on an assertion failure, startup failure, or required missing report. ZAP CI can begin as documented observation mode.

### Step 6: Complete Workshop 4 artifact analysis

Capture the built image ID/digest, base image information, runtime user, size, image inspection and history, dependency tree, Dependency-Check report, and Trivy report. Confirm what each scan actually examines: Java dependencies, OS packages, Dockerfile/Compose configuration, and secrets are different targets.

Review mutable base tags and `apk upgrade`: these improve the update path but make identical source builds potentially resolve different packages. Document the tradeoff; consider digest pinning with an update process. Evaluate read-only runtime filesystem, dropped capabilities, and no-new-privileges with a real startup/HTTP check instead of adding settings blindly.

Build and scan the same artifact intended for delivery. If publishing is added later, gate publication on all required checks and publish the scanned digest. Rescan maintained images over time because vulnerability databases change even without a source commit.

Acceptance: a before/after table explains actual risk reduction, functionality remains intact, and unresolved findings have a rationale, owner, and review date.

### Step 7: Finish the course evidence and explanation

For each workshop, retain its objective map, commands, reports, chosen findings, fixes, residual risks, and final verification. Verify required status checks and branch rules in GitHub; the workflow file alone cannot prove merge enforcement.

Use this finding template:

`ID | workshop/objective | commit/date | endpoint/file | tool/version | request or scan command | observed result | impact and preconditions | priority | chosen fix | regression test | after evidence | residual risk/owner/review date`

## 5. Learn the course through one transfer

Trace a transfer from HTTP JSON through DTO validation, controller, service, repositories, transaction, database constraints, response mapping, and error handling. For each boundary, explain what is trusted and which rule is enforced there.

Then study the same operation through each workshop:

1. **Workshop 1:** make a correct transfer work and explain the modular architecture.
2. **Workshop 2:** inspect weak assumptions and dependencies; write a test that would detect a faulty balance rule.
3. **Workshop 3:** send invalid requests to the running application and prove what happens to balances and history.
4. **Workshop 4:** inspect what is packaged and who runs it; explain how the pipeline preserves and rechecks the result.

For every new tool, predict what it should find before running it. Read one finding deeply. Explain why the other tools might miss it. Make one justified fix and rerun the same experiment.

Useful distinctions:

- SAST examines implementation; DAST observes a running system. DAST means **Dynamic Application Security Testing**; the expanded acronym in Workshop 2 contains a translation error.
- Dependency-Check is SCA (software composition analysis), even though the course places it in the static pipeline. See [OWASP's definition](https://owasp.org/www-project-dependency-check/).
- Coverage measures executed code, not whether assertions would catch incorrect results. See [JaCoCo counters](https://www.jacoco.org/jacoco/trunk/doc/counters.html).
- Mutation testing evaluates whether tests detect deliberate code changes. PIT's cross-module behavior needs explicit care; see [PIT Maven guidance](https://pitest.org/quickstart/maven/).
- ZAP baseline performs crawling and passive analysis; it is not an exhaustive active attack campaign or a proof of correct banking authorization. See [ZAP baseline documentation](https://www.zaproxy.org/docs/docker/baseline-scan/).
- Transactions, validation, authorization, and database constraints address different failure modes. One cannot replace the others.

You understand a control when you can explain the risk, show the failing example, explain why the fix works, and show what still remains outside its protection.

## 6. Documentation corrections to include

- `docs/evidence/IMPLEMENTATION_DESIGN.md` refers to a missing `docs/evidence/README.md`; create a usable evidence index.
- `docs/operations/container-and-ci.md` omits the PMD and SpotBugs jobs and prioritizes registry publishing too early for the workshop gaps.
- The design note attributes the application health check to the Docker image; it is currently configured in Compose, not in the Dockerfile.
- `BankAccountServiceImpl` says the transfer module uses `updateBalance`, but the current transfer service updates the repository directly.
- Transfer OpenAPI documentation says account state is checked, but no such check exists in the service.
- `.dockerignore` excludes `.env` files, but `.gitignore` does not; align local-secret handling before adding new tool credentials.
- Keep historic completed-task notes as historical records rather than treating them as fresh execution evidence.

## 7. Verification performed during this review

- `mvn -B -o clean verify`: **passed** on Java 17.0.19. Surefire XML reports total **67 tests**, zero failures, zero errors, zero skipped; this includes the two Cucumber scenarios. Module totals: common 11, customer 16, account 12, transfer 17, web 11. This verifies the current test suite, not all proposed security properties.
- `mvn -B -o pmd:aggregate-pmd-check`: **passed**. This is an aggregate goal; skipped child entries in the final reactor summary do not mean the aggregate omitted their sources.
- SpotBugs: **not verified**. Offline invocation could not resolve the configured plugin from the local Maven cache. This is a tool-resolution limitation, not a detected application vulnerability.
- Docker execution: **not verified**. Access to the local Docker socket was denied in this environment. No fresh image build, Compose test, Trivy scan, Newman run, or ZAP run is claimed here.
- Remote GitHub Actions and branch rules: **not inspected**. Existing implementation-plan checkmarks describe prior work and were not used as current remote execution evidence.

An initial verification attempt overlapped a PMD compile in the same workspace and encountered a missing test class. The subsequent isolated clean verification passed; the initial failure is not classified as a repository defect. Run Maven checks sequentially in a shared local checkout.

Review-only logs are under `/tmp/digibank-workshop-review/`; preserve selected logs in the course evidence system before relying on them as long-term artifacts. The report is the only new repository document created by this review.
