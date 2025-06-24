# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

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

## Architecture Overview

This is a Quarkus + HTMX scaffold application following a layered architecture pattern:

### Core Stack
- **Quarkus 3.22.3** with Java 17
- **PostgreSQL** database with Flyway migrations
- **Hibernate ORM with Panache** for data persistence
- **Qute** templating engine for server-side rendering
- **HTMX** for dynamic frontend interactions
- **Quarkus REST** (REST-Easy Reactive) for API endpoints

### Package Structure
- `domain/` - Entity classes using Panache Active Record pattern
- `resource/` - REST API endpoints (`/api/*` paths)
- `web/` - HTML UI controllers (`/*-ui` paths) using Qute templates
- `repository/` - Data access layer with custom queries and business logic
- `util/` - Utility classes like TemplateGlobals for global template variables
- `service/` - Business logic layer
- `health/` - Health check endpoints (currently empty, uses Quarkus SmallRye Health)

### Dual API Pattern
The application provides both REST API and HTML UI for entities:
- **REST Resources** (`resource/` package): JSON API endpoints at `/api/*`
- **Web Routers** (`web/` package): HTML UI endpoints at `/*-ui` with Qute templates
- Currently implemented for: Gender entity
- Pattern ready for: Title and future entities

### Database Configuration
- Uses profile-based configuration (`%dev`, `%test`, `%prod`)
- Flyway migrations in `src/main/resources/db/migration/`
- Environment variables for database credentials:
  - `DEV_DB_USERNAME`, `DEV_DB_PASSWORD` (development)
  - `TEST_DB_USERNAME`, `TEST_DB_PASSWORD` (testing)
  - `PROD_DB_USERNAME`, `PROD_DB_PASSWORD` (production)
- Automatic clean/migrate on startup in dev/test modes
- **Note**: Create `.env` file in project root with database credentials (not committed to git)

### Template System
- Qute templates in `src/main/resources/templates/`
- CheckedTemplate pattern for type-safe template references
- TemplateGlobals utility provides common variables (current year, app version)
- Follows directory structure matching template organization
- Refer to https://quarkus.io/guides/qute-reference for guidance on templates

### Entity Pattern
Entities use Hibernate Panache Active Record pattern:
- Extend `PanacheEntityBase` with manual ID management
- Business logic methods directly on entity classes
- Repository classes for complex queries and sorting
- Audit fields: `created_at`, `updated_at`, `created_by`, `updated_by`, `is_active`
- Refer to https://quarkus.io/guides/hibernate-orm-panache for guidance

### Form Handling
- HTML forms use `@FormParam` for parameter binding
- HTMX returns partial HTML fragments for dynamic updates
- Error handling returns appropriate HTTP status codes
- Jakarta Bean Validation for form validation
- Bootstrap styling with responsive design

### Entity Relationship Diagram
```mermaid
erDiagram
    Gender {
        bigint id PK
        varchar code UK
        text description UK
        boolean is_active "NOT NULL DEFAULT true"
        timestamp created_at "NOT NULL DEFAULT now()"
        timestamp updated_at "NULL"
        varchar created_by "NOT NULL DEFAULT 'system'"
        varchar updated_by "NULL"
    }
    
    Title {
        bigint id PK
        varchar code UK
        text description UK
        boolean is_active "NOT NULL DEFAULT true"
        timestamp created_at "NOT NULL DEFAULT now()"
        timestamp updated_at "NULL"
        varchar created_by "NOT NULL DEFAULT 'system'"
        varchar updated_by "NULL"
    }
    
    Person {
        bigint id PK
        varchar first_name
        varchar last_name "NOT NULL"
        varchar email UK
        bigint gender_id FK
        bigint title_id FK
        boolean is_active "NOT NULL DEFAULT true"
        timestamp created_at "NOT NULL DEFAULT now()"
        timestamp updated_at "NULL"
        varchar created_by "NOT NULL DEFAULT 'system'"
        varchar updated_by "NULL"
    }
    
    User {
        bigint id PK
        bigint person_id FK
        varchar username UK
        varchar password_hash
        boolean is_active "NOT NULL DEFAULT true"
        timestamp last_login
        timestamp created_at "NOT NULL DEFAULT now()"
        timestamp updated_at "NULL"
        varchar created_by "NOT NULL DEFAULT 'system'"
        varchar updated_by "NULL"
    }
    
    Role {
        bigint id PK
        varchar name UK
        text description UK
        boolean is_active "NOT NULL DEFAULT true"
        timestamp created_at "NOT NULL DEFAULT now()"
        timestamp updated_at "NULL"
        varchar created_by "NOT NULL DEFAULT 'system'"
        varchar updated_by "NULL"
    }
    
    UserRole {
        bigint user_id FK
        bigint role_id FK
        timestamp assigned_at
        boolean is_active "NOT NULL DEFAULT true"
        timestamp created_at "NOT NULL DEFAULT now()"
        timestamp updated_at "NULL"
        varchar created_by "NOT NULL DEFAULT 'system'"
        varchar updated_by "NULL"
    }
    
    Gender ||--o{ Person : "has"
    Title ||--o{ Person : "has"
    Person ||--o| User : "becomes"
    User ||--o{ UserRole : "has"
    Role ||--o{ UserRole : "assigned to"
    User ||--o{ Gender : "created_by"
    User ||--o{ Title : "created_by"
    User ||--o{ Person : "created_by"
    User ||--o{ Role : "created_by"
    User ||--o{ UserRole : "created_by"
```

## Current Implementation Status

### ✅ Completed Features
- **Gender Entity**: Full CRUD operations with REST API and HTML UI
- **Title Entity**: Domain model and database schema (UI pending)
- **Database Setup**: PostgreSQL with Flyway migrations
- **Template System**: Qute templates with HTMX integration
- **Development Environment**: Hot reload, DevUI, Swagger documentation

### 🚧 In Progress / Planned
- **Title Entity**: REST API and HTML UI implementation
- **Person Entity**: Complete implementation per ERD
- **User Management**: User, Role, UserRole entities and authentication
- **Additional Features**: As defined in entity relationship diagram

### 🏗️ Architecture Scaffold Ready
- Dual API pattern (REST + HTML UI) established
- Audit trail fields on all entities
- Form validation and error handling
- Bootstrap responsive design
- HTMX dynamic interactions

## Screenshot Location
- Look in folder /home/geraldo/Pictures/Screenshots/ for screenshots

## Test Strategy
- Test documentation is here: https://quarkus.io/guides/getting-started-testing
- Use @QuarkusTest annotation and follow https://quarkus.io/guides/getting-started-testing#testing-a-specific-endpoint for testing endpoints. 
- Use PostgreSQL database as defined in %test.quarkus.datasource 
- Database credentials are loaded from .env file (TEST_DB_USERNAME, TEST_DB_PASSWORD)
- Use Selenide for end-to-end testing as it is based on Selenium
- Cover critical user journeys
- Test across different browsers and screen sizes
- Automate UI interaction and validation scenarios
 
## HTMX Documentation
- HTMX documentation is here: https://htmx.org/docs/

