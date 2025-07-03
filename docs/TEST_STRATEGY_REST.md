# Test Strategy for REST API Resources

## Overview

This document outlines the testing strategy for REST API endpoints in the Quarkus application, establishing comprehensive guidelines for validating API contracts, business logic, and error handling scenarios.

## Testing Architecture Layers

### 1. REST API Contract Testing
**Purpose**: Validate HTTP endpoints, status codes, and JSON response structures.

**Scope**:
- HTTP method routing
- Status code validation  
- Request/response JSON schema
- Content-Type headers
- Path parameter handling

**Implementation**: REST Assured with `@QuarkusTest`

### 2. Business Logic Validation
**Purpose**: Ensure service layer business rules and entity validation work correctly.

**Scope**:
- Entity validation annotations
- Service layer business rules
- Repository operations
- Transaction handling
- Database constraints

**Implementation**: JUnit 5 with `@TestTransaction`

### 3. Error Handling Testing
**Purpose**: Validate proper error responses and exception handling.

**Scope**:
- Validation error responses
- Not found scenarios
- Conflict detection
- Malformed request handling
- Exception mapper behavior

**Implementation**: REST Assured response validation

## Testing Framework Configuration

### Required Dependencies
```xml
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

### Essential Annotations
```java
@QuarkusTest                    // Starts Quarkus for integration testing
@TestTransaction                // Database rollback for test isolation
@DisplayName("Description")     // Human-readable test descriptions
```

## Testing Guidelines

### When to Test REST Endpoints

✅ **Always test**:
- All CRUD operations (GET, POST, PUT, DELETE)
- Entity validation rules via API
- Business logic through endpoint calls
- Error response structures
- Status code compliance

✅ **Required for each endpoint**:
- Happy path scenarios
- Validation error scenarios
- Not found scenarios
- Duplicate detection (if applicable)
- Malformed request handling

❌ **Avoid testing**:
- Pure unit logic already covered in service tests
- Database connection details
- Framework internals

### REST API Test Structure Template

```java
@QuarkusTest
@DisplayName("EntityName Resource API Tests")
class EntityNameResourceTest {
    
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    
    @BeforeEach
    void setUp() {
        requestSpec = given()
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON);
            
        responseSpec = expect()
            .contentType(ContentType.JSON);
    }
    
    // CRUD Happy Path Tests
    @Test @TestTransaction
    @DisplayName("GET /entities should return empty list initially")
    void testGetEntities_EmptyList() { }
    
    @Test @TestTransaction
    @DisplayName("POST /entities should create new entity with valid data")
    void testCreateEntity_ValidData() { }
    
    @Test @TestTransaction
    @DisplayName("GET /entities/{id} should return entity by ID")
    void testGetEntityById_Found() { }
    
    @Test @TestTransaction
    @DisplayName("PUT /entities/{id} should update existing entity")
    void testUpdateEntity_ValidData() { }
    
    @Test @TestTransaction
    @DisplayName("DELETE /entities/{id} should delete existing entity")
    void testDeleteEntity_Found() { }
    
    // Validation Tests
    @Test @TestTransaction
    @DisplayName("POST /entities should return 400 for missing required fields")
    void testCreateEntity_MissingRequiredFields() { }
    
    @Test @TestTransaction
    @DisplayName("POST /entities should return 409 for duplicate code")
    void testCreateEntity_DuplicateCode() { }
    
    // Error Handling Tests
    @Test @TestTransaction
    @DisplayName("GET /entities/{id} should return 404 for non-existent ID")
    void testGetEntityById_NotFound() { }
}
```

## Current Implementation Patterns

### GenderResourceTest Pattern (Reference Implementation)

#### Request/Response Specifications
```java
private RequestSpecification requestSpec = given()
    .contentType(ContentType.JSON)
    .accept(ContentType.JSON);

private ResponseSpecification responseSpec = expect()
    .contentType(ContentType.JSON);
