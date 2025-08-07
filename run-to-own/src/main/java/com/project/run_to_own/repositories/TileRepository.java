package com.project.run_to_own.repositories;

import com.project.run_to_own.model.Tile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TileRepository extends JpaRepository<Tile, String> {

    /**
     * Finds all tiles currently owned by a specific user.
     * @param ownerId The ID of the user.
     * @return A list of tiles owned by the user.
     */
    List<Tile> findByOwnerId(Long ownerId);

}