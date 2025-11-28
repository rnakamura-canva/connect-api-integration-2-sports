package com.example.demo.service;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for OAuth state and PKCE parameters.
 * Used as a fallback when HTTP sessions don't work properly across redirects.
 */
@Component
public class OAuthStateStore {

    private final Map<String, StateData> stateStore = new ConcurrentHashMap<>();

    public void store(String state, String codeVerifier) {
        // Clean up expired entries first
        cleanExpired();

        StateData data = new StateData(codeVerifier, Instant.now().plusSeconds(600)); // 10 minutes TTL
        stateStore.put(state, data);
    }

    public String getCodeVerifier(String state) {
        StateData data = stateStore.get(state);
        if (data == null) {
            return null;
        }

        // Check if expired
        if (Instant.now().isAfter(data.expiresAt)) {
            stateStore.remove(state);
            return null;
        }

        return data.codeVerifier;
    }

    public void remove(String state) {
        stateStore.remove(state);
    }

    private void cleanExpired() {
        Instant now = Instant.now();
        stateStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiresAt));
    }

    private static class StateData {
        final String codeVerifier;
        final Instant expiresAt;

        StateData(String codeVerifier, Instant expiresAt) {
            this.codeVerifier = codeVerifier;
            this.expiresAt = expiresAt;
        }
    }
}