```

#### CRUD Operation Testing
```java
@Test @TestTransaction
@DisplayName("POST /genders should create gender with valid data")
void testCreateGender_ValidData() {
    String requestBody = """
        {
            "code": "T",
            "description": "Test Gender"
        }
        """;
    
    requestSpec
        .body(requestBody)
    .when()
        .post("/genders")
    .then()
        .spec(responseSpec)
        .statusCode(201)
        .body("id", notNullValue())
        .body("code", equalTo("T"))
        .body("description", equalTo("Test Gender"))
        .body("createdAt", notNullValue())
        .body("updatedAt", notNullValue());
}
```

#### Validation Error Testing
```java
@Test @TestTransaction
@DisplayName("POST /genders should return 409 for duplicate code")
void testCreateGender_DuplicateCode() {
    // Given: existing gender
    requestSpec.body("""{"code": "X", "description": "Existing"}""")
        .post("/genders");
    
    // When: attempt duplicate
    String duplicateRequest = """{"code": "X", "description": "Duplicate"}""";
    
    // Then: conflict response
    requestSpec
        .body(duplicateRequest)
    .when()
        .post("/genders")
    .then()
        .statusCode(409)
        .body("message", containsString("already exists"));
}
```

## Test Coverage Requirements

### Mandatory Test Categories

| Category | Test Count | Examples |
|----------|------------|----------|
| **CRUD Happy Path** | 7 tests | GET empty, POST create, GET by ID, PUT update, DELETE, GET populated |
| **Validation Errors** | 5+ tests | Required fields, format validation, business rules, duplicates |
| **Error Handling** | 4+ tests | 404 not found, 409 conflicts, 400 bad requests |
| **Edge Cases** | 2+ tests | Boundary values, special characters |

### JSON Response Validation Pattern

```java
.then()
    .spec(responseSpec)
    .statusCode(expectedStatus)
    .body("id", notNullValue())
    .body("code", equalTo(expectedCode))
    .body("description", equalTo(expectedDescription))
    .body("createdAt", notNullValue())
    .body("updatedAt", notNullValue());
```

### Error Response Validation Pattern

```java
.then()
    .statusCode(400)
    .body("message", notNullValue())
    .body("violations", hasSize(greaterThan(0)))
    .body("violations[0].field", equalTo("fieldName"))
    .body("violations[0].message", containsString("validation message"));
```

## Testing Decision Matrix

| Validation Type | REST API Testing | Unit Testing | Rationale |
|-----------------|------------------|--------------|-----------|
| HTTP Status Codes | ✅ Required | ❌ Not applicable | API contract compliance |
| JSON Response Structure | ✅ Required | ❌ Not applicable | API contract validation |
| Entity Validation | ✅ Via API | ✅ Direct testing | Both integration and unit coverage |
| Business Rules | ✅ Via API | ✅ Service testing | Integration validation essential |
| Database Operations | ✅ Via API | ✅ Repository testing | Transaction behavior validation |
| Exception Mapping | ✅ Required | ❌ Framework testing | Error response structure |

## Implementation Guidelines

### For New Resource Tests

1. **Start with CRUD happy path** - ensure basic functionality works
2. **Add validation scenarios** - test all entity validation rules
3. **Include error handling** - test all expected error scenarios  
4. **Use unique test data** - avoid conflicts with existing data
5. **Validate JSON structure** - ensure API contract compliance
6. **Test audit fields** - verify createdAt/updatedAt population

### Test Data Management

```java
// Use unique identifiers to avoid conflicts
private String getUniqueCode() {
    return "T" + System.currentTimeMillis() % 10000;
}

// Create test entities with conflict avoidance
private String createUniqueGender() {
    return """
        {
            "code": "%s",
            "description": "Test Gender %d"
        }
        """.formatted(getUniqueCode(), System.currentTimeMillis());
}
```

### Maintenance Guidelines

1. **Update tests when API contracts change**
2. **Add validation tests for new business rules**
3. **Maintain consistent error response structures**
4. **Keep test data generation conflict-free**
5. **Use GenderResourceTest as the reference pattern**

## Benefits of This Strategy

- **API Contract Validation**: Ensures endpoints behave correctly
- **Business Logic Testing**: Validates service layer through API calls
- **Error Handling Coverage**: Tests exception mapping and error responses
- **Integration Testing**: Validates full request/response cycle
- **Maintainable Tests**: Clear patterns and reusable components
- **Fast Execution**: `@TestTransaction` provides quick rollback
