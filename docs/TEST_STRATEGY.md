# Test Strategy

This document outlines the comprehensive testing strategy for the Rescaffold application, covering both REST API testing and CRUD template testing approaches to ensure robust validation coverage while avoiding redundancy.

## Overview

The Rescaffold application implements a multi-layered testing strategy that validates different aspects of the application at appropriate levels:
- **REST API Testing**: Contract validation, business logic, and error handling
- **Template Testing**: User interface workflows and server-side validation integration
- **Client-Side Validation**: HTML5 validation for immediate user feedback

## REST API Testing Strategy

### Overview

This section outlines the testing strategy for REST API endpoints in the Quarkus application, establishing comprehensive guidelines for validating API contracts, business logic, and error handling scenarios.

### Testing Architecture Layers

#### 1. REST API Contract Testing
**Purpose**: Validate HTTP endpoints, status codes, and JSON response structures.

**Scope**:
- HTTP method routing
- Status code validation  
- Request/response JSON schema
- Content-Type headers
- Path parameter handling

**Implementation**: REST Assured with `@QuarkusTest`

#### 2. Business Logic Validation
**Purpose**: Ensure service layer business rules and entity validation work correctly.

**Scope**:
- Entity validation annotations
- Service layer business rules
- Repository operations
- Transaction handling
- Database constraints

**Implementation**: JUnit 5 with `@TestTransaction`

#### 3. Error Handling Testing
**Purpose**: Validate proper error responses and exception handling.

**Scope**:
- Validation error responses
- Not found scenarios
- Conflict detection
- Malformed request handling
- Exception mapper behavior

**Implementation**: REST Assured response validation

### Testing Framework Configuration

#### Required Dependencies
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

#### Essential Annotations
```java
@QuarkusTest                    // Starts Quarkus for integration testing
@TestTransaction                // Database rollback for test isolation
@DisplayName("Description")     // Human-readable test descriptions
```

### REST API Testing Guidelines

#### When to Test REST Endpoints

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

#### REST API Test Structure Template

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

### Current Implementation Patterns

#### GenderResourceTest Pattern (Reference Implementation)

**Request/Response Specifications**:
```java
private RequestSpecification requestSpec = given()
    .contentType(ContentType.JSON)
    .accept(ContentType.JSON);

private ResponseSpecification responseSpec = expect()
    .contentType(ContentType.JSON);
```

**CRUD Operation Testing**:
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

**Validation Error Testing**:
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

### REST API Test Coverage Requirements

#### Mandatory Test Categories

| Category | Test Count | Examples |
|----------|------------|----------|
| **CRUD Happy Path** | 7 tests | GET empty, POST create, GET by ID, PUT update, DELETE, GET populated |
| **Validation Errors** | 5+ tests | Required fields, format validation, business rules, duplicates |
| **Error Handling** | 4+ tests | 404 not found, 409 conflicts, 400 bad requests |
| **Edge Cases** | 2+ tests | Boundary values, special characters |

#### JSON Response Validation Pattern

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

#### Error Response Validation Pattern

```java
.then()
    .statusCode(400)
    .body("message", notNullValue())
    .body("violations", hasSize(greaterThan(0)))
    .body("violations[0].field", equalTo("fieldName"))
    .body("violations[0].message", containsString("validation message"));
```

### Testing Decision Matrix

| Validation Type | REST API Testing | Unit Testing | Rationale |
|-----------------|------------------|--------------|-----------|
| HTTP Status Codes | ✅ Required | ❌ Not applicable | API contract compliance |
| JSON Response Structure | ✅ Required | ❌ Not applicable | API contract validation |
| Entity Validation | ✅ Via API | ✅ Direct testing | Both integration and unit coverage |
| Business Rules | ✅ Via API | ✅ Service testing | Integration validation essential |
| Database Operations | ✅ Via API | ✅ Repository testing | Transaction behavior validation |
| Exception Mapping | ✅ Required | ❌ Framework testing | Error response structure |

### REST API Implementation Guidelines

#### For New Resource Tests

