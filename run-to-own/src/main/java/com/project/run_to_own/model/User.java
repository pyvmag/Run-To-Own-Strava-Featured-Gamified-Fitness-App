package com.project.run_to_own.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id; // The Strava Athlete ID

    private String username;
    private String email;
    private double totalDistance = 0.0;

    // ✅ NEW FIELD to track the last sync time
    private Instant lastSyncTimestamp;

    // --- Getters and Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(double totalDistance) {
        this.totalDistance = totalDistance;
    }

    // ✅ NEW GETTER AND SETTER for the timestamp
    public Instant getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(Instant lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }

    // --- Helper Methods ---

    public void addDistance(double meters) {
        this.totalDistance += meters;
    }
}