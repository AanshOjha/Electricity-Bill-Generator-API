# Role-Based Access Control (RBAC) Implementation

## Overview
The Electricity Bill Generator API now implements comprehensive role-based access control with two distinct roles:
- **ROLE_USER**: Regular users who can view their own bills and submit readings
- **ROLE_ADMIN**: Administrators who can perform any action including user management and bill generation

## Security Architecture

### 1. User Roles
- **ROLE_USER (Default)**: Limited access to own data
- **ROLE_ADMIN**: Full system access

### 2. Authentication
- Uses Spring Security with HTTP Basic Authentication
- Passwords are encrypted using BCryptPasswordEncoder
- Users authenticate using email/password combination

### 3. Authorization Matrix

| Endpoint | ROLE_USER | ROLE_ADMIN | Notes |
|----------|-----------|------------|-------|
| `POST /api/v1/bill/users/register` | ✅ Public | ✅ Public | Open registration |
| `POST /api/v1/bill/login` | ✅ Public | ✅ Public | Authentication endpoint |
| `POST /api/v1/bill/admin/create` | ❌ | ✅ | Create admin users |
| `GET /api/v1/bill/users` | ❌ | ✅ | List all users |
| `GET /api/v1/bill/users/{meterId}` | ✅ Own Only | ✅ Any | View user details |
| `DELETE /api/v1/bill/users/{meterId}` | ❌ | ✅ | Delete users |
| `DELETE /api/v1/bill/users` | ❌ | ✅ | Delete all users |
| `POST /api/v1/bill/readings` | ✅ Own Only | ✅ Any | Submit readings |
| `POST /api/v1/bill/generate/{meterId}` | ❌ | ✅ | Generate bills |
| `POST /api/v1/bill/bills/generate` | ❌ | ✅ | Generate bills |
| `POST /api/v1/bill/bills/generate/monthly/{meterId}` | ❌ | ✅ | Generate monthly bills |
| `POST /api/v1/bill/bills/process-overdue` | ❌ | ✅ | Process overdue bills |
| `GET /api/v1/bill/users/{meterId}/bills` | ✅ Own Only | ✅ Any | View user bills |
| `GET /api/v1/bill/bills/{billId}` | ✅ Own Only | ✅ Any | View bill details |

## Implementation Details

### 1. Security Configuration (`SecurityConfig.java`)
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    // Configures HTTP security, authentication, and authorization
    // Uses BCryptPasswordEncoder for password hashing
    // Enables method-level security with @PreAuthorize
}
```

### 2. User Entity Updates (`BillUser.java`)
- Added `UserRole role` field with default value `ROLE_USER`
- Enhanced constructors to support role assignment
- Database column: `role` (VARCHAR) with enum mapping

### 3. Custom User Details Service (`CustomUserDetailsService.java`)
- Implements Spring Security's `UserDetailsService`
- Loads user details from database for authentication
- Maps user roles to Spring Security authorities

### 4. Authentication Service (`AuthenticationService.java`)
- Provides utility methods for checking current user permissions
- `getCurrentUser()`: Gets the currently authenticated user
- `isCurrentUserAdmin()`: Checks if current user is admin
- `canAccessMeter(meterId)`: Checks if user can access specific meter

### 5. Method-Level Security Annotations
```java
// Admin only
@PreAuthorize("hasRole('ADMIN')")

// User can access own data, admin can access any
@PreAuthorize("hasRole('ADMIN') or @authenticationService.canAccessMeter(#meterId)")

// Both roles with additional business logic checks
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
```

## Security Features

### 1. Password Security
- All passwords are hashed using BCrypt with salt
- No plain-text passwords stored in database
- Strong password encoding prevents rainbow table attacks

### 2. Access Control
- **Horizontal Access Control**: Users can only access their own data
- **Vertical Access Control**: Clear separation between user and admin privileges
- **Method-Level Security**: Fine-grained control using SpEL expressions

### 3. Default Admin User
- System automatically creates default admin user on first startup
- **Default Credentials** (CHANGE IMMEDIATELY):
  - Email: `admin@billgenerator.com`
  - Password: `admin123`
  - Meter ID: `1000`

## API Usage Examples

### 1. User Registration (Public)
```bash
curl -X POST http://localhost:8080/api/v1/bill/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "address": "123 Main St",
    "password": "userpassword"
  }'
