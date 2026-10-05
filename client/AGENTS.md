# Client (vethub)

SvelteKit 2 / Svelte 5 (runes) / TypeScript / Vite 7 / Tailwind CSS 4 / shadcn-svelte frontend for the vethub monorepo. Package manager is **bun** (pinned 1.3.0 in root `mise.toml`) — ignore `package-lock.json`.

## Commands

Run from `client/`:

```bash
bun install            # install dependencies
bun run dev            # dev server (expects backend on localhost:8080)
bun run build          # production build
bun run check          # svelte-check typecheck (run this after every change)
bun run check:watch    # typecheck in watch mode
bun run sync:api       # full API sync: start server, download openapi.json, regenerate types
bun run generate:api   # regenerate types only (from ../server/openapi.json)
```

There are no test, lint, or format scripts. Verification = `bun run check`.

## Architecture

- `src/routes/` — SvelteKit file routing: `owners/` (list, `[id]`, `[id]/edit`, `[id]/pets/[petId]`, `[id]/pets/[petId]/visits/new`, `owners/new`), `vets/` (`[id]`, `[id]/edit`, `vets/new`), `visits/`
- `src/lib/api/` — domain API layer: `client.ts` (openapi-fetch typed client with Basic Auth, SSR-safe), `models.ts`, per-domain controllers (`owner/`, `pet/`, `pet-type/`, `specialty/`, `vet/`, `visit/`)
- `src/lib/components/` — `layout/`, `owners/`, `pets/`, `vets/`, plus `ui/` (shadcn-svelte primitives: badge, button, card, dialog, dropdown-menu, input, label, select, separator, table, textarea)
- `src/lib/config/constants.ts` — `SERVER_BASE_URL`, `API_USERNAME`, `API_PASSWORD`
- `src/lib/utils.ts` — `cn` helper (clsx + tailwind-merge)

## API-first workflow

- `src/lib/types/api.d.ts` and `src/lib/types/openapi.json` are **generated** (openapi-typescript from the server spec). Never hand-edit them.
- If the API contract changed, regenerate: `bun run sync:api` (or `../scripts/openapi-sync.sh`), then `bun run check`.
- API calls go through the typed `openapi-fetch` client in `src/lib/api/client.ts` — do not use raw fetch.

## Frontend-Backend integration

The frontend uses openapi-fetch with auto-generated TypeScript types. After changing backend endpoints:

1. Start the backend: `cd server && ./gradlew bootRun`
2. Regenerate types: `cd client && bun run generate:api` (or `bun run sync:api` to do both steps automatically)
3. Generated types land in `src/lib/types/api.d.ts`

## Style

- Svelte 5 runes syntax (`$state`, `$derived`, `$props`, `$effect`) — not legacy `let`/`export let` reactivity
- Tailwind CSS 4, CSS-first: theme lives in `src/app.css` (custom "ilionx Paws" brand: ilionx Red #E8003D, Poppins/JetBrains Mono, light mode only). No `tailwind.config` file.
- shadcn-svelte components live in `src/lib/components/ui/` (config in `components.json`, baseColor slate)
- Icons: `lucide-svelte`; toasts: `svelte-sonner`; dates: `@internationalized/date`
- `vite.config.ts` defines global `__APP_VERSION__` from package.json
