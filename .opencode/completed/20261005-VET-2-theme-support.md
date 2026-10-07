---
epic: "UI/UX Polish"
title: "Theme Support — Light / Dark / System / Disco"
estimate: M
status: ready
created: 2026-10-05
depends_on: []
labels: [frontend]
priority: P2
---

## 1. User Story

**As a** VetHub user
**I want** to switch between light, dark, system, and disco theme, with my choice remembered
**So that** the app is comfortable to use in any lighting, feels polished, and can be fun

## 2. Business Context & Value

The app currently ships light mode only (the `ilionx Paws` design system in
`app.css` is explicitly light-only). A theme picker is a low-cost, high-visibility
polish feature: it improves usability in low-light environments, respects user
OS preferences, and makes the app feel modern. The theme state will be designed
to extend to additional themes later (fun/branded themes are a planned follow-up).

## 3. Acceptance Criteria

- [ ] **AC1: Four theme options**
  - Given the theme dropdown in the header
  - When I select Light, Dark, System, or Disco
  - Then the UI switches immediately, including the Toaster and all shadcn-svelte components
- [ ] **AC2: System default on first visit**
  - Given no stored preference
  - When I open the app
  - Then the theme follows `prefers-color-scheme` and updates live when the OS setting changes
- [ ] **AC3: Preference persists**
  - Given I selected a theme
  - When I reload or revisit
  - Then my selection is applied from `localStorage`, overriding system
- [ ] **AC4: No flash of wrong theme (FOUC)**
  - Given a stored preference of Dark
  - When the page loads
  - Then dark tokens apply before first paint (inline script in `app.html`)
- [ ] **AC5: Dark palette reviewed**
  - Dark mode keeps ilionx Red as primary on dark surfaces; the palette is an
    explicitly authored `.dark` token block, not an automatic inversion
- [ ] **AC6: Disco theme animates the background**
  - Given I select Disco
  - When I look at any page
  - Then the page background shows an animated gradient cycling through
    disco-style colors (CSS keyframes), while text/cards stay readable
    (foreground/card tokens remain dark-surface based)
- [ ] **AC7: Disco respects reduced motion**
  - Given the OS has `prefers-reduced-motion: reduce`
  - When Disco is active
  - Then the animation does not run — a static colorful gradient is shown instead

## 4. Technical Requirements

- **API Changes**: none (frontend only)
- **Database**: none
- **Security**: none (no user data leaves the browser; preference in `localStorage`)
- **Performance**: theme applied pre-paint via inline script; `matchMedia` listener
  for live system-mode changes must be cleaned up on component destroy

### Implementation detail

- **Tailwind 4 (CSS-first)** in `src/app.css`:
  - Add `@custom-variant dark (&:where(.dark, .dark *));`
  - Add a `.dark { ... }` token block: dark surfaces for background/card/popover,
    adjusted foregrounds, borders, muted, chart colors; keep `--primary` as
    ilionx Red (`hsl(344 100% 45%)`) with white foreground
- **Disco theme** in `src/app.css`:
  - A `html.disco` block: keeps dark-surface foreground/card tokens for
    readability, overrides `--background` with a colorful animated gradient
  - `@keyframes` (e.g. `disco-hue`) animating a large multi-stop
    `linear-gradient` / `background-position` or `filter: hue-rotate` cycle
    (~8–15s loop, subtle speed)
  - Wrapped in `@media (prefers-reduced-motion: no-preference)` so reduced
    motion users get a static gradient
- **Theme state**: new `src/lib/theme.svelte.ts` (Svelte 5 runes module)
  - Mode type: `'light' | 'dark' | 'system' | 'disco'`
  - Persists selection to `localStorage` (e.g. key `vethub-theme`)
  - Resolves `system` via `window.matchMedia('(prefers-color-scheme: dark)')`
    and subscribes to changes while mode is `system`
  - Toggles the `dark` class on `<html>` for `dark`/`disco` (disco builds on
    dark-surface tokens) and the `disco` class for disco
- **FOUC prevention**: small inline `<script>` in `src/app.html` that reads
  localStorage and sets the `dark` class on `<html>` before the app hydrates;
  update the `theme-color` meta per active mode
- **Verification**: `bun run check` in `client/`; manual visual pass over
  owners, pets, vets, visits pages, dialogs, and toasts in all four modes

## 5. Design & UI/UX

Theme dropdown added to the Header (desktop) and inside the mobile menu.

```
┌──────────────────────────────────────────────────────────┐
│ [logo]   Home  Owners  Vets  Visits      [🌙 Theme ▾]  ☰ │
├──────────────────────────────────────────────────────────┤
│  ┌────────────────┐                                      │
│  │ ☀️ Light       │                                      │
│  │ 🌙 Dark        │                                      │
│  │ 💻 System   ✓  │                                      │
│  │ 🪩 Disco       │                                      │
│  └────────────────┘                                      │
└──────────────────────────────────────────────────────────┘
```

- Dropdown trigger icon reflects current resolved mode (Sun / Moon / Monitor / Sparkles)
- Active option is marked with a check
- Dropdown item labels: Light, Dark, System, Disco

## 6. Implementation Notes

- **Existing code to reuse**:
  - `src/lib/components/ui/dropdown-menu/` (shadcn-svelte primitive)
  - `src/lib/components/layout/Header.svelte` (add trigger + dropdown; also
    expose in mobile menu section)
  - `lucide-svelte` icons: `Sun`, `Moon`, `Monitor`, `Sparkles` (disco trigger; `🪩` emoji or `Disc3` as item icon)
  - Existing token structure in `src/app.css` (extend, don't replace)
- **New code needed**:
  - `src/lib/theme.svelte.ts` — theme state module
  - `.dark` token block + `@custom-variant dark` in `app.css`
  - `html.disco` token block + `@keyframes disco` animation in `app.css`
  - Inline pre-paint script in `app.html`
- **Patterns to follow**: Svelte 5 runes (`$state`, `$derived`, `$effect`),
  `cn` helper for conditional classes, CSS-var-driven styling (no hardcoded
  colors in components)

## 7. Out of Scope

- Additional fun/branded themes beyond Disco (future tickets; theme state is
  designed to extend)
- Backend changes
- Per-user server-side preference storage
