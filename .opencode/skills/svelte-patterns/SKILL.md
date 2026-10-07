---
name: svelte-patterns
description: Frontend conventions for the vethub client — SvelteKit 2 routing, Svelte 5 runes syntax, typed openapi-fetch API layer, Tailwind CSS 4 CSS-first theming, shadcn-svelte components, bun commands. Use when writing or changing code in client/.
---

# When to use

Use when writing or modifying frontend code in `client/` (routes, components, API layer, styling).

## Commands (run from `client/`, package manager is **bun**)

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

- `src/routes/` — SvelteKit file routing (`owners/`, `pets/`, `vets/`, `visits/` with `[id]`, `edit`, `new` nested routes)
- `src/lib/api/` — domain API layer: `client.ts` (typed openapi-fetch client with Basic Auth, SSR-safe), `models.ts`, per-domain controllers (`owner/OwnerController.ts`, `pet/`, `pet-type/`, `specialty/`, `vet/`, `visit/`)
- `src/lib/components/` — `layout/`, `owners/`, `pets/`, `vets/`, plus `ui/` (shadcn-svelte primitives: badge, button, card, dialog, dropdown-menu, input, label, select, separator, table, textarea)
- `src/lib/config/constants.ts` — `SERVER_BASE_URL`, `API_USERNAME`, `API_PASSWORD`
- `src/lib/utils.ts` — `cn` helper (clsx + tailwind-merge)

## Rules

- **Svelte 5 runes only** — `$state`, `$derived`, `$props`, `$effect`. Never legacy `let` / `export let` reactivity.
- **Never hand-edit** `src/lib/types/api.d.ts` or `src/lib/types/openapi.json` — they are generated from the server spec. After backend contract changes: `bun run sync:api`, then `bun run check`.
- **No raw fetch** — all API calls go through the typed `openapi-fetch` client in `src/lib/api/client.ts`.
- API controller modules are thin (HTTP only, no business logic); pages hold state and logic.
- **Tailwind CSS 4, CSS-first**: theme lives in `src/app.css` ("ilionx Paws" brand: ilionx Red #E8003D, Poppins/JetBrains Mono, light mode only). No `tailwind.config` file.
- shadcn-svelte components in `src/lib/components/ui/` (config in `components.json`, baseColor slate).
- Icons: `lucide-svelte`; toasts: `svelte-sonner`; dates: `@internationalized/date`.
- `vite.config.ts` defines global `__APP_VERSION__` from package.json.

## Examples

Typed API controller (from `src/lib/api/owner/OwnerController.ts`):

```ts
import { client } from '$lib/api/client';
import type { OwnerResponse, CreateOwnerRequest } from '$lib/api/models';

export async function createOwner(request: CreateOwnerRequest): Promise<OwnerResponse> {
	const { data, error } = await client.POST('/v1/owners', { body: request });
	if (error) throw error;
	if (!data) throw new Error('Failed to create owner');
	return data;
}
```

Runes in a page (from `src/routes/owners/+page.svelte`):

```svelte
<script lang="ts">
	import { getOwners } from '$lib/api/owner/OwnerController';
	import type { OwnerResponse } from '$lib/api/models';
	import { toast } from 'svelte-sonner';

	let owners = $state<OwnerResponse[]>([]);
	let searchQuery = $state('');

	let filteredOwners = $derived(() =>
		searchQuery.trim() ? owners.filter((o) => o.lastName?.toLowerCase().includes(searchQuery.toLowerCase())) : owners
	);

	$effect(() => {
		getOwners()
			.then((data) => (owners = data))
			.catch(() => toast.error('Failed to load owners'));
	});
</script>

{#each filteredOwners() as owner (owner.id)}
	<a href="/owners/{owner.id}">{owner.firstName} {owner.lastName}</a>
{/each}
```
