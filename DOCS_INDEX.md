# Documentation Index

Welcome to the Resource Booking API documentation! This index will help you find the information you need quickly.

## 🚀 Getting Started

### For New Users
1. **[Quick Start Guide](QUICKSTART.md)** - Get running in 5 minutes
2. **[README](README.md)** - Complete project overview
3. **[Help & FAQ](HELP.md)** - Common issues and solutions

### For Developers
1. **[Architecture Guide](ARCHITECTURE.md)** - System design and implementation
2. **[API Documentation](API_DOCUMENTATION.md)** - Detailed endpoint specifications
3. **[Security Guide](SECURITY.md)** - Security best practices

## 📚 Documentation Overview

### Core Documentation

| File | Purpose | Audience | Time to Read |
|------|---------|----------|--------------|
| **README.md** | Project overview, setup, features | All | 5 min |
| **QUICKSTART.md** | Fastest way to get running | Users | 3 min |
| **HELP.md** | Troubleshooting and FAQ | Users/Dev | 10 min |
| **API_DOCUMENTATION.md** | Detailed API specs | Developers | 15 min |
| **ARCHITECTURE.md** | System design and patterns | Developers | 20 min |
| **SECURITY.md** | Security implementation and best practices | Dev/SecOps | 15 min |

### Supporting Files

| File | Purpose | Format |
|------|---------|--------|
| **postman_collection.json** | Complete test collection | Postman |
| **pom.xml** | Maven dependencies | XML |
| **application.properties** | Configuration template | Properties |

## 🎯 Choose Your Path

### I want to try the API right now!
- Follow [Quick Start Guide](QUICKSTART.md)
- Import [Postman Collection](postman_collection.json)
- Explore [Swagger UI](http://localhost:8080/swagger-ui.html) when running

### I want to understand how it works
- Read [Architecture Guide](ARCHITECTURE.md)
- Study [API Documentation](API_DOCUMENTATION.md)
- Check [Security Guide](SECURITY.md)

### I want to customize or extend it
- [Architecture Guide](ARCHITECTURE.md) - Design decisions
- [API Documentation](API_DOCUMENTATION.md) - Endpoints
- [Security Guide](SECURITY.md) - Security implementation
- [README.md](README.md) - Project structure

### I'm having issues
- [Help & FAQ](HELP.md) - Common problems
- Check [GitHub Issues](https://github.com/your-repo/issues)
- Review logs when running

### I want to deploy it
- [README.md](README.md#production-deployment) - Docker/Kubernetes
- [Security Guide](SECURITY.md#deployment) - Security checklist
- [Architecture Guide](ARCHITECTURE.md#deployment) - Deployment considerations

## 🔧 Technical Details

### Technology Stack
- **Framework**: Spring Boot 4.1.1
- **Language**: Java 17+
- **Database**: PostgreSQL 12+
- **Security**: JWT with HS512, Spring Security
- **Documentation**: Swagger/OpenAPI 3.1.1
- **Build**: Maven

### Key Features
- JWT Authentication
- Role-Based Access Control (RBAC)
- Resource Management (CRUD)
- Reservation System with status tracking
- Pagination & Filtering
- Comprehensive Error Handling
- Seed Data for testing

### API Endpoints Summary

| Category | Endpoints | Access Level |
|----------|-----------|--------------|
| **Authentication** | `/api/auth/login` | Public |
| **Resources** | `/api/resources` | USER (read), ADMIN (write) |
| | `/api/resources/{id}` | USER (read), ADMIN (write) |
| | `/api/resources/search` | USER, ADMIN |
| **Reservations** | `/api/reservations` | USER, ADMIN |
| | `/api/reservations/my-*` | USER, ADMIN |
| | `/api/reservations/all` | ADMIN only |
| | `/api/reservations/status/*` | ADMIN only |

### Security Features
- Stateless JWT authentication
- Method-level authorization with `@PreAuthorize`
- Password hashing with BCrypt
- Input validation and sanitization
- CORS configuration
- Security headers

## 📖 Documentation Structure

### 1. User Documentation
- **Quick Start**: Setup and basic usage
- **README**: Project overview and features
- **Help**: Troubleshooting and FAQ
- **API Docs**: Detailed endpoint specifications
- **Postman Collection**: Ready-to-use test collection

### 2. Developer Documentation
- **Architecture**: Design patterns and structure
- **Security**: Implementation details and best practices
- **Code Examples**: Throughout documentation
- **Testing**: Test strategies and examples

### 3. Operations Documentation
- **Deployment**: Docker, Kubernetes examples
- **Monitoring**: Health checks and logging
- **Security**: Hardening checklist
- **Backup**: Database backup strategies

## 🚨 Important Notes

### Security Considerations
- **JWT Secret**: Must be changed in production
- **Database**: Use strong credentials and SSL
- **HTTPS**: Required for production deployment
- **Environment Variables**: Never commit secrets

### Development Tips
- Use the included Maven wrapper (`./mvnw`)
- Start with H2 database for faster development
- Use Swagger UI for API testing
- Import Postman collection for comprehensive testing

### Production Checklist
- [ ] Change JWT secret
- [ ] Configure HTTPS
- [ ] Set proper database permissions
- [ ] Disable debug logging
- [ ] Set up monitoring
- [ ] Configure backup strategy

## 📞 Getting Help

### First Steps
1. **Documentation**: Check the relevant markdown files
2. **Quick Start**: Follow the 5-minute setup guide
3. **Swagger UI**: Interactive documentation when running
4. **Postman Collection**: Complete test suite

### Community Support
- **Stack Overflow**: Tag with `spring-boot` and `jwt`
- **Spring Community**: https://spring.io/community
- **PostgreSQL Community**: https://www.postgresql.org/community/

### Reporting Issues
When reporting issues, please include:
- Application version
- Java version
- PostgreSQL version
- Full error logs
- Steps to reproduce

## 🔄 Documentation Updates

This documentation is maintained with the codebase. For the latest updates:
- Check the markdown files in the repository
- Refer to the commit history
- Submit pull requests for improvements

## 📊 Metrics

- **Documentation Coverage**: 100% of endpoints documented
- **Code Examples**: Included for all major operations
- **Security Guidelines**: Comprehensive security documentation
- **Troubleshooting**: 15+ common issues covered
- **API Testing**: Complete Postman collection included

---

**Next Steps:**
1. Choose your path from the sections above
2. Follow the Quick Start for immediate use
3. Read Architecture for deeper understanding
4. Check Security for production deployment

Happy coding! 🎉