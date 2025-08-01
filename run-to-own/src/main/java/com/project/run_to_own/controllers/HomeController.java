package com.project.run_to_own.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(HttpSession session) {
        if (session.getAttribute("access_token") == null) {
            return "redirect:/";
        }
        return "home"; // home.html
    }
}