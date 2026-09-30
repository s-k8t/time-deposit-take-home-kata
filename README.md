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

### Database Schema

Flyway migrations in `kotlin/src/main/resources/db/migration` create two tables:

| Table | Columns |
|---|---|
| `time_deposits` | `id` (integer, primary key), `plan_type` (varchar), `balance` (decimal 19,2), `start_date` (date, defaults to the current date), `version` (bigint) |
| `withdrawals` | `id` (integer, primary key), `time_deposit_id` (integer, foreign key to `time_deposits`), `amount` (decimal 19,2), `date` (date), `version` (bigint) |

**Deviation from the brief: `start_date` instead of `days`.** The brief defines a `days` column on `timeDeposits`. A stored day count never changes, so a deposit would never age into or out of an interest rule (for example, premium interest starting after 45 days). The table therefore stores when the deposit started, and `days` is calculated as the number of days from `start_date` to today:

- The `GET` endpoint still returns `days`, so the API contract is unchanged.
- The balance update calculates `days` the same way before passing each deposit to `TimeDepositCalculator`, so the shared `TimeDeposit` class and the `updateBalance` signature stay unchanged.
- Migration `V3__replace_days_with_start_date.sql` converts existing data by setting `start_date = CURRENT_DATE - days` before dropping `days`, so every deposit keeps its age.

Column names are snake_case (`plan_type`, `time_deposit_id`, `start_date`), which is the PostgreSQL convention, rather than the brief's camelCase. The `version` columns support optimistic locking for the balance update.

### API Endpoints

Both endpoints are under `/v1/deposits`, and the OpenAPI contract is generated from the code (see below). Neither endpoint takes parameters or a request body.

| Method | Path | Success | Purpose |
|---|---|---|---|
| `GET` | `/v1/deposits` | `200 OK` | List all time deposits with their withdrawals |
| `POST` | `/v1/deposits/balances` | `204 No Content` | Apply one month of interest to every time deposit |

#### `GET /v1/deposits`

Returns every time deposit, ordered by `id`. An empty database returns `[]`.

| Field | Type | Description |
|---|---|---|
| `id` | integer | Time deposit id |
| `planType` | string | `BASIC`, `STUDENT`, `PREMIUM`, or `UNDEFINED` for any plan type not recognised in the database |
| `balance` | number | Current balance |
| `days` | integer | Days since the deposit's start date, calculated on each request (the database stores `start_date`) |
| `withdrawals` | array | The deposit's withdrawals, ordered by `date`; `[]` if there are none |
| `withdrawals[].id` | integer | Withdrawal id |
| `withdrawals[].amount` | number | Withdrawn amount |
| `withdrawals[].date` | string | Withdrawal date, ISO-8601 (`YYYY-MM-DD`) |

```json
[
  {
    "id": 1,
    "planType": "BASIC",
    "balance": 1234.56,
    "days": 45,
    "withdrawals": [{ "id": 1, "amount": 100.0, "date": "2026-09-01" }]
  }
]
```

#### `POST /v1/deposits/balances`

Recalculates and stores the balance of every time deposit by adding one month of interest, according to its plan. `days` is the number of days from the deposit's start date to today:

| Plan | Annual rate | Interest is applied when |
|---|---|---|
| Basic | 1% | `days` ≥ 31 |
| Student | 3% | 31 ≤ `days` ≤ 365 (none after one year) |
| Premium | 5% | `days` ≥ 46 |
| Any other plan type | none | never |

Monthly interest is `balance × annual rate / 12`, rounded half-up to cents.

- **Not idempotent:** every call applies another month of interest. That's why it's a `POST`.
- **Concurrent changes are never overwritten:** each deposit is updated only if it still has the version read at the start of the calculation. A deposit modified in the meantime keeps that change and is skipped in this run.

### Calling the Endpoints via Swagger

**Prerequisites:** JDK 21 and a running Docker daemon.

**1. Start the application**

```bash
cd kotlin
./gradlew bootRun
```

Spring Boot starts PostgreSQL from `compose.yaml`, Flyway creates the schema, and the API listens on port `8080`. If that port is taken, run `./gradlew bootRun --args='--server.port=8081'` and use `8081` in the URLs below.

