# rescaffold

## Overview

Scaffold application using Quarkus + HTMX with PostgreSQL database

## Prerequisites

### Database Setup

1. **Install PostgreSQL** (if not already installed)
2. **Create databases:**
   ```sql
   CREATE DATABASE scaffold_dev;
   CREATE DATABASE scaffold_test;
   CREATE DATABASE scaffold_prod;
   ```

3. **Create .env file** in project root with database credentials:
   ```bash
   # Development
   DEV_DB_USERNAME=your_username
   DEV_DB_PASSWORD=your_password
   
   # Testing
   TEST_DB_USERNAME=your_username
   TEST_DB_PASSWORD=your_password
   
   # Production
   PROD_DB_USERNAME=your_username
   PROD_DB_PASSWORD=your_password
   ```

**Note:** The `.env` file is ignored by git for security.

### Run development mode

```bash
quarkus dev
```

Notes: 
- Browse to http://localhost:8080/
- DevUI is at http://localhost:8080/q/dev/
- Swagger UI is at http://localhost:8080/q/swagger-ui/

### Upgrade Quarkus (when necessary)

Stop the application and upgrade to the latest version:

```bash
quarkus upgrade
quarkus dev --clean
```

### Testing

Run all tests:
```bash
./mvnw test
```

Run specific test(s):
```bash
./mvnw test -Dtest=TestClassName
./mvnw test -Dtest="*ResourceTest"
./mvnw test -Dtest="*CrudTest"
```

Run integration tests:
```bash
./mvnw verify
```

### Code Formatting

Format code (if Spotless plugin configured):
```bash
./mvnw spotless:apply
```

### Flyway database management

Flyway migration scripts are in `src/main/resources/db/migration`

Database is automatically cleaned and migrated on startup in dev/test modes.

## Packaging

### Runnable JAR

```bash
quarkus build --clean
java -jar target/quarkus-app/quarkus-run.jar
```

Notes:
- The build uses the `%prod` profile
- Runs the test suite
- Builds a fast-jar by default, with indexed information about the dependency jars to speed it up (the fast-jar is slower to start than a native executable, but is preferred for long-running applications because the JVM optimises code paths)

### Container image with fast-jar

First build the JAR in the default fast-jar format:

```bash
quarkus build --clean
```

Then build the image and run it:

```bash
podman build -f src/main/docker/Dockerfile.jvm -t quarkus/rescaffold-jvm .
podman run -i --rm -p 8080:8080 quarkus/rescaffold-jvm
```

Notes:
- Use the `Dockerfile.jvm` container definition to generate a container image with the fast-jar

### GraalVM executable

```bash
quarkus build --native --clean
./target/rescaffold-1.0.0-runner
```

Notes:
- The configuration properties in `application.properties` specify a container build with Podman, not Docker
- The native executable is fast to start so it is preferred for serverless applications

## Developing the application

### Initial generation

Generate the application and start in dev mode:

```bash
cd ~/quarkus
quarkus create app \
--description='Scaffold Quarkus+HTMX application' \
--extensions='rest, rest-jackson, jdbc-postgresql, hibernate-orm-panache, hibernate-validator, qute, smallrye-openapi, flyway' \
--maven \
--app-config='quarkus.log.console.darken=1' \
--no-code \
io.archton.scaffold:rescaffold:1.0.0

cd rescaffold
git init
git add .
git commit -m 'Initial commit'
quarkus dev
```

