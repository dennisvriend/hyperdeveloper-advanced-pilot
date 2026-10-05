<script lang="ts">
	import { page } from '$app/stores';
	import { Users, Stethoscope, Calendar, Home, Menu, X, Sun, Moon, Monitor, Sparkles, Check } from 'lucide-svelte';
	import { Button } from '$lib/components/ui/button';
	import { theme, type ThemeMode } from '$lib/theme.svelte';
	import ThemeToggle from './ThemeToggle.svelte';

	const themeOptions: { value: ThemeMode; label: string; icon: typeof Sun }[] = [
		{ value: 'light', label: 'Light', icon: Sun },
		{ value: 'dark', label: 'Dark', icon: Moon },
		{ value: 'system', label: 'System', icon: Monitor },
		{ value: 'disco', label: 'Disco', icon: Sparkles }
	];

	let mobileMenuOpen = $state(false);

	const navItems = [
		{ href: '/', label: 'Home', icon: Home },
		{ href: '/owners', label: 'Owners', icon: Users },
		{ href: '/vets', label: 'Veterinarians', icon: Stethoscope },
		{ href: '/visits', label: 'Visits', icon: Calendar }
	];

	function isActive(href: string): boolean {
		if (href === '/') {
			return $page.url.pathname === '/';
		}
		return $page.url.pathname.startsWith(href);
	}

	function closeMobileMenu() {
		mobileMenuOpen = false;
	}
</script>

<header class="sticky top-0 z-50 border-b border-border bg-card/95 backdrop-blur supports-[backdrop-filter]:bg-card/80">
	<div class="container mx-auto px-4">
		<div class="flex h-16 items-center justify-between">
			<!-- Logo -->
			<a href="/" class="flex items-center gap-2 transition-opacity hover:opacity-80">
				<img src="/logo-vethub.svg" alt="VetHub" class="h-10 w-auto" />
			</a>

			<!-- Desktop Navigation -->
			<nav class="hidden md:flex md:items-center md:gap-1">
				{#each navItems as item (item.href)}
					<a
						href={item.href}
						class="flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors
							{isActive(item.href)
								? 'bg-primary/10 text-primary'
								: 'text-muted-foreground hover:bg-muted hover:text-foreground'}"
					>
						<item.icon class="h-4 w-4" />
						{item.label}
					</a>
				{/each}
			</nav>

			<!-- Theme Picker + Mobile Menu Button -->
			<div class="flex items-center gap-1">
				<ThemeToggle />
				<Button
					variant="ghost"
					size="icon"
					class="md:hidden"
					onclick={() => (mobileMenuOpen = !mobileMenuOpen)}
					aria-label={mobileMenuOpen ? 'Close menu' : 'Open menu'}
				>
					{#if mobileMenuOpen}
						<X class="h-5 w-5" />
					{:else}
						<Menu class="h-5 w-5" />
					{/if}
				</Button>
			</div>
		</div>

		<!-- Mobile Navigation -->
		{#if mobileMenuOpen}
			<nav class="border-t border-border py-4 md:hidden">
				<div class="flex flex-col gap-1">
					{#each navItems as item (item.href)}
						<a
							href={item.href}
							onclick={closeMobileMenu}
							class="flex items-center gap-3 rounded-md px-3 py-3 text-sm font-medium transition-colors
								{isActive(item.href)
									? 'bg-primary/10 text-primary'
									: 'text-muted-foreground hover:bg-muted hover:text-foreground'}"
						>
							<item.icon class="h-5 w-5" />
							{item.label}
						</a>
					{/each}
					<!-- Theme options (mobile) -->
					<div class="mt-2 border-t border-border pt-3">
						<p class="px-3 pb-2 text-xs font-medium uppercase tracking-wider text-muted-foreground">
							Theme
						</p>
						<div class="flex flex-col gap-1">
							{#each themeOptions as option (option.value)}
								<button
									type="button"
									onclick={() => {
										theme.setMode(option.value);
										closeMobileMenu();
									}}
									class="flex items-center gap-3 rounded-md px-3 py-3 text-sm font-medium transition-colors
										{theme.mode === option.value
											? 'bg-primary/10 text-primary'
											: 'text-muted-foreground hover:bg-muted hover:text-foreground'}"
								>
									<option.icon class="h-5 w-5" />
									{option.label}
									{#if theme.mode === option.value}
										<Check class="ms-auto h-4 w-4" />
									{/if}
								</button>
							{/each}
						</div>
					</div>
				</div>
			</nav>
		{/if}
	</div>
</header>
