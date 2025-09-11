# Electricity Bill Generator API

A modern, secure REST API for electricity bill management built with Spring Boot, featuring JWT authentication, proper entity relationships, and clean architecture.

## 🚀 Features

### ✅ **Core Features Implemented:**
- **Customer Management** - Registration, authentication, profile management
- **Meter Management** - Auto-generated meter assignment
- **Smart Meter Reading** - Automatic bill calculation from readings
- **Dynamic Tariff System** - Configurable rates for different customer types
- **Bill Generation** - Automated billing with proper calculations
- **🆕 Automated Monthly Billing** - Scheduled bill generation on the 1st of each month
- **🆕 Admin Billing Controls** - Manual billing triggers and statistics
- **🆕 Data Seeding Tools** - Quick test data generation for development
- **JWT Security** - Secure API with token-based authentication
- **Data Validation** - Comprehensive input validation
- **Exception Handling** - Global error handling with meaningful responses

### 🤖 **NEW: Automated Billing System**
- **Scheduled Processing** - Automatically generates bills on the 1st of every month at 1:00 AM
- **Smart Consumption Calculation** - Calculates units consumed from meter readings
- **Dynamic Tariff Application** - Applies correct rates based on customer type and date
- **Manual Override** - Admin endpoints for on-demand bill generation and monitoring
- **Comprehensive Logging** - Detailed logs for monitoring and debugging
- **Error Resilience** - Continues processing even if individual customer bills fail

📖 **[View Complete Automated Billing Documentation](docs/AUTOMATED_BILLING_SYSTEM.md)**

### 🏗️ **Architecture Highlights:**
- **Clean Separation of Concerns** - Controllers, Services, Repositories, DTOs
- **Entity Relationships** - Proper JPA mapping with foreign keys
- **Business Logic** - Encapsulated in service layer
- **Security** - JWT token authentication
- **Validation** - Bean validation with custom messages
- **Error Handling** - Global exception handler

## 🗄️ Database Schema

```
customers (Customer registration and authentication)
├── meters (One customer can have multiple meters)
│   ├── meter_readings (Historical readings for each meter)
│   └── bills (Generated bills from readings)
│       └── payments (Payment tracking - ready for future)
└── tariffs (Rate configuration for different customer types)
```

## 🔧 Technology Stack

- **Backend:** Spring Boot 3.3.0
- **Database:** MySQL
- **Security:** Spring Security + JWT
- **Validation:** Jakarta Bean Validation
- **Build Tool:** Maven
- **Java Version:** 22

## 🚀 Getting Started

### Prerequisites
- Java 22
- MySQL Server
- Maven (or use included wrapper)

### Setup
1. **Clone the repository**
2. **Configure Database**
   ```properties
   # Update src/main/resources/application.properties
   spring.datasource.url=jdbc:mysql://localhost:3306/elec_bill
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

3. **Create Database**
   ```sql
   CREATE DATABASE elec_bill;
   ```

4. **Run the Application**
   ```bash
   ./mvnw spring-boot:run
   ```

The API will start on `http://localhost:8080`

## 📡 API Endpoints

### Authentication Endpoints (`/api/auth`)

