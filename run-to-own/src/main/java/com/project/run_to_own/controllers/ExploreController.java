package com.project.run_to_own.controllers;

import com.project.run_to_own.dto.TileDataDto;
import com.project.run_to_own.model.Athlete;
import com.project.run_to_own.model.User;
import com.project.run_to_own.repositories.TileRepository;
import com.project.run_to_own.repositories.UserRepository;
import com.project.run_to_own.service.TileService;
import com.project.run_to_own.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/explore")
public class ExploreController {

    private final TileService tileService;
    private final UserService userService;
    private final TileRepository tileRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public ExploreController(TileService tileService, UserService userService, TileRepository tileRepository, UserRepository userRepository) {
        this.tileService = tileService;
        this.userService = userService;
        this.tileRepository = tileRepository;
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    /**
     * ✅ NEW: Processes a user's ENTIRE Strava history, page by page.
     * This is a long-running, one-time operation for new users.
     */
    @PostMapping("/process-full-history")
    public ResponseEntity<String> processFullHistory(HttpSession session) {
        User currentUser = getOrCreateUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) {
            return ResponseEntity.status(401).body("Access token not found in session");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        int page = 1;
        int totalProcessed = 0;
        try {
            while (true) {
                String url = "https://www.strava.com/api/v3/athlete/activities?per_page=100&page=" + page;
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                        url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {}
                );

                List<Map<String, Object>> activities = response.getBody();
                if (activities == null || activities.isEmpty()) {
                    break; // Stop when we get an empty page
                }

                processActivities(currentUser, activities);
                totalProcessed += activities.size();
                page++;
            }
        } catch (HttpClientErrorException e) {
            // Log the error for debugging
            System.err.println("Strava API error during full history sync: " + e.getMessage());
            // Return a user-friendly error message
            return ResponseEntity.status(e.getStatusCode()).body("Error fetching data from Strava: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            // Catch other potential exceptions (network issues, etc.)
            System.err.println("An unexpected error occurred during full history sync: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }

        // After processing, set the sync timestamp so future syncs are quick
        currentUser.setLastSyncTimestamp(Instant.now());
        userRepository.save(currentUser);

        return ResponseEntity.ok("Full history sync complete. Processed " + totalProcessed + " activities.");
    }

    /**
     * ✅ RENAMED & IMPROVED: Processes only NEW runs since the last sync.
     * This is the method the "Sync Latest Runs" button should call.
     */
    @PostMapping("/process-new-runs")
    public ResponseEntity<String> processNewRuns(HttpSession session) {
        User currentUser = getOrCreateUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).body("User not authenticated");
        }
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) {
            return ResponseEntity.status(401).body("Access token not found in session");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        String url = "https://www.strava.com/api/v3/athlete/activities?per_page=50";
        if (currentUser.getLastSyncTimestamp() != null) {
            url += "&after=" + currentUser.getLastSyncTimestamp().getEpochSecond();
        }

        List<Map<String, Object>> activities;
        try {
            ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, httpEntity, new ParameterizedTypeReference<>() {}
            );
            activities = response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (HttpClientErrorException e) {
            System.err.println("Strava API error during new run sync: " + e.getMessage());
            return ResponseEntity.status(e.getStatusCode()).body("Error fetching data from Strava: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during new run sync: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An unexpected error occurred.");
        }

        if (!activities.isEmpty()) {
            processActivities(currentUser, activities);
        }

        currentUser.setLastSyncTimestamp(Instant.now());
        userRepository.save(currentUser);

        return ResponseEntity.ok("Processed " + (activities != null ? activities.size() : 0) + " new activities.");
    }

    // Helper method to find/create user, used by both endpoints
    private User getOrCreateUser(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) return null;

        return userService.findOrCreateUser(athlete);
    }

    // Helper method to process a list of activities
    private void processActivities(User user, List<Map<String, Object>> activities) {
        double totalDistanceOfSync = 0;
        for (Map<String, Object> activity : activities) {
            if (activity.get("distance") instanceof Number) {
                totalDistanceOfSync += ((Number) activity.get("distance")).doubleValue();
            }
            tileService.processStravaActivity(user, activity);
        }
        user.addDistance(totalDistanceOfSync);
        // The final save is handled by the calling method
    }

    @GetMapping("/tiles")
    public List<TileDataDto> getAllTiles(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        Long currentUserId = (athlete != null) ? athlete.getId() : -1L;
        return tileRepository.findAllByOwnerIdIsNotNull().stream()
                .map(tile -> new TileDataDto(
                        tile.getH3Index(),
                        tile.getOwnerName(),
                        tile.getOwnerId().equals(currentUserId)
                ))
                .collect(Collectors.toList());
    }
}