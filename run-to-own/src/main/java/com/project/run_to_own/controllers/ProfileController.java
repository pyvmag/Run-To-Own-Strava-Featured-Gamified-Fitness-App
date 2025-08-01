package com.project.run_to_own.controllers;

import com.project.run_to_own.model.Athlete;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Controller
public class ProfileController {

    private final RestTemplate restTemplate;

    public ProfileController() {
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile"; // profile.html
    }

    /**
     * Return athlete info from session for profile page
     */
    @GetMapping("/strava/athlete")
    @ResponseBody
    public Athlete getLoggedInAthlete(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) {
            Athlete fallback = new Athlete();
            fallback.setFirstname("Unknown");
            fallback.setLastname("");
            return fallback;
        }
        return athlete;
    }

    /**
     * Fetch athlete stats from Strava API
     */
    @GetMapping("/strava/profile/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAthleteStats(HttpSession session) {
        String accessToken = (String) session.getAttribute("access_token");
        Athlete athlete = (Athlete) session.getAttribute("athlete");

        if (accessToken == null || athlete == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String statsUrl = "https://www.strava.com/api/v3/athletes/" + athlete.getId() + "/stats";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            ResponseEntity<Map> response = restTemplate.exchange(
                    statsUrl,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception ex) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("message", "Unable to fetch stats at the moment.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallback);
        }
    }

    /**
     * Fetch gear (bikes + shoes)
     */
    @GetMapping("/strava/profile/gear")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAthleteGear(HttpSession session) {
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            // Get athlete info from Strava
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.strava.com/api/v3/athlete",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            Map<String, Object> athleteData = response.getBody();
            if (athleteData == null) return ResponseEntity.ok(Collections.emptyList());

            List<Map<String, Object>> gear = new ArrayList<>();
            if (athleteData.containsKey("bikes")) {
                gear.addAll((List<Map<String, Object>>) athleteData.get("bikes"));
            }
            if (athleteData.containsKey("shoes")) {
                gear.addAll((List<Map<String, Object>>) athleteData.get("shoes"));
            }

            return ResponseEntity.ok(gear);
        } catch (Exception ex) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}
