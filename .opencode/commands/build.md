---
description: TDD lifecycle via specialist subagents (usage /build VET-5)
agent: build
---
Conduct story $1. Do not write or edit code yourself; delegate with the task tool and wait for each result.

1. task → `tester`: "RED for the story at <path>". Continue only if the reply contains `GATE PASSED`.
2. task → `spring-agent`: "GREEN for the story at <path>". Then run `scripts/tdd-gate.sh green` yourself; continue only if it passes.
3. Only if the story labels contain `frontend`: task → `svelte-agent`: "UI for the story at <path>". Then run `scripts/tdd-gate.sh verify` yourself.
4. Report per acceptance criterion: test → result. Ask me to review the diff and run `/done $1`.

Story path (injected by the harness):

!`ls .opencode/refined/$1-*.md 2>/dev/null || echo "NO STORY FOUND for '$1' — stop and tell the user."`
