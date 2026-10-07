---
description: Full TDD lifecycle for a refined story (usage /build VET-5)
agent: build
---
Build story $1 with TDD. Run the phases strictly in order.
A phase is only finished when its gate prints GATE PASSED; never skip a gate.

1. RED — write failing tests only under `server/src/test/` (skill: `testing-standards`).
   Gate: `scripts/tdd-gate.sh red`
2. GREEN — implement under `server/src/main/` (skills: `spring-conventions`, `database-migrations`).
   Never modify tests. Gate: `scripts/tdd-gate.sh green`
3. CONTRACT — if the API changed run `scripts/openapi-sync.sh`; if the story is labelled
   frontend, implement it in `client/` (skill: `svelte-patterns`). Gate: `scripts/tdd-gate.sh verify`
4. STOP — report per acceptance criterion: test → result. Ask me to review and run `/done $1`.

Story (injected by the harness):

!`cat .opencode/refined/$1-*.md 2>/dev/null || echo "NO STORY FOUND for '$1' in .opencode/refined/ — stop and tell the user."`
