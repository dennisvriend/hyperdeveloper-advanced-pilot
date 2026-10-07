---
description: TDD RED — write failing tests for a refined story (usage /red VET-5)
agent: build
---
TDD phase RED for story $1.

Rules:
- Only create or change files under `server/src/test/`. Never touch `server/src/main/` or `client/`.
- Load the `testing-standards` skill first.
- Write one test per backend-testable acceptance criterion.
- You are done when `scripts/tdd-gate.sh red` prints GATE PASSED.
  If it fails, fix the tests (never production code) and run it again.
- Finish with a table: AC → test method → gate result.

Story (injected by the harness):

!`cat .opencode/refined/$1-*.md 2>/dev/null || echo "NO STORY FOUND for '$1' in .opencode/refined/ — stop and tell the user."`
