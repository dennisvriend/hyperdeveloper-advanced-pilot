package dev.ilionx.workshop.api.pet.repository;

import dev.ilionx.workshop.api.pet.model.Pet;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Pet} entities.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {

    /**
     * Finds all pets belonging to the specified owner.
     *
     * @param ownerId the owner's ID
     * @return list of pets for the given owner
     */
    List<Pet> findByOwnerId(Integer ownerId);

    /**
     * Finds all pets whose name contains the given value, ignoring case.
     *
     * @param name the name fragment to search for
     * @return list of pets with matching names
     */
    List<Pet> findByNameContainingIgnoreCase(String name);
}
