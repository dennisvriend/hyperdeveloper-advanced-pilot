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

## Workflow (TDD)

Refined stories live in `.opencode/refined/`. Build them test-first:
`/red VET-#` → `/green VET-#` → `/done VET-#` (or `/build VET-#` for the whole cycle).
Gates are in `scripts/tdd-gate.sh`; a phase is done only when its gate passes.
Never modify tests to make them pass.
