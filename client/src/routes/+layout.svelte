<script lang="ts">
	import '../app.css';
	import { Toaster } from 'svelte-sonner';
	import { Header, Footer } from '$lib/components/layout';

	let { children } = $props();
</script>

<Toaster position="top-right" richColors closeButton />

<div class="app-shell flex min-h-screen flex-col bg-background">
	<Header />

	<main class="flex-1">
		{@render children()}
	</main>

	<Footer />
</div>
