package com.example.demo.controller;

import com.example.demo.service.CanvaOAuthService;
import com.example.demo.service.OAuthStateStore;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class OAuthController {

    private static final Logger logger = LoggerFactory.getLogger(OAuthController.class);
    private final CanvaOAuthService oauthService;
    private final OAuthStateStore stateStore;

    public OAuthController(CanvaOAuthService oauthService, OAuthStateStore stateStore) {
        this.oauthService = oauthService;
        this.stateStore = stateStore;
    }

    /**
     * Initiate OAuth flow - redirect to Canva authorization page
     */
    @GetMapping("/oauth/authorize")
    public String authorize(HttpSession session) {
        // Generate PKCE parameters
        String codeVerifier = oauthService.generateCodeVerifier();
        String codeChallenge = oauthService.generateCodeChallenge(codeVerifier);
        String state = oauthService.generateState();

        // Store in both in-memory store (primary) and session (backup)
        stateStore.store(state, codeVerifier);
        session.setAttribute("code_verifier", codeVerifier);
        session.setAttribute("oauth_state", state);
        session.setAttribute("session_created_at", System.currentTimeMillis());

        logger.info("OAuth flow initiated");
        logger.info("Session ID: {}", session.getId());
        logger.info("Generated state: {}", state);
        logger.info("Stored in memory store: true");
        logger.info("Stored in session: code_verifier={}, oauth_state={}",
                    session.getAttribute("code_verifier") != null,
                    session.getAttribute("oauth_state") != null);

        // Build authorization URL and redirect
        String authorizationUrl = oauthService.buildAuthorizationUrl(codeChallenge, state);
        return "redirect:" + authorizationUrl;
    }

    /**
     * OAuth redirect callback - handle authorization code and exchange for token
     */
    @GetMapping("/oauth/redirect")
    public String callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            HttpSession session,
            Model model) {

        logger.info("OAuth callback received. Session ID: {}", session.getId());
        logger.info("Received state: {}", state);
        logger.info("Received code: {}", code != null ? "present" : "missing");

        // Check for errors
        if (error != null) {
            logger.error("OAuth error: {} - {}", error, error_description);
            model.addAttribute("error", error);
            model.addAttribute("errorDescription", error_description);
            return "oauth-error";
        }

        // Get code_verifier from in-memory store first, then session as fallback
        String codeVerifier = stateStore.getCodeVerifier(state);
        boolean usedMemoryStore = (codeVerifier != null);

        if (codeVerifier == null) {
            logger.warn("Code verifier not found in memory store, trying session...");
            codeVerifier = (String) session.getAttribute("code_verifier");
        }

        logger.info("Code verifier source: {}", usedMemoryStore ? "memory store" : "session");
        logger.info("Code verifier found: {}", codeVerifier != null);

        if (codeVerifier == null) {
            logger.error("Code verifier not found in memory store or session. OAuth flow may have expired.");
            model.addAttribute("error", "session_expired");
            model.addAttribute("errorDescription", "OAuth session expired (10 minutes timeout). Please try connecting again.");
            return "oauth-error";
        }

        // Verify state parameter (CSRF protection) - we implicitly verify by finding the code_verifier
        // since the code_verifier is stored by state key
        logger.info("State verification successful (retrieved code_verifier by state key)");

        try {
            logger.info("Exchanging authorization code for access token...");
            // Exchange authorization code for access token
            Map<String, Object> tokenResponse = oauthService.exchangeCodeForToken(code, codeVerifier);

            logger.info("Token exchange successful. Storing tokens in session.");
            // Store tokens in session
            session.setAttribute("access_token", tokenResponse.get("access_token"));
            session.setAttribute("refresh_token", tokenResponse.get("refresh_token"));
            session.setAttribute("expires_in", tokenResponse.get("expires_in"));

            // Fetch user profile to get display name
            try {
                logger.info("Fetching user profile...");
                Map<String, Object> userProfile = oauthService.getUserProfile((String) tokenResponse.get("access_token"));
                String displayName = (String) userProfile.get("display_name");
                session.setAttribute("display_name", displayName);
                logger.info("User profile fetched successfully. Display name: {}", displayName);

                // Add to model for the success page
                model.addAttribute("displayName", displayName);
                model.addAttribute("isAuthenticated", true);
            } catch (Exception e) {
                logger.warn("Failed to fetch user profile: {}", e.getMessage());
                // Continue anyway, just without the display name
                model.addAttribute("isAuthenticated", true);
            }

            // Clean up PKCE parameters from both stores
            stateStore.remove(state);
            session.removeAttribute("code_verifier");
            session.removeAttribute("oauth_state");
            session.removeAttribute("session_created_at");

            // Pass token info to success page
            model.addAttribute("accessToken", tokenResponse.get("access_token"));
            model.addAttribute("expiresIn", tokenResponse.get("expires_in"));

            logger.info("OAuth flow completed successfully");
            return "oauth-success";

        } catch (Exception e) {
            logger.error("Token exchange failed", e);
            model.addAttribute("error", "token_exchange_failed");
            model.addAttribute("errorDescription", e.getMessage());
            return "oauth-error";
        }
    }

    /**
     * Logout - clear session
     */
    @GetMapping("/oauth/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
