# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Development Commands

### Java environment
The project uses Java 21 and GraalVM, so you must run the following in a terminal before running Maven or Quarkus:
```bash
sdk use java 21.0.2-graalce 
```

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
source .env
sdk use java 21.0.2-graalce
./mvnw test
```

### Run Single Test
```bash
source .env
sdk use java 21.0.2-graalce
./mvnw test -Dtest=TestClassName
```

### Run Selenium E2E Tests
End-to-end tests using Selenium require the application to be running first:
```bash
# Terminal 1: Start the application
source .env
sdk use java 21.0.2-graalce
quarkus dev

# Terminal 2: Run E2E tests (requires Chrome/Chromium installed)
bash -c "source ~/.sdkman/bin/sdkman-init.sh && sdk use java 21.0.2-graalce && ./mvnw test -Dtest=GenderNavbarTest"
```
**Prerequisites for E2E tests:**
- Chrome or Chromium browser installed
- Application running on http://localhost:8080
- Tests run in headless mode by default
- GenderNavbarTest verifies the Gender menu option exists in navbar

### Native Build
```bash
sdk use java 21.0.2-graalce
quarkus build --native --clean
```

### Upgrade Quarkus
```bash
sdk use java 21.0.2-graalce
quarkus upgrade
quarkus dev --clean
```

## Architecture Overview

This is a Quarkus + HTMX scaffold application following a layered architecture pattern:

### Core Stack
- **Quarkus 3.22.3** with Java 21
- **PostgreSQL** database with Flyway migrations
- **Hibernate ORM with Panache** for data persistence
- **Qute** templating engine for server-side rendering
- **HTMX** for dynamic frontend interactions
- **JAX-RS REST** for API endpoints

### Package Structure
- `domain/` - Entity classes using Panache Active Record pattern
- `resource/` - REST API endpoints (`/api/*` paths)
- `web/` - HTML UI controllers (`/*-ui` paths) using Qute templates
- `repository/` - Data access layer with custom queries and business logic
- `util/` - Utility classes like TemplateConfig for template variables
- `service/` - Business logic layer
- `health/` - Health check endpoints

### Dual API Pattern
The application provides both REST API and HTML UI for the same entities:
- **REST Resources** (`resource/` package): JSON API endpoints at `/api/*`
- **Web Routers** (`web/` package): HTML UI endpoints at `/*-ui` with Qute templates

### Database Configuration
- Uses profile-based configuration (`%dev`, `%test`, `%prod`)
- Flyway migrations in `src/main/resources/db/migration/`
- Environment variables for database credentials (e.g., `DEV_DB_USERNAME`)
- Automatic clean/migrate on startup in dev/test modes

### Template System
- Qute templates in `src/main/resources/templates/`
- CheckedTemplate pattern for type-safe template references
- TemplateConfig utility provides common variables (current year, app version)
- Follows directory structure matching template organization
- Refer to https://quarkus.io/guides/qute-reference for guidance on templates

### Entity Pattern
Entities use Hibernate Panache Active Record pattern:
- Extend `PanacheEntity` for auto-generated ID
- Business logic methods directly on entity classes
- Repository classes for complex queries and sorting

### Form Handling
- HTML forms use `@FormParam` for parameter binding
- HTMX returns partial HTML fragments for dynamic updates
- Error handling returns appropriate HTTP status codes

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

## Screenshot Location
- Look in folder /home/geraldo/Pictures/Screenshots/ for screenshots

## Test Strategy
- Test documentation is here: https://quarkus.io/guides/getting-started-testing
- Use @QuarkusTest annotation and follow https://quarkus.io/guides/getting-started-testing#testing-a-specific-endpoint for testing endpoints. 
- Use PostgreSQL database as defined in %test.quarkus.datasource 
- Database credentials are loaded from .env file (TEST_DB_USERNAME, TEST_DB_PASSWORD)
- Tests require Java 21 GraalVM to run properly
- Example: GenderResourceTest covers all CRUD operations with 18 test cases
- **E2E Testing**: Selenium tests in `src/test/java/io/archton/scaffold/ui/` package test UI functionality
  - GenderNavbarTest verifies Gender menu option exists in navbar
  - Requires application running on localhost:8080 before executing tests 

## HTMX Documentation
- HTMX documentation is here: https://htmx.org/docs/
