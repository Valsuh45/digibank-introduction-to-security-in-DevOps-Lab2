# DigiBank — Team Task Breakdown & Role Assignment (Workshop 1)

This document provides a comprehensive **5-Role Task Breakdown** for the DigiBank project (*UCC152-2: Introduction to Security in DevOps*). Each team member can pick one role and work directly on their designated modules/tasks with minimal overlap.

---

## 🏗️ Architecture Quick Overview

```text
digibank-parent/
├── common-module/      # Shared API responses, custom exceptions, global utilities
├── customer-module/    # Customer domain (Entities, DTOs, Repository, Service, REST Controller)
├── account-module/     # Account domain (Entities, DTOs, Repository, Service, REST Controller)
├── transfer-module/    # Transfer domain (Entities, DTOs, Service, REST Controller, History)
├── digibank-web/       # Main Spring Boot entry point, Web config, Swagger/OpenAPI, Landing Page
├── .github/workflows/  # GitHub Actions CI workflow
├── Dockerfile          # Multi-stage Docker build
├── docker-compose.yml  # Docker Compose file for App + PostgreSQL
└── pom.xml             # Root Maven Parent POM
```

---

## 👥 5-Member Team Division of Work

### 👤 Role 1: Project Architecture & Cross-Cutting Foundation (`common-module` & Maven Setup)
* **Target Directory / Files:** `pom.xml`, `common-module/`
* **Key Tasks:**
  1. **Root Maven Parent POM (`pom.xml`):**
     * Configure Java 17 baseline, Spring Boot 3.x parent, and dependency management (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `postgresql`, `flyway-core`, `springdoc-openapi-starter-webmvc-ui`, `lombok`, `junit-jupiter`, `mockito`, `cucumber-java`, `cucumber-junit-platform-engine`).
     * Declare submodules: `common-module`, `customer-module`, `account-module`, `transfer-module`, `digibank-web`.
  2. **Standard API Wrapper (`common-module`):**
     * `ApiResponse<T>`: Include `success` (boolean), `message` (String), `data` (T), and `timestamp` (LocalDateTime).
     * Provide helper factory methods: `ApiResponse.success(data, message)` and `ApiResponse.error(message)`.
  3. **Custom Exception Hierarchy (`common-module`):**
     * `DigiBankException` (base runtime exception).
     * Subclasses: `ResourceNotFoundException`, `InsufficientBalanceException`, `BusinessException`, `InvalidOperationException`.
  4. **Shared DTOs & Validation Utils:**
     * Generic error response structure `ErrorDetails`.
     * Shared constants (date formats, default currency).

---

### 👤 Role 2: Customer & Account Domain Modules (`customer-module` & `account-module`)
* **Target Directory / Files:** `customer-module/`, `account-module/`
* **Key Tasks:**
  1. **Customer Management Module (`customer-module`):**
     * **Entity:** `Customer` (`id`, `firstName`, `lastName`, `email`, `identityNumber`, `status`, `createdAt`).
     * **DTOs & Validation:** `CustomerRequestDto` (`@NotBlank`, `@Email`, `@Size`), `CustomerResponseDto`.
     * **Repository:** `CustomerRepository` (with custom lookup `findByEmail`, `findByIdentityNumber`).
     * **Service Layer:** `CustomerService` interface & `CustomerServiceImpl` (create customer, get by ID, get by email, list all customers).
     * **REST Controller (`CustomerController`):**
       * `POST /api/v1/customers` (Create customer)
       * `GET /api/v1/customers/{id}` (Get customer by ID)
       * `GET /api/v1/customers/email/{email}` (Get customer by email)
       * `GET /api/v1/customers` (List all customers)
  2. **Bank Account Management Module (`account-module`):**
     * **Entity:** `BankAccount` (`id`, `accountNumber`, `balance`, `currency`, `accountType`, `status`, `createdAt`, `customer_id` relation).
     * **DTOs & Validation:** `AccountRequestDto` (`@NotNull customerId`, `@NotNull accountType`, `@Min initialBalance`), `AccountResponseDto`.
     * **Repository:** `BankAccountRepository` (lookup `findByAccountNumber`, `findByCustomerId`).
     * **Service Layer:** `BankAccountService` & `BankAccountServiceImpl` (create account for customer, get balance, lookup by account number, list customer accounts).
     * **REST Controller (`BankAccountController`):**
       * `POST /api/v1/accounts` (Create bank account)
       * `GET /api/v1/accounts/{id}` (Get account details)
       * `GET /api/v1/accounts/number/{accountNumber}` (Get account by number)
       * `GET /api/v1/accounts/customer/{customerId}` (List accounts for a customer)

---

