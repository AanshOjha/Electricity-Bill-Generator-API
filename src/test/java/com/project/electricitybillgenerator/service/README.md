# BillCalculationService Unit Tests

This project contains comprehensive unit tests for the `BillCalculationService` class, covering all edge cases including:

- First-ever reading for a user (previous reading is 0)
- Current reading is less than previous reading
- Zero consumption scenarios
- Input validation and error handling
- Integration scenarios

## Test Files

### 1. BillCalculationServiceTest.java (Recommended)
This is the main test file that uses manual mocks to work around Java 24 compatibility issues with Mockito's Byte Buddy library.

**Features:**
- ✅ Compatible with Java 24
- ✅ All 24 comprehensive test cases
- ✅ Manual mocks for `ReadingService` and `BillConfiguration`
- ✅ Tests all edge cases as requested

### 2. BillCalculationServiceMockitoTest.java (Alternative)
This is an alternative version using Mockito annotations for environments that support it.

**Requirements:**
- Java 23 or lower, OR
- Java 24+ with `-Dnet.bytebuddy.experimental=true` VM argument

**Features:**
- ✅ Uses Mockito annotations (@Mock, @ExtendWith)
- ✅ Cleaner syntax with `when()` and `verify()` 
- ❌ Requires Java 24 compatibility flag or lower Java version

## Test Coverage

### Edge Cases Covered

1. **First-ever reading for user (previous reading is 0)**
   ```java
   @Test
   void shouldHandleFirstEverReadingForUser() {
       // Tests when previous reading = 0.0
       // Ensures units consumed = current reading
       // Verifies correct bill calculation
   }
   ```

2. **Current reading less than previous reading**
   ```java
   @Test
   void shouldHandleCurrentReadingLessThanPrevious() {
       // Tests meter reset or tampering scenarios
       // Ensures units consumed = 0 (not negative)
       // Verifies bill amount = 0
   }
   ```

3. **Zero consumption**
   ```java
   @Test
   void shouldHandleZeroConsumption() {
       // Tests when current = previous reading
       // Ensures units consumed = 0
       // Verifies bill amount = 0
   }
   ```

### Additional Test Categories

- **Input Validation**: Null checks for readings, dates, and meter IDs
- **Calculation Logic**: Bill amount and units consumed calculations
- **Error Handling**: Negative readings and invalid inputs
- **Integration Scenarios**: Complete workflow testing

## Running the Tests

### Using Maven
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=BillCalculationServiceTest

# Run with Java 24 Mockito support (if needed)
mvn test -Dnet.bytebuddy.experimental=true
```

### Using IDE
- Right-click on the test class and select "Run Tests"
- For Java 24 with Mockito, add `-Dnet.bytebuddy.experimental=true` to VM options

## Test Structure

The tests are organized using JUnit 5's nested test classes:

```
BillCalculationServiceTest
├── ProcessBillReadingTests (9 tests)
│   ├── Normal processing
│   ├── Edge cases (first reading, negative consumption, zero consumption)
│   └── Input validation
├── CalculateBillAmountTests (5 tests)
│   ├── Rate calculations
│   └── Error handling
├── CalculateUnitsConsumedTests (7 tests)
│   ├── Normal calculations
│   ├── Edge cases
│   └── Validation
└── IntegrationTests (3 tests)
    └── End-to-end scenarios
```

## Dependencies

The tests use:
- **JUnit 5**: Test framework
- **AssertJ**: Fluent assertions
- **Mockito** (optional): For `BillCalculationServiceMockitoTest.java`

All dependencies are included in Spring Boot's `spring-boot-starter-test`.

## Notes

- The manual mock approach in `BillCalculationServiceTest.java` provides the same functionality as Mockito but with Java 24 compatibility
- Both test files cover identical scenarios and can be used interchangeably
- Choose the version that best fits your Java version and preference for mocking frameworks
