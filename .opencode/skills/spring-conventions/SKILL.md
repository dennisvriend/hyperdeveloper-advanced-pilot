---
name: spring-conventions
description: Spring Boot backend conventions for the vethub server — feature-module package layout, controller/service/repository layering, DTO + MapStruct mapping rules, Paths constants, OpenAPI-first contract, Gradle commands and quality gates. Use when writing or changing Java code in server/.
---

# When to use

Use when writing or modifying backend Java code in `server/` (controllers, services, repositories, entities, mappers, validators, config).

## Commands (run from `server/`)

```bash
./gradlew build              # build; spotlessApply runs automatically before compile
./gradlew test               # run all tests (JUnit 5)
./gradlew bootRun            # run locally (dev profile, port 8080, context path /api)
./gradlew spotlessApply      # format code (eclipse formatter, config in src/quality/config/spotless)
./gradlew check              # full quality gate: checkstyle, PMD, SpotBugs, CodeNarc, CPD
./gradlew dependencyUpdates  # check for dependency updates
```

Always run `./gradlew build` (or at minimum `test` + `check`) before finishing a task. The compiler uses `-Xlint:all -Werror` — warnings fail the build. Versions/coordinates live in `gradle.properties`.

## Package layout

Package root `dev.ilionx.workshop`:

- `api/<feature>/` — feature modules (`owner`, `pet`, `vet`, `visit`), each with `controller/`, `service/`, `repository/`, `model/` (+ `model/request/`, `model/response/`, `model/mapper/`, `model/validator/`)
- `api/Paths.java` — shared path constants
- `common/` — `config/` (WebSecurityConfig, OpenAPIConfig, RequestMatcherConfig, properties), `security/filter/`, `exception/` (`ApiErrorCode` enum)

New features get their own package under `api/` with the same layering.

## Layering rules

- **Never expose JPA entities through the API.** Use separate `CreateXRequest`/`UpdateXRequest` and `XResponse` DTOs, mapped via MapStruct.
- **Controllers** are thin: validate (`Validator.validateAndThrow`), delegate to service, map entity → response DTO. Constructor injection via Lombok `@RequiredArgsConstructor`.
- **Services** hold business logic, are `@Transactional` (`readOnly = true` for queries), and work with entities + repositories. Not-found: `orElseThrow(() -> new DataNotFoundException(X_NOT_FOUND))` with an `ApiErrorCode` constant.
- **Repositories** are Spring Data JPA interfaces.
- **Entities** use Lombok `@Getter @Setter @NoArgsConstructor`, `@Entity @Table(name = "...")`, explicit `@Column(name = "snake_case")` names, `@GeneratedValue(strategy = GenerationType.IDENTITY)`.
- All REST paths come from `api/Paths.java` constants — never inline path strings.

## Adding a new feature (backend checklist)

1. Liquibase changeset for schema (see database-migrations skill)
2. Entity class with JPA annotations
3. Request and response DTOs
4. MapStruct mapper
5. Spring Data JPA repository
6. Service with business logic
7. Controller with REST endpoints
8. Tests: unit test for service, integration test for controller (see testing-standards skill)

## API-first contract

This server owns the OpenAPI contract consumed by `client/`. Spec served at `GET /api/v1/public/docs`. After changing controllers/models, regenerate client types: `../scripts/openapi-sync.sh` (starts server, downloads `server/openapi.json`, runs `bun run generate:api` in `client/`), then `bun run check` in `client/`.

## Examples

Controller endpoint (from `OwnerController.java`):

```java
@Tag(name = "Owner", description = "Owner management endpoints")
@RestController
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService ownerService;
    private final OwnerMapper ownerMapper;
    private final OwnerValidator ownerValidator;

    @ResponseStatus(CREATED)
    @Operation(summary = "Create owner", description = "Creates a new pet owner")
    @PostMapping(path = OWNERS, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<OwnerResponse> createOwner(@RequestBody final CreateOwnerRequest request) {
        ownerValidator.validateAndThrow(request);
        final Owner owner = ownerService.create(request);
        return ResponseEntity.status(CREATED).body(ownerMapper.toResponse(owner));
    }
}
```

Service with error code (from `OwnerService.java`):

```java
@Transactional(readOnly = true)
public Owner findById(final Integer id) {
    return ownerRepository.findById(id)
        .orElseThrow(() -> new DataNotFoundException(OWNER_NOT_FOUND));
}
```

MapStruct mapper (from `OwnerMapper.java`):

```java
@Mapper(config = SharedMapperConfig.class)
public abstract class OwnerMapper {

    public abstract OwnerResponse toResponse(Owner owner);

    public abstract List<OwnerResponse> toResponseList(List<Owner> owners);

    @Mapping(source = "type.name", target = "typeName")
    public abstract PetSummaryResponse toPetSummaryResponse(Pet pet);
}
```

Path constants (from `Paths.java`):

```java
public static final String OWNERS = BASE_PATH + "/owners";
public static final String OWNER_BY_ID = OWNERS + ID_PART;   // "/v1/owners/{id}"
```
