# Resource Test Strategy

This document outlines the comprehensive testing approach for REST API endpoints in the Quarkus application, using the `GenderResourceTest` as the reference implementation.

## Table of Contents

- [Overview](#overview)
- [Testing Framework Stack](#testing-framework-stack)
- [Test Class Structure](#test-class-structure)
- [REST Assured Best Practices](#rest-assured-best-practices)
- [Test Categories](#test-categories)
- [Data Management Strategy](#data-management-strategy)
- [Common Issues and Solutions](#common-issues-and-solutions)
- [Template for New Resource Tests](#template-for-new-resource-tests)

## Overview

The resource test strategy provides comprehensive coverage for REST API endpoints, ensuring both API contract compliance and business logic correctness. Tests are designed to be maintainable, reliable, and fast-executing while following Quarkus and REST Assured best practices.

### Key Principles

1. **Comprehensive Coverage**: Test all CRUD operations, validation rules, error scenarios, and edge cases
2. **Test Isolation**: Each test should be independent and not affect others
3. **Clear Intent**: Tests should be self-documenting with descriptive names and comments
4. **Maintainability**: Use reusable components and follow DRY principles
5. **Performance**: Optimize for fast execution while maintaining thorough coverage

## Testing Framework Stack

```xml
<!-- Core Testing Dependencies -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-junit5</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```

### Key Annotations

- `@QuarkusTest`: Starts Quarkus application for integration testing
- `@TestTransaction`: Provides database rollback for test isolation
- `@DisplayName`: Provides human-readable test descriptions

## Test Class Structure

### Basic Template

```java
@QuarkusTest
@DisplayName("Entity Resource REST API Tests")
class EntityResourceTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    void setUp() {
        // Common request specification
        requestSpec = new RequestSpecBuilder()
                .setBasePath("/api/entities")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        // Common response specification
        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    // Helper methods for test data creation
    // Test methods organized by category
}
```

## REST Assured Best Practices

### 1. RequestSpecification & ResponseSpecification

**Purpose**: Eliminate code duplication and ensure consistent request/response handling

```java
// Reusable request configuration
RequestSpecification requestSpec = new RequestSpecBuilder()
    .setBasePath("/api/genders")
    .setContentType(ContentType.JSON)
    .setAccept(ContentType.JSON)
    .build();

// Reusable response validation
ResponseSpecification responseSpec = new ResponseSpecBuilder()
    .expectContentType(ContentType.JSON)
    .build();
```

### 2. Given-When-Then Pattern

**Structure**: Organize tests for maximum readability

```java
@Test
@TestTransaction
@DisplayName("POST /api/genders - Should create gender successfully")
void testCreateGender_ValidData() {
    Gender gender = createValidGender("M", "Male");

    given()
            .spec(requestSpec)
            .body(gender)
    .when()
            .post()
    .then()
            .spec(responseSpec)
            .statusCode(201)
            .body("id", notNullValue())
            .body("code", equalTo("M"))
            .body("description", equalTo("Male"));
}
```

### 3. Static Imports for Clean Syntax

```java
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
```

### 4. JSON Path Validation

**Comprehensive Response Validation**:

```java
.body("$", hasSize(3))                          // Array size
.body("code", hasItems("A", "B", "C"))          // Array contains items
.body("id", notNullValue())                     // Field presence
.body("isActive", equalTo(true))                // Field value
.body("createdAt", notNullValue())              // Audit fields
.body("error", containsString("already exists")) // Error messages
```

### 5. Type Handling

**Important**: REST Assured returns Integer for ID fields, not Long

```java
// Correct - extract as Integer
Integer createdId = given()...extract().path("id");

// Incorrect - ClassCastException
Long createdId = given()...extract().path("id");
```

## Test Categories

### 1. Happy Path CRUD Operations

**Coverage**: All successful operations

```java
- GET /api/entities - Empty list
- POST /api/entities - Create valid entity
- GET /api/entities/{id} - Retrieve by ID
- GET /api/entities/code/{code} - Retrieve by unique field
- PUT /api/entities/{id} - Update existing
- DELETE /api/entities/{id} - Delete existing
- GET /api/entities - Populated list
```

### 2. Validation Tests

**Coverage**: Bean Validation and business rules

```java
- Required field validation (@NotNull, @NotBlank)
- Format validation (@Pattern, @Size)
- Business logic validation (duplicate checks)
- Invalid ID inclusion in POST requests
```

### 3. Error Handling Tests

**Coverage**: All error scenarios with proper HTTP status codes

```java
- 400 Bad Request: Validation failures, malformed requests
- 404 Not Found: Non-existent resources
- 409 Conflict: Business rule violations (duplicates)
- 500 Internal Server Error: Unexpected errors
```

### 4. Data Processing Tests

**Coverage**: Service layer behavior

```java
- Data normalization (uppercase conversion, trimming)
- Audit field population
- Response structure validation
```

## Data Management Strategy

### 1. Test Data Isolation

**Challenge**: `@TestTransaction` rollback may not provide complete isolation

**Solution**: Use unique identifiers for each test

```java
// Bad - conflicts between tests
Gender first = createValidGender("A", "First");
Gender second = createValidGender("B", "Second");

// Good - unique per test
Gender first = createValidGender("Q", "Duplicate Code Test First");
Gender second = createValidGender("R", "Duplicate Code Test Second");
```

### 2. Helper Methods

**Consistent Test Data Creation**:

```java
private Entity createValidEntity(String code, String description) {
    Entity entity = new Entity();
    entity.code = code;
    entity.description = description;
    return entity;
}

private Entity createInvalidEntityWithMultiCharCode() {
    Entity entity = new Entity();
    entity.code = "AB"; // Violates single character constraint
    entity.description = "Invalid Multi-Char";
    return entity;
}
```

### 3. Constraint-Aware Test Data

**Important**: Understand validation order

1. **Bean Validation** occurs first (in JAX-RS layer)
2. **Service Normalization** occurs second (in service layer)

```java
// This will fail Bean Validation before normalization
Gender gender = new Gender();
gender.code = "m"; // @Pattern expects uppercase
gender.description = "Lowercase Test";

// Bean Validation rejects before service can normalize to "M"
```

## Common Issues and Solutions

### 1. Nested Class CDI Issues

**Problem**: `@TestTransaction` doesn't work with nested test classes

```java
// Bad - Causes CDI interceptor binding errors
@Nested
class ValidationTests {
    @Test
    @TestTransaction
    void testValidation() { ... }
}
```

**Solution**: Use flat test structure with descriptive naming

```java
// Good - Works properly
@Test
@TestTransaction
@DisplayName("POST /api/genders - Should reject null code")
void testCreateGender_NullCode() { ... }
```

### 2. Type Casting Issues

**Problem**: REST Assured returns Integer for numeric fields

```java
// Bad - ClassCastException
Long id = given()...extract().path("id");
```

**Solution**: Extract as Integer, use in assertions correctly

```java
// Good - Correct type handling
Integer createdId = given()...extract().path("id");
.body("id", equalTo(createdId))  // Not createdId.intValue()
```

### 3. Test Isolation Problems

**Problem**: Tests seeing data from other tests

**Solution**: Use unique test data and robust assertions

```java
// Instead of exact size matching
.body("$", hasSize(3))

// Use minimum size with specific item validation
.body("$", hasSize(greaterThanOrEqualTo(3)))
.body("code", hasItems("J", "K", "L"))
```

## Template for New Resource Tests

### 1. Create Test Class

```java
@QuarkusTest
@DisplayName("EntityName Resource REST API Tests")
class EntityNameResourceTest {

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;

    @BeforeEach
    void setUp() {
        requestSpec = new RequestSpecBuilder()
                .setBasePath("/api/entitynames")
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();

        responseSpec = new ResponseSpecBuilder()
                .expectContentType(ContentType.JSON)
                .build();
    }

    // Helper methods
    private EntityName createValidEntity(String field1, String field2) {
        EntityName entity = new EntityName();
        entity.field1 = field1;
        entity.field2 = field2;
        return entity;
    }

    // Happy path tests
    @Test
    @TestTransaction
    @DisplayName("GET /api/entitynames - Should return empty list initially")
    void testGetAllEntities_EmptyList() { ... }

    // Validation tests
    @Test
    @TestTransaction
    @DisplayName("POST /api/entitynames - Should reject null required field")
    void testCreateEntity_NullField() { ... }

    // Error handling tests
    @Test
    @TestTransaction
    @DisplayName("GET /api/entitynames/{id} - Should return 404 for non-existent ID")
    void testGetEntityById_NotFound() { ... }
}
```

### 2. Test Method Naming Convention

**Pattern**: `test{Operation}{Entity}_{Scenario}`

**Examples**:
- `testCreateGender_ValidData()`
- `testGetGenderById_NotFound()`
- `testUpdateGender_DuplicateCode()`
- `testDeleteGender_ValidId()`

### 3. Required Test Coverage

**Minimum Tests Per Resource**:

1. **CRUD Happy Path** (7 tests):
   - GET empty list
   - POST create valid
   - GET by ID
   - GET by unique field (if applicable)
   - PUT update
   - DELETE
   - GET populated list

2. **Validation** (5+ tests):
   - Required field validation
   - Format validation
   - Business rule validation
   - Duplicate prevention

3. **Error Handling** (4+ tests):
   - 404 scenarios
   - 409 conflicts
   - 400 bad requests

4. **Data Processing** (1+ tests):
   - Service behavior validation

### 4. Checklist for New Tests

- [ ] Uses `@QuarkusTest` and `@TestTransaction`
- [ ] Implements RequestSpec/ResponseSpec pattern
- [ ] Follows Given-When-Then structure
- [ ] Uses descriptive `@DisplayName` annotations
- [ ] Includes comprehensive JSON path validation
- [ ] Tests all HTTP status codes relevant to endpoint
- [ ] Uses unique test data to avoid conflicts
- [ ] Validates both success and error response structures
- [ ] Tests audit field population
- [ ] Covers all business validation rules

## Integration with CLAUDE.md

Add this reference to the main CLAUDE.md file:

```markdown
### Testing Strategy
- Resource testing strategy documented in `docs/resource-test-strategy.md`
- Follow the GenderResourceTest pattern for all new REST resource tests
- Use @TestTransaction for database rollback and unique test data for isolation
- Comprehensive coverage includes CRUD, validation, error handling, and data processing tests
```

## Conclusion

This testing strategy ensures robust, maintainable, and comprehensive coverage for REST API endpoints. By following these patterns and practices, new resource tests will be consistent, reliable, and provide confidence in API behavior across all scenarios.