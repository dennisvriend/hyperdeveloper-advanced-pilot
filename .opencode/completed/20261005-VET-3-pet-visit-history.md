---
epic: "Client Visit History"
title: "View Pet Visit History — Improve Limited Visits Page"
ticket: VET-3
estimate: M
status: ready
created: 2026-10-05
depends_on: []
labels: [frontend]
priority: P1
---
## 1. User Story
**As a** receptionist
**I want** visits shown with full context (pet, type, owner, date, description) and click-through to the pet
**So that** I can answer client calls about past visits without decoding raw IDs

## 2. Business Context & Value
Clients call the clinic asking about their pet's visit history. The data exists today, but the global `/visits` page is limited: it shows a raw `Pet #{petId}` column (no pet name, no owner), search only matches description/date, and the "view pet details" button is disabled. This story makes visit history readable and reachable using existing data only. It is deliverable 1 of 2; deliverable 2 (VET-4) closes the domain model gap — Visit has no relationship to Vet, so it is impossible to tell which vet performed a visit.

**Interview decisions:**
- Audience: staff only (no owner login exists; single Basic Auth credential)
- Detail level: full context (pet name + type badge + owner) on every visit row
- Sorting: newest first, fixed
- Volume: clinic-scale client-side join/filter; no pagination

## 3. Acceptance Criteria
- [ ] **AC1: Visits show full context**
  - Given the `/visits` page is loaded
  - When visits render
  - Then each row shows Date, Pet (name + type badge), Owner, Description — resolved by a client-side join of `getVisits()` + `getPets()` + `getOwners()`. A visit whose pet was deleted shows "Unknown pet" instead of failing.
- [ ] **AC2: Search extended**
  - Given the `/visits` search box
  - When I type
  - Then rows filter live (case-insensitive) on pet name, owner name, description, and date; empty state shown when nothing matches.
- [ ] **AC3: Click-through to pet history**
  - Given a visit row
  - When I click the row action (replacing the currently disabled "coming soon" button)
  - Then I navigate to `/owners/{ownerId}/pets/{petId}`.
- [ ] **AC4: Newest-first ordering**
  - Given the visits list is loaded
  - When the table renders
  - Then visits are sorted by date, newest first.
- [ ] **AC5: Pet detail visit count**
  - Given a pet with visits on `/owners/{ownerId}/pets/{petId}`
  - When the page loads
  - Then the Visit History header shows a count, e.g., "Visit History (3)".
- [ ] **AC6: Loading and error states**
  - Given the API is loading or fails
  - Then a spinner shows while fetching and an error toast appears on failure.
- [ ] **AC7: Verification gate passes**
  - `bun run check` in `client/` succeeds. No backend changes in this story.

## 4. Technical Requirements
- **API Changes**: None. Reuse `GET /v1/visits`, `GET /v1/pets`, `GET /v1/owners`; the client API layer already exposes `getVisits()`, `getPets()`, `getOwners()`.
- **Database**: None.
- **Security**: Existing app-wide Basic Auth; no changes.
- **Performance**: Client-side join + filtering over full lists; fine at clinic-scale data volume.

## 5. Design & UI/UX

```
Global /visits page:
┌────────────────────────────────────────────────────────────────────┐
│ 📅 Visits                                                          │
│ [🔍 Search by pet, owner, description, or date...]                 │
├────────────┬─────────────────┬────────────────┬───────────────────┤
│ Date       │ Pet             │ Owner          │ Description       │
├────────────┼─────────────────┼────────────────┼───────────────────┤
│ Sep 4, '23 │ Samantha [Cat]→ │ Jean Coleman   │ Spayed            │
│ Jun 4, '23 │ Max [Dog]    →  │ Jean Coleman   │ Neutered          │
├────────────┴─────────────────┴────────────────┴───────────────────┤
│ Showing 2 of 4 visits                                              │
└────────────────────────────────────────────────────────────────────┘
   → row action navigates to /owners/{ownerId}/pets/{petId}

Pet detail page (visit section):
┌──────────────────────────────────────────────┐
│ Visit History (3)             [+ Add Visit]  │
├──────────────────────────────────────────────┤
│ ● Spayed          September 4, 2023          │
│ ● Neutered        June 4, 2023               │
│ ● Rabies shot     March 4, 2023              │
└──────────────────────────────────────────────┘
```

## 6. Implementation Notes
- **Existing code to reuse**:
  - `client/src/routes/visits/+page.svelte` — extend in place (Table, search Input, runes `$state`/`$derived`, toasts, spinner already present)
  - `client/src/lib/api/visit/VisitController.ts`, `pet/PetController.ts`, `owner/OwnerController.ts` — typed API calls
  - `client/src/routes/owners/+page.svelte` and `/pets` page — patterns for client-side join/filter, type badges, result count
  - shadcn-svelte `table`, `badge`, `input`, `button`; lucide-svelte icons; `svelte-sonner` toasts
- **New code needed**: pet/owner lookup maps (`Map<petId, PetResponse>`, `Map<ownerId, OwnerResponse>`) derived once per load inside the visits page; extended `$derived` filter; enabled row action using `goto()` or Button `href`
- **Patterns to follow**: Svelte 5 runes; typed `openapi-fetch` client only (no raw fetch); no hand-edited generated types. Frontend-only → no `scripts/openapi-sync.sh` needed; verification is `bun run check`.

## 7. Out of Scope
- Vet column on visits / vet attribution anywhere (VET-4 — the domain model has no Visit↔Vet relationship yet)
- Server-side search or pagination
- Visit create/edit/delete UI on the global page
- Owner/client-facing portal (no login exists; staff-facing only)
