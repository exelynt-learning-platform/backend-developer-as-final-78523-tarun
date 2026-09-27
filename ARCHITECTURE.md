# Resource Booking API - Architecture Documentation

## Overview

This document describes the architecture, design decisions, and technical implementation of the Resource Booking API.

## System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Client Applications                       │
│  (Web UI, Mobile Apps, Third-party Integrations)                │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot Application                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │  Controllers │──►│   Services   │──►│    Repositories      │  │
│  │  (REST API)  │  │ (Business    │  │  (Data Access)       │  │
│  │              │  │  Logic)      │  │                      │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
│         │                 │                    │                 │
│         ▼                 ▼                    ▼                 │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐  │
│  │   Security   │  │     DTOs     │  │      Entities        │  │
│  │ (JWT, RBAC)  │  │ (Validation) │  │   (JPA/Hibernate)    │  │
│  └──────────────┘  └──────────────┘  └──────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      PostgreSQL Database                         │
└─────────────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.example.booking
├── config/                 # Configuration classes
│   ├── DataInitializer     # Seed data on startup
│   └── WebSecurityConfig   # Spring Security configuration
├── controller/             # REST Controllers
│   ├── AuthController      # Authentication endpoints
│   ├── ResourceController  # Resource CRUD
│   └── ReservationController # Reservation management
├── dto/                    # Data Transfer Objects
│   ├── JwtResponse         # Login response with token
│   ├── LoginRequest        # Login credentials
│   ├── PageResponse        # Generic pagination wrapper
│   ├── ReservationDto      # Reservation request/response
│   └── ResourceDto         # Resource request/response
├── exception/              # Exception handling
│   └── GlobalExceptionHandler # Centralized error handling
├── model/                  # JPA Entities
│   ├── User                # User with roles
│   ├── Resource            # Bookable resource
│   └── Reservation         # Booking with status
├── repository/             # Spring Data JPA Repositories
│   ├── UserRepository
│   ├── ResourceRepository
│   └── ReservationRepository
├── security/               # Security components
│   ├── AuthEntryPointJwt   # 401 handler
│   ├── AuthTokenFilter     # JWT token filter
│   ├── JwtUtils            # JWT generation/validation
│   ├── UserPrincipal       # UserDetails implementation
│   └── impl/
│       └── UserDetailsServiceImpl # Load user from DB
├── service/                # Business logic interfaces
│   ├── AuthService
│   ├── ResourceService
│   ├── ReservationService
│   └── UserService
└── service/impl/           # Service implementations (package-private)
    ├── AuthServiceImpl
    ├── ResourceServiceImpl
    ├── ReservationServiceImpl
    └── UserServiceImpl
```

## Key Design Decisions

### 1. Layered Architecture
- **Controller Layer**: Handles HTTP requests/responses, validation, authorization
- **Service Layer**: Contains business logic, transactions, orchestrates repositories
- **Repository Layer**: Data access using Spring Data JPA
- **DTO Layer**: Decouples API from database entities

### 2. Security Implementation
- **Stateless JWT Authentication**: No server-side sessions
- **Role-Based Access Control**: Method-level `@PreAuthorize` annotations
- **User Identity from JWT**: User ID extracted from token, never from request body
- **BCrypt Password Hashing**: Industry-standard password storage

### 3. Reservation Conflict Prevention
- Database-level check for overlapping reservations
- Status-based filtering (only PENDING/CONFIRMED block slots)
- Optimistic locking via `@Version` could be added for high concurrency

### 4. Pagination & Sorting
- Spring Data `Pageable` for consistent pagination
- Configurable default/max page sizes
- Sortable fields validated to prevent injection

### 5. Validation Strategy
- Bean Validation (JSR-380) on DTOs
- Custom validation in service layer for business rules
- Global exception handler for consistent error responses

## Data Model

### User
```sql
users
├── id (PK, BIGINT)
├── username (VARCHAR, UNIQUE)
├── password (VARCHAR, BCrypt)
├── email (VARCHAR, UNIQUE)
├── roles (ElementCollection: ROLE_USER, ROLE_ADMIN)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

### Resource
```sql
resources
├── id (PK, BIGINT)
├── name (VARCHAR)
├── description (VARCHAR)
├── price (DECIMAL)
├── is_available (BOOLEAN)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

### Reservation
```sql
reservations
├── id (PK, BIGINT)
├── user_id (FK → users.id)
├── resource_id (FK → resources.id)
├── start_time (TIMESTAMP)
├── end_time (TIMESTAMP)
├── total_price (DECIMAL)
├── status (ENUM: PENDING, CONFIRMED, CANCELLED)
├── created_at (TIMESTAMP)
└── updated_at (TIMESTAMP)
```

## Security Flow

### Login Flow
```
1. Client POST /api/auth/login {username, password}
2. AuthenticationManager authenticates via DaoAuthenticationProvider
3. UserDetailsServiceImpl loads User from DB
4. JwtUtils generates HS512 signed token with claims:
   - subject: username
   - issuedAt: now
   - expiration: now + 1 hour (configurable)
   - authorities: roles
