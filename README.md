# Electricity Bill Generator API

A robust REST API for managing electricity billing operations including user registration, meter readings, and automated bill calculations.

## Features

- **User Management**: Register, retrieve, update, and delete users
- **Meter Reading Management**: Record monthly electricity readings
- **Automated Bill Calculation**: Automatically calculate units consumed and bill amounts
- **Historical Data**: Track previous readings for accurate consumption calculation
- **Error Handling**: Comprehensive error handling with meaningful messages
- **Logging**: Structured logging for monitoring and debugging
- **Database Integration**: MySQL database with JPA/Hibernate
- **RESTful API**: Well-designed REST endpoints following industry standards

## Technology Stack

- **Java 17+**
- **Spring Boot 3.x**
- **Spring Data JPA**
- **MySQL Database**
- **Maven** for dependency management
- **SLF4J + Logback** for logging
- **JUnit 5** for testing

## Project Structure

```
src/
├── main/
│   ├── java/com/project/electricitybillgenerator/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Exception handling
│   │   ├── model/           # JPA entities
│   │   ├── repository/      # Data access layer
│   │   ├── service/         # Business logic layer
│   │   └── BillApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/                # Unit and integration tests
```

## API Endpoints

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/bill/users/register` | Register a new user |
| GET | `/api/v1/bill/users` | Get all users |
| GET | `/api/v1/bill/users/{meterId}` | Get user by meter ID |
| DELETE | `/api/v1/bill/users/{meterId}` | Delete user by meter ID |
| DELETE | `/api/v1/bill/users` | Delete all users |

### Reading Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/bill/readings` | Submit a new meter reading |

## API Usage Examples

### Register a User
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

### Submit a Reading
```bash
curl -X POST http://localhost:8080/api/v1/bill/readings \
  -H "Content-Type: application/json" \
  -d '{
    "meterId": 1234,
    "currentMonthReading": 1500.5,
    "date": "2025-09-08"
  }'
```

### Get All Users
```bash
curl -X GET http://localhost:8080/api/v1/bill/users
```

## Setup and Installation

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher

### Database Setup

1. Create a MySQL database:
```sql
CREATE DATABASE elec_bill;
```

2. Update database credentials in `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/elec_bill
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Running the Application

1. Clone the repository:
```bash
git clone <repository-url>
cd Electricity-Bill-Generator-API
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

### Running Tests

```bash
mvn test
```

## Configuration

The application can be configured via `application.properties`:

```properties
# Bill Configuration
bill.rate-per-unit=7.5
bill.min-meter-id=1000
bill.max-meter-id=9999
bill.max-reading-value=999999.99

# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/elec_bill
spring.datasource.username=root
spring.datasource.password=password

# Logging Configuration
logging.level.com.project.electricitybillgenerator=INFO
```

## Business Logic

### Bill Calculation

1. **Previous Reading Retrieval**: The system automatically retrieves the previous month's reading for the same meter
2. **Units Consumed Calculation**: `Units = Current Reading - Previous Reading`
3. **Bill Amount Calculation**: `Bill = Units × Rate Per Unit`
4. **Validation**: All readings are validated for consistency and business rules

### Meter ID Generation

- Meter IDs are automatically generated using a secure random number generator
- Range: configurable (default 1000-9999)
- Uniqueness is guaranteed through database constraints

## Error Handling

The API provides comprehensive error handling with structured error responses:

```json
{
  "timestamp": "2025-09-08T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Meter ID must be positive",
  "path": "/api/v1/bill/readings"
}
```

## Logging

The application uses structured logging with different log levels:

- **INFO**: General application flow
- **DEBUG**: Detailed debugging information
- **WARN**: Warning conditions
- **ERROR**: Error conditions

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions, please open an issue in the repository or contact the development team.
