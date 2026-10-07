---
epic: "Vet Management"
title: "Add phone number to Vet (backend)"
estimate: S
status: ready
created: 2026-10-07
depends_on: []
labels: [backend]
priority: P2
---

## 1. User Story

**As a** clinic staff member
**I want** to store and retrieve a phone number for each veterinarian
**So that** I can contact the vet directly when needed (e.g. urgent cases, schedule changes)

## 2. Business Context & Value

Vets currently only have a name and specialties in the system. A phone number is the most basic contact detail missing; without it staff must look up vet contact info outside the application. This is the first contact field for the Vet entity and establishes the pattern for future fields (email, emergency contact).

## 3. Acceptance Criteria

- [ ] **AC1: Phone number is stored on the Vet entity**
  - Given a Liquibase migration adding a nullable `phone` column to the `vets` table
  - When the application starts on a fresh and on an existing database
  - Then the schema contains the new column and existing rows have `phone = NULL`

- [ ] **AC2: Phone can be set and updated via the API**
  - Given a valid `POST /api/v1/vets` request body containing `"phone": "+31 70 123 4567"`
  - When the request is processed
  - Then the created vet persists the phone value exactly as provided, and `GET /api/v1/vets/{id}` returns it in `VetResponse`
  - And `PUT /api/v1/vets/{id}` with a different phone value updates it

- [ ] **AC3: Phone is optional on create and update**
  - Given a create or update request without a `phone` field (or with `null`)
  - When the request is processed
  - Then it succeeds (no 4xx) and the stored value is `null`

- [ ] **AC4: Phone length is validated**
  - Given a create or update request with a phone value longer than 20 characters
  - When the request is processed
  - Then the API rejects it with a 400 validation error, and no data is written

- [ ] **AC5: Phone appears in list responses**
  - Given vets with phone numbers exist
  - When `GET /api/v1/vets` is called
  - Then each `VetResponse` in the list includes the `phone` field
  - And `VetSummaryResponse` (used in visit attribution) also includes `phone`

- [ ] **AC6: OpenAPI contract updated**
  - Given the server changes are complete
  - When `scripts/openapi-sync.sh` runs
  - Then the generated OpenAPI spec and client types include the new optional `phone` field on the vet schemas (client types are generated, not hand-edited; no frontend UI work)

## 4. Technical Requirements

- **API Changes**:
  - `CreateVetRequest`: add optional `phone` field (`@Size(max = 20)`, Schema example `"+31 70 123 4567"`, `RequiredMode.NOT_REQUIRED`)
  - `UpdateVetRequest`: same field, same constraints
  - `VetResponse` and `VetSummaryResponse`: add `phone` (required in response sense — always present as a key, may be `null`)
  - No endpoint changes — existing `POST/PUT/GET /v1/vets` and `/v1/vets/{id}` carry the new field
- **Database**: Liquibase changeset adding nullable `VARCHAR(20)` column `phone` to `vets`, following the timestamp-ENV-slug naming convention, registered in `db.changelog-master.yaml`. Never via JPA ddl-auto.
- **Security**: no new endpoints; existing auth applies. Phone is treated as non-sensitive contact data.
- **Performance**: additive nullable column; no impact.

## 5. Design & UI/UX

Not applicable — backend only. No frontend pages or components change in this story.

## 6. Implementation Notes

- **Existing code to reuse**:
  - `server/src/main/java/dev/ilionx/workshop/api/vet/model/Vet.java` — add `phone` field with `@Column(name = "phone")` (nullable by default)
  - `server/src/main/java/dev/ilionx/workshop/api/vet/model/request/CreateVetRequest.java` and `UpdateVetRequest.java` — add field following the existing `@Schema` + `@Accessors(chain = true)` pattern
  - `server/src/main/java/dev/ilionx/workshop/api/vet/model/response/VetResponse.java` and `VetSummaryResponse.java` — add field
  - `server/src/main/java/dev/ilionx/workshop/api/vet/model/mapper/VetMapper.java` — MapStruct maps `phone` automatically (same name on both sides); no explicit mapping needed. Note: entity↔request mapping happens in `VetService.create/update` via manual setters — add `vet.setPhone(request.getPhone())` there
  - Follow the Liquibase rules in `.opencode/skills/database-migrations`
- **New code needed**: one Liquibase changeset; the field additions listed above; test coverage
- **Patterns to follow**: mirror how `firstName`/`lastName` flow through the stack; add Bean Validation on request DTOs (`@Size(max = 20)`); update `VetControllerTest` (MockMvc, Given/When/Then) and `VetServiceTest` (Mockito + Hamcrest) per `.opencode/skills/testing-standards`
- **Verification**: `./gradlew build` in `server/`, then `scripts/openapi-sync.sh` and `bun run check` in `client/` (generated types only — no hand-written frontend changes)

## 7. Out of Scope

- Any frontend UI changes (forms, columns, detail views showing phone)
- Phone number format validation beyond max length (no E.164 or regex enforcement)
- Phone numbers for owners or other entities
- Uniqueness constraints on phone
- Internationalization/normalization of phone formats
