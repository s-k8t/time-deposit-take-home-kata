# Time Deposit Kata: Rules for Claude Code

The solution is the Kotlin module in `kotlin/`. The requirements are in `README.md`.

## Build and Verify

- `cd kotlin && ./gradlew build` runs ktlint and all tests. The integration tests use Testcontainers, so Docker must be running.
- Run `./gradlew ktlintFormat` before finishing a change.
- Run the build after every change and report the real result. Never claim a test passes without running it.
- For new behaviour, write or adjust the tests first, then the implementation.

## Hard Constraints

- Do not introduce breaking changes to `TimeDeposit` or to the `TimeDepositCalculator.updateBalance` signature. Both stay in the `org.ikigaidigital` package.
- Exactly two endpoints: `GET /v1/deposits` and `POST /v1/deposits/balances`. Add no others.
- No verbs in API paths.

## Architecture

- Query/command split instead of hexagonal architecture. Package names are singular.
  - `api`: facade interfaces (`TimeDepositQueryFacade`, `TimeDepositCommandFacade`) and `api.model` (response DTOs, value classes, enums).
  - `query/{controller,service,datasource}` and `command/{controller,service,datasource,config}`.
  - `store`: `Store` implements the datasource interfaces; `store.entity` and `store.repository` hold the persistence code.
  - `model`: types shared by both sides (`PlanType`).
- Datasource interfaces are `internal`, with nested `Input`/`Output` data classes and `suspend` functions.
- Mapping extensions live in `QueryExtensions.kt` / `CommandExtensions.kt`.

## Non-Blocking Stack

- Everything on the request path is `suspend`: controller → service → datasource → `Store`.
- Spring WebFlux, Spring Data R2DBC with `CoroutineCrudRepository`. Never add blocking calls.
- Collect repository `Flow`s. `saveAll(...)` writes nothing until it is collected.
- Entities have `@Version val version: Long? = null`. Schema changes go in new Flyway migrations. Never edit an applied one.

## Code Style

- No explanatory comments. Make the code self-explanatory through naming.
- Match the style of the surrounding code.

## Tests

- Name tests `should <outcome> when <condition>`, including parameterized display names.
- Split the body into `// prepare`, `// execute` and `// verify` sections.
- Create test objects only through `TestFixtures.createXxx(...)`, using default arguments and overriding only the fields that matter.
- Mock with MockK and springmockk only. Mockito is excluded from the build.
  - Unit tests: `@ExtendWith(MockKExtension::class)`, mocks as `@param:MockK` and the subject as `@param:InjectMockKs` constructor parameters. Stub suspend functions with `coEvery` and wrap tests in `runTest`.
  - Spring tests: `@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)`, no `@Autowired`, mocks as `@MockkBean private val` constructor properties.
- Controller tests: `@WebFluxTest` with `WebTestClient`. Build the expected JSON by interpolating the fixture's fields and compare it with `JsonCompareMode.STRICT`.
- Integration tests live in `org.ikigaidigital.integration`, with datasource tests in `integration.datasource`. Create data with `repository.save(createXxxEntity(...))` and clean up with `deleteAll()` in `@AfterEach`. Do not use SQL scripts.
