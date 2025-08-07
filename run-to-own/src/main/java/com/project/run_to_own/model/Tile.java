package com.project.run_to_own.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tiles")
public class Tile {

    @Id
    @Column(name = "h3_index")
    private String h3Index; // The unique H3 address for this tile (e.g., "8928308280fffff")

    @Column(name = "owner_id")
    private Long ownerId; // The ID of the User who currently owns this tile

    @Column(name = "owner_name")
    private String ownerName; // The username of the owner for quick lookups

    // Constructors
    public Tile() {}

    public Tile(String h3Index) {
        this.h3Index = h3Index;
    }

    // Getters and Setters
    public String getH3Index() {
        return h3Index;
    }

    public void setH3Index(String h3Index) {
        this.h3Index = h3Index;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }
}