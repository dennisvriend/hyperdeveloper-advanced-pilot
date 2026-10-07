---
name: testing-standards
description: Test patterns for the vethub server — @IntegrationTest vs @UnitTest base classes, MockMvc controller tests with Given/When/Then comments and @DisplayName, Mockito service unit tests with Hamcrest, factory helpers, test profile config. Use when writing or changing tests in server/src/test.
---

# When to use

Use when writing or modifying tests in `server/src/test/java`.

## Test infrastructure

- JUnit 5 via spring-boot-starter-test; Testcontainers available.
- Custom base classes in `src/test/java/.../support/`:
  - `@IntegrationTest` → extend `IntegrationTest` (full Spring context, H2, MockMvc via `WebMvcConfigurator`). Use for controller tests.
  - `@UnitTest` → extend `UnitTest` (no Spring context, `@ExtendWith(MockitoExtension.class)`, fast). Use for service/validator tests.
- Test config: `src/test/resources/application-test.yml` (H2, liquibase drop-first, basic auth).
- Reference examples: `VetControllerTest`, `PetServiceTest`, `OwnerValidatorTest`, `VisitGlobalControllerTest` — follow these patterns.

## Conventions

- One test class per production class: `XServiceTest` (unit), `XControllerTest` (integration).
- Every test method has `@DisplayName("Should ...")` written in behavior language.
- Body structured with `// Given:` / `// When:` / `// Then:` comments.
- Constants for all test values at the top of the class (`VALID_OWNER_ID`, `NON_EXISTENT_VET_ID = 999`, ...).
- Assertions: Hamcrest (`assertThat(x, is(equalTo(y)))`), `assertThrows` for exceptions. MockMvc: `jsonPath` + `status()`.
- Mocking: Mockito `mock(...)`, BDD style `given(...).willReturn(...)`, `verify(...)`.
- Fixtures come from the base classes: `UnitTest` provides `aValidOwner()`, `aValidPet()`, ...; `IntegrationTest` provides `aSavedOwner()`, `aCreateOwnerRequest()`, `anUpdateOwnerRequest()`, ... plus DB cleanup before/after each test (seed rows with id > seed maximum are preserved assumptions — check `cleanDatabase()` when adding new seed data).

## Examples

Controller integration test (from `VetControllerTest.java`):

```java
@DisplayName("Integration Test - Vet Controller")
class VetControllerTest extends IntegrationTest {

    @Test
    @DisplayName("Should return not found when vet ID does not exist")
    void shouldReturnNotFoundWhenVetIdDoesNotExist() throws Exception {
        // Given: No vet exists with ID 999

        // When: Getting vet by non-existent ID
        final ResultActions response = mockMvc.perform(get(VET_BY_ID, NON_EXISTENT_VET_ID));

        // Then: HTTP 404 Not Found should be returned with error message
        response.andExpect(status().isNotFound());

        final String content = response.andReturn().getResponse().getContentAsString();
        final ErrorResponseResource error = fromJson(content, ErrorResponseResource.class);
        assertThat(error.getErrorMessage(), is(equalTo(VET_NOT_FOUND.getReason())));
    }
}
```

Service unit test (from `PetServiceTest.java`):

```java
@DisplayName("Unit Test - Pet Service")
class PetServiceTest extends UnitTest {

    private PetRepository petRepository;
    private PetService petService;

    @BeforeEach
    void setUp() {
        petRepository = mock(PetRepository.class);
        petService = new PetService(petRepository, petTypeRepository, ownerRepository);
    }

    @Test
    @DisplayName("Should return pets when owner has pets")
    void shouldReturnPetsWhenOwnerHasPets() {
        // Given: An owner exists with 2 pets in the repository
        given(ownerRepository.findById(VALID_OWNER_ID)).willReturn(Optional.of(aValidOwner()));
        given(petRepository.findByOwnerId(VALID_OWNER_ID)).willReturn(List.of(aValidPet()));

        // When: Finding pets by owner ID
        final List<Pet> actualPets = petService.findByOwnerId(VALID_OWNER_ID);

        // Then: The pets should be returned
        assertThat(actualPets, hasSize(1));
    }
}
```

Run with `./gradlew test` from `server/`.
