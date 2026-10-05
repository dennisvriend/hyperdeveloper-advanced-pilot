# Server (vethub)

Java 25 / Spring Boot backend for the vethub monorepo. REST API for a pet clinic domain (owners, pets, vets, visits).

## Commands

Run from `server/` (Gradle wrapper 9.3.1, Java 25 temurin via root `mise.toml`):

```bash
./gradlew build              # build; spotlessApply runs automatically before compile
./gradlew test               # run all tests (JUnit 5)
./gradlew bootRun            # run locally (dev profile, port 8080, context path /api)
./gradlew spotlessApply      # format code (eclipse formatter, config in src/quality/config/spotless)
./gradlew check              # full quality gate: checkstyle, PMD, SpotBugs, CodeNarc, CPD
./gradlew dependencyUpdates  # check for dependency updates
```

Always run `./gradlew build` (or at minimum `test` + `check`) before finishing a task. The compiler uses `-Xlint:all -Werror`, so warnings fail the build.

## Architecture

- Package root: `dev.ilionx.workshop` (`src/main/java`)
- `Application.java` — main class
- `api/` — feature modules: `owner`, `pet`, `vet`, `visit`, each with `controller/`, `service/`, `repository/`, `model/` (+ `request/`, `response/`, `mapper/` (MapStruct), `validator/`). Shared paths in `api/Paths.java`.
- `common/` — `config/` (WebSecurityConfig, OpenAPIConfig, RequestMatcherConfig, properties), `security/filter/`, `exception/`

Follow the existing feature-module pattern: new features get their own package under `api/` with controller/service/repository/model layers. Never expose JPA entities through the API — use separate request and response DTOs with MapStruct mappers.

## Adding a new feature (backend)

1. Liquibase changeset for schema changes (registered in `db.changelog-master.yaml`)
2. Entity class with JPA annotations
3. Request and response DTOs
4. MapStruct mapper interface
5. Repository (Spring Data JPA interface)
6. Service with business logic
7. Controller with REST endpoints
8. Tests: unit test for the service, integration test for the controller

## Configuration

- `src/main/resources/application.yml` — placeholders (`APPLICATION_NAME/GROUP/VERSION`) filtered by ProcessResources; context path `/api`, port 8080, actuator at `/v1/public/actuator`, Swagger at `/v1/public/docs/openapi.html`, H2 in-memory, JPA + Liquibase
- `src/main/resources/application-dev.yml` — dev profile (liquibase drop-first, basic auth)
- Liquibase: `src/main/resources/db/changelog/db.changelog-master.yaml` with changesets (PRD schema, TST seed data). Schema changes go through Liquibase, never JPA ddl-auto. New changelogs must be included in `db.changelog-master.yaml`.

## Testing

- JUnit 5 via spring-boot-starter-test; Testcontainers available
- Custom annotations in `src/test/java/.../support/`: `@IntegrationTest` (full Spring context, H2), `@UnitTest` (no Spring context, fast) — both with TestContextInitializer, WebMvcConfigurator
- MockMvc for controller tests; Mockito for mocking in unit tests
- Test config: `src/test/resources/application-test.yml` (H2, liquibase drop-first, basic auth)
- Existing examples: `VetControllerTest`, `PetServiceTest`, `OwnerValidatorTest`, `VisitGlobalControllerTest` — follow these patterns.

## API-first contract

This server is the source of the OpenAPI contract consumed by `client/`. The endpoint `GET /api/v1/public/docs` serves the spec. After changing controllers/models, regenerate the client types via `../scripts/openapi-sync.sh` (starts the server, downloads `server/openapi.json`, runs `bun run generate:api` in `client/`).

## Versions and coordinates

All versions and coordinates live in `gradle.properties` (artifact `ilionx-pet-store`, group `dev.ilionx.workshop`, version 0.1.0-SNAPSHOT). Update dependencies there.
