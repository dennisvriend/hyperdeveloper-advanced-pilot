<script lang="ts">
	import { getVisits } from '$lib/api/visit/VisitController';
	import { getPets } from '$lib/api/pet/PetController';
	import { getOwners } from '$lib/api/owner/OwnerController';
	import type { OwnerResponse, PetResponse, VisitResponse } from '$lib/api/models';
	import { Button } from '$lib/components/ui/button';
	import { Badge } from '$lib/components/ui/badge';
	import { Input } from '$lib/components/ui/input';
	import * as Table from '$lib/components/ui/table';
	import { Calendar, Search, Loader2, ExternalLink } from 'lucide-svelte';
	import { toast } from 'svelte-sonner';

	let visits = $state<VisitResponse[]>([]);
	let pets = $state<PetResponse[]>([]);
	let owners = $state<OwnerResponse[]>([]);
	let loading = $state(true);
	let searchQuery = $state('');

	const petById = $derived(new Map(pets.map((pet) => [pet.id, pet])));
	const ownerById = $derived(new Map(owners.map((owner) => [owner.id, owner])));

	const filteredVisits = $derived.by(() => {
		const query = searchQuery.trim().toLowerCase();
		return visits
			.filter((visit) => {
				if (!query) return true;
				const pet = petById.get(visit.petId);
				const owner = pet ? ownerById.get(pet.ownerId) : undefined;
				return (
					visit.description.toLowerCase().includes(query) ||
					visit.date.toLowerCase().includes(query) ||
					(pet?.name?.toLowerCase().includes(query) ?? false) ||
					(owner
						? `${owner.firstName} ${owner.lastName}`.toLowerCase().includes(query)
						: false)
				);
			})
			.sort((a, b) => b.date.localeCompare(a.date));
	});

	async function loadVisits() {
		loading = true;
		try {
			[visits, pets, owners] = await Promise.all([getVisits(), getPets(), getOwners()]);
		} catch (e) {
			toast.error('Failed to load visits');
			console.error('Error loading visits:', e);
		} finally {
			loading = false;
		}
	}

	$effect(() => {
		loadVisits();
	});
</script>

<svelte:head>
	<title>Visits | VetHub</title>
</svelte:head>

<div class="container mx-auto px-4 py-8">
	<!-- Header -->
	<div class="mb-8">
		<div class="flex items-center gap-3 mb-2">
			<div class="flex h-10 w-10 items-center justify-center rounded-lg bg-primary/10">
				<Calendar class="h-5 w-5 text-primary" />
			</div>
			<h1 class="text-3xl font-bold text-foreground">Visits</h1>
		</div>
		<p class="text-muted-foreground">View all veterinary visits across all pets</p>
	</div>

	<!-- Search -->
	<div class="mb-6">
		<div class="relative max-w-md">
			<Search class="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
			<Input
				type="text"
				placeholder="Search by pet, owner, description, or date..."
				class="pl-10"
				bind:value={searchQuery}
			/>
		</div>
	</div>

	<!-- Content -->
	{#if loading}
		<div class="flex items-center justify-center py-12">
			<Loader2 class="h-8 w-8 animate-spin text-primary" />
		</div>
	{:else if visits.length === 0}
		<div class="rounded-lg border border-dashed p-12 text-center">
			<Calendar class="mx-auto h-12 w-12 text-muted-foreground/50" />
			<h3 class="mt-4 text-lg font-medium text-foreground">No visits yet</h3>
			<p class="mt-2 text-sm text-muted-foreground">
				Visits will appear here once they are recorded for pets.
			</p>
		</div>
	{:else}
		<div class="rounded-lg border bg-card">
			<Table.Root>
				<Table.Header>
					<Table.Row>
						<Table.Head>Date</Table.Head>
						<Table.Head>Pet</Table.Head>
							<Table.Head>Owner</Table.Head>
							<Table.Head>Vet</Table.Head>
							<Table.Head>Description</Table.Head>
						<Table.Head class="w-[100px]">Actions</Table.Head>
					</Table.Row>
				</Table.Header>
				<Table.Body>
					{#each filteredVisits as visit (visit.id)}
						{@const pet = petById.get(visit.petId)}
						{@const owner = pet ? ownerById.get(pet.ownerId) : undefined}
						<Table.Row>
							<Table.Cell class="font-medium">
								{new Date(visit.date).toLocaleDateString()}
							</Table.Cell>
							<Table.Cell>
								{#if pet}
									<span class="inline-flex items-center gap-2">
										<span class="font-medium">{pet.name}</span>
										{#if pet.type}
											<Badge variant="secondary">{pet.type.name}</Badge>
										{/if}
									</span>
								{:else}
									<span class="text-muted-foreground">Unknown pet</span>
								{/if}
							</Table.Cell>
							<Table.Cell>
								{#if owner}
									{owner.firstName} {owner.lastName}
								{:else}
									<span class="text-muted-foreground">—</span>
								{/if}
							</Table.Cell>
							<Table.Cell>
								{#if visit.vet}
									Dr. {visit.vet.firstName} {visit.vet.lastName}
								{:else}
									<span class="text-muted-foreground">—</span>
								{/if}
							</Table.Cell>
							<Table.Cell>{visit.description}</Table.Cell>
							<Table.Cell>
								{#if pet}
									<Button
										variant="ghost"
										size="sm"
										href="/owners/{pet.ownerId}/pets/{pet.id}"
										title="View pet details"
									>
										<ExternalLink class="h-4 w-4" />
									</Button>
								{/if}
							</Table.Cell>
						</Table.Row>
					{:else}
					<Table.Row>
						<Table.Cell colspan={6} class="text-center text-muted-foreground py-8">
							No visits match your search
						</Table.Cell>
					</Table.Row>
					{/each}
				</Table.Body>
			</Table.Root>
		</div>

		<p class="mt-4 text-sm text-muted-foreground">
			Showing {filteredVisits.length} of {visits.length} visits
		</p>
	{/if}
</div>
