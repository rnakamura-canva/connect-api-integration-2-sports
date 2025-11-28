package com.example.demo.controller;

import com.example.demo.canva.model.UserProfileResponse;
import com.example.demo.service.CanvaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    private final CanvaService canvaService;

    public PageController(CanvaService canvaService) {
        this.canvaService = canvaService;
    }

    /**
     * Helper method to add authentication info to model
     */
    private void addAuthInfo(HttpSession session, Model model) {
        String accessToken = (String) session.getAttribute("access_token");
        boolean isAuthenticated = accessToken != null && !accessToken.isEmpty();

        model.addAttribute("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            try {
                UserProfileResponse userProfile = canvaService.getUserProfile();
                String displayName = userProfile.getProfile() != null ?
                    userProfile.getProfile().getDisplayName() : null;
                model.addAttribute("displayName", displayName);
            } catch (Exception e) {
                model.addAttribute("displayName", null);
            }
        }
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        addAuthInfo(session, model);
        return "home";
    }

    @GetMapping("/test")
    public String test(HttpSession session, Model model) {
        addAuthInfo(session, model);
        return "test";
    }

}
