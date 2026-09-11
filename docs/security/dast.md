# Dynamic Security Analysis (DAST) — Workshop 3

This document summarises how DigiBank was analysed, hardened, and revalidated from a **dynamic**
(runtime) perspective in Workshop 3, complementing the static (SAST) work of Workshop 2.

## Approach

DAST observes the application's actual behaviour while it runs. DigiBank was started against
PostgreSQL, the REST surface was mapped with Swagger/curl, targeted scenarios were built in Postman,
replayed automatically with Newman, and observed with OWASP ZAP. Each finding was traced back to a
code/configuration choice, remediated, and revalidated.

Artifacts live under [`dast/`](../../dast/README.md): a Newman-ready Postman validation collection, a
Postman environment, and OWASP ZAP notes.

## Dynamic findings and remediations

| # | Observed weakness (runtime) | Root cause | Remediation |
|---|---|---|---|
| 1 | Error responses echoed the requested id (`Customer not found with id: 99999`) and business state (`A customer with this email already exists`, `Insufficient balance`). This enables identifier enumeration and data-existence probing. | `GlobalExceptionHandler` returned `exception.getMessage()` for not-found and business exceptions. | Handler now returns generic messages (`Resource not found`, `Request could not be processed`); the real reason is logged server-side. |
| 2 | Not-found message embedded the raw identifier from the client. | `CustomerServiceImpl.getCustomerById` built the message from the id. | Message made generic (`Customer not found`); no identifier in the public text. |
| 3 | Duplicate creation revealed whether an email/identity already existed. | `CustomerServiceImpl.createCustomer` used distinct, revealing messages. | Combined into a single generic message (`A customer with the supplied details already exists`). |
| 4 | The identity number had no structural constraint, so malformed values (`???`) could be attempted. | `CustomerRequestDto.identityNumber` only had `@Size`. | Added `@Pattern` (`^[A-Z0-9][A-Z0-9-]{3,99}$`) so the format is enforced at the boundary. |
| 5 | Interactive API documentation was exposed in every profile. | `springdoc` had no environment gating. | `dev` keeps Swagger/API-docs enabled; `prod` and `ci` profiles disable them (`application.yml`, `application-ci.yml`). |
| 6 | Customer API responses were reviewed for over-exposure. | Response DTO historically included the identity number. | Confirmed/kept `CustomerResponseDto` without `identityNumber`; the controller test asserts it is absent. |

Already-defensive areas were verified rather than changed: `TransferRequestDto` rejects non-positive
amounts, `TransferServiceImpl` rejects null/zero/same-account transfers, and `AccountRequestDto` +
`BankAccountServiceImpl` reject negative balances. These business safeguards now surface as generic
client messages through the hardened handler.

## Revalidation

- `mvn clean verify` passes (unit + integration + Cucumber).
- The Postman validation collection replays functional regression, input-validation, error-handling,
  information-exposure, and remediation checks. Run with Newman:

  ```bash
  newman run dast/postman/DigiBank-DAST-Validation.postman_collection.json \
    -e dast/postman/DigiBank-local.postman_environment.json \
    --reporters cli,html,json \
    --reporter-html-export dast/reports/newman-report.html \
    --reporter-json-export dast/reports/newman-report.json
  ```

- OWASP ZAP baseline scan is run before/after to compare the visible surface and error verbosity.

## CI/CD

[`.github/workflows/digibank-dast.yml`](../../.github/workflows/digibank-dast.yml) automates two
levels:

1. **Level 1 (Newman)** — blocking: starts PostgreSQL + DigiBank (`ci` profile), waits for health,
   replays the DAST collection, uploads Newman reports and the app log.
2. **Level 2 (ZAP)** — observation: runs the stack with Docker Compose and a ZAP baseline scan,
   publishing the report as evidence without blocking the pipeline.

## Acceptance criteria

- Essential business flows still work (functional regression check).
- Prioritised dynamic weaknesses are reduced/removed (input validation, error handling, exposure).
- Remediations are consistent with the architecture and linked to code/config.
- Evidence is reproducible (Newman collection, environment, ZAP notes, CI artifacts).
