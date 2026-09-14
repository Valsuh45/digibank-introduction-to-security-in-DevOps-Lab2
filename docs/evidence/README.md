# Workshop 1 Evidence Index

This index maps the Workshop 1 PDF's objectives (section 1.5), Flyway proof (section 7.14), and final deliverables (section 8.1) to the implementation and execution evidence. It is an evidence record, not a substitute for inspecting actual results. The original [implementation plan](IMPLEMENTATION_PLAN.md) is historical; [local development](../operations/local-development.md) gives reproducible commands.

## Specification Mapping

| Objective | Implementation / verification source | Evidence to retain |
|---|---|---|
| A1.1: explain the architecture | [Architecture](../architecture/overview.md), root POM | Module tree and explanation of one runtime, one database, layered responsibilities |
| A1.2–A1.3: modules and layers | [Module map](../modules/overview.md), domain source/test trees | Final project tree in the IDE; representative DTO/controller/service/repository/test |
| A1.4: functional REST APIs | [API map](../api/endpoints.md), `scripts/verify-workshop1.py` | Customer/account creation, successful transfer, rejected transfer with unchanged persisted state |
| A1.5: index and OpenAPI | Homepage, Swagger UI, `/v3/api-docs` | Browser captures of homepage and interactive Swagger |
| A1.6: JUnit and Cucumber | `mvn clean verify`, module `target/surefire-reports` | Build result and test totals; successful and rejected transfer scenarios |
| A1.7: JAR, Docker, CI | Dockerfile, Compose, `.github/workflows/ci.yml` | JAR packaging, successful Compose startup, health, Actions run URL with matching SHA |
| A1.8: usable foundation | Current verification record and documented limits | Repeatable execution and explanation of validation, constraints, transaction boundary and remaining exposure |
| Section 7.14: Flyway | V1/V2 migrations and PostgreSQL | Migration logs, `flyway_schema_history`, seeded customers/accounts/history and successful restart |

The guide uses illustrative `/api/...` routes, entity/table names and seed counts. This implementation uses versioned `/api/v1/...` routes and the fixtures in its actual V2 migration. Assess equivalent behavior against the objectives and record differences rather than assuming the PDF examples are literal API contracts.

## Evidence Record Format

For each execution retain: date, Git SHA, whether local modifications were present, command/tool version, observed outcome, and a relative artifact path or stable CI URL. Keep generated reports outside the source tree or as CI artifacts. Before an expiring artifact disappears, retain an exported copy with the course submission. Redact credentials and use only fictional customer data.

The final Workshop 1 PDF explicitly requests captures of clean Maven verification, Compose startup, homepage, Swagger UI, customer creation, account creation, successful transfer, GitHub Actions, and the final IDE project tree. Machine-readable HTTP checks strengthen these proofs but do not by themselves supply the requested browser/IDE captures.

## Current Verification

Verified on 11 September 2026 against local modifications based on `84048ed`. The [source hashes](workshop1/source-sha256.txt) identify the checked source files; these changes have not been committed or pushed.

| Check | Result |
|---|---|
| Clean Maven verification, followed by final `mvn -B -o verify pmd:aggregate-pmd-check` after review fixes | 85 tests, zero failures/errors/skips; PMD passed |
| `mvn -B compile com.github.spotbugs:spotbugs-maven-plugin:4.9.3.2:check` | Passed with Find Security Bugs and the existing exclusion policy |
| Dockerfile build + isolated Compose/PostgreSQL | Passed; image runs as `digibank`; both containers healthy |
| Real HTTP campaign | 57 requests passed, including fresh customers/accounts, transfer, invalid requests and unchanged state after rejection |
| Container recreation with the same volume | Seven customer/account/transfer/history resources unchanged; Flyway remains at V2 |
| Browser inspection | Homepage and Swagger loaded and were captured |
| Independent review | Two findings corrected and rechecked; no remaining actionable code defects identified |

Retained artifacts: [results](workshop1/results.json), [verification excerpts](workshop1/verification.txt), [HTTP responses and observations](workshop1/http.json), [persistence result](workshop1/persistence.json), [Flyway history](workshop1/flyway.txt), [homepage capture](screenshots/workshop1-homepage.png), [Swagger capture](screenshots/workshop1-swagger.png), and [review](workshop1-review.md). All customer data in HTTP evidence is synthetic.

The [base revision's DigiBank CI run](https://github.com/Valsuh45/digibank-introduction-to-security-in-DevOps-Lab2/actions/runs/34573756139) passed all six jobs. It does not validate these uncommitted changes. A final GitHub run requires committing/pushing the changes; no such action was performed here. Local Trivy scans were not rerun during this Workshop 1 closeout. The isolated verification containers and synthetic-data volume were removed after evidence collection; unrelated containers were left untouched.

The PDF's submission captures still need an IDE project-tree screenshot and any teacher-required terminal/API execution screenshots beyond the retained machine-readable evidence. Native IDE control is unavailable in this session; no substitute screenshot is presented as an IDE capture.

## Learning and Scope

Explain a transfer from request validation through the service and repository transaction to database constraints and response mapping. Explain why a rejected request must leave both balances and history unchanged, and why successful H2 tests still need PostgreSQL execution. Explain why validation does not provide authentication or account ownership controls: those controls are absent in this educational version.

Advanced SAST/DAST campaigns, production authentication, concurrent-transfer locking, registry publication, and deployment hardening are separate follow-ups. They do not replace Workshop 1's functional and execution proof.

## Integrated security fix verification

See [security-gap fixes](security-gap-fixes.md) for refreshed results on the combined Workshop 1–4 tree. The Workshop 1 snapshots above remain historical and are not a claim about the combined revision.
