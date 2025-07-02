# Architecture Documentation

## Overview

This is a Quarkus + HTMX scaffold application following a layered architecture pattern with clean separation of concerns. The application provides both REST API and HTML UI endpoints for comprehensive entity management.

## Core Technology Stack

- **Quarkus 3.22.3** with Java 17
- **PostgreSQL** database with Flyway migrations
- **Hibernate ORM with Panache Repository Pattern** for data persistence
- **Qute** templating engine for server-side rendering
- **HTMX** for dynamic frontend interactions
- **Quarkus REST** (REST-Easy Reactive) for API endpoints
- **Jakarta Bean Validation** for data validation
- **Bootstrap** for responsive UI styling

## Architecture Pattern

The application follows a layered architecture:

```
┌─────────────────────────────────────────┐
│             Presentation Layer          │
├─────────────────┬───────────────────────┤
│   Web Layer     │    REST API Layer     │
│   (HTML UI)     │    (JSON API)         │
│   /*-ui paths   │    /api/* paths       │
└─────────────────┴───────────────────────┘
┌─────────────────────────────────────────┐
│             Service Layer               │
│        (Business Logic)                 │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│           Repository Layer              │
│        (Data Access)                    │
└─────────────────────────────────────────┘
┌─────────────────────────────────────────┐
│             Domain Layer                │
│        (Entities & Models)              │
└─────────────────────────────────────────┘
```

## Package Structure

```
io.archton.scaffold/
├── domain/          # Plain JPA entity classes with validation annotations
├── repository/      # Data access layer implementing PanacheRepository pattern
├── service/         # Business logic layer with transaction management
├── resource/        # REST API endpoints (/api/* paths)
├── web/            # HTML UI controllers (/*-ui paths) using Qute templates
├── util/           # Utility classes (TemplateGlobals, WebErrorHandler)
├── exception/      # Custom exception classes and global exception mappers
└── health/         # Health check endpoints
```

### Layer Responsibilities

#### Domain Layer (`domain/`)
- **Pure JPA entities** with no inheritance from Panache classes
- Focus solely on data structure with Jakarta Bean Validation annotations
- Audit fields: `created_at`, `updated_at`, `created_by`, `updated_by`
- No business logic or database operations

#### Repository Layer (`repository/`)
- Implements `PanacheRepository<Entity>` pattern
- Handles all database operations and queries
- Provides custom finder methods for complex queries
- Maintains data access abstraction

#### Service Layer (`service/`)
- Contains business logic and validation rules
- Manages transactions with `@Transactional`
- Handles manual timestamp updates for better control
- Coordinates repository operations

#### Presentation Layer (`resource/` and `web/`)
- **REST Resources** (`resource/`): JSON API endpoints at `/api/*`
- **Web Routers** (`web/`): HTML UI endpoints at `/*-ui` with Qute templates
- Form handling with `@FormParam` for HTML forms
- Error handling with appropriate HTTP status codes

## Dual API Pattern

The application implements a dual API approach providing both REST and HTML interfaces:

### REST API (`/api/*`)
- JSON-based API endpoints
- RESTful resource operations (GET, POST, PUT, DELETE)
- OpenAPI/Swagger documentation
- Suitable for frontend frameworks, mobile apps, and API integrations

### HTML UI (`/*-ui`)
- Server-side rendered HTML using Qute templates
- HTMX for dynamic interactions and partial page updates
- Bootstrap responsive design
- Form-based interactions with validation feedback

## Database Architecture

### Configuration Strategy
- **Profile-based configuration**: `%dev`, `%test`, `%prod`
- **Environment variables** for database credentials:
  - Development: `DEV_DB_USERNAME`, `DEV_DB_PASSWORD`
  - Testing: `TEST_DB_USERNAME`, `TEST_DB_PASSWORD`
  - Production: `PROD_DB_USERNAME`, `PROD_DB_PASSWORD`
- **Flyway migrations** in `src/main/resources/db/migration/`
- **Automatic clean/migrate** on startup in dev/test modes

