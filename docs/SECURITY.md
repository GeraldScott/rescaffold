# Security Architecture

This document outlines the comprehensive security implementation for the Rescaffold application, covering JWT authentication, authorization patterns, and security best practices.

## Table of Contents

- [Overview](#overview)
- [Authentication Flow](#authentication-flow)
- [JWT Implementation](#jwt-implementation)
- [Authorization Model](#authorization-model)
- [Key Management](#key-management)
- [Database Security](#database-security)
- [Security Configuration](#security-configuration)
- [Best Practices Implemented](#best-practices-implemented)
- [Security Testing](#security-testing)
- [Production Considerations](#production-considerations)
- [Security Roadmap](#security-roadmap)

## Overview

The Rescaffold application implements a **stateless JWT-based authentication and authorization system** using Quarkus security features. The architecture follows enterprise security patterns with proper separation of concerns, role-based access control (RBAC), and comprehensive audit trails.

### Key Security Components

- **JWT Authentication**: Stateless token-based authentication
- **BCrypt Password Hashing**: Industry-standard password security
- **Role-Based Authorization**: Granular access control
- **Audit Trails**: Comprehensive security logging
- **Key Management**: RSA key pair for JWT signing/verification

## Authentication Flow

### 1. User Login Process

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "password"
}
```

**Flow Steps:**
1. User submits credentials to `AuthResource.login()`
2. `AuthService.authenticate()` verifies password using BCrypt
3. User's `lastLogin` timestamp updated in database
4. JWT token generated with user claims and roles
5. `TokenInfo` returned with access token and expiration

### 2. Token Usage

```http
GET /api/protected-resource
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9...
```

**Verification Process:**
1. MicroProfile JWT validates token signature using public key
2. `SecurityContext` extracts user claims for request processing
3. JAX-RS security annotations enforce role-based authorization

### 3. Token Refresh

```http
POST /api/auth/refresh
Authorization: Bearer <existing-token>
```

Generates new token with updated expiration for authenticated users.

## JWT Implementation

### Token Structure

**Standard Claims:**
- `iss` (issuer): `https://rescaffold.archton.io`
- `sub` (subject): Username
- `groups`: User roles for authorization
- `iat` (issued at): Token creation timestamp
- `nbf` (not before): Token validity start
- `exp` (expires): Token expiration (60 minutes default)
- `jti` (JWT ID): Unique token identifier

**Custom Claims:**
- `userId`: Database user ID for efficient lookups
- `personId`: Associated person record ID
- `name`: Full name or display name

### Token Generation

```java
// AuthService.generateToken()
Jwt.issuer(issuer)
   .subject(user.getUsername())
   .groups(roleNames)
   .claim("name", displayName)
   .claim("userId", user.getId())
   .claim("personId", user.getPersonId())
   .expiresAt(expirationTime)
   .sign(); // Uses configured private key
```

### Token Validation

Automatic validation via MicroProfile JWT:
- Signature verification using public key
- Expiration time validation
- Issuer validation
- Claims extraction for SecurityContext

## Authorization Model

### Role Hierarchy

```
ROLE_ADMIN    (Full system access)
ROLE_MANAGER  (Management functions)
ROLE_USER     (Standard user access)  
ROLE_GUEST    (Read-only access)
```

## Key Management

### RSA Key Pair Generation

```bash
# Generate 2048-bit RSA private key
openssl genrsa -out jwt-private.pem 2048

# Extract public key
openssl rsa -in jwt-private.pem -pubout -out jwt-public.pem
```

### Secure Storage

```
.certs/
├── jwt-private.pem  (600 permissions - signing only)
└── jwt-public.pem   (644 permissions - verification)
```

**Security Features:**
- Keys stored outside version control (`.certs/` gitignored)
- Proper file permissions (private key read-only by owner)
- Environment-configurable paths for deployment flexibility

### Configuration

```properties
# JWT Key Configuration
mp.jwt.verify.publickey.location=${JWT_PUBLIC_KEY_PATH:file:.certs/jwt-public.pem}
smallrye.jwt.sign.key.location=${JWT_PRIVATE_KEY_PATH:file:.certs/jwt-private.pem}

# JWT Settings
mp.jwt.verify.issuer=https://rescaffold.archton.io
jwt.duration.minutes=60
```

## Database Security

### Password Security

- **BCrypt Hashing**: Industry-standard adaptive hashing
- **Salt Rounds**: 10 (balanced security/performance)
- **Password Storage**: Never store plain text passwords

```java
// Password hashing during user creation
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));

// Password verification during login
boolean isValid = BCrypt.checkpw(providedPassword, storedHash);
```
### Connection Security

```properties
# Database connection encryption
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/scaffold_prod?ssl=true&sslmode=require

# Connection credentials via environment variables
%prod.quarkus.datasource.username=${PROD_DB_USERNAME}
%prod.quarkus.datasource.password=${PROD_DB_PASSWORD}
```

## Security Configuration

### CORS Configuration

```properties
# Cross-Origin Resource Sharing
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:3000,http://localhost:8080
quarkus.http.cors.headers=accept,authorization,content-type,x-requested-with
quarkus.http.cors.methods=GET,POST,PUT,DELETE,OPTIONS
quarkus.http.cors.exposed-headers=authorization
```

### Security Logging

```properties
# Security debugging in development
%dev.quarkus.log.category."io.quarkus.security".level=DEBUG
%dev.quarkus.log.category."io.archton.scaffold".level=DEBUG
```

### Profile-Specific Security

**Development:**
- Enhanced logging for debugging
- Test data seeding
- Relaxed CORS for local development

**Production:**
- Minimal logging (WARN level)
- Strict CORS policies  
- Environment-based secrets

## Best Practices Implemented

### ✅ Authentication Security

- **Strong Password Hashing**: BCrypt with appropriate cost factor
- **Secure Session Management**: Stateless JWT tokens
- **Password Policies**: Validation at application layer
- **Account Audit**: Login tracking and timestamps

### ✅ Authorization Security

- **Principle of Least Privilege**: Role-based access control
- **Fine-Grained Permissions**: Method-level security annotations
- **Security Context**: Request-scoped user information
- **Role Hierarchy**: Clear permission levels

### ✅ Token Security

- **RSA Signature**: Strong cryptographic signing
- **Token Expiration**: Time-limited access tokens
- **Claims Validation**: Proper JWT claim verification
- **Secure Key Storage**: Protected private key

### ✅ Data Security

- **Input Validation**: Jakarta Bean Validation
- **SQL Injection Prevention**: Parameterized queries via JPA
- **Audit Trails**: Comprehensive change logging
- **Data Encryption**: TLS for data in transit

### ✅ Application Security

- **Exception Handling**: No sensitive information leakage
- **Security Headers**: CORS and content type enforcement
- **Environment Configuration**: Secrets via environment variables
- **Clean Architecture**: Separation of security concerns

## Security Testing

### Authentication Testing

```bash
# Test login endpoint
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"adminpassword"}'

# Test protected endpoint access
curl -X GET http://localhost:8080/api/users \
  -H "Authorization: Bearer <jwt-token>"
```

### Authorization Testing

```java
@Test
@TestSecurity(user = "testUser", roles = {"ROLE_USER"})
public void testUserCanAccessOwnData() {
    given()
        .when().get("/api/users/me")
        .then()
        .statusCode(200);
}

@Test  
@TestSecurity(user = "testUser", roles = {"ROLE_USER"})
public void testUserCannotAccessAdminEndpoint() {
    given()
        .when().post("/api/users")
        .then()
        .statusCode(403); // Forbidden
}
```

### Integration Testing

```bash
# Run auth flow test
./auth_flow_test.sh

# Validates:
# - Admin login and token generation
# - Protected endpoint access
# - Role-based authorization
# - Token refresh functionality
```

## Production Considerations

### Key Management

**File-Based (Current):**
```bash
# Secure file permissions
chmod 600 .certs/jwt-private.pem
chmod 644 .certs/jwt-public.pem
```

**Cloud-Based (Recommended):**
- AWS Secrets Manager / Parameter Store
- Azure Key Vault
- Google Secret Manager
- HashiCorp Vault

### Monitoring and Alerting

**Security Events to Monitor:**
- Failed login attempts
- Token generation/validation failures
- Unauthorized access attempts
- Admin privilege escalations
- Unusual access patterns

**Recommended Tools:**
- Application metrics via Micrometer
- Security event logging
- Failed authentication alerting
- Token usage analytics

### SSL/TLS Configuration

```properties
# Production HTTPS configuration
%prod.quarkus.http.ssl.certificate.files=cert.pem
%prod.quarkus.http.ssl.certificate.key-files=key.pem
%prod.quarkus.http.ssl-port=8443
%prod.quarkus.http.redirect-to-https=true
```

### Container Security

```dockerfile
# Use non-root user
RUN adduser --disabled-password --gecos '' appuser
USER appuser

# Secure file permissions
COPY --chown=appuser:appuser .certs/ /app/.certs/
RUN chmod 600 /app/.certs/jwt-private.pem
```

## Security Roadmap

### Phase 1: Enhanced Authentication (High Priority)

- [ ] **Account Lockout**: Implement failed attempt tracking
- [ ] **Password Policy**: Enforce complexity requirements  
- [ ] **Rate Limiting**: Protect authentication endpoints
- [ ] **Token Blacklisting**: Support for secure logout

### Phase 2: Advanced Authorization (Medium Priority)

- [ ] **Resource Permissions**: Fine-grained access control
- [ ] **Dynamic Roles**: Runtime role assignment
- [ ] **Permission Inheritance**: Hierarchical role structure
- [ ] **Audit Events**: Security event logging

### Phase 3: Security Hardening (Low Priority)

- [ ] **Key Rotation**: Automated key management
- [ ] **Security Headers**: HSTS, CSP, etc.
- [ ] **CAPTCHA Integration**: Brute force protection
- [ ] **Multi-Factor Authentication**: Enhanced security

### Phase 4: Compliance and Monitoring

- [ ] **Security Scanning**: Automated vulnerability detection
- [ ] **Compliance Reporting**: GDPR, SOC2 requirements
- [ ] **Penetration Testing**: Regular security assessments
- [ ] **Incident Response**: Security event handling

## API Reference

### Authentication Endpoints

| Endpoint | Method | Description | Authorization |
|----------|--------|-------------|---------------|
| `/api/auth/login` | POST | User authentication | Public |
| `/api/auth/refresh` | POST | Token refresh | JWT Required |

### Protected Resources

| Resource | Methods | Required Roles |
|----------|---------|----------------|
| `/api/users` | GET, POST | ROLE_ADMIN |
| `/api/genders` | GET | Any authenticated |
| `/api/genders` | POST, PUT, DELETE | ROLE_ADMIN, ROLE_MANAGER |

### Security Context

```java
// Access current user information
@Inject SecurityContext securityContext;

String username = securityContext.getCurrentUsername();
Long userId = securityContext.getCurrentUserId();
boolean isAdmin = securityContext.isAdmin();
```

---

**Last Updated**: July 2025  
**Security Review**: Required quarterly  
**Next Review**: October 2025