#### Register Customer
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "address": "123 Main St, City",
  "phone": "1234567890",
  "customerType": "RESIDENTIAL"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "customer": {
    "id": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "address": "123 Main St, City",
    "phone": "1234567890",
    "customerType": "RESIDENTIAL",
    "active": true,
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

### Customer Endpoints (`/api/customers`)
*Requires Authorization header: `Bearer <token>`*

#### Get Profile
```http
GET /api/customers/profile
Authorization: Bearer <token>
```

#### Get All Customers
```http
GET /api/customers
Authorization: Bearer <token>
```

### Bill Management (`/api/bills`)
*Requires Authorization header: `Bearer <token>`*

#### Generate Bill from Meter Reading
```http
POST /api/bills/generate
Authorization: Bearer <token>
Content-Type: application/json

{
  "meterId": 1,
  "currentReading": 1250.5,
  "readingDate": "2024-01-15"
}
```

**Response:**
```json
{
  "id": 1,
  "billNumber": "BILL17052389740001",
  "meterNumber": "MTR00001234",
  "customerName": "John Doe",
  "billDate": "2024-01-15",
  "dueDate": "2024-02-14",
  "unitsConsumed": 125.5,
  "amount": 941.25,
  "tax": 0.00,
  "totalAmount": 941.25,
  "remainingAmount": 941.25,
  "status": "PENDING",
  "createdAt": "2024-01-15T10:30:00"
}
```

#### Get My Bills
```http
GET /api/bills/my-bills
Authorization: Bearer <token>
```

#### Get Bill by Number
```http
GET /api/bills/number/BILL17052389740001
Authorization: Bearer <token>
```

### Admin Endpoints (`/api/admin`)
*Requires Authorization header: `Bearer <token>`*

#### Create Tariff
```http
POST /api/admin/tariffs
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "Residential Standard 2024",
  "customerType": "RESIDENTIAL",
  "ratePerUnit": 8.50,
  "fixedCharge": 60.00,
  "minimumCharge": 120.00,
  "effectiveDate": "2024-01-01"
}
```

### 🌱 **NEW: Data Seeding Endpoints (`/api/admin/debug`)**
*Perfect for development and testing!*

#### Seed Test Data
```http
POST /api/admin/debug/seed-data
Authorization: Bearer <admin-token>
```
Creates 3 sample customers with 8 months of historical meter readings.

#### Get Test Customers
```http
GET /api/admin/debug/test-customers
Authorization: Bearer <admin-token>
```

#### Clear Test Data
```http
DELETE /api/admin/debug/clear-test-data
Authorization: Bearer <admin-token>
```

**Test Credentials:**
- Email: `user.a@example.com` | Password: `password123`
- Email: `user.b@example.com` | Password: `password123`  
- Email: `user.c@example.com` | Password: `password123`

📖 **[View Complete Data Seeding Guide](docs/DATA_SEEDING_GUIDE.md)**

## 💡 Business Logic

### Tariff System
- **Residential:** ₹7.50/unit + ₹50 fixed + ₹100 minimum
- **Commercial:** ₹9.00/unit + ₹100 fixed + ₹200 minimum  
- **Industrial:** ₹6.50/unit + ₹200 fixed + ₹500 minimum

### Bill Calculation
```
Units Consumed = Current Reading - Previous Reading
Variable Charge = Units Consumed × Rate Per Unit
Total Charge = Variable Charge + Fixed Charge
Final Amount = Max(Total Charge, Minimum Charge)
```

### Security Features
- **JWT Authentication** with configurable expiry
- **Password Encryption** using BCrypt
- **Role-based Access** (Customer/Admin ready)
- **Request Validation** with meaningful error messages

## 🛡️ Error Handling

The API returns structured error responses:

```json
{
  "code": "BUSINESS_ERROR",
  "message": "Email already exists",
  "timestamp": "2024-01-15T10:30:00"
}
```

**Error Types:**
- `BUSINESS_ERROR` - Business rule violations
- `VALIDATION_ERROR` - Input validation failures
- `USER_NOT_FOUND` - Authentication failures
- `INVALID_CREDENTIALS` - Login failures

## 🎯 What Makes This Implementation Stand Out

### 1. **Professional Architecture**
- Clean separation between controllers, services, and repositories
- Proper DTO usage for API requests/responses
- Entity relationships with proper foreign keys

### 2. **Security First**
- JWT token authentication
- Password encryption
- Request validation at multiple levels

### 3. **Business Logic**
- Automatic meter assignment on customer registration
- Smart bill calculation with previous reading lookup
- Configurable tariff system

### 4. **Code Quality**
- Meaningful variable and method names
- Proper exception handling
- Comprehensive validation messages
- No hardcoded values

### 5. **Scalability Ready**
- Database-first design with proper indexing
- Stateless authentication
- Clean APIs ready for frontend integration

This implementation demonstrates enterprise-level Spring Boot development with proper security, validation, and business logic - perfect for impressing recruiters! 🚀

## 📚 Documentation

- **[Automated Billing System](docs/AUTOMATED_BILLING_SYSTEM.md)** - Complete guide to the scheduled billing system
- **[Data Seeding Guide](docs/DATA_SEEDING_GUIDE.md)** - Quick test data generation for development
- **[Frontend Developer Guide](docs/FRONTEND_DEVELOPER_GUIDE.md)** - Comprehensive API reference for frontend developers

## 🛠️ Scripts

- **[Demo Data Seeding](scripts/demo-data-seeding.ps1)** - Interactive PowerShell script for seeding test data
