package com.project.run_to_own.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "friendships")
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester; // The user who sent the request

    @ManyToOne
    @JoinColumn(name = "addressee_id", nullable = false)
    private User addressee; // The user who received the request

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FriendshipStatus status;

    private Instant actionTimestamp;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getRequester() { return requester; }
    public void setRequester(User requester) { this.requester = requester; }

    public User getAddressee() { return addressee; }
    public void setAddressee(User addressee) { this.addressee = addressee; }

    public FriendshipStatus getStatus() { return status; }
    public void setStatus(FriendshipStatus status) { this.status = status; }

    public Instant getActionTimestamp() { return actionTimestamp; }
    public void setActionTimestamp(Instant actionTimestamp) { this.actionTimestamp = actionTimestamp; }
}