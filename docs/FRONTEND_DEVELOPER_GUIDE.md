# Frontend Developer Guide - Electricity Bill Generator API

## Table of Contents
1. [Quick Start](#quick-start)
2. [Authentication & Authorization](#authentication--authorization)
3. [API Endpoints Reference](#api-endpoints-reference)
4. [Data Models & DTOs](#data-models--dtos)
5. [Implementation Examples](#implementation-examples)
6. [Error Handling](#error-handling)
7. [Best Practices](#best-practices)
8. [Testing Guide](#testing-guide)

## Quick Start

### Backend Setup
1. **Prerequisites**: MySQL 8.0+, Java 17+, Maven 3.6+
2. **Database**: Create MySQL database named `elec_bill`
3. **Start Backend**: 
   ```bash
   ./mvnw spring-boot:run
   ```
4. **Base URL**: `http://localhost:8080`
5. **API Base Path**: `/api`

### Default Admin Account
- **Email**: `admin@electricitybill.com`
- **Password**: `admin123`
- **Role**: `ADMIN`

> **Note**: This admin account is automatically created when the application starts for the first time. If the account already exists, it will not be recreated.

## Authentication & Authorization

### Role-Based Access Control
The system has two roles:
- **ROLE_USER**: Regular customers (default for new registrations)
- **ROLE_ADMIN**: System administrators

### JWT Token Structure
```json
{
  "sub": "customer@email.com",
  "role": "ROLE_USER",
  "customerId": 123,
  "iat": 1644156000,
  "exp": 1644242400
}
```

### Authentication Flow
1. **Register** → Get customer details
2. **Login** → Get JWT token + customer info
3. **Include token** in all authenticated requests: `Authorization: Bearer <jwt_token>`

## API Endpoints Reference

### 🔓 Public Endpoints (No Authentication)

#### Register Customer
```http
POST /api/auth/register
Content-Type: application/json
```
**Request Body:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "address": "123 Main Street, City",
  "phone": "1234567890",
  "customerType": "RESIDENTIAL"
}
```
**Response:**
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@example.com",
  "address": "123 Main Street, City",
  "phone": "1234567890",
  "customerType": "RESIDENTIAL",
  "role": "ROLE_USER",
  "active": true,
  "createdAt": "2024-09-09T21:07:15"
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json
```
**Request Body:**
```json
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
    "address": "123 Main Street, City",
    "phone": "1234567890",
    "customerType": "RESIDENTIAL",
    "role": "ROLE_USER",
    "active": true,
    "createdAt": "2024-09-09T21:07:15"
  }
}
```

### 👤 User + Admin Endpoints

#### Get Own Profile
```http
GET /api/customers/profile
Authorization: Bearer <jwt_token>
```
**Response:** Customer object (same as registration response)

#### Update Own Profile
```http
PUT /api/customers/profile
Authorization: Bearer <jwt_token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "name": "John Updated",
  "address": "456 New Street",
  "phone": "9876543210"
}
```

#### Get My Bills
```http
GET /api/bills/my-bills
Authorization: Bearer <jwt_token>
```
**Response:**
```json
[
  {
    "id": 1,
    "billNumber": "BILL-2024-001",
    "customerId": 1,
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "meterId": 1,
    "billingPeriod": "2024-09",
    "previousReading": 1000.0,
    "currentReading": 1250.5,
    "unitsConsumed": 250.5,
    "ratePerUnit": 8.50,
    "energyCharges": 2129.25,
    "fixedCharges": 60.00,
    "totalAmount": 2189.25,
    "dueDate": "2024-10-15",
    "billStatus": "PENDING",
    "generatedAt": "2024-09-09T21:30:00"
  }
]
```

#### Submit Meter Reading
```http
POST /api/meter-readings/submit
Authorization: Bearer <jwt_token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "currentReading": 1350.75,
  "readingDate": "2024-09-15"
}
```
**Response:**
```json
{
  "id": 5,
  "meterId": 1,
  "readingValue": 1350.75,
  "readingDate": "2024-09-15T00:00:00",
  "readingType": "REGULAR",
  "recordedAt": "2024-09-09T21:35:00"
}
```

#### Get My Meter Readings
```http
GET /api/meter-readings/my-readings
Authorization: Bearer <jwt_token>
```

#### Get Bill by Number
```http
GET /api/bills/number/{billNumber}
Authorization: Bearer <jwt_token>
```

### 🔐 Admin Only Endpoints

#### Get All Customers
```http
GET /api/customers
Authorization: Bearer <admin_jwt_token>
```

#### Get Customer by ID
```http
GET /api/customers/{id}
Authorization: Bearer <admin_jwt_token>
```

#### Delete Customer
```http
DELETE /api/customers/{id}
Authorization: Bearer <admin_jwt_token>
```

#### Generate Bill (Admin)
```http
POST /api/bills/generate
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "meterId": 1,
  "currentReading": 1450.25,
  "readingDate": "2024-09-30"
}
```

#### Get Customer Bills
```http
GET /api/bills/customer/{customerId}
Authorization: Bearer <admin_jwt_token>
```

#### Get Overdue Bills
```http
GET /api/bills/overdue
Authorization: Bearer <admin_jwt_token>
```

#### Promote User to Admin
```http
POST /api/admin/users/{userId}/promote
Authorization: Bearer <admin_jwt_token>
```

#### Demote Admin to User
```http
POST /api/admin/users/{userId}/demote
Authorization: Bearer <admin_jwt_token>
```

#### Activate Customer Account
```http
POST /api/admin/users/{userId}/activate
Authorization: Bearer <admin_jwt_token>
```

#### Deactivate Customer Account
```http
POST /api/admin/users/{userId}/deactivate
Authorization: Bearer <admin_jwt_token>
```

#### Create Tariff
```http
POST /api/admin/tariffs
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json
```
**Request Body:**
```json
{
  "name": "Residential Rate 2024",
  "customerType": "RESIDENTIAL",
  "ratePerUnit": 8.50,
  "fixedCharge": 60.00,
  "minimumCharge": 120.00,
  "effectiveDate": "2024-01-01"
}
```

#### Get All Tariffs
```http
GET /api/admin/tariffs
Authorization: Bearer <admin_jwt_token>
```

#### Get Pending Bills
```http
GET /api/meter-readings/pending-bills
Authorization: Bearer <admin_jwt_token>
```

## Data Models & DTOs

### Customer Types
```javascript
const CUSTOMER_TYPES = {
  RESIDENTIAL: 'RESIDENTIAL',
  COMMERCIAL: 'COMMERCIAL', 
  INDUSTRIAL: 'INDUSTRIAL'
};
```

### User Roles
```javascript
const USER_ROLES = {
  USER: 'USER',
  ADMIN: 'ADMIN'
};
```

### Bill Status
```javascript
const BILL_STATUS = {
  PENDING: 'PENDING',
  PAID: 'PAID',
  OVERDUE: 'OVERDUE',
  CANCELLED: 'CANCELLED'
};
```

### Meter Types
```javascript
const METER_TYPES = {
  ANALOG: 'ANALOG',
  DIGITAL: 'DIGITAL',
  SMART: 'SMART'
};
```

### Meter Status
```javascript
const METER_STATUS = {
  ACTIVE: 'ACTIVE',
  INACTIVE: 'INACTIVE',
  MAINTENANCE: 'MAINTENANCE'
};
```

### Reading Types
```javascript
const READING_TYPES = {
  REGULAR: 'REGULAR',
  ESTIMATED: 'ESTIMATED',
  FINAL: 'FINAL'
};
```

## Implementation Examples

### 1. React/JavaScript Authentication Service

```javascript
class AuthService {
  constructor() {
    this.baseURL = 'http://localhost:8080/api';
    this.token = localStorage.getItem('token');
  }

  // Register new customer
  async register(userData) {
    const response = await fetch(`${this.baseURL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData)
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Registration failed');
    }
    
    return await response.json();
  }

  // Login customer
  async login(email, password) {
    const response = await fetch(`${this.baseURL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ email, password })
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Login failed');
    }
    
    const data = await response.json();
    this.token = data.token;
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data.customer));
    
    return data;
  }

  // Logout
  logout() {
    this.token = null;
    localStorage.removeItem('token');
    localStorage.removeItem('user');
  }

  // Check if user is logged in
  isAuthenticated() {
    return !!this.token;
  }

  // Get current user
  getCurrentUser() {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user) : null;
  }

  // Check if user is admin
  isAdmin() {
    const user = this.getCurrentUser();
    return user && user.role === 'ADMIN';
  }

  // Get auth headers
  getAuthHeaders() {
    return {
      'Authorization': `Bearer ${this.token}`,
      'Content-Type': 'application/json',
    };
  }
}

const authService = new AuthService();
export default authService;
```

### 2. API Service for Bills

```javascript
class BillService {
  constructor() {
    this.baseURL = 'http://localhost:8080/api/bills';
  }

  // Get authenticated headers
  getHeaders() {
    return {
      'Authorization': `Bearer ${localStorage.getItem('token')}`,
      'Content-Type': 'application/json',
    };
  }

  // Get my bills (USER/ADMIN)
  async getMyBills() {
    const response = await fetch(`${this.baseURL}/my-bills`, {
      headers: this.getHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch bills');
    }
    
    return await response.json();
  }

  // Generate bill (ADMIN only)
  async generateBill(meterReadingData) {
    const response = await fetch(`${this.baseURL}/generate`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(meterReadingData)
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to generate bill');
    }
    
    return await response.json();
  }

  // Get customer bills (ADMIN only)
  async getCustomerBills(customerId) {
    const response = await fetch(`${this.baseURL}/customer/${customerId}`, {
      headers: this.getHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch customer bills');
    }
    
    return await response.json();
  }

  // Get overdue bills (ADMIN only)
  async getOverdueBills() {
    const response = await fetch(`${this.baseURL}/overdue`, {
      headers: this.getHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch overdue bills');
    }
    
    return await response.json();
  }

  // Get bill by number
  async getBillByNumber(billNumber) {
    const response = await fetch(`${this.baseURL}/number/${billNumber}`, {
      headers: this.getHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Bill not found');
    }
    
    return await response.json();
  }
}

const billService = new BillService();
export default billService;
```

### 3. Meter Reading Service

```javascript
class MeterReadingService {
  constructor() {
    this.baseURL = 'http://localhost:8080/api/meter-readings';
  }

  getHeaders() {
    return {
      'Authorization': `Bearer ${localStorage.getItem('token')}`,
      'Content-Type': 'application/json',
    };
  }

  // Submit meter reading (USER/ADMIN)
  async submitReading(readingData) {
    const response = await fetch(`${this.baseURL}/submit`, {
      method: 'POST',
      headers: this.getHeaders(),
      body: JSON.stringify(readingData)
    });
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || 'Failed to submit reading');
    }
    
    return await response.json();
  }

  // Get my readings (USER/ADMIN)
  async getMyReadings() {
    const response = await fetch(`${this.baseURL}/my-readings`, {
      headers: this.getHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch readings');
    }
    
    return await response.json();
  }

  // Get pending bills (ADMIN only)
  async getPendingBills() {
    const response = await fetch(`${this.baseURL}/pending-bills`, {
      headers: this.getHeaders()
    });
    
    if (!response.ok) {
      throw new Error('Failed to fetch pending bills');
    }
    
    return await response.json();
  }
}

const meterReadingService = new MeterReadingService();
export default meterReadingService;
```

### 4. React Hook for Authentication

```javascript
import { useState, useEffect, createContext, useContext } from 'react';
import authService from './authService';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initAuth = () => {
      if (authService.isAuthenticated()) {
        setUser(authService.getCurrentUser());
      }
      setLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email, password) => {
    try {
      const response = await authService.login(email, password);
      setUser(response.customer);
      return response;
    } catch (error) {
      throw error;
    }
  };

  const register = async (userData) => {
    try {
      const response = await authService.register(userData);
      return response;
    } catch (error) {
      throw error;
    }
  };

  const logout = () => {
    authService.logout();
    setUser(null);
  };

  const isAdmin = () => {
    return user && user.role === 'ADMIN';
  };

  const value = {
    user,
    login,
    register,
    logout,
    isAdmin,
    isAuthenticated: !!user,
    loading
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};
```

### 5. Protected Route Component

```javascript
import { Navigate } from 'react-router-dom';
import { useAuth } from './useAuth';

const ProtectedRoute = ({ children, adminOnly = false }) => {
  const { isAuthenticated, isAdmin, loading } = useAuth();

  if (loading) {
    return <div>Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (adminOnly && !isAdmin()) {
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;
```

### 6. Customer Dashboard Component Example

```javascript
import React, { useState, useEffect } from 'react';
import { useAuth } from './useAuth';
import billService from './billService';
import meterReadingService from './meterReadingService';

const CustomerDashboard = () => {
  const { user } = useAuth();
  const [bills, setBills] = useState([]);
  const [readings, setReadings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newReading, setNewReading] = useState({
    currentReading: '',
    readingDate: new Date().toISOString().split('T')[0]
  });

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    try {
      const [billsData, readingsData] = await Promise.all([
        billService.getMyBills(),
        meterReadingService.getMyReadings()
      ]);
      setBills(billsData);
      setReadings(readingsData);
    } catch (error) {
      console.error('Error loading data:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmitReading = async (e) => {
    e.preventDefault();
    try {
      await meterReadingService.submitReading({
        currentReading: parseFloat(newReading.currentReading),
        readingDate: newReading.readingDate
      });
      
      // Reset form and reload data
      setNewReading({
        currentReading: '',
        readingDate: new Date().toISOString().split('T')[0]
      });
      loadData();
      
      alert('Reading submitted successfully!');
    } catch (error) {
      alert(`Error: ${error.message}`);
    }
  };

  if (loading) return <div>Loading...</div>;

  return (
    <div className="dashboard">
      <h1>Welcome, {user.name}!</h1>
      
      {/* Customer Info */}
      <div className="customer-info">
        <h2>Your Information</h2>
        <p><strong>Email:</strong> {user.email}</p>
        <p><strong>Address:</strong> {user.address}</p>
        <p><strong>Phone:</strong> {user.phone}</p>
        <p><strong>Type:</strong> {user.customerType}</p>
      </div>

      {/* Submit Reading Form */}
      <div className="submit-reading">
        <h2>Submit Meter Reading</h2>
        <form onSubmit={handleSubmitReading}>
          <div>
            <label>Current Reading:</label>
            <input
              type="number"
              step="0.01"
              value={newReading.currentReading}
              onChange={(e) => setNewReading({...newReading, currentReading: e.target.value})}
              required
            />
          </div>
          <div>
            <label>Reading Date:</label>
            <input
              type="date"
              value={newReading.readingDate}
              onChange={(e) => setNewReading({...newReading, readingDate: e.target.value})}
              required
            />
          </div>
          <button type="submit">Submit Reading</button>
        </form>
      </div>

      {/* Bills List */}
      <div className="bills-section">
        <h2>Your Bills</h2>
        {bills.length === 0 ? (
          <p>No bills found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Bill Number</th>
                <th>Period</th>
                <th>Units Consumed</th>
                <th>Amount</th>
                <th>Due Date</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {bills.map(bill => (
                <tr key={bill.id}>
                  <td>{bill.billNumber}</td>
                  <td>{bill.billingPeriod}</td>
                  <td>{bill.unitsConsumed}</td>
                  <td>₹{bill.totalAmount}</td>
                  <td>{new Date(bill.dueDate).toLocaleDateString()}</td>
                  <td className={`status-${bill.billStatus.toLowerCase()}`}>
                    {bill.billStatus}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Recent Readings */}
      <div className="readings-section">
        <h2>Recent Meter Readings</h2>
        {readings.length === 0 ? (
          <p>No readings found.</p>
        ) : (
          <table>
            <thead>
              <tr>
                <th>Reading Value</th>
                <th>Date</th>
                <th>Type</th>
                <th>Recorded At</th>
              </tr>
            </thead>
            <tbody>
              {readings.slice(0, 5).map(reading => (
                <tr key={reading.id}>
                  <td>{reading.readingValue}</td>
                  <td>{new Date(reading.readingDate).toLocaleDateString()}</td>
                  <td>{reading.readingType}</td>
                  <td>{new Date(reading.recordedAt).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default CustomerDashboard;
```

## Error Handling

### Common HTTP Status Codes

| Status Code | Meaning | Common Causes |
|-------------|---------|---------------|
| 400 | Bad Request | Invalid input data, validation errors |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Insufficient permissions for role |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Email already exists, duplicate data |
| 500 | Internal Server Error | Server-side errors |

### Error Response Format
```json
{
  "timestamp": "2024-01-01T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/auth/register",
  "details": {
    "name": "Name is required",
    "email": "Invalid email format"
  }
}
```

### Error Handling Utility

```javascript
class ErrorHandler {
  static handleApiError(error, response) {
    if (response) {
      switch (response.status) {
        case 400:
          return this.handleValidationError(error);
        case 401:
          return this.handleUnauthorizedError();
        case 403:
          return this.handleForbiddenError();
        case 404:
          return this.handleNotFoundError();
        case 409:
          return this.handleConflictError(error);
        default:
          return this.handleGenericError(error);
      }
    }
    return this.handleNetworkError();
  }

  static handleValidationError(error) {
    if (error.details) {
      return Object.values(error.details).join(', ');
    }
    return error.message || 'Invalid input data';
  }

  static handleUnauthorizedError() {
    // Redirect to login
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login';
    return 'Session expired. Please login again.';
  }

  static handleForbiddenError() {
    return 'You do not have permission to perform this action.';
  }

  static handleNotFoundError() {
    return 'The requested resource was not found.';
  }

  static handleConflictError(error) {
    return error.message || 'This data already exists.';
  }

  static handleGenericError(error) {
    return error.message || 'An unexpected error occurred.';
  }

  static handleNetworkError() {
    return 'Network error. Please check your connection.';
  }
}

export default ErrorHandler;
```

## Best Practices

### 1. Token Management
- Store JWT token securely (localStorage for web apps)
- Include token in Authorization header: `Bearer <token>`
- Handle token expiration gracefully
- Clear token on logout

### 2. API Request Patterns
```javascript
// Good: Centralized API configuration
const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 10000,
});

// Add request interceptor for auth
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

### 3. Form Validation
```javascript
// Client-side validation to match backend requirements
const validateRegistration = (data) => {
  const errors = {};
  
  if (!data.name || data.name.length > 100) {
    errors.name = 'Name is required and must be under 100 characters';
  }
  
  if (!data.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(data.email)) {
    errors.email = 'Valid email is required';
  }
  
  if (!data.password || data.password.length < 6) {
    errors.password = 'Password must be at least 6 characters';
  }
  
  if (!data.phone || data.phone.length > 15) {
    errors.phone = 'Phone is required and must be under 15 characters';
  }
  
  return errors;
};
```

### 4. Role-Based UI
```javascript
// Conditional rendering based on user role
const Navigation = () => {
  const { user, isAdmin } = useAuth();
  
  return (
    <nav>
      <Link to="/dashboard">Dashboard</Link>
      <Link to="/bills">My Bills</Link>
      <Link to="/readings">Meter Readings</Link>
      
      {isAdmin() && (
        <>
          <Link to="/admin/customers">All Customers</Link>
          <Link to="/admin/bills">All Bills</Link>
          <Link to="/admin/tariffs">Manage Tariffs</Link>
        </>
      )}
    </nav>
  );
};
```

### 5. Loading States and UX
```javascript
const [loading, setLoading] = useState(false);
const [error, setError] = useState(null);

const handleSubmit = async (data) => {
  setLoading(true);
  setError(null);
  
  try {
    const result = await apiService.submitData(data);
    // Handle success
  } catch (err) {
    setError(ErrorHandler.handleApiError(err, err.response));
  } finally {
    setLoading(false);
  }
};
```

## Testing Guide

### 1. Test User Accounts

#### Default Admin
- **Email**: `admin@electricitybill.com`
- **Password**: `admin123`

#### Test Regular User
You can register or use:
- **Email**: `test@example.com`
- **Password**: `password123`

### 2. API Testing with Postman/Insomnia

#### Environment Variables
```json
{
  "baseUrl": "http://localhost:8080/api",
  "userToken": "",
  "adminToken": "",
  "customerId": ""
}
```

#### Test Sequence
1. Register user → Save customer ID
2. Login user → Save user token
3. Login admin → Save admin token
4. Test user endpoints with user token
5. Test admin endpoints with admin token
6. Verify access control (user token on admin endpoints should fail)

### 3. Frontend Testing Checklist

#### Authentication Flow
- [ ] Registration with valid data works
- [ ] Registration with invalid data shows proper errors
- [ ] Login with correct credentials works
- [ ] Login with wrong credentials shows error
- [ ] Token is stored and included in requests
- [ ] Logout clears token and redirects
- [ ] Protected routes redirect to login when not authenticated

#### User Features
- [ ] User can view own profile
- [ ] User can update own profile
- [ ] User can submit meter readings
- [ ] User can view own bills
- [ ] User can view own meter readings
- [ ] User cannot access admin endpoints

#### Admin Features
- [ ] Admin can view all customers
- [ ] Admin can generate bills
- [ ] Admin can view customer bills
- [ ] Admin can promote/demote users
- [ ] Admin can activate/deactivate accounts
- [ ] Admin can manage tariffs

#### Error Handling
- [ ] Network errors are handled gracefully
- [ ] Validation errors are displayed properly
- [ ] Unauthorized access shows appropriate message
- [ ] Token expiration redirects to login

### 4. Sample Test Data

#### Registration Data
```json
{
  "name": "Test Customer",
  "email": "customer@test.com",
  "password": "password123",
  "address": "123 Test Street, Test City",
  "phone": "1234567890",
  "customerType": "RESIDENTIAL"
}
```

#### Meter Reading Data
```json
{
  "currentReading": 1250.75,
  "readingDate": "2024-09-15"
}
```

#### Tariff Data (Admin)
```json
{
  "name": "Test Residential Rate",
  "customerType": "RESIDENTIAL",
  "ratePerUnit": 9.00,
  "fixedCharge": 50.00,
  "minimumCharge": 100.00,
  "effectiveDate": "2024-01-01"
}
```

## Troubleshooting

### Common Issues

1. **CORS Errors**: Backend includes `@CrossOrigin(origins = "*")` - should work for development
2. **401 Unauthorized**: Check if token is included in Authorization header
3. **403 Forbidden**: Verify user has correct role for the endpoint
4. **Connection Refused**: Ensure backend is running on `localhost:8080`
5. **Database Errors**: Ensure MySQL is running and `elec_bill` database exists

### Development Tips

1. **Use Browser DevTools**: Check Network tab for API requests/responses
2. **Console Logging**: Log API responses to understand data structure
3. **Postman Collection**: Create collection for API testing
4. **Environment Configuration**: Use environment variables for API URLs
5. **Error Boundaries**: Implement React error boundaries for better UX

This guide provides everything a frontend developer needs to successfully integrate with the Electricity Bill Generator API. The backend provides a robust foundation with proper authentication, authorization, and data management capabilities.