```

### 2. User Login (Public)
```bash
curl -X POST http://localhost:8080/api/v1/bill/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "password": "userpassword"
  }'
```

### 3. User Operations (Authenticated)
```bash
# Submit reading (users can only submit for their own meter)
curl -X POST http://localhost:8080/api/v1/bill/readings \
  -u "john@example.com:userpassword" \
  -H "Content-Type: application/json" \
  -d '{
    "meterId": 1234,
    "currentMonthReading": 150.5,
    "date": "2025-09-09"
  }'

# View own bills
curl -X GET http://localhost:8080/api/v1/bill/users/1234/bills \
  -u "john@example.com:userpassword"
```

### 4. Admin Operations (Admin Only)
```bash
# Create new admin user
curl -X POST http://localhost:8080/api/v1/bill/admin/create \
  -u "admin@billgenerator.com:admin123" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Admin User",
    "email": "newadmin@example.com",
    "address": "Admin Office",
    "password": "secureadminpass"
  }'

# Generate bill for any user
curl -X POST http://localhost:8080/api/v1/bill/generate/1234 \
  -u "admin@billgenerator.com:admin123"

# View all users
curl -X GET http://localhost:8080/api/v1/bill/users \
  -u "admin@billgenerator.com:admin123"
```

## Security Best Practices Implemented

### 1. Principle of Least Privilege
- Users have minimal permissions required for their role
- Clear separation between user and admin capabilities

### 2. Defense in Depth
- Multiple layers of security: HTTP Basic Auth + Method-level annotations + Business logic checks
- Input validation and error handling

### 3. Secure Defaults
- New users get ROLE_USER by default
- Passwords are automatically hashed
- CSRF protection disabled for API usage

### 4. Audit and Logging
- Comprehensive logging of authentication attempts
- Security-related actions are logged with appropriate levels
- Failed access attempts are logged for monitoring

## Database Schema Updates

The `bill_user` table now includes:
```sql
ALTER TABLE bill_user ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'ROLE_USER';
```

## Error Responses

### Authentication Errors
```json
{
  "error": "Invalid credentials",
  "timestamp": "2025-09-09"
}
```

### Authorization Errors
```json
{
  "error": "Access denied: You can only view your own bills",
  "timestamp": "2025-09-09"
}
```

### HTTP Status Codes
- `401 Unauthorized`: Invalid credentials
- `403 Forbidden`: Valid credentials but insufficient permissions
- `404 Not Found`: Resource not found or access denied

## Migration Guide

### For Existing Users
1. All existing users will be assigned `ROLE_USER` by default
2. Admin users must be created using the default admin account or by existing admins
3. No breaking changes to existing API endpoints (authentication now required)

### For Developers
1. Add authentication headers to all API calls
2. Handle 401/403 status codes appropriately
3. Use appropriate endpoints based on user role

## Future Enhancements

1. **JWT Token Authentication**: Replace HTTP Basic with JWT for better scalability
2. **Role Hierarchy**: Add intermediate roles like MANAGER
3. **Permission-Based Access**: More granular permissions beyond roles
4. **Account Lockout**: Implement account lockout after failed attempts
5. **Password Policies**: Enforce strong password requirements
6. **Two-Factor Authentication**: Add 2FA for admin accounts

## Security Considerations

1. **HTTPS Required**: Always use HTTPS in production
2. **Password Rotation**: Regular password changes for admin accounts
3. **Session Management**: Proper session handling and timeout
4. **Input Validation**: All inputs are validated and sanitized
5. **Error Handling**: Generic error messages to prevent information disclosure
