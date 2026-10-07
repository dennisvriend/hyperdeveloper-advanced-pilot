---
description: Close a story — final gate, move to completed, changelog (usage /done VET-5)
agent: build
---
The harness already ran the final gate for story $1:

!`scripts/tdd-gate.sh done $1`

- If the output says GATE FAILED: report the reason and stop. Change nothing.
- If it says GATE PASSED: add one row to `.opencode/CHANGELOG.md`
  (`| yyyymmdd | <story title> | <one-line summary> |`) based on the story and
  `git status --short`, then show me the row. Do not commit.
