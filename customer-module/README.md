# customer-module

## Purpose

`customer-module` owns customer registration and customer lookup. It keeps customer rules separate from account and transfer logic while still running inside the same Spring Boot application.

## Main Responsibilities

- Accept validated customer registration requests.
- Normalize email addresses before storage and lookup.
- Reject duplicate email and identity numbers.
- Store customer records through Spring Data JPA.
- Return safe customer response DTOs that do not expose identity numbers.

## Important Files

- `CustomerController`: REST endpoints under `/api/v1/customers`.
- `CustomerService` and `CustomerServiceImpl`: customer business rules.
- `CustomerRepository`: database access for customer records.
- `Customer`: JPA entity mapped to the `customers` table.
- `CustomerRequestDto` and `CustomerResponseDto`: API input and output models.

## Security Notes

The module validates client input at the DTO/controller boundary and also relies on database unique constraints as a second layer of protection against duplicate records. Sensitive identity numbers are stored for uniqueness checks but are not returned in the public response DTO.

## Verification

Run this module alone with:

```bash
mvn -pl customer-module test
```
