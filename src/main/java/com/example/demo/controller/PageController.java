package com.example.demo.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        boolean isAuthenticated = accessToken != null && !accessToken.isEmpty();
        String displayName = (String) session.getAttribute("display_name");

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("displayName", displayName);

        return "home";
    }

    @GetMapping("/test")
    public String test(HttpSession session, Model model) {
        // Check if user is authenticated
        String accessToken = (String) session.getAttribute("access_token");
        boolean isAuthenticated = accessToken != null && !accessToken.isEmpty();
        String displayName = (String) session.getAttribute("display_name");

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("displayName", displayName);

        return "test";
    }

}
