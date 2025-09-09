# Electricity Bill Generator API

A secure, role-based REST API for managing electricity billing operations with comprehensive user authentication and authorization.

## 🚀 Features

### Core Functionality
- **User Management**: Register, retrieve, update, and delete users
- **Meter Reading Management**: Record monthly electricity readings
- **Automated Bill Generation**: Generate bills with automated calculations
- **Bill Management**: View, track, and manage electricity bills
- **Historical Data**: Track previous readings for accurate consumption calculation

### Security Features
- **Role-Based Access Control (RBAC)**: Two distinct user roles (USER/ADMIN)
- **Authentication**: Secure HTTP Basic Authentication with encrypted passwords
- **Authorization**: Method-level security with granular permissions
- **Password Security**: BCrypt password hashing with salt
- **Access Control**: Users can only access their own data, admins have full access

### Technical Features
- **RESTful API**: Well-designed REST endpoints following industry standards
- **Error Handling**: Comprehensive error handling with meaningful messages
- **Logging**: Structured logging for monitoring and debugging
- **Database Integration**: MySQL database with JPA/Hibernate
- **Auto-Configuration**: Automatic admin user creation on first startup

## 🛡️ Security & Roles

### User Roles

| Role | Description | Access Level |
|------|-------------|--------------|
| **ROLE_USER** | Regular users | Can view own bills and submit readings |
| **ROLE_ADMIN** | Administrators | Full system access including user management |

### Default Admin Account
On first startup, a default admin account is created:
- **Email**: `admin@billgenerator.com`
- **Password**: `admin123`
- **⚠️ IMPORTANT**: Change the default password immediately!

## 🔧 Technology Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Security** (Authentication & Authorization)
- **Spring Data JPA**
- **MySQL Database**
- **Maven** for dependency management
- **BCrypt** for password encryption
- **SLF4J + Logback** for logging
- **JUnit 5** for testing

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/project/electricitybillgenerator/
│   │   ├── config/          # Configuration classes (Security, Bill settings)
│   │   ├── controller/      # REST controllers (Bill, Auth)
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Exception handling
│   │   ├── model/           # JPA entities (User, Bill, Reading, Roles)
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic layer
│   │   └── BillApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/                # Unit and integration tests
```

## 🔐 API Endpoints & Permissions

### Authentication Endpoints (Public)

| Method | Endpoint | Description | Access |
|--------|----------|-------------|---------|
| POST | `/api/v1/bill/users/register` | Register a new user | 🌐 Public |
| POST | `/api/v1/bill/login` | User authentication | 🌐 Public |

### User Management

| Method | Endpoint | Description | USER | ADMIN |
|--------|----------|-------------|------|-------|
| POST | `/api/v1/bill/admin/create` | Create admin user | ❌ | ✅ |
| GET | `/api/v1/bill/users` | Get all users | ❌ | ✅ |
| GET | `/api/v1/bill/users/{meterId}` | Get user by meter ID | 👤 Own Only | ✅ Any |
| DELETE | `/api/v1/bill/users/{meterId}` | Delete user | ❌ | ✅ |
| DELETE | `/api/v1/bill/users` | Delete all users | ❌ | ✅ |

### Reading Management

| Method | Endpoint | Description | USER | ADMIN |
|--------|----------|-------------|------|-------|
| POST | `/api/v1/bill/readings` | Submit meter reading | 👤 Own Only | ✅ Any |

### Bill Management

| Method | Endpoint | Description | USER | ADMIN |
|--------|----------|-------------|------|-------|
| POST | `/api/v1/bill/generate/{meterId}` | Generate bill | ❌ | ✅ |
| POST | `/api/v1/bill/bills/generate` | Generate bill (custom) | ❌ | ✅ |
| POST | `/api/v1/bill/bills/generate/monthly/{meterId}` | Generate monthly bill | ❌ | ✅ |
| POST | `/api/v1/bill/bills/process-overdue` | Process overdue bills | ❌ | ✅ |
| GET | `/api/v1/bill/users/{meterId}/bills` | Get user's bills | 👤 Own Only | ✅ Any |
| GET | `/api/v1/bill/bills/{billId}` | Get bill details | 👤 Own Only | ✅ Any |

**Legend:**
- 🌐 Public: No authentication required
- 👤 Own Only: Users can only access their own data
- ✅ Any: Full access
- ❌ No access

## 💡 API Usage Examples

### 🌐 Public Endpoints (No Authentication)

#### Register a New User
```bash
curl -X POST http://localhost:8080/api/v1/bill/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "address": "123 Main St, City",
    "email": "john.doe@example.com",
    "password": "securepassword"
  }'
