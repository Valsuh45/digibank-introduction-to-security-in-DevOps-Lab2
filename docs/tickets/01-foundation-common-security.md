# Ticket 1: Foundation and Shared Security Baseline

## Owner
Team member 1

## Scope
Own the parent Maven setup and `common-module`.

## Tasks
- Verify the root Maven parent is configured for Java 17 and Spring Boot 3.x.
- Ensure all modules are declared in the root `pom.xml`.
- Configure shared dependency management for internal modules and test libraries.
- Implement or verify `ApiResponse<T>` with `success`, `message`, `data`, and `timestamp`.
- Implement or verify `ApiResponse.success(data, message)` and `ApiResponse.error(message)`.
- Implement or verify `ErrorDetails` for safe validation/error responses.
- Implement the shared exception hierarchy:
  - `DigiBankException`
  - `ResourceNotFoundException`
  - `InsufficientBalanceException`
  - `BusinessException`
  - `InvalidOperationException`
- Add shared constants where useful, including default currency.
- Add unit tests for `ApiResponse`, `ErrorDetails`, and exception behavior.

## Security Acceptance Criteria
- Shared API/error responses do not expose stack traces.
- Error models are predictable enough for future DAST validation.
- Exceptions support safe messages without leaking database or framework internals.
- No secrets or environment-specific credentials are committed.
- The common module can be reused by all business modules without circular dependencies.

## Verification
- Run `mvn -pl common-module test`.
- Run `mvn clean verify` from the root after integration.

## Evidence
- Test output for `common-module`.
- Screenshot or copied output from root Maven verification once all tickets are merged.
