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
- **Core Domain Entities**: Complete CRUD operations with REST API and HTML UI
  - **Gender Entity**: Single character code (M/F/X) with description
  - **Title Entity**: Personal titles (MR, MS, DR, etc.) with validation
  - **Person Entity**: Complete person management with relationships to Gender and Title entities
- **User Management System**: Complete authentication and authorization infrastructure
  - **User Entity**: User accounts with username/password and person association
  - **Role Entity**: Role-based access control with uppercase naming convention
  - **Many-to-Many Relationship**: User-Role association via join table
- **Database Setup**: PostgreSQL with Flyway migrations
- **Template System**: Qute templates with HTMX integration
- **Development Environment**: Hot reload, DevUI, Swagger documentation
- **Clean Architecture**: Proper separation of concerns across all layers
- **Security Infrastructure**: Authentication resources and services implemented

### 🚧 In Progress / Planned
- **Authentication Flow**: Login/logout implementation and session management
- **Authorization Rules**: Role-based access control enforcement
- **API Enhancement**: Rate limiting, API versioning, GraphQL support
- **Monitoring and Observability**: Metrics collection, distributed tracing, health checks enhancement
- **Scalability Improvements**: Caching layer (Redis), message queuing, microservices decomposition

## HTMX Documentation
- HTMX documentation is here: https://htmx.org/docs/
