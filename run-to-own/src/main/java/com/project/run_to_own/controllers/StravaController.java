package com.project.run_to_own.controllers;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.*;
@Controller
public class StravaController {
    private final RestTemplate restTemplate = new RestTemplate();
    @GetMapping("/strava/activities")
    @ResponseBody
    public List<Map<String, Object>> getActivities(
            HttpSession session,
            @RequestParam(defaultValue = "5") int count,
            @RequestParam(defaultValue = "0") int offset) {

        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) return List.of();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        String listUrl = "https://www.strava.com/api/v3/athlete/activities?per_page=" + count + "&page=" + (offset / count + 1);
        ResponseEntity<List> response = restTemplate.exchange(listUrl, HttpMethod.GET, new HttpEntity<>(headers), List.class);

        List<Map<String, Object>> activities = new ArrayList<>();
        if (response.getBody() != null) {
            for (Object obj : response.getBody()) {
                try {
                    Map<String, Object> act = (Map<String, Object>) obj;
                    Long actId = ((Number) act.get("id")).longValue();
                    String detailUrl = "https://www.strava.com/api/v3/activities/" + actId;
                    ResponseEntity<Map> detailResponse = restTemplate.exchange(detailUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
                    if (detailResponse.getBody() != null) {
                        activities.add(detailResponse.getBody());
                    }
                } catch (Exception ignored) {}
            }
        }

        return activities;
    }}