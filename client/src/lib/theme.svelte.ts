import { browser } from '$app/environment';

/**
 * Theme support for VetHub.
 *
 * Modes:
 * - 'light'  — force light tokens
 * - 'dark'   — force dark tokens (.dark class on <html>)
 * - 'system' — follow prefers-color-scheme (live via matchMedia)
 * - 'disco'  — dark tokens + animated gradient background (.disco class)
 *
 * The selection is persisted to localStorage under `vethub-theme`.
 * A matching inline script in app.html applies classes before first
 * paint to avoid a flash of the wrong theme (FOUC).
 */
export type ThemeMode = 'light' | 'dark' | 'system' | 'disco';

export const THEME_STORAGE_KEY = 'vethub-theme';

const VALID_MODES: ThemeMode[] = ['light', 'dark', 'system', 'disco'];

function readStoredMode(): ThemeMode {
	if (!browser) return 'system';
	try {
		const stored = localStorage.getItem(THEME_STORAGE_KEY);
		if (stored && (VALID_MODES as string[]).includes(stored)) {
			return stored as ThemeMode;
		}
	} catch {
		// localStorage unavailable (e.g. privacy mode) — fall back to system
	}
	return 'system';
}

function themeColorFor(mode: ThemeMode, resolvedDark: boolean): string {
	if (mode === 'disco') return '#2e1065';
	return resolvedDark ? '#0b1220' : '#e8003d';
}

class ThemeState {
	mode = $state<ThemeMode>(readStoredMode());

	systemDark = $state(browser ? window.matchMedia('(prefers-color-scheme: dark)').matches : false);

	#mediaQuery: MediaQueryList | null = browser ? window.matchMedia('(prefers-color-scheme: dark)') : null;
	#onChange = (event: MediaQueryListEvent): void => {
		this.systemDark = event.matches;
		this.apply();
	};

	constructor() {
		if (!browser) return;
		this.#mediaQuery?.addEventListener('change', this.#onChange);
		this.apply();
	}

	/** The mode's resolved dark flag: dark/disco are always dark, system follows the OS. */
	get resolvedDark(): boolean {
		if (this.mode === 'dark' || this.mode === 'disco') return true;
		if (this.mode === 'system') return this.systemDark;
		return false;
	}

	/** Select a theme and persist it; classes and theme-color meta are updated. */
	setMode(mode: ThemeMode): void {
		this.mode = mode;
		if (browser) {
			try {
				localStorage.setItem(THEME_STORAGE_KEY, mode);
			} catch {
				// ignore storage failures; theme still applies for this session
			}
		}
		this.apply();
	}

	/** Sync <html> classes and the theme-color meta with the current state. */
	apply(): void {
		if (!browser) return;
		const root = document.documentElement;
		root.classList.toggle('dark', this.resolvedDark);
		root.classList.toggle('disco', this.mode === 'disco');
		document
			.querySelector('meta[name="theme-color"]')
			?.setAttribute('content', themeColorFor(this.mode, this.resolvedDark));
	}

	/** Stop listening for OS color-scheme changes (app teardown). */
	destroy(): void {
		this.#mediaQuery?.removeEventListener('change', this.#onChange);
	}
}

export const theme = new ThemeState();