5. Return JwtResponse {token, user info, roles}
```

### Request Authentication Flow
```
1. Client sends Authorization: Bearer <token>
2. AuthTokenFilter (OncePerRequestFilter) intercepts
3. Extract JWT from header
4. JwtUtils.validateJwtToken() verifies signature & expiration
5. JwtUtils.getUserNameFromJwtToken() extracts username
6. UserDetailsServiceImpl.loadUserByUsername() loads User
7. Create UsernamePasswordAuthenticationToken with authorities
8. Set SecurityContextHolder authentication
9. Controller executes with @AuthenticationPrincipal UserDetails
```

### Authorization Flow
```
1. @PreAuthorize("hasRole('ADMIN')") on controller method
2. Spring Security evaluates expression against authentication
3. If authorized → proceed
4. If not → 403 Forbidden via AccessDeniedHandler
```

## Concurrency Handling

### Reservation Creation
```java
// In ReservationServiceImpl.createReservation()
1. Check resource exists and is available
2. Validate time range (start < end)
3. Query for conflicting reservations:
   SELECT * FROM reservations 
   WHERE resource_id = ? 
   AND status IN (PENDING, CONFIRMED)
   AND end_time > now()
4. In-memory check for time overlap
5. Save new reservation with PENDING status
```

**Note**: For high-concurrency environments, consider:
- Database advisory locks
- Optimistic locking with `@Version`
- Serializable transaction isolation

## Error Handling

### Exception Hierarchy
```
GlobalExceptionHandler
├── MethodArgumentNotValidException → 400 (validation errors)
├── UsernameNotFoundException → 404
├── BadCredentialsException → 401
├── RuntimeException → 500 (business logic errors)
└── Exception → 500 (unexpected)
```

### Error Response Format
```json
// Validation errors
{
  "fieldName": "error message"
}

// Other errors
{
  "error": "Description"
}
```

## Configuration

### Application Properties
```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=${JWT_SECRET}
app.jwt.expiration-ms=${JWT_EXPIRATION_MS:3600000}

# Pagination
spring.data.web.pageable.default-page-size=10
spring.data.web.pageable.max-page-size=100

# Swagger
springdoc.swagger-ui.path=/swagger-ui.html
```

### Environment Variables
| Variable | Required | Default |
|----------|----------|---------|
| SERVER_PORT | No | 8080 |
| DB_HOST | No | localhost |
| DB_PORT | No | 5432 |
| DB_NAME | No | booking_db |
| DB_USERNAME | No | postgres |
| DB_PASSWORD | No | postgres |
| JWT_SECRET | Yes* | dev secret |
| JWT_EXPIRATION_MS | No | 3600000 |

*Required for production

## Testing Strategy

### Unit Tests
- Service layer with mocked repositories
- Validation logic
- Business rule enforcement

### Integration Tests
- SpringBootTest with Testcontainers (PostgreSQL)
- Full request/response cycles
- Security configuration verification

### Test Data
- DataInitializer creates seed users/resources
- Tests use @DirtiesContext or transactional rollback

## Performance Considerations

### Database Indexes (Recommended)
```sql
CREATE INDEX idx_reservations_user_id ON reservations(user_id);
CREATE INDEX idx_reservations_resource_id ON reservations(resource_id);
CREATE INDEX idx_reservations_status ON reservations(status);
CREATE INDEX idx_reservations_time_range ON reservations(start_time, end_time);
CREATE INDEX idx_resources_available ON resources(is_available);
```

### Query Optimization
- Use projections for list views
- Avoid N+1 with EntityGraph or JOIN FETCH
- Pagination prevents large result sets

## Deployment

### Docker
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes (Example)
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: resource-booking-api
spec:
  replicas: 3
  selector:
    matchLabels:
      app: resource-booking-api
  template:
    spec:
      containers:
      - name: api
        image: resource-booking-api:latest
        ports:
        - containerPort: 8080
        env:
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: db-secret
              key: host
```

## Monitoring & Observability

### Health Checks
- Spring Boot Actuator endpoints
- `/actuator/health` for liveness/readiness

### Logging
- Structured JSON logging (Logback)
- Correlation IDs for request tracing
- SQL logging disabled in production

### Metrics (Recommended)
- Micrometer + Prometheus
- Custom metrics: reservation_created, conflicts_detected
- JVM metrics: memory, threads, GC

## Future Enhancements

1. **Email Notifications**: Async email on reservation status changes
2. **Calendar Integration**: iCal/Google Calendar export
3. **Recurring Reservations**: Weekly/monthly booking patterns
4. **Resource Categories**: Group resources by type
5. **Audit Logging**: Track all changes for compliance
6. **Rate Limiting**: Prevent API abuse
7. **Multi-tenancy**: Support multiple organizations
8. **WebSocket**: Real-time availability updates