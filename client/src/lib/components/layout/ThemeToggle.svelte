<script lang="ts">
	import { theme, type ThemeMode } from '$lib/theme.svelte';
	import { Button } from '$lib/components/ui/button';
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu';
	import { Sun, Moon, Monitor, Sparkles } from 'lucide-svelte';

	const options: { value: ThemeMode; label: string; icon: typeof Sun }[] = [
		{ value: 'light', label: 'Light', icon: Sun },
		{ value: 'dark', label: 'Dark', icon: Moon },
		{ value: 'system', label: 'System', icon: Monitor },
		{ value: 'disco', label: 'Disco', icon: Sparkles }
	];

	const triggerIcon = $derived(
		options.find((option) => option.value === theme.mode)?.icon ?? Monitor
	);
</script>

<DropdownMenu.Root>
	<DropdownMenu.Trigger>
		{#snippet child({ props })}
			<Button {...props} variant="ghost" size="sm" aria-label="Select theme">
				<triggerIcon class="h-4 w-4"></triggerIcon>
				<span class="hidden sm:inline">Theme</span>
			</Button>
		{/snippet}
	</DropdownMenu.Trigger>
	<DropdownMenu.Content align="end" class="w-40">
		<DropdownMenu.RadioGroup
			value={theme.mode}
			onValueChange={(value) => theme.setMode(value as ThemeMode)}
		>
			{#each options as option (option.value)}
				<DropdownMenu.RadioItem value={option.value}>
					<option.icon class="h-4 w-4" />
					{option.label}
				</DropdownMenu.RadioItem>
			{/each}
		</DropdownMenu.RadioGroup>
	</DropdownMenu.Content>
</DropdownMenu.Root>
