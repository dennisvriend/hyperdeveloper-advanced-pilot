---
epic: "Client Visit History"
title: "Record Which Vet Performed a Visit — Visit↔Vet Relationship"
ticket: VET-4
estimate: L
status: ready
created: 2026-10-05
depends_on: [VET-3]
labels: [backend, frontend]
priority: P1
---
## 1. User Story
**As a** clinic staff member
**I want** each visit to record which vet performed it, with a vet picker in the visit form and vet names shown in visit history
**So that** vets can see who handled a pet's previous care and history is complete when answering client questions

## 2. Business Context & Value
**Domain gap discovered during refinement:** the `Visit` entity (`server/src/main/java/dev/ilionx/workshop/api/visit/model/Visit.java`) holds only `id`, `date`, `description`, and `pet`. There is **no relationship between Visit and Vet** — it is impossible to tell which vet performed any visit, past or future. The API has full CRUD for visits but scoped to a pet (`/v1/owners/{ownerId}/pets/{petId}/visits`, plus a global `/v1/visits` surface). This story closes the gap end-to-end: Liquibase migration, JPA entity, request/response DTOs, MapStruct mapper, OpenAPI contract regeneration, and a vet picker in the visit form.

**Interview decisions:**
- Vet is **optional** on new visits (`vet_id` nullable) — historic rows stay NULL; backwards compatible with existing callers
- Demo data: TST seed changeset assigns vets to the 4 seeded visits
- Priority: P1, sequenced after VET-3 (display work lands first; this story builds on those views)

**Deliberate decisions:**
- FK uses **ON DELETE SET NULL** — deleting a vet must not destroy visit history
- `UpdateVisitRequest` gains `vetId` (null clears the assignment) so a mis-attributed visit can be corrected via API. No edit UI in this story.

## 3. Acceptance Criteria
- [ ] **AC1: Schema migration**
  - Given a fresh database and a database with existing visits
  - When Liquibase runs
  - Then `visits.vet_id` (INTEGER, NULLABLE) exists with FK to `vets(id)` ON DELETE SET NULL; existing rows keep `vet_id` NULL; app starts cleanly
- [ ] **AC2: Demo data seeded**
  - Given the TST seed changeset
  - When Liquibase runs
  - Then the 4 seeded visits have vets assigned (e.g., 1→2, 2→1, 3→3, 4→5)
- [ ] **AC3: Create visit with vet**
  - Given `POST /v1/owners/{ownerId}/pets/{petId}/visits` (or `POST /v1/visits`) with a valid `vetId`
  - When submitted
  - Then 201 is returned and `VisitResponse.vet` contains `{id, firstName, lastName}`; the value is persisted
- [ ] **AC4: Create visit without vet**
  - Given a create request omitting `vetId`
  - When submitted
  - Then the visit is created with `vet: null` (backwards compatible — existing callers unchanged)
- [ ] **AC5: Unknown vet rejected**
  - Given a create or update request with a nonexistent `vetId`
  - When submitted
  - Then 404 with `VET_NOT_FOUND` is returned and nothing is persisted
- [ ] **AC6: Update visit attribution**
  - Given `PUT /v1/visits/{id}` (or nested) with `vetId` set to a valid vet
  - When submitted
  - Then attribution is changed; with `vetId: null` the assignment is cleared
- [ ] **AC7: Visit responses carry vet**
  - Given any visit response (`VisitResponse`, and `VisitSummaryResponse` embedded in `PetResponse.visits`)
  - When serialized
  - Then it includes `vet: {id, firstName, lastName} | null`
- [ ] **AC8: Vet picker in visit form**
  - Given `/owners/{ownerId}/pets/{petId}/visits/new`
  - When the form loads
  - Then a "Veterinarian (optional)" Select is populated from `GET /v1/vets` with a "— none —" default; on submit the chosen `vetId` is sent; the created visit shows the vet on the pet history page
- [ ] **AC9: Vet shown in history views**
  - Given the pet detail visit cards and the global `/visits` table
  - When visits render
  - Then the vet name is shown ("—" when unattributed); deleting a vet sets affected visits' vet to null (verified via API)
