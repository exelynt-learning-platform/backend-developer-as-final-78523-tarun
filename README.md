# Resource Booking API

A secure RESTful API for resource booking with JWT authentication and role-based access control (RBAC) built with Spring Boot.

## 📚 Documentation

| Document | Description | Audience |
|----------|-------------|----------|
| **[📖 Documentation Index](DOCS_INDEX.md)** | Complete documentation overview | All |
| **[🚀 Quick Start](QUICKSTART.md)** | Get running in 5 minutes | Users |
| **[📋 API Reference](API_DOCUMENTATION.md)** | Detailed endpoint specifications | Developers |
| **[🏗️ Architecture Guide](ARCHITECTURE.md)** | System design and implementation | Developers |
| **[🔒 Security Guide](SECURITY.md)** | Security best practices | Dev/SecOps |
| **[❓ Help & FAQ](HELP.md)** | Common issues and solutions | All |

## Features

- **JWT-based Authentication**: Secure login with JSON Web Tokens
- **Role-Based Access Control (RBAC)**: ADMIN and USER roles with proper authorization
- **Resource Management**: CRUD operations for bookable resources
- **Reservation System**: Create, view, and manage reservations with status tracking
- **Pagination & Filtering**: Advanced filtering and pagination for resources and reservations
- **Database Integration**: PostgreSQL with JPA/Hibernate
- **API Documentation**: Swagger/OpenAPI integration
- **Seed Data**: Pre-configured users and sample resources for testing

## Technology Stack

- **Java 17+**
- **Spring Boot 4.1.1**
- **Spring Security**
- **JWT (Java Web Token)**
- **PostgreSQL**
- **JPA/Hibernate**
- **Spring Data JPA**
- **Swagger/OpenAPI 3.1.1**
- **Maven**

## Prerequisites

- Java 17 or higher
- PostgreSQL 12 or higher
- Maven 3.6 or higher

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd resource-booking-api
```

### 2. Database Configuration

Create a PostgreSQL database:

```sql
CREATE DATABASE booking_db;
```

### 3. Configure Application Properties

Update `src/main/resources/application.properties` with your database configuration:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/booking_db
spring.datasource.username=your_postgres_username
spring.datasource.password=your_postgres_password

# JWT Configuration (change in production)
app.jwt.secret=your-super-secret-jwt-key-change-this-in-production-at-least-32-characters-long
app.jwt.expiration-ms=3600000
```

### 4. Build and Run

```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

Or using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

## API Endpoints

### Authentication

- `POST /api/auth/login` - User login

### Resources

- `GET /api/resources` - Get all available resources (paginated)
- `GET /api/resources/{id}` - Get resource by ID
- `POST /api/resources` - Create new resource (ADMIN only)
- `PUT /api/resources/{id}` - Update resource (ADMIN only)
- `DELETE /api/resources/{id}` - Delete resource (ADMIN only)
- `GET /api/resources/search` - Search resources with filters

### Reservations

- `POST /api/reservations` - Create new reservation
- `GET /api/reservations/my-reservations` - Get user's reservations
- `GET /api/reservations/my-reservations/status/{status}` - Get user's reservations by status
- `GET /api/reservations/all` - Get all reservations (ADMIN only)
- `GET /api/reservations/status/{status}` - Get reservations by status (ADMIN only)
- `PUT /api/reservations/{id}/status` - Update reservation status (ADMIN only)
- `PUT /api/reservations/{id}/cancel` - Cancel reservation

## API Documentation

Interactive API documentation available at:
```
http://localhost:8080/swagger-ui.html
```

**Test Collection**: Import [postman_collection.json](postman_collection.json) for comprehensive testing

## Quick Links

- **🚀 Quick Start**: [QUICKSTART.md](QUICKSTART.md) - Get running in 5 minutes
- **📋 API Reference**: [API_DOCUMENTATION.md](API_DOCUMENTATION.md) - Detailed endpoint specs
- **🏗️ Architecture**: [ARCHITECTURE.md](ARCHITECTURE.md) - System design
- **🔒 Security**: [SECURITY.md](SECURITY.md) - Security best practices
- **❓ Help**: [HELP.md](HELP.md) - Common issues and solutions

## Seed Users

The application creates two default users on startup:

### Admin User
- **Username**: `admin`
- **Password**: `admin123`
- **Role**: `ROLE_ADMIN`

### Regular User
- **Username**: `user`
- **Password**: `user123`
- **Role**: `ROLE_USER`

## Usage Examples

### 1. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```