```

#### User Login
```bash
curl -X POST http://localhost:8080/api/v1/bill/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@example.com",
    "password": "securepassword"
  }'
```

### 👤 User Operations (Authentication Required)

#### Submit a Meter Reading (Own Meter Only)
```bash
curl -X POST http://localhost:8080/api/v1/bill/readings \
  -u "john.doe@example.com:securepassword" \
  -H "Content-Type: application/json" \
  -d '{
    "meterId": 1234,
    "currentMonthReading": 1500.5,
    "date": "2025-09-09"
  }'
```

#### View Own Bills
```bash
curl -X GET http://localhost:8080/api/v1/bill/users/1234/bills \
  -u "john.doe@example.com:securepassword"
```

#### View Specific Bill (If You Own It)
```bash
curl -X GET http://localhost:8080/api/v1/bill/bills/123 \
  -u "john.doe@example.com:securepassword"
```

### 🔐 Admin Operations (Admin Authentication Required)

#### Login as Admin (Default Account)
```bash
curl -X POST http://localhost:8080/api/v1/bill/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@billgenerator.com",
    "password": "admin123"
  }'
```

#### Create New Admin User
```bash
curl -X POST http://localhost:8080/api/v1/bill/admin/create \
  -u "admin@billgenerator.com:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin Smith",
    "email": "admin.smith@company.com",
    "address": "Admin Office",
    "password": "secureadminpassword"
  }'
```

#### Get All Users
```bash
curl -X GET http://localhost:8080/api/v1/bill/users \
  -u "admin@billgenerator.com:admin123"
```

#### Generate Bill for Any User
```bash
curl -X POST http://localhost:8080/api/v1/bill/generate/1234 \
  -u "admin@billgenerator.com:admin123"
```

#### Process Overdue Bills
```bash
curl -X POST http://localhost:8080/api/v1/bill/bills/process-overdue \
  -u "admin@billgenerator.com:admin123"
```

#### Delete User (Admin Only)
```bash
curl -X DELETE http://localhost:8080/api/v1/bill/users/1234 \
  -u "admin@billgenerator.com:admin123"
```

## ⚙️ Setup and Installation

### Prerequisites

- **Java 17** or higher
- **Maven 3.6** or higher  
- **MySQL 8.0** or higher

### Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE elec_bill;
```

2. Update database credentials in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/elec_bill
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Running the Application

1. **Clone the repository:**
```bash
git clone https://github.com/AanshOjha/Electricity-Bill-Generator-API.git
cd Electricity-Bill-Generator-API
```

2. **Build the project:**
```bash
./mvnw clean install
# or on Windows
.\mvnw.cmd clean install
```

3. **Run the application:**
```bash
./mvnw spring-boot:run
# or on Windows
.\mvnw.cmd spring-boot:run
```

4. **Application startup:**
   - The application will start on `http://localhost:8080`
   - Default admin user will be created automatically
   - Check console logs for default admin credentials

### 🧪 Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test
./mvnw test -Dtest=BillApplicationTests

# Run tests with coverage
./mvnw test jacoco:report
```

### 🚀 Quick Start Guide

1. **Start the application** (see steps above)
2. **First login:** Use default admin credentials from console logs
3. **Change admin password:** Create a new admin user and delete the default one
4. **Register users:** Use the registration endpoint or admin panel
5. **Start billing:** Users submit readings, admins generate bills

## 🔧 Configuration

### Application Properties

```properties
# Application Configuration
spring.application.name=Electricity Bill Generator API
server.port=8080

