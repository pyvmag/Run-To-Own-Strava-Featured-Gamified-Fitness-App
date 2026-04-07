package com.project.run_to_own.controllers;

import com.project.run_to_own.model.Athlete;
import com.project.run_to_own.model.Friendship;
import com.project.run_to_own.model.FriendshipStatus;
import com.project.run_to_own.model.User;
import com.project.run_to_own.repositories.FriendshipRepository;
import com.project.run_to_own.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/friendships")
public class FriendshipController {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipController(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String username, HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) { return ResponseEntity.status(401).build(); }
        Long currentUserId = athlete.getId();

        List<User> foundUsers = userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(username.toLowerCase())
                        && !user.getId().equals(currentUserId))
                .collect(Collectors.toList());
        return ResponseEntity.ok(foundUsers);
    }

    @PostMapping("/request")
    public ResponseEntity<String> sendFriendRequest(@RequestParam Long addresseeId, HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) { return ResponseEntity.status(401).body("You are not logged in."); }
        Long requesterId = athlete.getId();

        if (requesterId.equals(addresseeId)) {
            return ResponseEntity.badRequest().body("You cannot send a friend request to yourself.");
        }

        User requester = userRepository.findById(requesterId).orElse(null);
        User addressee = userRepository.findById(addresseeId).orElse(null);

        if (requester == null || addressee == null) { return ResponseEntity.notFound().build(); }

        boolean requestExists = friendshipRepository.findByRequesterAndAddressee(requester, addressee).isPresent() ||
                friendshipRepository.findByRequesterAndAddressee(addressee, requester).isPresent();

        if (requestExists) {
            return ResponseEntity.status(409).body("A friend request already exists.");
        }

        Friendship newRequest = new Friendship();
        newRequest.setRequester(requester);
        newRequest.setAddressee(addressee);
        newRequest.setStatus(FriendshipStatus.PENDING);
        newRequest.setActionTimestamp(Instant.now());
        friendshipRepository.save(newRequest);
        return ResponseEntity.ok("Friend request sent.");
    }

    @GetMapping("/requests/pending")
    public ResponseEntity<List<Friendship>> getPendingRequests(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) { return ResponseEntity.status(401).build(); }
        User currentUser = userRepository.findById(athlete.getId()).orElse(null);
        if (currentUser == null) { return ResponseEntity.status(404).body(null); }
        List<Friendship> pendingRequests = friendshipRepository.findByAddresseeAndStatus(currentUser, FriendshipStatus.PENDING);
        return ResponseEntity.ok(pendingRequests);
    }

    @PostMapping("/requests/accept")
    public ResponseEntity<String> acceptRequest(@RequestParam Long requesterId, HttpSession session) {
        return handleRequest(requesterId, session, FriendshipStatus.ACCEPTED);
    }

    @PostMapping("/requests/decline")
    public ResponseEntity<String> declineRequest(@RequestParam Long requesterId, HttpSession session) {
        return handleRequest(requesterId, session, FriendshipStatus.DECLINED);
    }

    @GetMapping("/accepted")
    public ResponseEntity<List<Friendship>> getAcceptedFriends(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) { return ResponseEntity.status(401).build(); }
        User currentUser = userRepository.findById(athlete.getId()).orElse(null);
        if (currentUser == null) { return ResponseEntity.status(404).body(null); }
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendshipsForUser(currentUser);
        return ResponseEntity.ok(friendships);
    }

    private ResponseEntity<String> handleRequest(Long requesterId, HttpSession session, FriendshipStatus newStatus) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) { return ResponseEntity.status(401).body("You are not logged in."); }

        User addressee = userRepository.findById(athlete.getId()).orElse(null);
        User requester = userRepository.findById(requesterId).orElse(null);

        if (addressee == null || requester == null) { return ResponseEntity.notFound().build(); }

        Friendship request = friendshipRepository.findByRequesterAndAddresseeAndStatus(requester, addressee, FriendshipStatus.PENDING).orElse(null);

        if (request == null) {
            return ResponseEntity.status(404).body("No pending friend request found from this user.");
        }

        request.setStatus(newStatus);
        request.setActionTimestamp(Instant.now());
        friendshipRepository.save(request);
        return ResponseEntity.ok("Friend request " + newStatus.toString().toLowerCase() + ".");
    }
}