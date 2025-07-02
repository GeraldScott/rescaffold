package io.archton.scaffold.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.util.Optional;

@RequestScoped
public class SecurityContext {

    @Inject
    JsonWebToken jwt;

    public Optional<String> getCurrentUsername() {
        if (jwt == null || jwt.getName() == null) {
            return Optional.empty();
        }
        return Optional.of(jwt.getName());
    }

    public Optional<Long> getCurrentUserId() {
        if (jwt == null) {
            return Optional.empty();
        }
        Object userIdClaim = jwt.getClaim("userId");
        if (userIdClaim instanceof Number) {
            return Optional.of(((Number) userIdClaim).longValue());
        }
        return Optional.empty();
    }

    public Optional<Long> getCurrentPersonId() {
        if (jwt == null) {
            return Optional.empty();
        }
        Object personIdClaim = jwt.getClaim("personId");
        if (personIdClaim instanceof Number) {
            return Optional.of(((Number) personIdClaim).longValue());
        }
        return Optional.empty();
    }

    public boolean hasRole(String role) {
        if (jwt == null || jwt.getGroups() == null) {
            return false;
        }
        return jwt.getGroups().contains(role);
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }

    public boolean isUser() {
        return hasRole("ROLE_USER");
    }
}