# Security Configuration
# (Passwords are automatically encrypted with BCrypt)

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/elec_bill?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true

# Bill Configuration
bill.rate-per-unit=7.5
bill.min-meter-id=1000
bill.max-meter-id=9999
bill.max-reading-value=999999.99

# Logging Configuration
logging.level.com.project.electricitybillgenerator=INFO
logging.level.org.springframework.security=DEBUG
```

### Environment Variables

You can override properties using environment variables:

```bash
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/elec_bill
export SPRING_DATASOURCE_USERNAME=your_username
export SPRING_DATASOURCE_PASSWORD=your_password
export BILL_RATE_PER_UNIT=8.0
```

## 💼 Business Logic

### Bill Calculation Process

1. **Reading Submission**: Users submit current meter readings
2. **Previous Reading Retrieval**: System automatically finds previous month's reading
3. **Units Consumed Calculation**: `Units = Current Reading - Previous Reading`
4. **Bill Amount Calculation**: `Bill Amount = Units × Rate Per Unit`
5. **Bill Generation**: Automated bill creation with due dates and status tracking

### Security & Access Control

- **Authentication**: HTTP Basic Authentication with encrypted passwords
- **Authorization**: Role-based access control with method-level security
- **Data Protection**: Users can only access their own data
- **Admin Privileges**: Full system access for administrative operations

### Meter ID Management

- **Auto-Generation**: Secure random number generation for meter IDs
- **Range Configuration**: Configurable ID range (default: 1000-9999)
- **Uniqueness**: Database constraints ensure unique meter IDs
- **Admin Override**: Admins can assign specific meter IDs

### Bill Status Management

- **DUE**: Newly generated bills
- **PAID**: Bills marked as paid
- **OVERDUE**: Bills past due date (automated processing available)

## 🚨 Error Handling & Security

### HTTP Status Codes

| Status | Description | When |
|--------|-------------|------|
| `200 OK` | Success | Successful operations |
| `201 Created` | Resource created | User/bill creation |
| `401 Unauthorized` | Authentication failed | Invalid credentials |
| `403 Forbidden` | Access denied | Insufficient permissions |
| `404 Not Found` | Resource not found | Invalid IDs or access denied |
| `400 Bad Request` | Invalid input | Validation errors |
| `500 Internal Server Error` | Server error | Unexpected errors |

### Error Response Format

```json
{
  "error": "Access denied: You can only view your own bills",
  "timestamp": "2025-09-09T10:30:00"
}
```

### Security Error Examples

#### Invalid Credentials
```json
{
  "error": "Invalid credentials",
  "timestamp": "2025-09-09T10:30:00"
}
```

#### Access Denied
```json
{
  "error": "Access denied: You can only view your own bills",
  "timestamp": "2025-09-09T10:30:00"
}
```

#### Admin Required
```json
{
  "error": "Admin access required for this operation",
  "timestamp": "2025-09-09T10:30:00"
}
```

## 📊 Logging & Monitoring

### Log Levels

- **INFO**: Authentication events, bill generation, user operations
- **DEBUG**: Detailed request/response information  
- **WARN**: Security warnings, validation failures
- **ERROR**: Authentication failures, system errors

### Security Logging

- All authentication attempts (success/failure)
- Authorization violations
- Admin operations
- Data access patterns

### Example Log Entries

```
INFO  - Successful login for user: john@example.com with role: ROLE_USER
WARN  - User john@example.com attempted to access bill 123 which belongs to different user
ERROR - Login failed for email: hacker@example.com
INFO  - Admin admin@company.com created new admin user: newadmin@company.com
```

## 🛠️ Development & Testing

### Architecture

- **Layered Architecture**: Controller → Service → Repository → Database
- **Dependency Injection**: Spring Boot's IoC container
- **Security Integration**: Spring Security with method-level annotations
- **Data Persistence**: JPA/Hibernate with MySQL

### Testing Strategy

```bash
# Unit Tests
./mvnw test -Dtest="*Test"

