# Ticket 2: Customer Management Domain

## Owner
Team member 2

## Scope
Own `customer-module`.

## Tasks
- Create the `Customer` JPA entity with:
  - `id`
  - `firstName`
  - `lastName`
  - `email`
  - `identityNumber`
  - `status`
  - `createdAt`
- Create a customer status enum.
- Create `CustomerRequestDto` with validation for required names, valid email, bounded text lengths, and required identity number.
- Create `CustomerResponseDto` with only safe output fields.
- Create `CustomerRepository` with `findByEmail` and `findByIdentityNumber`.
- Create `CustomerService` and `CustomerServiceImpl`.
- Implement:
  - create customer
  - get customer by ID
  - get customer by email
  - list all customers
- Create `CustomerController` endpoints:
  - `POST /api/v1/customers`
  - `GET /api/v1/customers/{id}`
  - `GET /api/v1/customers/email/{email}`
  - `GET /api/v1/customers`
- Add unit tests for creation, lookup, duplicate customer handling, and not-found behavior.

## Security Acceptance Criteria
- All incoming customer input is validated with Jakarta Bean Validation.
- Duplicate email and duplicate identity number are rejected safely.
- Responses do not expose persistence internals or stack traces.
- Email lookup behavior is consistent and does not leak internal errors.
- The module uses shared exceptions and response wrappers where applicable.

## Verification
- Run `mvn -pl customer-module test`.
- Run `mvn clean verify` from the root after integration.

## Evidence
- Unit test output for customer service behavior.
- Swagger screenshot showing customer endpoints after integration.
- Sample API call showing customer creation.