**2. Add sample data**

There is no endpoint for creating deposits, and the database starts empty. In a second terminal:

```bash
cd kotlin
docker compose exec -T postgres psql -U time_deposit -d time_deposits <<'SQL'
INSERT INTO time_deposits (plan_type, start_date, balance) VALUES
  ('basic', CURRENT_DATE - 45, 1234.56), ('student', CURRENT_DATE - 200, 5000.00), ('premium', CURRENT_DATE - 90, 10000.00);
INSERT INTO withdrawals (time_deposit_id, amount, date)
  SELECT id, 100.00, DATE '2026-09-01' FROM time_deposits WHERE plan_type = 'basic';
SQL
```

**3. Open Swagger UI** at <http://localhost:8080/swagger-ui.html>. Both endpoints are listed under **Time deposits**. The raw OpenAPI contract is at <http://localhost:8080/v3/api-docs>.

**4. Get all time deposits:** expand **`GET /v1/deposits`**, click **Try it out**, then **Execute**. The response is `200` with every deposit and its withdrawals:

```json
[
  {
    "id": 1,
    "planType": "BASIC",
    "balance": 1234.56,
    "days": 45,
    "withdrawals": [{ "id": 1, "amount": 100.0, "date": "2026-09-01" }]
  },
  { "id": 2, "planType": "STUDENT", "balance": 5000.0, "days": 200, "withdrawals": [] },
  { "id": 3, "planType": "PREMIUM", "balance": 10000.0, "days": 90, "withdrawals": [] }
]
```

**5. Update all balances:** expand **`POST /v1/deposits/balances`**, click **Try it out**, then **Execute**. It takes no request body and returns `204 No Content`. One month of interest is applied to every eligible deposit, in a single transaction.

**6. Check the result:** execute `GET /v1/deposits` again. The balances are now `1235.59` (basic), `5012.5` (student) and `10041.67` (premium). Each further `POST` applies another month of interest.

The same calls without Swagger:

```bash
curl http://localhost:8080/v1/deposits
curl -X POST http://localhost:8080/v1/deposits/balances
```

Stopping the application (`Ctrl+C`) also stops the PostgreSQL container, but keeps it, so the data is still there on the next `bootRun`. Step 2 is only needed once. To start again from an empty database, run `docker compose down` in `kotlin/`.

### AI-Assisted Development

#### Tools and Setup

| Tool | Role |
|---|---|
| [Claude Code](https://claude.com/claude-code) (CLI) with Claude Opus 5.5 | Coding agent: reads the codebase, edits files, runs Gradle, Docker and the tests, and reports the results |
| IntelliJ IDEA | Reviewing every change, running tests, and my own edits alongside the agent |
| Gradle build, ktlint, Testcontainers | Guardrails: every change had to pass `./gradlew build` (style and all tests against a real PostgreSQL) before it was accepted |

**Reproducing the setup:**

1. Install Claude Code: `npm install -g @anthropic-ai/claude-code`.
2. Run `claude` in the repository root.
3. Claude Code loads [`CLAUDE.md`](CLAUDE.md) automatically. It holds the project's custom rules: the shared-class constraints, package layout, non-blocking stack, code style, and test conventions.

No other system prompts, plugins or agent frameworks were used.

#### How It Was Used

I led the design, and the agent executed it in small steps:

- **I decided the structure and conventions:** the query/command split and package layout, the facades, the value classes and enums in `api.model`, the REST paths, and the test style. Each decision became a rule in `CLAUDE.md`, so later steps followed it without being reminded.
- **The implementation was done by me.** The agent's first drafts were rewritten or removed when they didn't fit the design.
- **The agent implemented missing tests**, covering the existing calculation logic before refactoring and each new layer as it was added.
- **The agent asked before decisions with trade-offs**, for example Spring Data JDBC vs R2DBC when moving to coroutines, or moving the shared `TimeDeposit` class, which the brief protects. It flagged problems in my own edits, such as a plan-type fixture that silently mapped to `UNDEFINED`.
- **Findings were checked, not assumed.** For example, the transaction rollback test was confirmed to fail when `@Transactional` is removed, and the Swagger instructions above were followed against the running application.