# Integration Tests  
./mvnw test -Dtest="*IntegrationTest"

# Security Tests
./mvnw test -Dtest="*SecurityTest"

# All Tests with Coverage
./mvnw clean test jacoco:report
```

### Database Schema

Key entities and their relationships:

```sql
-- Users with roles
CREATE TABLE bill_user (
    meter_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    address VARCHAR(255) NOT NULL,
    password VARCHAR(100) NOT NULL,  -- BCrypt hashed
    role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER'
);

-- Bills with status tracking
CREATE TABLE bill (
    bill_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meter_id INT NOT NULL,
    billing_period_start DATE NOT NULL,
    billing_period_end DATE NOT NULL,
    previous_reading DECIMAL(10,2),
    current_reading DECIMAL(10,2) NOT NULL,
    units_consumed DECIMAL(10,2) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    due_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (meter_id) REFERENCES bill_user(meter_id)
);

-- Readings history
CREATE TABLE billreading (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    meter_id INT NOT NULL,
    current_month_reading DOUBLE NOT NULL,
    date DATE NOT NULL,
    FOREIGN KEY (meter_id) REFERENCES bill_user(meter_id)
);
```

## 🔗 API Documentation

### Authentication Header Format

All protected endpoints require HTTP Basic Authentication:

```
Authorization: Basic base64(email:password)
```

### Content Type

All requests with body should use:

```
Content-Type: application/json
```

### Response Format

Successful responses return relevant data:

```json
{
  "billId": 123,
  "meterId": 1234,
  "billingPeriodStart": "2025-08-01",
  "billingPeriodEnd": "2025-08-31",
  "unitsConsumed": 150.5,
  "amount": 1128.75,
  "status": "DUE"
}
```

## 🚀 Deployment

### Production Checklist

- [ ] Change default admin password
- [ ] Configure secure database credentials
- [ ] Set up HTTPS/TLS encryption
- [ ] Configure proper logging levels
- [ ] Set up database backups
- [ ] Configure monitoring and alerting
- [ ] Review security configurations
- [ ] Set up load balancing (if needed)

### Docker Deployment (Optional)

```dockerfile
FROM openjdk:17-jre-slim
COPY target/electricity-bill-generator-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build and run with Docker
docker build -t electricity-bill-api .
docker run -p 8080:8080 electricity-bill-api
```

### Environment-Specific Configurations

```bash
# Development
export SPRING_PROFILES_ACTIVE=dev

# Production  
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:mysql://prod-db:3306/elec_bill
```

## 🤝 Contributing

### Development Setup

1. **Fork** the repository
2. **Clone** your fork locally
3. **Create** a feature branch: `git checkout -b feature/your-feature`
4. **Make** your changes
5. **Add tests** for new functionality
6. **Run tests**: `./mvnw test`
7. **Commit** your changes: `git commit -am 'Add some feature'`
8. **Push** to the branch: `git push origin feature/your-feature`
9. **Submit** a pull request

### Code Standards

- Follow Java naming conventions
- Add comprehensive JavaDoc comments
- Write unit tests for new features
- Ensure security best practices
- Update documentation as needed

### Security Considerations

- Always validate user input
- Use parameterized queries
- Implement proper error handling
- Log security events appropriately
- Follow principle of least privilege

## 📚 Additional Resources

- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [Spring Boot Reference Guide](https://spring.io/projects/spring-boot)
- [SECURITY_IMPLEMENTATION.md](./SECURITY_IMPLEMENTATION.md) - Detailed security guide
- [API Usage Examples](./NEW_ENDPOINTS_SUMMARY.md) - Comprehensive API examples

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support & Contact

For support, questions, or contributions:

- **Issues**: [GitHub Issues](https://github.com/AanshOjha/Electricity-Bill-Generator-API/issues)
- **Discussions**: [GitHub Discussions](https://github.com/AanshOjha/Electricity-Bill-Generator-API/discussions)
- **Security Issues**: Please report privately to the maintainers

---

**⚠️ Security Notice**: This application includes authentication and authorization. Always use HTTPS in production and change default passwords immediately after setup.
