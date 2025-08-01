package com.project.run_to_own.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Athlete {
    private Long id;
    private String username;
    private String firstname;
    private String lastname;
    private String city;
    private String country;

    @JsonProperty("profile")
    private String profile;          // Full-size profile picture

    @JsonProperty("profile_medium")
    private String profileMedium;    // Medium-size profile picture (for posts)

    // --- Default Constructor ---
    public Athlete() {}

    // --- Getters & Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getProfile() { return profile; }
    public void setProfile(String profile) { this.profile = profile; }

    public String getProfileMedium() { return profileMedium; }
    public void setProfileMedium(String profileMedium) { this.profileMedium = profileMedium; }
}