1. **Start with CRUD happy path** - ensure basic functionality works
2. **Add validation scenarios** - test all entity validation rules
3. **Include error handling** - test all expected error scenarios  
4. **Use unique test data** - avoid conflicts with existing data
5. **Validate JSON structure** - ensure API contract compliance
6. **Test audit fields** - verify createdAt/updatedAt population

#### Test Data Management

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

## CRUD Template Testing Strategy

### Overview

This section outlines the testing strategy for CRUD templates in the Quarkus application, establishing clear guidelines for when to use client-side HTML validation versus E2E testing to avoid redundancy while ensuring comprehensive coverage.

### Validation Layers Architecture

#### 1. Client-Side HTML Validation (Templates)
**Purpose**: Immediate user feedback and basic input validation before server submission.

**Scope**: 
- Required field validation (`required`)
- Length constraints (`minlength`, `maxlength`)
- Pattern matching (`pattern`)
- Input type validation (`type="email"`, `type="number"`)
- Custom validation messages (`title`)

**Implementation**: HTML5 attributes in Qute templates

#### 2. Server-Side Validation (Business Logic)
**Purpose**: Data integrity, business rules, and security validation.

**Scope**:
- Duplicate detection
- Cross-field validation
- Database constraints
- Business rule enforcement
- Security validations

**Implementation**: Bean Validation annotations and custom validators

#### 3. E2E Testing (Complete Workflows)
**Purpose**: End-to-end workflow validation and UI interaction testing.

**Scope**:
- Complete CRUD workflows
- UI component interactions
- Server-side business logic validation
- Error handling flows
- Integration testing

**Implementation**: Selenide-based E2E tests

### Template Testing Guidelines

#### When Client-Side HTML Validation is Sufficient

✅ **Use HTML validation for**:
- Required field validation
- Basic format validation (email, numeric)
- Length constraints (min/max length)
- Pattern matching (regex validation)
- Input type constraints

❌ **Do NOT create E2E tests for**:
- Simple required field validation already covered by `required` attribute
- Length validation already covered by `minlength`/`maxlength`
- Format validation already covered by `pattern` or `type` attributes

#### When E2E Testing is Required

✅ **Create E2E tests for**:
- Server-side business logic validation (e.g., duplicate detection)
- Complex validation requiring server interaction
- Complete CRUD workflows
- UI interaction flows
- Error handling scenarios
- Integration between multiple components

✅ **Always test**:
- Create, Read, Update, Delete operations
- Form submission and response handling
- Navigation flows
- Error message display
- Confirmation dialogs

#### E2E Test Coverage

**Comprehensive Testing (GenderCrudTest)**:
✅ **Server-side validation testing**:
- Duplicate code validation (create & update)
- Duplicate description validation (create & update)
- Successful CRUD operations with conflict avoidance

✅ **UI workflow testing**:
- Page load and table display
- Button visibility and interactions
- Form opening and navigation
- Delete confirmation workflow

**Basic Testing (Other CRUD Tests)**:
✅ **UI interaction testing**:
- Page load verification
- Button visibility checks
- Form navigation
- Delete confirmation

❌ **Missing coverage** (recommended additions):
- Server-side duplicate validation testing
- Complete CRUD operation workflows
- Business logic validation scenarios

### Recommended Test Patterns

#### Template Structure for E2E Tests

```java
@DisplayName("EntityName CRUD E2E Tests")
class EntityNameCrudTest extends BaseSelenideTest {
    
    // Basic UI Tests (Always Include)
    @Test void shouldDisplayTableWhenPageLoads()
    @Test void shouldDisplayCreateButton()
    @Test void shouldDisplayActionButtonsForEachTableRow()
    @Test void shouldOpenCreateScreenWhenCreateButtonIsClicked()
    @Test void shouldOpenViewScreenWhenViewButtonIsClicked()
    @Test void shouldOpenEditScreenWhenEditButtonIsClicked()
    @Test void shouldRespondWhenDeleteButtonIsClicked()
    
    // CRUD Workflow Tests (Always Include)
    @Test void shouldCreateNewRecordSuccessfully()
    @Test void shouldUpdateRecordSuccessfully()
    @Test void shouldDeleteRecordSuccessfully()
    
    // Server-Side Validation Tests (Include if applicable)
    @Test void shouldShowValidationErrorWhenCreatingWithDuplicateCode()
    @Test void shouldShowValidationErrorWhenCreatingWithDuplicateDescription()
    @Test void shouldShowValidationErrorWhenUpdatingWithDuplicateCode()
    @Test void shouldShowValidationErrorWhenUpdatingWithDuplicateDescription()
}
```

