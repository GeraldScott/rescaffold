package io.archton.scaffold.service;

import io.archton.scaffold.domain.User;
import io.archton.scaffold.exception.EntityNotFoundException;
import io.archton.scaffold.security.TokenInfo;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AuthService {

    private static final Logger log = Logger.getLogger(AuthService.class);

    @Inject
    UserService userService;

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "jwt.duration.minutes", defaultValue = "60")
    long tokenDurationMinutes;

    @Transactional
    public TokenInfo authenticate(String username, String password) {
        log.debugf("Authenticating user: %s", username);

        if (!userService.verifyPassword(username, password)) {
            log.warnf("Authentication failed for user: %s", username);
            return null;
        }

        User user = userService.findByUsername(username);
        if (user == null) {
            log.warnf("User not found after password verification: %s", username);
            return null;
        }

        // Update last login timestamp
        userService.updateLastLogin(user.id);

        // Generate and return the JWT token
        return generateToken(user);
    }

    private TokenInfo generateToken(User user) {
        if (user == null) {
            throw new EntityNotFoundException("User", "null");
        }

        // Extract role names from user roles
        List<String> roles = user.roles.stream()
                .map(role -> role.name)
                .collect(Collectors.toList());

        // Set expiration time
        Instant expirationTime = Instant.now().plus(Duration.ofMinutes(tokenDurationMinutes));

        // Build the JWT with standard claims
        String token = Jwt.issuer(issuer)
                .subject(user.username)
                .groups(new HashSet<>(roles))
                .claim("name", user.person != null ? user.person.getFullName() : user.username)
                .claim("userId", user.id)
                .claim("personId", user.person != null ? user.person.id : null)
                .issuedAt(Instant.now())
                .claim("nbf", Instant.now())
                .expiresAt(expirationTime)
                .sign();

        // Create TokenInfo with token and expiration
        return new TokenInfo(token, expirationTime);
    }

    @Transactional
    public TokenInfo refreshToken(String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new EntityNotFoundException("User", username);
        }

        return generateToken(user);
    }
}