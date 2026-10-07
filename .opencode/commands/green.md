---
description: TDD GREEN — make the failing tests pass without touching them (usage /green VET-5)
agent: spring-agent
---
TDD phase GREEN for story $1.

Rules:
- Never modify anything under `server/src/test/`. The tests are the spec; the gate checks this with git.
- Load the `spring-conventions` and `database-migrations` skills before changing code.
- Make the smallest change that turns the tests green, following the story's technical notes.
- You are done when `scripts/tdd-gate.sh green` prints GATE PASSED.
- If the API contract changed, run `scripts/openapi-sync.sh`, then `scripts/tdd-gate.sh verify`.

Current test results (injected by the harness):

!`scripts/tdd-gate.sh status`

Story (injected by the harness):

!`cat .opencode/refined/$1-*.md 2>/dev/null || echo "NO STORY FOUND for '$1' in .opencode/refined/ — stop and tell the user."`