#### Validation Testing Decision Matrix

| Validation Type | HTML Validation | E2E Testing | Rationale |
|-----------------|----------------|-------------|-----------|
| Required Fields | ✅ `required` | ❌ Redundant | HTML provides immediate feedback |
| Length Limits | ✅ `minlength`/`maxlength` | ❌ Redundant | HTML prevents invalid input |
| Pattern Matching | ✅ `pattern` | ❌ Redundant | HTML validates format client-side |
| Email Format | ✅ `type="email"` | ❌ Redundant | HTML validates email format |
| Duplicate Detection | ❌ Not possible | ✅ Required | Requires server-side database check |
| Business Rules | ❌ Not possible | ✅ Required | Complex logic requires server validation |
| Cross-Field Validation | ❌ Limited | ✅ Required | Multiple field dependencies |
| Database Constraints | ❌ Not possible | ✅ Required | Database-level validation |

### Template Implementation Recommendations

#### For New CRUD Templates

1. **Always implement HTML5 validation** for basic input constraints
2. **Include comprehensive `title` attributes** for user-friendly error messages
3. **Use appropriate input types** (`email`, `number`, etc.)
4. **Apply consistent pattern matching** for code fields
5. **Implement Bootstrap validation classes** for visual feedback

#### For New E2E Tests

1. **Start with basic UI interaction tests** (table display, buttons, navigation)
2. **Add complete CRUD workflow tests** (create, update, delete operations)
3. **Include server-side validation tests** only when business logic requires it
4. **Use the GenderCrudTest pattern** as a template for comprehensive testing
5. **Implement intelligent conflict avoidance** in test data generation

## General Testing Guidelines

### Maintenance Guidelines

#### REST API Tests
1. **Update tests when API contracts change**
2. **Add validation tests for new business rules**
3. **Maintain consistent error response structures**
4. **Keep test data generation conflict-free**
5. **Use GenderResourceTest as the reference pattern**

#### Template Tests
1. **Review HTML validation** when adding new fields to templates
2. **Update E2E tests** when business rules change
3. **Avoid duplicating** HTML validation scenarios in E2E tests
4. **Focus E2E tests** on server-side logic and complete workflows
5. **Maintain consistent test patterns** across all CRUD entities

### Benefits of This Strategy

- **Reduced test redundancy**: No duplication between HTML and E2E validation
- **Faster feedback**: HTML validation provides immediate user experience
- **Comprehensive coverage**: E2E tests focus on server-side logic and workflows
- **Maintainable tests**: Clear separation of concerns between validation layers
- **Better user experience**: Client-side validation prevents unnecessary server requests
- **Robust validation**: Server-side validation ensures data integrity and business rules
- **API Contract Validation**: Ensures endpoints behave correctly
- **Business Logic Testing**: Validates service layer through API calls
- **Error Handling Coverage**: Tests exception mapping and error responses
- **Integration Testing**: Validates full request/response cycle

## Conclusion

This comprehensive testing strategy ensures robust validation coverage across all layers of the application while avoiding redundancy. The multi-layered approach provides:

- **REST API Testing**: Validates API contracts, business logic, and error handling
- **Client-Side HTML Validation**: Provides immediate user feedback for basic input constraints
- **E2E Testing**: Focuses on server-side business logic, complete workflows, and integration scenarios

This strategy provides both immediate user feedback and robust data validation without unnecessary test duplication, ensuring high-quality software delivery with efficient testing practices.