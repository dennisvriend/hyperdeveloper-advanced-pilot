---
description: Brief a refined story (usage /story VET-5)
agent: plan
---
Brief me on story $1 in at most 5 bullets: goal, acceptance criteria count,
layers touched (db/backend/frontend), risks, and the first test you would write.

Story file (injected by the harness, not looked up by the model):

!`cat .opencode/refined/$1-*.md 2>/dev/null || echo "NO STORY FOUND for '$1' in .opencode/refined/"`
