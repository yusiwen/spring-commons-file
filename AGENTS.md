# AGENTS.md

## Build Commands

```bash
# Full compile
./mvnw compile

# Run tests
./mvnw test

# Run a single module
./mvnw compile -pl spring-commons-file-core -am

# Run tests with output
./mvnw test -pl spring-commons-file-test -am 2>&1 | tail -20
```

## Prerequisites

- JDK 8 installed at `~/.sdkman/candidates/java/8` (Azul)
- `~/.m2/toolchains.xml` configured with JDK 8

## Project Structure

```
spring-commons-file (parent)
├── spring-commons-file-core/     — FileOperator interface + 4 implementations
├── spring-commons-file-starter/  — Spring Boot auto-configuration wrapper
└── spring-commons-file-test/     — integration tests
```

## Architecture

- **Strategy pattern**: `FileOperator` interface with pluggable implementations
- **Auto-configuration**: defaults to `LocalFileOperator`
- **Cloud SDKs**: all optional, activated by `@ConditionalOnClass`
