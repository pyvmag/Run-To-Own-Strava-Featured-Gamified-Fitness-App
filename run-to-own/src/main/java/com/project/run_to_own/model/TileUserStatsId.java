// File: src/main/java/com/project/run_to_own/model/TileUserStatsId.java
package com.project.run_to_own.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class TileUserStatsId implements Serializable {

    @Column(name = "tile_id")
    private String tileId;

    @Column(name = "user_id")
    private Long userId;

    public TileUserStatsId() {}

    public TileUserStatsId(String tileId, Long userId) {
        this.tileId = tileId;
        this.userId = userId;
    }

    // Getters and Setters
    public String getTileId() { return tileId; }
    public void setTileId(String tileId) { this.tileId = tileId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TileUserStatsId that = (TileUserStatsId) o;
        return Objects.equals(tileId, that.tileId) && Objects.equals(userId, that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileId, userId);
    }
}