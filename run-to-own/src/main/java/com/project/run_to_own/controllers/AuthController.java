package com.project.run_to_own.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.run_to_own.model.Athlete;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Controller
public class AuthController {

    @Value("${strava.clientId}")
    private String clientId;

    @Value("${strava.clientSecret}")
    private String clientSecret;

    @Value("${strava.redirectUri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping("/")
    public String loginPage(HttpSession session) {
        return (session.getAttribute("access_token") != null) ? "redirect:/home" : "login";
    }

    @GetMapping("/strava/authorize")
    public String redirectToStrava() {
        return "redirect:https://www.strava.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&redirect_uri=" + redirectUri
                + "&response_type=code"
                + "&scope=read,activity:read";
    }

    @GetMapping("/strava/callback")
    public String handleCallback(String code, HttpSession session) {
        String tokenUrl = "https://www.strava.com/oauth/token";

        Map<String, String> params = new HashMap<>();
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, params, Map.class);
        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("access_token")) {
            String accessToken = (String) body.get("access_token");
            session.setAttribute("access_token", accessToken);

            // Fetch athlete profile
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            ResponseEntity<Map> athleteResp = restTemplate.exchange(
                    "https://www.strava.com/api/v3/athlete",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Map.class
            );

            if (athleteResp.getBody() != null) {
                Athlete athleteObj = mapper.convertValue(athleteResp.getBody(), Athlete.class);
                session.setAttribute("athlete", athleteObj);
            }
        }
        return "redirect:/home";
    }
}
