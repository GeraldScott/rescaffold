package io.archton.scaffold.resource;

import io.archton.scaffold.security.TokenInfo;
import io.archton.scaffold.service.AuthService;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import java.util.Map;

@Path("/api/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Authentication operations")
public class AuthResource {

    private static final Logger log = Logger.getLogger(AuthResource.class);

    @Inject
    AuthService authService;

    @Inject
    JsonWebToken jwt;

    private Map<String, String> createErrorResponse(String message) {
        return Map.of("error", message);
    }

    @POST
    @Path("/login")
    @PermitAll
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT token")
    @APIResponse(responseCode = "200", description = "Authentication successful")
    @APIResponse(responseCode = "401", description = "Authentication failed")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response login(LoginRequest loginRequest) {
        log.debugf("POST /api/auth/login - Username: %s", loginRequest.getUsername());

        try {
            TokenInfo tokenInfo = authService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
            if (tokenInfo == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("Invalid username or password"))
                        .build();
            }

            TokenResponse response = new TokenResponse(
                    tokenInfo.getToken(),
                    "Bearer",
                    tokenInfo.getExpiresIn()
            );

            return Response.ok(response).build();
        } catch (Exception e) {
            log.error("Login failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("An error occurred during authentication"))
                    .build();
        }
    }

    @POST
    @Path("/refresh")
    @RolesAllowed({"ROLE_ADMIN", "ROLE_MANAGER", "ROLE_USER"})
    @Operation(summary = "Refresh token", description = "Refreshes the JWT token")
    @APIResponse(responseCode = "200", description = "Token refreshed successfully")
    @APIResponse(responseCode = "401", description = "Unauthorized")
    @APIResponse(responseCode = "500", description = "Internal server error")
    public Response refreshToken() {
        try {
            // Get the current user from the JWT token
            String username = jwt.getName();
            if (username == null || username.isEmpty()) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(createErrorResponse("No valid token found"))
                        .build();
            }

            TokenInfo tokenInfo = authService.refreshToken(username);

            TokenResponse response = new TokenResponse(
                    tokenInfo.getToken(),
                    "Bearer",
                    tokenInfo.getExpiresIn()
            );

            return Response.ok(response).build();
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(createErrorResponse("An error occurred during token refresh"))
                    .build();
        }
    }

    // Request and response classes
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class TokenResponse {
        private String accessToken;
        private String tokenType;
        private long expiresIn;

        public TokenResponse(String accessToken, String tokenType, long expiresIn) {
            this.accessToken = accessToken;
            this.tokenType = tokenType;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() {
            return accessToken;
        }

        public void setAccessToken(String accessToken) {
            this.accessToken = accessToken;
        }

        public String getTokenType() {
            return tokenType;
        }

        public void setTokenType(String tokenType) {
            this.tokenType = tokenType;
        }

        public long getExpiresIn() {
            return expiresIn;
        }

        public void setExpiresIn(long expiresIn) {
            this.expiresIn = expiresIn;
        }
    }
}