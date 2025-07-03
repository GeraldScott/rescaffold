# TEST_STRATEGY

This document outlines the comprehensive testing approach for REST API endpoints in the Quarkus application, using the `GenderResourceTest` as the reference implementation.

## Overview

The resource test strategy provides comprehensive coverage for REST API endpoints, ensuring both API contract compliance and business logic correctness. Tests are designed to be maintainable, reliable, and fast-executing while following Quarkus and REST Assured best practices.

### Key Principles

1. **Comprehensive Coverage**: Test all CRUD operations, validation rules, error scenarios, and edge cases
2. **Test Isolation**: Each test should be independent and not affect others
3. **Clear Intent**: Tests should be self-documenting with descriptive names and comments
4. **Maintainability**: Use reusable components and follow DRY principles
5. **Performance**: Optimize for fast execution while maintaining thorough coverage
6. **Business Logic**: REST API endpoints must test the business logic in the service classes, as well as Hibernate validation logic in the entity classes 

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

## Template for New Resource Tests

### 1. Test Method Naming Convention

**Pattern**: `test{Operation}{Entity}_{Scenario}`

**Examples**:
- `testCreateGender_ValidData()`
- `testGetGenderById_NotFound()`
- `testUpdateGender_DuplicateCode()`
- `testDeleteGender_ValidId()`

### 2. Required Test Coverage

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

### 3. Checklist for New Tests

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