### 👤 Role 3: Transfer Engine, Transaction History & Global Exception Handler (`transfer-module`)
* **Target Directory / Files:** `transfer-module/`, `digibank-web/.../exception/`
* **Key Tasks:**
  1. **Transfer & History Module (`transfer-module`):**
     * **Entity:** `Transfer` (`id`, `transferReference`, `sourceAccountNumber`, `targetAccountNumber`, `amount`, `status`, `executionDate`, `description`).
     * **DTOs & Validation:** `TransferRequestDto` (`@NotBlank sourceAccountNumber`, `@NotBlank targetAccountNumber`, `@Positive amount`), `TransferResponseDto`.
     * **Repository:** `TransferRepository` (lookup `findBySourceAccountNumberOrTargetAccountNumber`).
     * **Service Layer (`TransferService` & `TransferServiceImpl`):**
       * Implement `@Transactional` `executeTransfer(...)` logic:
         1. Verify source and target accounts exist (throw `ResourceNotFoundException` if missing).
         2. Verify source account has sufficient balance (throw `InsufficientBalanceException` if balance < amount).
         3. Deduct amount from source account, credit amount to target account.
         4. Generate unique `transferReference`, record transaction log in DB with status `SUCCESS`.
       * Implement `getAccountTransactionHistory(accountNumber)`.
     * **REST Controller (`TransferController`):**
       * `POST /api/v1/transfers` (Execute money transfer)
       * `GET /api/v1/transfers/{id}` (Get transfer details)
       * `GET /api/v1/transfers/account/{accountNumber}` (Get transaction history for an account)
  2. **Global Exception Handler (`GlobalExceptionHandler`):**
     * Create `@RestControllerAdvice` class handling:
       * `DigiBankException` / `ResourceNotFoundException` / `InsufficientBalanceException` -> Returns HTTP 400/404 wrapped in `ApiResponse.error()`.
       * `MethodArgumentNotValidException` -> Formats field validation errors cleanly into `ApiResponse`.
       * `Exception` (fallback generic 500 server error).

---

### 👤 Role 4: Application Bootstrap, UI, Flyway Migrations & Swagger (`digibank-web`)
* **Target Directory / Files:** `digibank-web/`, `Flyway scripts`, `OpenAPI Config`
* **Key Tasks:**
  1. **Spring Boot Bootstrap:**
     * Create entry point `DigiBankApplication.java` with `@SpringBootApplication` and package scanning across all modules (`com.m2ibank.*` or `com.digibank.*`).
     * Setup multi-profile configuration in `application.yml` (`dev`, `prod` profiles; DB URL, username, password parameterized via environment variables like `${SPRING_DATASOURCE_URL}`).
  2. **Flyway Schema & Seed Data Migrations (Part 7 of PDF):**
     * `src/main/resources/db/migration/V1__init_schema.sql`: DDL for tables `customers`, `bank_accounts`, `transfers`.
     * `src/main/resources/db/migration/V2__seed_data.sql`: DML inserting test customers, accounts, and sample transfers for reproducible local execution.
  3. **UI Landing Page & Swagger API Specs:**
     * Configure Springdoc OpenAPI (`OpenApiConfig.java`) with API info, version `1.0`, description, and contact info.
     * Create static HTML homepage (`src/main/resources/static/index.html`):
       * Styled presentation of DigiBank.
       * Direct links to Swagger UI (`/swagger-ui.html` or `/swagger-ui/index.html`) and OpenAPI JSON (`/v3/api-docs`).

---

### 👤 Role 5: Quality Assurance (Tests), Dockerization & GitHub Actions CI
* **Target Directory / Files:** `src/test/`, `Dockerfile`, `docker-compose.yml`, `.github/workflows/`
* **Key Tasks:**
  1. **Unit Testing (JUnit 5 & Mockito):**
     * `CustomerServiceTest`: Test creation logic and validation.
     * `BankAccountServiceTest`: Test account assignment to customer.
     * `TransferServiceTest`: Test successful transfer, insufficient funds failure case, non-existent account failure case.
  2. **Integration & BDD Testing (Cucumber):**
     * Write Cucumber feature files (e.g. `src/test/resources/features/transfer.feature` for BDD scenarios: successful transfer, insufficient balance scenario).
     * Implement step definitions (`TransferStepDefinitions.java`) using `@SpringBootTest`.
     * Create Cucumber runner test class (`RunCucumberTest.java`).
  3. **Containerization & Deployment Artifacts:**
     * Multi-stage `Dockerfile`: Stage 1 builds with `maven:3.9-eclipse-temurin-17`, Stage 2 runs jar on `eclipse-temurin:17-jre`.
     * `docker-compose.yml`: Services `postgres` DB (port 5432 with health check) and `digibank-app` (port 8080 depending on postgres health).
  4. **CI Pipeline Automation:**
     * Create `.github/workflows/ci.yml`:
       * Triggers on `push` and `pull_request` to `main`.
       * Jobs: JDK 17 setup, caching Maven dependencies, running `mvn clean verify` (compiles code + runs JUnit & Cucumber), building Docker container.

---

## 🎯 Final Deliverables & Audit Proofs (Part 8 of PDF)

Before completing Workshop 1, the team must verify and gather the following audit evidence required by the course syllabus:
- [ ] `mvn clean verify` output log showing 100% build & test success.
- [ ] `docker compose up --build` output log showing DB migration and Spring Boot startup.
- [ ] Screenshot of static Homepage (`http://localhost:8080/`).
- [ ] Screenshot of Swagger UI interactive docs (`http://localhost:8080/swagger-ui.html`).
- [ ] Sample API call evidence (Customer creation, Account creation, Transfer execution).
- [ ] GitHub Actions successful workflow run screenshot.
