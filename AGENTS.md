# vethub

Veterinary clinic management application. Fullstack monorepo with a Spring Boot backend (`server/`) and SvelteKit frontend (`client/`). Toolchain pinned in root `mise.toml` (temurin-25, bun 1.3.0, node 22). No root build — work inside `server/` or `client/`.

## Tech stack

- **Backend:** Spring Boot 4.1 (milestone), Java 25, Gradle
- **Frontend:** SvelteKit 2, Svelte 5 (runes API), TypeScript
- **Database:** H2 in-memory, managed by Liquibase migrations
- **Mapping:** MapStruct for entity-to-DTO conversion
- **API types:** openapi-fetch with auto-generated TypeScript types from the OpenAPI spec

## Domain model

- Owner has many Pets (one-to-many)
- Pet has many Visits (one-to-many)
- Pet has one PetType (many-to-one)
- Vet has many Specialties (many-to-many)
- Visit currently has no link to Vet (known gap)

Read the package-level instruction files before working there:

- `server/AGENTS.md` — Gradle commands, quality gates (`-Werror` build, spotless auto-format), Liquibase schema rules, test setup
- `client/AGENTS.md` — bun commands, `bun run check` as the only verification, Svelte 5 runes + Tailwind 4 conventions

## API-first contract flow

The server owns the OpenAPI contract; client types are generated, never hand-edited. After changing server controllers/models:

```bash
scripts/openapi-sync.sh   # starts server, downloads openapi.json, regenerates client types
```

Then run `bun run check` in `client/`. Details in both package AGENTS.md files:

1. Start the backend: `cd server && ./gradlew bootRun`
2. Regenerate types: `cd client && bun run generate:api` (or `bun run sync:api` to do both steps automatically)
3. Generated types land in `client/src/lib/types/api.d.ts`

## Verification order

1. `server/`: `./gradlew build` (includes tests + quality checks)
2. `client/`: `bun run check`
