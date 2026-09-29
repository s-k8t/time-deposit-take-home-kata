# Time Deposit Refactoring Kata - Take-Home Assignment

## XA Bank Time Deposit

### Context
A junior developer implemented domain logic for a time deposit system but did not complete the API functionality. Your task is to refactor the existing codebase to implement all required functionalities based on the provided business requirements, ensuring no breaking changes occur.

### Requirements

1. **API Endpoints**:
    - Create a RESTful API endpoint to update the balances of all time deposits in the database.
    - Create a RESTful API endpoint to retrieve all time deposits.
        - The GET endpoint should return a list of all time deposits with the following schema:
            - `id`
            - `planType`
            - `balance`
            - `days`
            - `withdrawals`

2. **Database Setup**:
    - Store all time deposit plans in a database.
    - Define the following tables:
        - `timeDeposits`:
            - `id`: Integer (primary key)
            - `planType`: String (required)
            - `days`: Integer (required)
            - `balance`: Decimal (required)
        - `withdrawals`:
            - `id`: Integer (primary key)
            - `timeDepositId`: Integer (foreign key, required)
            - `amount`: Decimal (required)
            - `date`: Date (required)

3. **Interest Calculation**:
    - Implement logic to calculate monthly interest based on the plan type:
        - **Basic Plan**: 1% interest
        - **Student Plan**: 3% interest (no interest after 1 year)
        - **Premium Plan**: 5% interest (interest starts after 45 days)
    - No interest is applied for the first 30 days for any existing plans.

4. **Refactoring Constraints**:
    - Do not introduce breaking changes to the shared `TimeDeposit` class or modify the `updateBalance` method signature.
    - Ensure the design is extensible to accommodate future complexities in interest calculations.

5. **Code Quality**:
    - Adhere to SOLID principles, design patterns, and clean code practices where applicable.

6. **AI-Assisted Development**:
    - Set up an AI harness or agent workflow and use it throughout the development for this take-home exercise.
    - Briefly document the tools and setup used (e.g., LLMs, coding assistants, agentic frameworks, configuration).
    - Ensure your AI setup is practical and reproducible.
    - Include any custom rules, system prompts, or agent configurations used.
    - Include a brief summary of which parts of the solution were AI-assisted and why.

### Important Guidelines
- The existing `TimeDepositCalculator.updateBalance` method is functioning correctly. Ensure its behavior remains unchanged after refactoring.
- The final solution must include **exactly two API endpoints**. Do not develop additional endpoints.
- **Do not** create a pull request or a new branch in the ikigai-digital repository. Instead, fork the repository into your own GitHub repository and develop the solution there.
- Handling invalid input or exceptions is not required.
- Use any tools, frameworks, or libraries you find suitable.
- In case of ambiguity, make logical assumptions and justify them in code comments.

### Preferred Stack
- Use an OpenAPI Swagger contract.
- Embrace Hexagonal Architecture.
- Follow atomic commit practices.
- Utilize testcontainers.
- Leverage AI-assisted development tools for code generation, testing, and refactoring.

### Submission Instructions
- Provide clear instructions on how to trigger the endpoints using the Swagger contract.
- Email the link to your public GitHub repository.

---

## Solution (Kotlin)

The solution lives in the [`kotlin`](kotlin) module.

### Tech Stack

| Area | Technology |
|---|---|
| Language / runtime | Kotlin 2.4.10 on JVM 21 |
| Build | Gradle 9.7 (Kotlin DSL), ktlint |
| Framework | Spring Boot 4.0 (Spring Framework 7) |
| Web | Spring WebFlux on Reactor Netty, Jackson 3 with the Kotlin module |
| Concurrency | Kotlin coroutines (`kotlinx-coroutines-reactor`) |
| Persistence | Spring Data R2DBC with `CoroutineCrudRepository`, `r2dbc-postgresql` driver, optimistic locking via `@Version` |
| Database | PostgreSQL 17, schema migrations with Flyway |
| API contract | OpenAPI 3 / Swagger UI via springdoc-openapi (WebFlux) |
| Local environment | Docker Compose (started automatically by Spring Boot's Docker Compose support) |
| Testing | JUnit 6, AssertJ, MockK + springmockk, `kotlinx-coroutines-test`, WebTestClient, Testcontainers (PostgreSQL) |

### Non-Blocking, Top to Bottom

Every layer of a request is non-blocking. Coroutines suspend instead of holding a thread while waiting for I/O:

```
HTTP request
  └─ Reactor Netty (WebFlux event loop)
      └─ TimeDepositQueryController      suspend fun  (implements the api facade)
          └─ GetTimeDepositsService      suspend fun
              └─ Store (data source)     suspend fun
                  └─ CoroutineCrudRepository  →  Flow<Entity>
                      └─ R2DBC driver  →  PostgreSQL
```

- **Web layer:** WebFlux runs `suspend` controller functions natively, so no servlet thread is held per request.
- **Service and data layers:** `suspend` all the way down. Nothing calls blocking code or needs `withContext(Dispatchers.IO)`.
- **Persistence:** R2DBC talks to PostgreSQL with a reactive, non-blocking protocol. Repository results come back as `Flow` and are collected with `toList()`.
- **The one blocking part:** Flyway runs its migrations over JDBC once, at startup, before the application accepts traffic.