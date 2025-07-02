package io.archton.scaffold.security;

import java.time.Instant;

public class TokenInfo {
    private String token;
    private Instant expiresAt;

    public TokenInfo(String token, Instant expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public long getExpiresIn() {
        return Instant.now().until(expiresAt, java.time.temporal.ChronoUnit.SECONDS);
    }
}