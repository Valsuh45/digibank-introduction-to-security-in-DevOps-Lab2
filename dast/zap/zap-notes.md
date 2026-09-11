# OWASP ZAP Notes — DigiBank DAST (Workshop 3)

This file records how OWASP ZAP was used to observe DigiBank at runtime, what was noticed, and the
known limitations of local scanning. It supports the before/after comparison required by Workshop 3.

## Target

- Target URL: `http://localhost:8080`
- Method: direct (automated) baseline scan and, optionally, proxy mode through ZAP (port 8080/8090).

## What to observe in ZAP

For DigiBank the following are expected to appear and should be recorded:

- Discovered routes: application root, homepage, and the REST surface under `/api/v1/*`.
- The presence/absence of interactive documentation: `/swagger-ui.html`, `/v3/api-docs`, `/api-docs`
  (expected present in the `dev` profile, absent in `ci`/`prod`).
- Passive alerts related to HTTP response headers (e.g. missing security headers).
- Responses containing overly revealing error messages (before remediation).
- Parameters identified on the transfer, account, and customer endpoints.

## Link observations to code

ZAP alerts should be tied to a concrete application choice:

| Observation | Likely source |
|---|---|
| Interactive API docs exposed | `springdoc` config in `digibank-web/src/main/resources/application.yml` |
| Overly explicit error body | `GlobalExceptionHandler` + service exception messages (before remediation) |
| Weak/absent input constraints | `CustomerRequestDto`, `AccountRequestDto`, `TransferRequestDto` |
| Rich response data | `CustomerResponseDto` and other response DTOs |

## Known limitations of local scanning

- A baseline scan is reconnaissance, not a proof of exploitation. Absence of an alert does not prove
  the absence of a weakness.
- Some ZAP alerts come from the environment or the scanner itself and are not DigiBank weaknesses.
- The educational profile used locally (`dev`) intentionally exposes Swagger; this is not appropriate
  for production and is disabled in the `ci` and `prod` profiles.
- Only the endpoints reachable from `http://localhost:8080` are observed; anything behind additional
  network hops is out of scope for this local pass.

## Before / after

- Before remediation: verbose not-found messages (`Customer not found with id: X`), business messages
  (`A customer with this email already exists`, `Insufficient balance`), and an unconstrained identity
  number were observable.
- After remediation: responses return generic messages (`Resource not found`,
  `Request could not be processed`), the identity number is structurally validated, sensitive fields
  are no longer echoed, and Swagger is disabled outside the `dev` profile.

## Re-running

Start DigiBank (Docker Compose or `mvn spring-boot:run -pl digibank-web`), then run a ZAP automated
scan against `http://localhost:8080` and compare the alert set to the notes above.
