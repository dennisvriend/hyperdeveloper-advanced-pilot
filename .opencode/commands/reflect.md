---
description: Reflect on THIS session and propose improvements to one skill or agent (usage /reflect [name])
agent: build
---
Reflect on this session and propose improvements for: $ARGUMENTS

This command must run in the session you want to reflect on (no subtask):
the conversation above is the evidence.

## Inventory (injected by the harness)

Project skills (`.opencode/skills/<name>/SKILL.md`):
!`ls -1 .opencode/skills 2>/dev/null || echo "(none)"`

Project agents (`.opencode/agents/<name>.md`):
!`ls -1 .opencode/agents 2>/dev/null | sed 's/\.md$//' || echo "(none)"`

Global skills (`~/.config/opencode/skills/<name>/SKILL.md`):
!`ls -1 ~/.config/opencode/skills 2>/dev/null || echo "(none)"`

Global agents (`~/.config/opencode/agents/<name>.md`):
!`ls -1 ~/.config/opencode/agents 2>/dev/null | sed 's/\.md$//' || echo "(none)"`

Today: !`date +%Y-%m-%d`

## Steps

1. **Target** — use `$ARGUMENTS`. If it is empty or not in the inventory, use the
   question tool to let me pick one from the inventory. Prefer the project version
   when a name exists both globally and in the project.
2. **Read** the target file in full.
3. **Scan this conversation** for signals about that skill or agent:
   - 🔴 **Corrections** — I said "no", "not like that", "always / never do X", or I undid its work
   - 🟡 **Successes** — I said "perfect", "great", or accepted the result without changes
   - 🔵 **Edge cases** — workarounds, questions the file does not answer, permission denials
   Quote my words as evidence. No evidence, no change.
4. **Propose** at most 5 changes in this format:

```
┌─ Reflection: <name> (<skill|agent>, <project|global>) ─┐
│ Signals: X corrections, Y successes, Z edge cases       │
│                                                         │
│ 🔴 [HIGH] + <section>: "<exact text to add/change>"     │
│    evidence: "<quote from this session>"                │
│ 🟡 [MED]  + <section>: "<exact text to add/change>"     │
│ 🔵 [LOW]  ~ note: "<observation>"                       │
│                                                         │
│ Commit message: "<name>: <summary>"                     │
└─────────────────────────────────────────────────────────┘
```

5. **Ask** with the question tool: *Apply* · *Tweak* · *Save as observation* · *Discard*.
6. **Apply** — edit only the target file, then show the change:
   - project file: `git diff -- <file>`
   - global file: show the changed lines before/after
   Do not commit; give me the commit message to use.
7. **Save as observation** — append the box, dated, to `.opencode/reflect/OBSERVATIONS.md`
   (create the file if needed). Change nothing else.

## Rules

- Be specific: exact wording, exact section. No "improve clarity".
- One target per run. Never edit other skills, agents, commands or code.
- Changes to `permission:` in an agent's frontmatter are always 🔴 HIGH and must be called out
  as a security change: it changes what the harness allows.
- Keep skill `description`s short and in "what + when to use" form; they decide when a skill loads.
- Never apply anything without my explicit choice in step 5.
