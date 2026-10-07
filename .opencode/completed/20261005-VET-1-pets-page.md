---
epic: "EPIC"
title: "Pets Page — Searchable Patient List with CRUD"
ticket: VET-1
estimate: M
status: ready
created: 2026-10-05
depends_on: []
labels: [frontend]
priority: P1
---
## 1. User Story
**As a** receptionist
**I want** a Pets page at `/pets` where I can search patients and add, edit, or delete them directly
**So that** I can look up and manage patient records without navigating through an owner first

## 2. Business Context & Value
The home page "Pets" quick-link is currently dead (`href="/pets"` but no route exists). Receptionists must find an owner first to reach any pet data. A direct, searchable, CRUD-capable pets page removes that friction for the most common front-desk task.

## 3. Acceptance Criteria
- [ ] **AC1: Page exists and is reachable**
  - Given the app is running, When I click "Pets" on the home page, Then `/pets` loads showing all pets in a table (Name, Type badge, Owner, Birth Date, # Visits) with a result count.
- [ ] **AC2: Client-side search**
  - Given the pets list is loaded, When I type in the search box, Then the table filters live (case-insensitive) on pet name, pet type, and owner name. Empty state shown when no matches.
- [ ] **AC3: Add pet**
  - Given I click "Add Pet", When I fill the form (name, birth date, type, owner via dropdown) and submit, Then the pet is created via `POST /v1/pets`, the list refreshes, and a success toast appears. Validation errors are shown inline.
- [ ] **AC4: Edit pet**
  - Given I click Edit on a row, When I change fields and save, Then `PUT /v1/pets/{id}` is called, the list updates, and a success toast appears.
- [ ] **AC5: Delete pet**
  - Given I click Delete on a row, When I confirm in a dialog, Then `DELETE /v1/pets/{id}` is called, the pet disappears from the list, and a success toast appears. Cancel leaves the list untouched.
- [ ] **AC6: Row expansion**
  - Given a pet has visits, When I click the row, Then it inline-expands showing that pet's visits (date + description). Clicking again collapses.
- [ ] **AC7: Loading & error states**
  - Given the API is loading/fails, Then a loading spinner is shown while fetching, and an error toast appears on failure.
- [ ] **AC8: Verification gates pass**
  - `./gradlew build` in `server/` (only if backend touched) and `bun run check` in `client/` succeed.

## 4. Technical Requirements
- **API Changes**: None. Reuse existing `GET /v1/pets`, `POST /v1/pets`, `PUT /v1/pets/{id}`, `DELETE /v1/pets/{id}` (PetGlobalController). Frontend API layer `client/src/lib/api/pet/PetController.ts` already exposes all methods.
- **Database**: None.
- **Security**: Existing app-wide Basic Auth; no additional role restrictions.
- **Performance**: Client-side filtering over the full pets list; fine at clinic-scale data volume.

## 5. Design & UI/UX

```
┌────────────────────────────────────────────────────────────┐
│ 🐾 Pets                                    [+ Add Pet]     │
├────────────────────────────────────────────────────────────┤
│ [🔍 Search pets by name, type, or owner...        ]        │
├──────┬─────────┬────────────┬────────────┬────────┬────────┤
│ Name │ Type    │ Owner      │ Birth Date │ Visits │ Actions│
├──────┼─────────┼────────────┼────────────┼────────┼────────┤
│ ▸ Max│ [Dog]   │ Smith      │ 2020-03-15 │   3    │ ✏️ 🗑️  │
│   └─ Visits: 2024-01-10 rabies shot · 2023-06-02 checkup  │
│ ▸ Luna│ [Cat]  │ Jones      │ 2019-11-02 │   1    │ ✏️ 🗑️  │
├──────┴─────────┴────────────┴────────────┴────────┴────────┤
│ 5 pets                                                     │
└────────────────────────────────────────────────────────────┘
```

## 6. Implementation Notes
- **Existing code to reuse**:
  - `client/src/routes/owners/+page.svelte` — pattern template (Svelte 5 runes `$state`/`$derived`, Table primitives, search Input with icon, loading spinner card, empty state, result count footer, toasts).
  - `client/src/lib/components/pets/PetForm.svelte` — reuse for add/edit.
  - `client/src/lib/api/pet/PetController.ts` — `getPets()`, `getPetById()`, create/update/delete methods.
  - `client/src/lib/api/owner/OwnerController.ts` — `getOwners()` for the owner dropdown (CreatePetRequest requires `ownerId`).
  - shadcn-svelte primitives: `table`, `input`, `button`, `badge`, `select`, `dialog`; `svelte-sonner` toasts; lucide-svelte icons.
- **New code needed**: `client/src/routes/pets/+page.svelte` (+ optional small inline-expand component for visits). Owner Select added alongside PetForm when invoked from `/pets`.
- **Patterns to follow**: No backend changes → no `scripts/openapi-sync.sh` run needed. Verification: `bun run check` in `client/`.

## 7. Out of Scope
- Dedicated pet detail page/route
- Server-side (paginated) search
- Visit CRUD from this page
- Role-based access control beyond existing Basic Auth