- [ ] **AC10: Contract regenerated and gates pass**
  - `scripts/openapi-sync.sh` run after server changes; client types updated; `./gradlew build` in `server/` (unit + integration tests included) and `bun run check` in `client/` both succeed

## 4. Technical Requirements
- **API Changes** (no new endpoints; contract change only):
  - `CreateVisitRequest`: + optional `vetId` (Integer, nullable)
  - `UpdateVisitRequest`: + optional `vetId`; null clears attribution
  - `VisitResponse`, `VisitSummaryResponse`: + nested `vet: VetSummaryResponse | null` (new `VetSummaryResponse`: id, firstName, lastName)
  - Affected endpoints: `POST/PUT` on `/v1/visits`, `/v1/visits/{id}`, `/v1/owners/{ownerId}/pets/{petId}/visits(/...)`
- **Database**: New PRD changeset (auto-included via `includeAll` in `db.changelog-master.yaml`): `ALTER TABLE visits ADD COLUMN vet_id INTEGER` + FK to `vets(id)` ON DELETE SET NULL. Follow-up TST changeset assigning vets to the 4 seeded visits.
- **Security**: Existing Basic Auth; server-side validation that `vetId` references an existing vet (404 `VET_NOT_FOUND`).
- **Performance**: `@ManyToOne` on `Visit.vet`; keep `VetSummaryResponse` limited to id/name to keep payloads small.

## 5. Design & UI/UX

```
Visit form (/owners/{ownerId}/pets/{petId}/visits/new):
┌────────────────────────────┐
│ Visit Date  [2026-10-05]   │
│ Veterinarian (optional)    │
│ [ — none —            ▾]   │
│   James Carter (Radiology) │
│   Helen Leary (Radiology)  │
│ Description [________]     │
│              [Cancel][Add] │
└────────────────────────────┘

/visits table gains a Vet column:
┌──────┬────────┬───────┬─────────┬──────────┐
│ Date │ Pet    │ Owner │ Vet     │ Descr.   │
├──────┼────────┼───────┼─────────┼──────────┤
│ 9/4  │ Saman. │ Jean  │ H.Leary │ Spayed   │
│ 1/1  │ Saman. │ Jean  │ —       │ Rabies   │
└──────┴────────┴───────┴─────────┴──────────┘
```

## 6. Implementation Notes
- **Existing code to reuse**:
  - Backend: feature-module pattern (`api/visit/`); `VetRepository` + `VET_NOT_FOUND` error code for resolution; `VisitMapper` mapping style (`@Mapping(source="pet.id", target="petId")` → add vet mapping for the nested summary); embedded-DTO precedent (`VisitSummaryResponse` in `PetMapper`); test patterns `VisitGlobalControllerTest`, `VetControllerTest`
  - Frontend: `client/src/lib/api/vet/VetController.ts` `getVets()`; shadcn `select` primitive; `visits/new` page and both history views from VET-3
- **New code needed**:
  - `VetSummaryResponse` DTO; `vet` field on `Visit` entity (`@ManyToOne`, `@JoinColumn(name="vet_id")`); mapper updates; `VetRepository` injected into `VisitService` for create/update resolution
  - Liquibase changesets (PRD migration + TST seed)
  - Vet `Select` in the visit form; vet display in pet visit cards and `/visits` table
- **Patterns to follow**: API-first — server changes → `scripts/openapi-sync.sh` → regenerated `client/src/lib/types/api.d.ts` → `bun run check`; never expose entities; never hand-edit generated types
- **Tests**: unit test `VisitService` vet resolution (valid, unknown, null); integration tests for create-with-vet, create-without-vet, unknown-vet→404, update set/clear, and `vet` present in responses

## 7. Out of Scope
- Visit edit/delete UI (API supports correction; UI is a separate story)
- Backfilling/re-attributing real historic visits via UI or batch
- Making vet required on new visits (revisit with PO after adoption)
- Appointments, vet schedules, or vet-per-day assignment
- Audit trail of which staff user recorded the visit (no user model beyond shared Basic Auth)
- Visits list filtered by vet
