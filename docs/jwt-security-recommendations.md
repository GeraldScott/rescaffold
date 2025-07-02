# JWT Security Recommendations for Rescaffold Application

## Critical Issues Identified

The security audit of the JWT implementation in the Rescaffold application revealed several critical security issues that need to be addressed:

### 1. Insecure JWT Key Storage

**Issue:** The application stores JWT keys in environment variables and sets them as system properties.

```java
// From JwtKeyProvider.java
private void setupJwtKeys() {
    try {
        String privateKey = getProcessedKey("JWT_PRIVATE_KEY");
        String publicKey = getProcessedKey("JWT_PUBLIC_KEY");

        // Set system properties for MicroProfile JWT to use
        System.setProperty("mp.jwt.verify.publickey", publicKey);
        System.setProperty("smallrye.jwt.sign.key", privateKey);

        log.info("JWT keys loaded successfully from environment variables");
    } catch (Exception e) {
        log.errorf(e, "Failed to load JWT keys from environment variables");
        throw new IllegalStateException("JWT keys not configured properly", e);
    }
}
```

**Risk:** Environment variables can be leaked through process listings, logs, or crash dumps. System properties are accessible to all code running in the JVM.

### 2. Non-Standard Key Format

**Issue:** Keys are stored with pipes replacing newlines and processed at runtime.

```java
private String getProcessedKey(String envVarName) {
    return Optional.ofNullable(System.getenv(envVarName))
            .map(key -> key.replace("|", "\n"))
            .orElseThrow(() -> new IllegalStateException(
                "Environment variable " + envVarName + " is not set"));
}
```

**Risk:** Custom encoding schemes increase complexity and the likelihood of errors.

### 3. No Key Rotation Mechanism

**Issue:** There is no evidence of a key rotation mechanism in the codebase.

**Risk:** Without regular key rotation, a compromised key can be exploited indefinitely.

### 4. Long Token Lifetimes

**Issue:** JWT tokens have a default lifetime of 60 minutes.

```java
@ConfigProperty(name = "jwt.duration.minutes", defaultValue = "60")
long tokenDurationMinutes;
```

**Risk:** Longer token lifetimes increase the window of opportunity for attackers if a token is compromised.

### 5. Inappropriate Key Storage Locations

**Issue:** Private keys are stored in application resources folders.

**Risk:** Application resources are typically packaged with the application and may be accessible to unauthorized parties.

## Recommendations

### 1. Implement Proper Key Management

- **Use a Secret Management Service:**
  - HashiCorp Vault
  - AWS Secrets Manager
  - Azure Key Vault
  - Google Cloud Secret Manager

- **Implement Key Separation:**
  - The authentication service should be the only component with access to the private key
  - Other services should only have access to the public key for verification

- **Example Configuration for Quarkus:**
  ```properties
  # For the authentication service
  smallrye.jwt.sign.key.location=${VAULT_JWT_PRIVATE_KEY_PATH}
  
  # For resource services
  mp.jwt.verify.publickey.location=${VAULT_JWT_PUBLIC_KEY_PATH}
  ```

### 2. Implement Key Rotation

- **Establish a Regular Rotation Schedule:**
  - Rotate keys at least quarterly for normal applications
  - Consider more frequent rotation for high-security applications

- **Use Key IDs (kid):**
  ```java
  // When signing tokens
  String token = Jwt.issuer("https://example.com")
                  .subject(username)
                  .groups(roles)
                  .claim("kid", currentKeyId)  // Include key ID
                  .sign();
  ```

- **Implement a JWKS Endpoint:**
  - Create a `/.well-known/jwks.json` endpoint to publish current public keys
  - Allow for multiple valid keys during rotation periods

### 3. Improve Token Lifecycle Management

- **Reduce Token Lifetimes:**
  ```properties
  # Shorter lifetime for sensitive operations
  jwt.duration.minutes=15
  ```

- **Implement Token Revocation:**
  - Create a token blacklist for invalidated tokens
  - Consider using Redis or another distributed cache for the blacklist

- **Use Refresh Tokens:**
  - Issue short-lived access tokens with longer-lived refresh tokens
  - Require a new authentication for sensitive operations

### 4. Enhance JWT Security Configuration

- **Always Validate Claims:**
  ```java
  // Verify essential claims
  if (!validIssuers.contains(jwt.getIssuer())) {
      throw new SecurityException("Invalid token issuer");
  }
  
  if (jwt.getExpirationTime().before(new Date())) {
      throw new SecurityException("Token expired");
  }
  ```

- **Use Strong Algorithms:**
  ```properties
  # Use RS256 or ES256 (preferred)
  mp.jwt.verify.publickey.algorithm=ES256
  ```

- **Enable Encryption for Sensitive Payloads:**
  - Consider using JWE (JSON Web Encryption) for tokens with sensitive claims

### 5. Implement Secure Configuration

- **Separate Configuration by Environment:**
  ```properties
  # Development
  %dev.smallrye.jwt.sign.key.location=classpath:dev/private-key.pem
  
  # Production
  %prod.smallrye.jwt.sign.key.location=${VAULT_JWT_PRIVATE_KEY_PATH}
  ```

- **Validate Configuration at Startup:**
  ```java
  @ApplicationScoped
  public class SecurityConfigValidator {
      @Inject
      Logger log;
      
      void onStart(@Observes StartupEvent ev) {
          validateJwtConfig();
      }
      
      private void validateJwtConfig() {
          // Check for secure algorithm
          // Verify key source is appropriate for environment
          // etc.
      }
  }
  ```

### 6. Follow MicroProfile JWT Best Practices

- **Always Check Signatures:**
  - Never skip signature verification, even for internal services

- **Validate Issuer:**
  ```properties
  mp.jwt.verify.issuer=https://auth.example.com
  ```

- **Require Essential Claims:**
  - `iss` (issuer)
  - `exp` (expiration time)
  - `iat` (issued at)
  - `sub` (subject)

### 7. Implement Proper Error Handling

- **Don't Leak Sensitive Information:**
  ```java
  try {
      // JWT validation logic
  } catch (JwtException e) {
      // Log details for diagnostics (server-side only)
      log.debug("JWT validation failed", e);
      
      // Return generic error to client
      throw new AuthenticationException("Authentication failed");
  }
  ```

- **Add Monitoring and Alerting:**
  - Track failed authentication attempts
  - Alert on unusual patterns that might indicate attacks

## Implementation Plan

1. **Immediate Actions:**
   - Remove private keys from application resources
   - Implement proper key storage using a secrets manager
   - Review and reduce token lifetimes

2. **Short-term (1-2 weeks):**
   - Implement key rotation mechanism
   - Enhance claim validation
   - Improve error handling

3. **Medium-term (1 month):**
   - Set up JWKS endpoint for public key distribution
   - Implement token revocation capability
   - Add monitoring and alerting

4. **Long-term (3 months):**
   - Consider implementing JWE for sensitive payloads
   - Conduct a thorough security review
   - Create automated security tests

## References

- [MicroProfile JWT RBAC Specification](https://microprofile.io/specifications/jwt/)
- [Quarkus Security JWT Guide](https://quarkus.io/guides/security-jwt)
- [RFC 7519: JSON Web Token (JWT)](https://datatracker.ietf.org/doc/html/rfc7519)
- [RFC 8725: JWT Best Current Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [OWASP JWT Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)