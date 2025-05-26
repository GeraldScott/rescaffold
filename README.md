# rescaffold

## Overview

Scaffold application using QUARKUS+HTMX

### Run development mode

```bash
sdk use java 21.0.2-graalce
quarkus dev
```

Notes: 
- Browse to http://localhost:8080/
- DevUI is at http://localhost:8080/q/dev/
- Swagger UI is at http://localhost:8080/q/swagger-ui/

### Upgrade Quarkus

Stop the application and upgrade to the latest version:

```bash
quarkus upgrade
quarkus dev --clean
```

### Flyway database management

Flyway migration scripts are in `src/main/resources/db/migration`

## Packaging

### Runnable JAR

```bash
sdk use java 21.0.2-graalce
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
sdk use java 21.0.2-graalce
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
sdk use java 21.0.2-graalce
quarkus build --native --clean
./target/rescaffold-1.0.0-SNAPSHOT-runner
```

Notes:
- The configuration properties in `application.properties` specify a container build with Podman, not Docker
- The native executable is fast to start so it is preferred for serverless applications

## Developing the application

### Initial generation

Generate the application and start in dev mode:

```bash
cd ~/quarkus
sdk use java 21.0.2-graalce
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

