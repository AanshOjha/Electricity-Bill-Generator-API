# Data Seeding Documentation

## Debug Controller Endpoints

The `DebugController` provides several endpoints for populating the database with test data. These endpoints are only accessible to users with ADMIN role.

**Base URL**: `/api/admin/debug`

### 1. Seed Sample Data

**Endpoint**: `POST /api/admin/debug/seed-data`

**Description**: Creates 3 sample customers with historical meter readings for the last 8 months.

**Sample Customers Created**:
- John Doe (user.a@example.com) - Residential customer
- Jane Smith (user.b@example.com) - Residential customer  
- Bob Johnson (user.c@example.com) - Residential customer

**Default Password**: `password123` for all test users

**Response**: Success message with customer emails created

**Example Usage**:
```bash
# Run the interactive demo script
./scripts/demo-data-seeding.ps1

# Or use curl directly
curl -X POST http://localhost:8080/api/admin/debug/seed-data \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -H "Content-Type: application/json"
```

### 2. Seed Readings for Specific Customer

**Endpoint**: `POST /api/admin/debug/seed-readings/{customerId}`

**Description**: Generates historical meter readings for a specific customer for the last 8 months.

**Parameters**:
- `customerId` (path parameter) - The ID of the customer

**Example Usage**:
```bash
curl -X POST http://localhost:8080/api/admin/debug/seed-readings/1 \
  -H "Authorization: Bearer <admin-jwt-token>" \
  -H "Content-Type: application/json"
```

### 3. Get Test Customers Information

**Endpoint**: `GET /api/admin/debug/test-customers`

**Description**: Lists all test customers and their basic information including meter numbers and reading counts.

**Example Usage**:
```bash
curl -X GET http://localhost:8080/api/admin/debug/test-customers \
  -H "Authorization: Bearer <admin-jwt-token>"
```

### 4. Clear Test Data

**Endpoint**: `DELETE /api/admin/debug/clear-test-data`

**Description**: Removes all test customers (those with emails containing "example.com").

**Example Usage**:
```bash
curl -X DELETE http://localhost:8080/api/admin/debug/clear-test-data \
  -H "Authorization: Bearer <admin-jwt-token>"
```

## Generated Data Details

### Customers
- Each customer gets a unique meter automatically
- Meters have numbers in format: `TEST######` (6 random digits)
- All customers are set as RESIDENTIAL type
- All customers have ROLE_USER by default

### Meter Readings
- Historical readings for 8 months (starting 8 months ago)
- Monthly consumption between 150-500 units
- Starting meter reading between 10,000-15,000 units
- All readings are marked as MANUAL type
- Previous readings are calculated automatically

## Use Cases

1. **Initial Development**: Use `/seed-data` to quickly populate your database with test customers and readings
2. **Testing Specific Features**: Use `/seed-readings/{customerId}` to add more data for a specific customer
3. **Clean Up**: Use `/clear-test-data` to remove test data without affecting real customers

## Security Notes

- All endpoints require ADMIN role
- Only creates test data (identifiable by email domain)
- Safe to run multiple times (checks for existing data)
- Test customers use weak passwords - not suitable for production

## Integration with Other Features

After seeding data, you can:
- Generate bills for the test customers
- Test the automated billing system
- Verify meter reading workflows
- Test customer management features
