# DigiBank DAST Artifacts (Workshop 3)

This directory holds the dynamic-security-analysis (DAST) artifacts for Workshop 3. It keeps the
Postman/Newman collections, OWASP ZAP notes, and generated reports together so the test campaign is
reproducible and traceable.

## Layout

```text
dast/
├── postman/
│   ├── DigiBank-DAST-Validation.postman_collection.json   # final validation + Newman checks
│   └── DigiBank-local.postman_environment.json            # local environment variables
├── zap/
│   └── zap-notes.md                                       # ZAP observations and before/after
└── reports/                                               # Newman / ZAP outputs (generated, git-ignored)
```

## Running the collection locally

Start DigiBank, then replay the collection with Newman:

```bash
# from the repository root, with the app running on http://localhost:8080
newman run dast/postman/DigiBank-DAST-Validation.postman_collection.json \
  -e dast/postman/DigiBank-local.postman_environment.json \
  --reporters cli,htmlextra,json \
  --reporter-htmlextra-export dast/reports/newman-report.html \
  --reporter-json-export dast/reports/newman-report.json
```

The collection requires a fresh database each run (the functional flow creates its own customers,
accounts, and a transfer and stores the generated identifiers in collection variables). Re-run on a
clean database (e.g. `docker compose down -v` then `docker compose up -d --build`) to avoid duplicate
customer/identity conflicts.

## Collection structure

- `01-functional-regression-check` — core business flows still work after hardening.
- `02-input-validation-check` — malformed input is rejected at the boundary.
- `03-error-handling-check` — not-found/business errors return generic, non-revealing messages.
- `04-information-exposure-check` — no stack traces or framework internals leak.
- `05-remediation-verification` — sensitive fields are omitted and docs are profile-gated.

### Stronger state assertions

The collection generates unique customer fixtures for every run and creates accounts owned by
both customers. Before and after every successful or rejected transfer it reads both account
records and both histories. Success must move the exact amount and add exactly one matching audit
record to each history; zero, negative, fractional-cent, oversized, same-account and insufficient-funds
requests must leave all four snapshots unchanged. Missing state reads fail the run. In the CI profile,
API documentation must return 404. Newman uses pinned compatible versions and `--bail` in CI.
