package com.project.run_to_own.repositories;

import com.project.run_to_own.model.TileUserStats;
import com.project.run_to_own.model.TileUserStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TileUserStatsRepository extends JpaRepository<TileUserStats, TileUserStatsId> {

    /**
     * Finds the single user stat record with the highest distance for a given tile.
     * This is how we determine the owner of a tile.
     * The underscore (_) tells Spring to look at the 'h3Index' property of the 'tile' object.
     * @param tileH3Index The H3 index of the tile to check.
     * @return An Optional containing the TileUserStats of the top runner (the owner).
     */
    Optional<TileUserStats> findTopByTile_H3IndexOrderByTotalDistanceInMetersDesc(String tileH3Index);

    /**
     * Finds the specific stats record for a single user in a single tile.
     * @param userId The ID of the user.
     * @param tileH3Index The H3 index of the tile.
     * @return An Optional containing the specific user's stats for that tile.
     */
    Optional<TileUserStats> findByUser_IdAndTile_H3Index(Long userId, String tileH3Index);

}