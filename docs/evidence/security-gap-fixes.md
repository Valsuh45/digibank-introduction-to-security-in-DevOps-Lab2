# Integrated Workshop 1–4 security fixes

Date: 2026-09-14. Branch: `fix/workshop-security-gaps`, created from `origin/main`
(`56de99e`), incorporating Workshop 1 (`7428f0e`) and Workshop 4 (`06866a9`, merged as
`f6b015d`). This is local branch integration, not a merge into remote main.

## Changes and measured results

| Area | Change | Validation |
| --- | --- | --- |
| Workshop 1 reconciliation | Preserved monetary validation, rollback integration tests and safe framework errors; aligned public-message expectations with main | 57 HTTP requests passed against PostgreSQL; refreshed Maven tests below |
| Build and coverage | JaCoCo per-module reports plus web-assembly aggregate includes integration execution across all five modules; service floors 80% lines / 70% branches | 104 tests, zero failures/errors/skips; JaCoCo check passes |
| Style and static checks | Checkstyle convention for production and tests; preserve PMD and SpotBugs/FindSecBugs | Checkstyle, aggregate PMD and SpotBugs all pass |
| Mutation testing | Handwritten customer/account/transfer services; 80% mutation / 90% line thresholds per module; no-mutation runs fail | Customer 10/10, account 33/33, transfer 51/51 mutants killed |
| Dependency gate | Aggregate reactor scan; scanner errors and unsuppressed CVSS >= 7 findings fail; always upload HTML/JSON | Workflow lint passes; deliberately missing NVD database returns exit 1 (fails closed); fresh NVD-backed scan remains external pending rotated credential |
| Dependency exceptions | Replace CPE suppression targets with explicit version-scoped Maven GAV selectors plus named CVEs; retain original expiration | Does not certify those CVEs as false positives. Original risk acceptance requires a current scan and owner review |
| Secrets | Remove literal NVD key; inject `secrets.NVD_API_KEY`; Gitleaks custom NVD rule plus defaults | Current source: zero findings. Full fetched history: two NVD findings, intentionally not hidden |
| SonarQube | Analyze after coverage; wait for server quality gate; fail clearly on missing setup | Workflow syntax checked; server-side analysis not run (configuration absent) |
| DAST | Unique fixtures; compare both account records and both complete histories around every transfer; assert exact success effects and 404 documentation response | Newman: 82 requests and 134 assertions, all pass in CI profile |
| Workshop 4 | Integrate existing container workflow, review notes, local script and Swagger profile hardening; save Trivy scan artifacts | Image build passed; remaining scan/runtime details in the machine-readable results |

`security-gap-fixes/results.json` and `source-sha256.txt` record measurements and source hashes.
The earlier `workshop1/` evidence and Workshop 2/4 team notes remain historical. A passing local
build is not evidence of a successful hosted CI run or a clean dependency inventory.

## What the measurements taught us

The new coverage gate initially rejected `BankAccountServiceImpl`: branch coverage was 50%.
Tests now exercise invalid identifiers and incomplete creation requests without persistence,
account-number collision exhaustion, and persisted detail/balance reads. The service now has
100% line and branch coverage in its module report.

The first PIT run measured customer 10/10, account 33/33, transfer 43/51 (84%). Transfer had
seven survivors and one uncovered mutant. These identified missing observations of history input
validation, zero transfer IDs, successful audit lookup, null/blank/trimmed descriptions, the
255-character description boundary, and the maximum 17-digit amount. Added behavioral assertions
kill all 51 transfer mutants. These results cover the configured default operators and service
scope only; they do not prove the whole application bug-free or replace real transaction tests.

The thresholds are an initial repository policy for review, not course-mandated percentages or
a claim that the team previously agreed them. Raising them should follow stable repeated evidence;
lowering them requires a documented explanation of the lost protection.

Gitleaks found the exposed NVD key in commits `ce8410c75e0b` and `bb0493a18e92`.
No secret values are included here. The default gate checks the current source; the manual
`audit_history` workflow option runs a separate strict, redacted audit of all fetched history.
It will continue failing on these historical findings until the team follows a deliberate
incident/history policy. Rotation invalidates the credential but does not erase historical bytes.

## External actions still required

1. The NVD key owner must revoke/rotate the exposed credential in their NVD account, then store
   the replacement as the GitHub Actions secret **NVD_API_KEY**. The only secret name visible
   during this run was **NVM_API_KEY**; its value was not read and its identity/validity was not
   assumed. Delete or rename that old entry only after confirming its purpose. A local source
   deletion cannot perform revocation. Do not paste replacement keys into chat or commits.
2. Configure **SONAR_TOKEN** as a secret and **SONAR_HOST_URL** / **SONAR_PROJECT_KEY** as
   repository variables. Use a server reachable by GitHub runners with the branch/PR analysis
   support needed by the workflow and a reviewed server quality gate. A developer's localhost
   cannot be reached by a hosted runner. Fork PRs intentionally skip this credential-dependent job.
3. Rerun dependency analysis with a current NVD database, review the retained Spring exceptions,
   and retain the resulting reports. OSS Index is disabled by the inherited Workshop 4 setup;
   the scan is not a claim of coverage by every vulnerability source.
4. Publish this fix branch and run the hosted workflows when authorized. Integrating Workshop 4
   into remote main still requires the team's normal review/merge process. No PR is created here.

## Reproduction

```bash
mvn -B clean install
mvn -B pmd:aggregate-pmd-check spotbugs:check
mvn -B -pl customer-module,account-module,transfer-module org.pitest:pitest-maven:mutationCoverage
# Requires a freshly rotated NVD_API_KEY in the environment:
mvn -B org.owasp:dependency-check-maven:aggregate
# Requires SONAR_TOKEN and SONAR_HOST_URL in the environment:
mvn -B verify sonar:sonar -Dsonar.projectKey="$SONAR_PROJECT_KEY" -Dsonar.qualitygate.wait=true
gitleaks dir . --config .gitleaks.toml --redact=100
gitleaks git . --log-opts=--all --config .gitleaks.toml --redact=100
```

For HTTP evidence run `scripts/verify-workshop1.py` against the dev profile. Run the Postman
collection against the CI profile with Newman 6.2.1 and reporter `newman-reporter-htmlextra` 1.23.1.
Use a disposable database: both suites create synthetic data. Never reuse real customer records.

## Independent review

A separate review agent inspected the working tree against the annotated gaps and workshop
requirements. It found two defects: the inherited DAST documentation check targeted `/api-docs`
instead of `/v3/api-docs`, and missing Newman reports only warned. Both were corrected: the
collection now checks the real OpenAPI endpoint and both Swagger routes, and CI requires both
nonempty report files. The reviewer rechecked the corrections and reported no remaining actionable
defects within its reviewed scope. The corrected Newman run passes all 82 requests / 134 assertions.
