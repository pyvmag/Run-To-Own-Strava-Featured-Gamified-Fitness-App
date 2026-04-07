package com.project.run_to_own.controllers;

import com.project.run_to_own.model.Athlete;
import com.project.run_to_own.model.User;
import com.project.run_to_own.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProfileController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    // A single constructor to initialize all dependencies
    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.restTemplate = new RestTemplate();
    }

    @GetMapping("/profile")
    public String profilePage() {
        return "profile";
    }

    @GetMapping("/strava/athlete")
    @ResponseBody
    public Athlete getLoggedInAthlete(HttpSession session) {
        return (Athlete) session.getAttribute("athlete");
    }

    @GetMapping("/strava/profile/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAthleteStats(HttpSession session) {
        String accessToken = (String) session.getAttribute("access_token");
        Athlete athlete = (Athlete) session.getAttribute("athlete");

        if (accessToken == null || athlete == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String url = "https://www.strava.com/api/vv3/athletes/" + athlete.getId() + "/stats";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class
            );
            return ResponseEntity.ok(response.getBody());
        } catch (Exception ex) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("message", "Stats fetch failed.");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(fallback);
        }
    }

    @GetMapping("/api/user/details")
    @ResponseBody
    public ResponseEntity<User> getCurrentUser(HttpSession session) {
        Athlete athlete = (Athlete) session.getAttribute("athlete");
        if (athlete == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return userRepository.findById(athlete.getId())
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/strava/profile/gear")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> getAthleteGear(HttpSession session) {
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://www.strava.com/api/v3/athlete",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );
            Map<String, Object> athleteData = response.getBody();
            List<Map<String, Object>> gearList = new ArrayList<>();
            if (athleteData != null) {
                if (athleteData.containsKey("bikes")) {
                    gearList.addAll((List<Map<String, Object>>) athleteData.get("bikes"));
                }
                if (athleteData.containsKey("shoes")) {
                    gearList.addAll((List<Map<String, Object>>) athleteData.get("shoes"));
                }
            }
            return ResponseEntity.ok(gearList);
        } catch (Exception ex) {
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}