# Security Guide - Resource Booking API

## Overview

This document describes the security mechanisms implemented in the Resource Booking API and provides guidance for secure deployment.

## Authentication

### JWT (JSON Web Tokens)

**Algorithm**: HS512 (HMAC with SHA-512)
- Symmetric key derived from secret using SHA-512
- Token expiration: 1 hour (configurable)
- Claims included:
  - `sub`: Username
  - `iat`: Issued at timestamp
  - `exp`: Expiration timestamp
  - `authorities`: User roles

**Token Format**:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIs...
```

### Key Management

**Development**: Uses default secret from `application.properties`
```properties
app.jwt.secret=ChangeThisToANewLongSecretKeyForHS512_1234567890abcdef
```

**Production Requirements**:
- Generate cryptographically secure random secret (256+ bits)
- Store in environment variable `JWT_SECRET`
- Rotate periodically (every 90 days recommended)
- Use different secrets per environment

```bash
# Generate secure secret
openssl rand -base64 64
```

### Token Validation

Implemented in `AuthTokenFilter`:
1. Extract token from `Authorization` header
2. Verify signature using derived HS512 key
3. Check expiration timestamp
4. Extract username from `sub` claim
5. Load user from database
6. Create `Authentication` object with authorities

## Authorization

### Role-Based Access Control (RBAC)

Two roles defined:
| Role | Description |
|------|-------------|
| `ROLE_USER` | Standard user - can view resources, create/manage own reservations |
| `ROLE_ADMIN` | Administrator - full CRUD on resources and all reservations |

### Method-Level Security

Applied via `@PreAuthorize` annotations:

```java
// Resources - Admin only for mutations
@PreAuthorize("hasRole('ADMIN')")
@PostMapping
public ResponseEntity<ResourceDto> createResource(...)

@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{id}")
public ResponseEntity<ResourceDto> updateResource(...)

@PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")
public ResponseEntity<Void> deleteResource(...)

// Reservations - User or Admin
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@PostMapping
public ResponseEntity<ReservationDto> createReservation(...)

@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@GetMapping("/my-reservations")
public ResponseEntity<PageResponse<ReservationDto>> getMyReservations(...)

// Admin only for global operations
@PreAuthorize("hasRole('ADMIN')")
@GetMapping("/all")
public ResponseEntity<PageResponse<ReservationDto>> getAllReservations(...)

@PreAuthorize("hasRole('ADMIN')")
@PutMapping("/{id}/status")
public ResponseEntity<ReservationDto> updateReservationStatus(...)
```

### Resource-Level Authorization

**User Reservations**: Users can only access their own reservations
- User ID extracted from JWT token
- Queries filtered by `user_id = current_user_id`

**Admin Access**: Admins bypass ownership checks
- Separate endpoints for admin operations (`/all`, `/status/{status}`)
- Admin can update any reservation status
- Admin can cancel any reservation

## Password Security

### Storage
- BCrypt with default cost factor (10)
- Salt generated per password
- Never stored in plain text

### Requirements (enforced by validation)
- Minimum 6 characters
- Recommendation: 12+ characters with mixed case, numbers, symbols

## Input Validation

### DTO Validation (Bean Validation)
```java
// ResourceDto
@NotBlank(message = "Name is required")
@Size(max = 100, message = "Name must be less than 100 characters")
private String name;

@NotNull(message = "Price is required")
@DecimalMin(value = "0.01", message = "Price must be greater than 0")
private BigDecimal price;

// ReservationDto
@NotNull(message = "Resource ID is required")
private Long resourceId;

@NotNull(message = "Start time is required")
@FutureOrPresent(message = "Start time must be in the present or future")
private LocalDateTime startTime;

@NotNull(message = "End time is required")
@FutureOrPresent(message = "End time must be in the present or future")
private LocalDateTime endTime;

@NotNull(message = "Total price is required")
@DecimalMin(value = "0.01", message = "Total price must be greater than 0")
private BigDecimal totalPrice;
```

### Business Logic Validation
- Time range validation (start < end)
- Resource availability check
- Overlapping reservation prevention
- Ownership verification for user operations

## CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(List.of("*"));  // Restrict in production!
    configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(false);
    // ...
}
```

**Production**: Restrict to specific origins
```java
configuration.setAllowedOrigins(List.of("https://yourdomain.com"));
configuration.setAllowCredentials(true);
```

## Security Headers

Configured in `WebSecurityConfig`:
```java
http.headers(headers -> headers
    .frameOptions(frameOptions -> frameOptions.sameOrigin())
    .contentTypeOptions(Customizer.withDefaults())
    .httpStrictTransportSecurity(hsts -> hsts
        .maxAgeInSeconds(31536000)
        .includeSubDomains(true)
    )
);
```

## HTTPS/TLS

### Development
- HTTP allowed for local development
- Self-signed certificates acceptable

### Production (Required)
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=booking-api
server.port=8443