### 2. Get Available Resources

```bash
curl -X GET "http://localhost:8080/api/resources?page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 3. Create Reservation

```bash
curl -X POST http://localhost:8080/api/reservations \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "resourceId": 1,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-15T12:00:00",
    "totalPrice": 50.00
  }'
```

### 4. Search Resources

```bash
curl -X GET "http://localhost:8080/api/resources/search?page=0&size=10&name=Conference&minPrice=30&maxPrice=100" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Security Features

- **JWT Authentication**: Stateless authentication with configurable expiration
- **Password Encryption**: BCrypt password hashing
- **Role-Based Access Control**: Method-level security with `@PreAuthorize`
- **Input Validation**: Bean validation with custom error messages
- **CORS Support**: Cross-origin resource sharing configured

## Database Schema

### Users Table
- `id` (Primary Key)
- `username` (Unique)
- `password` (BCrypt hashed)
- `email` (Unique)
- `roles` (ENUM: ROLE_USER, ROLE_ADMIN)
- `created_at`
- `updated_at`

### Resources Table
- `id` (Primary Key)
- `name`
- `description`
- `price` (Decimal)
- `is_available` (Boolean)
- `created_at`
- `updated_at`

### Reservations Table
- `id` (Primary Key)
- `user_id` (Foreign Key)
- `resource_id` (Foreign Key)
- `start_time`
- `end_time`
- `total_price` (Decimal)
- `status` (ENUM: PENDING, CONFIRMED, CANCELLED)
- `created_at`
- `updated_at`

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8080` | Server port |
| `DB_HOST` | `localhost` | Database host |
| `DB_PORT` | `5432` | Database port |
| `DB_NAME` | `booking_db` | Database name |
| `DB_USERNAME` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | `change-this-dev-secret...` | JWT signing secret |
| `JWT_EXPIRATION_MS` | `3600000` | JWT expiration time in milliseconds |

## Testing

### Run Tests

```bash
mvn test
```

### Test Endpoints

You can use the provided seed users to test the API:

```bash
# Login as admin
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'

# Login as regular user
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "user", "password": "user123"}'
```

## Development

### Project Structure

```
src/main/java/com/example/booking/
├── config/          # Configuration classes
├── controller/     # REST controllers
├── dto/            # Data Transfer Objects
├── exception/      # Exception handlers
├── model/          # JPA entities
├── repository/     # JPA repositories
├── security/       # Security utilities
├── service/        # Business logic services
└── ResourceBookingApiApplication.java
```

### Adding New Features

1. **New Entity**: Create model class with JPA annotations
2. **Repository**: Extend `JpaRepository` for data access
3. **Service**: Implement business logic in service layer
4. **Controller**: Create REST endpoints with proper authorization
5. **DTOs**: Create request/response DTOs for data transfer

## Production Deployment

### Security Considerations

1. **Change JWT Secret**: Use a strong, randomly generated secret
2. **Database Security**: Use strong database credentials
3. **HTTPS**: Configure SSL/TLS for production
4. **Environment Variables**: Use environment variables for sensitive data
5. **Database Migration**: Use proper migration scripts instead of DDL auto

### Docker Deployment

Create a `Dockerfile`:

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/resource-booking-api-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

Build and run:

```bash
docker build -t resource-booking-api .
docker run -p 8080:8080 resource-booking-api
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

### Development Resources

- **📚 Documentation Index**: [DOCS_INDEX.md](DOCS_INDEX.md) - Complete docs overview
- **🏗️ Architecture**: [ARCHITECTURE.md](ARCHITECTURE.md) - Design patterns
- **🔒 Security**: [SECURITY.md](SECURITY.md) - Security implementation
- **❓ Help**: [HELP.md](HELP.md) - Troubleshooting guide

## License

This project is licensed under the MIT License.