## Template Architecture

### Qute Template System
- Templates located in `src/main/resources/templates/`
- **CheckedTemplate pattern** for type-safe template references
- **TemplateGlobals utility** provides common variables (current year, app version)
- **Component-based structure** with reusable components in `templates/components/`

### Template Structure
```
templates/
├── base.html              # Base template with common layout
├── header.html            # Navigation header
├── footer.html            # Footer content
├── home.html             # Homepage template
├── components/           # Reusable UI components
│   ├── error-alert.html
│   ├── success-alert.html
│   └── validation-errors.html
└── [entity]/            # Entity-specific templates
    ├── create.html
    ├── edit.html
    ├── view.html
    ├── delete.html
    ├── [entities].html   # List view
    └── table.html        # Table component
```

## Exception Handling

### Custom Exception Architecture
- **EntityNotFoundException**: For missing entities
- **DuplicateEntityException**: For constraint violations
- **ValidationException**: For business rule violations
- **GenericExceptionMapper**: Global exception handler

### Error Response Strategy
- **REST API**: JSON error responses with appropriate HTTP status codes
- **HTML UI**: User-friendly error pages with validation feedback
- **Logging**: Comprehensive error logging for debugging

## Security Architecture

### Current Implementation
- **Input validation** using Jakarta Bean Validation
- **SQL injection prevention** through parameterized queries
- **XSS protection** through Qute template escaping
- **CSRF protection** (to be implemented)

### Planned Security Features
- **Authentication**: User/Role-based access control
- **Authorization**: Method-level security
- **JWT tokens**: For API authentication
- **Session management**: For web UI

## Testing Strategy

### Test Architecture
- **Unit Tests**: Service and repository layer testing
- **Integration Tests**: REST API testing with `@QuarkusTest`
- **E2E Tests**: Selenide-based browser testing
- **Database Testing**: PostgreSQL with test transactions

### Test Organization
```
src/test/java/
├── resource/           # REST API tests
├── e2e/               # End-to-end browser tests
│   ├── base/          # Base test classes
│   └── pages/         # Page object model
└── [other packages]   # Unit tests
```

## Build and Deployment

### Build Profiles
- **Development**: Hot reload, debug logging, automatic migration
- **Test**: In-memory setup, transaction rollback, test data
- **Production**: Optimized build, connection pooling, security hardening

### Deployment Options
- **JVM Mode**: Fast startup, traditional JVM deployment
- **Native Mode**: GraalVM native executable for containerized deployments
- **Container Images**: Docker/Podman support with optimized images

## Performance Considerations

### Database Optimization
- **Connection pooling** with HikariCP
- **Query optimization** through repository pattern
- **Lazy loading** for entity relationships
- **Database indexing** on frequently queried fields

### Frontend Performance
- **HTMX partial updates** reduce full page reloads
- **Static resource optimization** through Quarkus
- **Template caching** for frequently accessed pages
- **Responsive design** for mobile optimization

## Future Architecture Enhancements

### Planned Features
1. **User Management System**
   - User, Role, UserRole entities
   - Authentication and authorization
   - Session management

2. **API Enhancement**
   - Rate limiting
   - API versioning
   - GraphQL support

3. **Monitoring and Observability**
   - Metrics collection
   - Distributed tracing
   - Health checks enhancement

4. **Scalability Improvements**
   - Caching layer (Redis)
   - Message queuing (RabbitMQ/Apache Kafka)
   - Microservices decomposition

## Development Guidelines

### Code Conventions
- Follow existing patterns for new entities
- Use repository pattern for data access
- Implement dual API (REST + HTML) for all entities
- Include comprehensive validation rules
- Maintain audit trail fields

### Testing Requirements
- Write tests for all new features
- Follow the established test patterns
- Ensure both API and UI test coverage
- Include validation and error handling tests

### Documentation Standards
- Document all public APIs
- Maintain architecture documentation
- Update ERD for schema changes
- Include deployment instructions