# Redirect HTTP to HTTPS
server.forward-headers-strategy=framework
```

Or use reverse proxy (nginx, Traefik, AWS ALB) for TLS termination.

## Database Security

### Connection
```properties
# Use environment variables
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Connection pool (HikariCP)
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### PostgreSQL Hardening
```sql
-- Create dedicated user with minimal privileges
CREATE USER booking_app WITH ENCRYPTED PASSWORD 'strong_password';
GRANT CONNECT ON DATABASE booking_db TO booking_app;
GRANT USAGE ON SCHEMA public TO booking_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO booking_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO booking_app;

-- Revoke public access
REVOKE ALL ON SCHEMA public FROM PUBLIC;
```

### Encryption
- **At Rest**: Enable PostgreSQL TDE or filesystem encryption
- **In Transit**: Use SSL/TLS for database connections
```properties
spring.datasource.url=jdbc:postgresql://host:5432/db?sslmode=require
```

## Secrets Management

### Environment Variables (Recommended)
```bash
# .env file (not committed)
DB_PASSWORD=super_secure_random_password
JWT_SECRET=generated_64_char_base64_string
KEYSTORE_PASSWORD=another_secure_password
```

### Production Options
1. **Kubernetes Secrets**
2. **AWS Secrets Manager / Parameter Store**
3. **HashiCorp Vault**
4. **Docker Secrets**

### Never Commit Secrets
- Add `.env` to `.gitignore`
- Use `git-secrets` or similar pre-commit hooks
- Scan for secrets in CI/CD pipeline

## OWASP Top 10 Mitigation

| Risk | Mitigation |
|------|------------|
| A01: Broken Access Control | `@PreAuthorize`, ownership checks, admin-only endpoints |
| A02: Cryptographic Failures | BCrypt passwords, HS512 JWT, TLS in production |
| A03: Injection | Parameterized queries (JPA), input validation |
| A04: Insecure Design | Layered architecture, separation of concerns |
| A05: Security Misconfiguration | Secure defaults, env-based config, disabled debug |
| A06: Vulnerable Components | Dependency scanning, regular updates |
| A07: Auth Failures | JWT expiration, BCrypt, account lockout (add if needed) |
| A08: Software Integrity | Signed dependencies, SBOM, supply chain security |
| A09: Logging Failures | Structured logging, no sensitive data in logs |
| A10: SSRF | No outbound requests from user input |

## Security Testing

### Automated Checks
```bash
# Dependency vulnerability scan
mvn org.owasp:dependency-check-maven:check

# Static analysis
mvn spotbugs:check

# Test security configuration
mvn test -Dtest=*Security*
```

### Manual Testing
1. **Authentication Bypass**: Test endpoints without token
2. **Authorization Bypass**: Test admin endpoints with user token
3. **IDOR**: Try accessing other users' reservations
4. **Token Tampering**: Modify JWT payload/signature
5. **SQL Injection**: Test search/filter parameters
6. **XSS**: Test input fields with scripts

## Incident Response

### Compromised JWT Secret
1. Rotate `JWT_SECRET` immediately
2. All existing tokens become invalid
3. Force user re-login
4. Audit recent access logs

### Compromised Database Credentials
1. Rotate database password
2. Update application config
3. Restart application
4. Audit database access logs

### Data Breach
1. Identify scope of exposure
2. Notify affected users if PII exposed
3. Rotate all secrets
4. Conduct forensic analysis
5. Update security controls

## Compliance Considerations

### GDPR
- Minimal PII collected (username, email)
- Right to deletion: implement user deletion endpoint
- Data portability: export user data
- Encryption at rest and in transit

### PCI DSS (if payments added)
- Never store card data
- Use PCI-compliant payment processor
- Tokenize payment information

## Security Checklist for Deployment

- [ ] Strong JWT secret generated and stored in env var
- [ ] Database credentials in environment variables
- [ ] HTTPS/TLS configured with valid certificate
- [ ] CORS restricted to known origins
- [ ] Security headers enabled (HSTS, X-Frame-Options, etc.)
- [ ] Database SSL/TLS enabled
- [ ] Dedicated database user with minimal privileges
- [ ] Default admin password changed
- [ ] Debug/logging levels appropriate for production
- [ ] Dependency vulnerability scan passed
- [ ] Penetration testing completed
- [ ] Incident response plan documented
- [ ] Backup and recovery tested
- [ ] Monitoring and alerting configured

## Secure Coding Guidelines

1. **Never trust client input** - Validate on server side
2. **Use parameterized queries** - JPA handles this automatically
3. **Principle of least privilege** - Minimal DB permissions, role-based access
4. **Defense in depth** - Multiple validation layers
5. **Fail securely** - Default deny, explicit allow
6. **Don't log secrets** - Sanitize logs
7. **Keep dependencies updated** - Automated scanning
8. **Security by default** - Secure configuration out of the box