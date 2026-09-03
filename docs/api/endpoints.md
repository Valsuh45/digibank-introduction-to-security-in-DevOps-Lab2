# API Endpoints

## Documentation Endpoints

- `GET /`: static DigiBank landing page.
- `GET /swagger-ui/index.html`: interactive Swagger UI.
- `GET /v3/api-docs`: machine-readable OpenAPI JSON.
- `GET /actuator/health`: health endpoint used by Docker and CI.

## Customer Endpoints

- `POST /api/v1/customers`: create a customer.
- `GET /api/v1/customers/{id}`: get a customer by id.
- `GET /api/v1/customers/email/{email}`: get a customer by email.
- `GET /api/v1/customers`: list customers.

Customer responses intentionally do not expose identity numbers.

## Account Endpoints

- `POST /api/v1/accounts`: create an account for a customer.
- `GET /api/v1/accounts/{id}`: get account details by id.
- `GET /api/v1/accounts/number/{accountNumber}`: get account details by account number.
- `GET /api/v1/accounts/customer/{customerId}`: list accounts for a customer.

Clients do not choose account numbers. The server generates them.

## Transfer Endpoints

- `POST /api/v1/transfers`: execute a transfer.
- `GET /api/v1/transfers/{id}`: get transfer details by id.
- `GET /api/v1/transfers/account/{accountNumber}`: list transfer history for an account.

Transfer execution is transactional so debit, credit, and audit record creation succeed or fail together.
