// File: src/main/java/com/project/run_to_own/model/TileUserStats.java
package com.project.run_to_own.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tile_user_stats")
public class TileUserStats {

    @EmbeddedId
    private TileUserStatsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tileId")
    @JoinColumn(name = "tile_id")
    private Tile tile;

    @Column(name = "total_distance")
    private double totalDistanceInMeters = 0.0;

    public TileUserStats() {}

    public TileUserStats(User user, Tile tile) {
        this.user = user;
        this.tile = tile;
        this.id = new TileUserStatsId(tile.getH3Index(), user.getId());
    }

    public void addDistance(double meters) {
        this.totalDistanceInMeters += meters;
    }

    // Getters and Setters
    public TileUserStatsId getId() { return id; }
    public void setId(TileUserStatsId id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Tile getTile() { return tile; }
    public void setTile(Tile tile) { this.tile = tile; }
    public double getTotalDistanceInMeters() { return totalDistanceInMeters; }
    public void setTotalDistanceInMeters(double totalDistanceInMeters) { this.totalDistanceInMeters = totalDistanceInMeters; }
}