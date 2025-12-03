package com.example.demo.service;

import com.example.demo.config.CanvaOAuthProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
public class CanvaOAuthService {

    private final CanvaOAuthProperties oauthProperties;
    private final RestClient restClient;

    public CanvaOAuthService(CanvaOAuthProperties oauthProperties) {
        this.oauthProperties = oauthProperties;
        this.restClient = RestClient.builder().build();
    }

    /**
     * Generate a random code verifier for PKCE
     */
    public String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    /**
     * Generate code challenge from code verifier using SHA-256
     */
    public String generateCodeChallenge(String codeVerifier) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate code challenge", e);
        }
    }

    /**
     * Generate a random state parameter for CSRF protection
     */
    public String generateState() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] state = new byte[32];
        secureRandom.nextBytes(state);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(state);
    }

    /**
     * Build the authorization URL for Canva OAuth
     */
    public String buildAuthorizationUrl(String codeChallenge, String state) {
        // Convert comma-separated scopes to space-separated format required by OAuth 2.0
        String scopesFormatted = oauthProperties.getScopes().replace(",", " ").trim();

        return UriComponentsBuilder.fromHttpUrl(oauthProperties.getAuthorizationUrl())
                .queryParam("client_id", oauthProperties.getClientId())
                .queryParam("redirect_uri", oauthProperties.getRedirectUri())
                .queryParam("scope", scopesFormatted)
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("code_challenge", codeChallenge)
                .queryParam("code_challenge_method", "S256")
                .build()
                .toUriString();
    }

    /**
     * Exchange authorization code for access token
     */
    public Map<String, Object> exchangeCodeForToken(String code, String codeVerifier) {
        // Create Basic Auth header
        String auth = oauthProperties.getClientId() + ":" + oauthProperties.getClientSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        // Build form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("code", code);
        formData.add("code_verifier", codeVerifier);
        formData.add("redirect_uri", oauthProperties.getRedirectUri());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        // Make token exchange request
        ResponseEntity<Map> response = restClient.post()
                .uri(oauthProperties.getTokenUrl())
                .headers(h -> h.addAll(headers))
                .body(formData)
                .retrieve()
                .toEntity(Map.class);

        return response.getBody();
    }

    /**
     * Refresh an access token using a refresh token
     */
    public Map<String, Object> refreshAccessToken(String refreshToken) {
        // Create Basic Auth header
        String auth = oauthProperties.getClientId() + ":" + oauthProperties.getClientSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        // Build form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);

        // Make token refresh request
        ResponseEntity<Map> response = restClient.post()
                .uri(oauthProperties.getTokenUrl())
                .headers(h -> h.addAll(headers))
                .body(formData)
                .retrieve()
                .toEntity(Map.class);

        return response.getBody();
    }

    /**
     * Get user profile information from Canva API
     */
    public Map<String, Object> getUserProfile(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        ResponseEntity<Map> response = restClient.get()
                .uri("https://api.canva.com/rest/v1/users/me/profile")
                .headers(h -> h.addAll(headers))
                .retrieve()
                .toEntity(Map.class);

        return response.getBody();
    }
}
