# CLAUDE.md

## Development Commands

### Application settings
The application settings are in `src/main/resources/application.properties`

### Package name
All Java code is under the root package `io.archton.scaffold`

### Start Development Server
```bash
quarkus dev
```
- Browse to http://localhost:8080/
- DevUI at http://localhost:8080/q/dev/
- Swagger UI at http://localhost:8080/q/swagger-ui/

### Build and Test
```bash
quarkus build --clean
```

### Run Tests
```bash
./mvnw test
```

### Run Single Test
```bash
./mvnw test -Dtest=TestClassName
```

### Format Code
```bash
./mvnw spotless:apply
```
(Note: Only if Spotless plugin is configured)

### Integration Tests
```bash
./mvnw verify
```

### E2E Tests (Selenide)
```bash
# Run all e2e tests (requires dev server running on localhost:8080)
./mvnw test -Dtest="**/e2e/**/*Test"

# Run specific e2e test class
./mvnw test -Dtest=HomePageNavigationTest

# Run e2e tests with browser visible (non-headless)
./mvnw test -Dtest="**/e2e/**/*Test" -Dselenide.headless=false

# Run all tests (unit + e2e)
./mvnw test

# If no e2e tests match pattern, ignore the error
./mvnw test -Dtest="**/e2e/**/*Test" -Dsurefire.failIfNoSpecifiedTests=false
```
**Note**: E2E tests require the dev server to be running on http://localhost:8080

### Native Build
```bash
quarkus build --native --clean
```

### Upgrade Quarkus
```bash
quarkus upgrade
quarkus dev --clean
```

## Architecture

For detailed architecture documentation, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Current Implementation Status

### ✅ Completed Features
- **Repository Pattern Architecture**: Full migration from Active Record to Repository pattern
- **Gender Entity**: Complete CRUD operations with REST API and HTML UI
- **Title Entity**: Complete CRUD operations with REST API and HTML UI
- **IdType Entity**: Complete CRUD operations with REST API and HTML UI
- **Person Entity**: Complete CRUD operations with REST API and HTML UI
- **Database Setup**: PostgreSQL with Flyway migrations
- **Template System**: Qute templates with HTMX integration
- **Development Environment**: Hot reload, DevUI, Swagger documentation
- **Clean Architecture**: Proper separation of concerns across all layers

### 🚧 In Progress / Planned
- **User Management**: User, Role, UserRole entities and authentication
- **Security**: Authentication and authorization implementation
- **Additional Features**: As defined in entity relationship diagram

### 🏗️ Architecture Benefits Achieved
- **Repository Pattern**: Clean separation between entities and data access
- **Plain JPA Entities**: Focused solely on data structure and validation
- **Explicit Transaction Management**: Manual timestamp updates in service layer
- **Better Testability**: Repository interfaces can be mocked for unit testing
- **Dual API Pattern**: REST + HTML UI established for all entities
- **Audit Trail**: Consistent audit fields across all entities
- **Exception Handling**: Proper HTTP status codes and error responses
- **Form Validation**: Jakarta Bean Validation with custom business rules
- **Bootstrap Responsive Design**: Mobile-first UI implementation
- **HTMX Dynamic Interactions**: Seamless partial page updates

For detailed architecture information, see [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Test Strategy
- **Resource Testing**: Comprehensive strategy documented in `docs/resource-test-strategy.md`
- **Reference Implementation**: Follow the `GenderResourceTest` pattern for all new REST resource tests
- **Framework**: Use @QuarkusTest with @TestTransaction for database rollback and REST Assured for API testing
- **Test Coverage**: CRUD operations, validation rules, error handling, and data processing scenarios
- **Data Isolation**: Use unique test data per test method to prevent conflicts
- **Documentation**: https://quarkus.io/guides/getting-started-testing
- **Database**: PostgreSQL as defined in %test.quarkus.datasource with credentials from .env file
- **E2E Testing**: Use Selenide for end-to-end browser testing
- **UI Testing**: Cover critical user journeys across different browsers and screen sizes
 
## HTMX Documentation
- HTMX documentation is here: https://htmx.org/docs/

