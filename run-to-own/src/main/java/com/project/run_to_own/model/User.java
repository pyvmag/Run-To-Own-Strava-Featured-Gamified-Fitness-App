package com.project.run_to_own.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {

    @Id
    private Long id; // The Strava Athlete ID

    private String username;
    private String email;
    private double totalDistance = 0.0;
    private Instant lastSyncTimestamp;

    // ✅ NEW FIELDS FOR STREAKS
    private int currentStreak = 0;
    private LocalDate lastActivityDate;

    // Add this field inside your User.java class
    private int bestStreak = 0;

    // --- Getters and Setters ---

    // Add these getter and setter methods
    public int getBestStreak() {
        return bestStreak;
    }
    public void setBestStreak(int bestStreak) {
        this.bestStreak = bestStreak;
    }

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

    public Instant getLastSyncTimestamp() {
        return lastSyncTimestamp;
    }

    public void setLastSyncTimestamp(Instant lastSyncTimestamp) {
        this.lastSyncTimestamp = lastSyncTimestamp;
    }

    // ✅ GETTERS AND SETTERS FOR STREAK FIELDS
    public int getCurrentStreak() {
        return currentStreak;
    }

    public void setCurrentStreak(int currentStreak) {
        this.currentStreak = currentStreak;
    }

    public LocalDate getLastActivityDate() {
        return lastActivityDate;
    }

    public void setLastActivityDate(LocalDate lastActivityDate) {
        this.lastActivityDate = lastActivityDate;
    }

    // --- Helper Methods ---

    public void addDistance(double meters) {
        this.totalDistance += meters;
    }
}