# Quick Start Guide

Get the Resource Booking API running in 5 minutes.

## Prerequisites

- **Java 17+** (tested with Java 25)
- **PostgreSQL 12+** running locally or remote
- **Maven 3.6+** (or use included wrapper)

## 1. Database Setup

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE booking_db;

# Exit
\q
```

## 2. Configure Application

Edit `src/main/resources/application.properties`:

```properties
# Database (update with your credentials)
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_db
spring.datasource.username=postgres
spring.datasource.password=your_password_here

# JWT Secret (CHANGE IN PRODUCTION!)
app.jwt.secret=your-super-secret-key-at-least-64-characters-long-for-hs512
```

**Or use environment variables:**
```bash
export DB_PASSWORD=your_password_here
export JWT_SECRET=your-super-secret-key-at-least-64-characters-long-for-hs512
```

## 3. Build & Run

```bash
# Using Maven wrapper (no Maven installation needed)
./mvnw spring-boot:run

# Or with Maven installed
mvn spring-boot:run
```

## 4. Verify It's Running

```bash
# Health check (no auth needed)
curl http://localhost:8080/actuator/health

# Swagger UI
open http://localhost:8080/swagger-ui.html
```

## 5. Test with Seed Users

### Login as Admin
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "roles": ["ROLE_ADMIN"]
}
```

### Login as Regular User
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}'
```

Save the token for subsequent requests!

## 6. Quick API Test

### Get Available Resources
```bash
TOKEN="your_token_here"
curl -X GET "http://localhost:8080/api/resources?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN"
```

### Create a Reservation (as User)
```bash
TOKEN="user_token_here"
curl -X POST http://localhost:8080/api/reservations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "resourceId": 1,
    "startTime": "2025-12-20T10:00:00",
    "endTime": "2025-12-20T12:00:00",
    "totalPrice": 100.00
  }'
```

### Create a Resource (as Admin)
```bash
TOKEN="admin_token_here"
curl -X POST http://localhost:8080/api/resources \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "New Room",
    "description": "Test room",
    "price": 75.00
  }'
```

## 7. Import Postman Collection

1. Open Postman
2. Click **Import** → Select `postman_collection.json`
3. Run "Login as Admin" then "Login as User" to populate tokens
4. Test all endpoints!

## Common Issues

### Port 8080 Already in Use
```properties
# application.properties
server.port=8081
```

### Database Connection Failed
- Check PostgreSQL is running: `systemctl status postgresql`
- Verify credentials in `application.properties`
- Check firewall/security groups

### JWT Secret Too Short
```
Error: The HS512 algorithm requires a key of at least 512 bits
```
Fix: Use a secret of at least 64 characters (512 bits)

### Tests Fail
```bash
# Clean and rebuild
./mvnw clean test
```

## Project Structure

```
resource-booking-api/
├── src/main/java/com/example/booking/
│   ├── config/         # Config & seed data
│   ├── controller/     # REST endpoints
│   ├── dto/            # Request/response objects
│   ├── exception/      # Error handling
│   ├── model/          # JPA entities
│   ├── repository/     # Data access
│   ├── security/       # JWT & auth
│   └── service/        # Business logic
├── src/main/resources/
│   └── application.properties
├── pom.xml
├── README.md
├── API_DOCUMENTATION.md
├── ARCHITECTURE.md
├── SECURITY.md
├── QUICKSTART.md
└── postman_collection.json
```

## Next Steps

1. **Read the docs:**
   - [API Documentation](API_DOCUMENTATION.md) - Detailed endpoint specs
   - [Architecture](ARCHITECTURE.md) - System design
   - [Security Guide](SECURITY.md) - Production hardening

2. **Customize:**
   - Add more resources via admin API
   - Modify seed data in `DataInitializer.java`
   - Adjust pagination defaults in `application.properties`

3. **Deploy:**
   - See [README.md](README.md#production-deployment) for Docker/K8s
   - Follow [SECURITY.md](SECURITY.md) for production checklist

## Need Help?

- Check logs: `./mvnw spring-boot:run` output
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`