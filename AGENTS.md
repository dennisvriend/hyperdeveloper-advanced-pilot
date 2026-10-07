# vethub

Veterinary clinic management application. Fullstack monorepo with a Spring Boot backend (`server/`) and SvelteKit frontend (`client/`). Toolchain pinned in root `mise.toml` (temurin-25, bun 1.3.0, node 22). No root build — work inside `server/` or `client/`.

Domain knowledge lives in skills under `.opencode/skills/`:

- `spring-conventions` — backend architecture, layering, DTO/MapStruct rules, Gradle commands
- `svelte-patterns` — frontend runes, API layer, Tailwind 4/shadcn-svelte conventions, bun commands
- `database-migrations` — Liquibase changeset rules
- `testing-standards` — test base classes and patterns

## Universal rules

- The server owns the OpenAPI contract; client types are generated, never hand-edited. After changing server controllers/models run `scripts/openapi-sync.sh`, then `bun run check` in `client/`.
- Verification order: `server/`: `./gradlew build` (includes tests + quality checks); then `client/`: `bun run check`.
- Never commit secrets.
- Be concise; delegate research to subagents when it saves time.

## Workflow

When I tell you a feature is complete:

1. Move the story from `.opencode/refined/` to `.opencode/completed/`
2. Prepend the date to the filename (e.g., `20261005-VET-#-<slug>.md`)
3. Add a row to `.opencode/CHANGELOG.md`
