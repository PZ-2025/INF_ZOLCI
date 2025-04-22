# BuildTask Backend

Backend service for BuildTask.

## Overview

The backend provides REST API services for managing construction tasks, users, and company resources. It's built with:

- Spring Boot 3.4.3
- Java 21
- MariaDB/H2 Database
- Liquibase for database migrations
- JPA/Hibernate for ORM

## Style and Bugs checks

Checks according to PDF from Sun's microsystems.

### Available checks

- **Checkstyle**: Ensures code adheres to our style guidelines
- **SpotBugs**: Identifies potential bugs and problematic code patterns

### Dependencies

```shell
   plugins {
       checkstyle
       id("com.github.spotbugs") version "6.1.7"
   }
```

### Running the checks

You can run both tools simultaneously using the custom Gradle task:

```shell
  ./gradlew codeQuality
```

## Environment Setup

### Prerequisites

- Java JDK 21 or higher
- MariaDB (for production) or H2 (for development)
- Gradle (included wrapper)

### Environment Variables

Create `.env` file in the `backend/` folder based on `.env-example`:

```properties
# Database configuration
MYSQL_ROOT_PASSWORD=root          # MariaDB root password
MYSQL_DATABASE=buildtask_db       # Database name
MYSQL_USER=buildtask_user         # Application database user
MYSQL_PASSWORD=buildtask_password # Application database password
MYSQL_CONNECTION_STRING="jdbc:mariadb://127.0.0.1:3306" # Database connection URL

# Application profile
SPRING_PROFILES_ACTIVE=test     # Active Spring profile
```

### Available Profiles

- `dev` - Development profile with test data initialization
- `build` - Production profile with minimal required data
- `test` - Testing profile with H2 database

## Running the Application

### Development Mode

```shell
./gradlew clean build test # check if app builds
./gradlew bootRun          # run app
```

This will:

- Start the application in development mode
- Use H2 in-memory database if SPRING_PROFILES_ACTIVE=test
- Enable auto-reload for development
- Initialize test data if using dev profile

### Production Mode

1. Build the application:

    ```shell
    ./gradlew clean build -x test
    ```

2. Run the JAR:

    ```shell
    java -jar build/libs/backend-{version}.jar
    ```

Make sure to:

- Configure proper database connection in `.env`
- Set SPRING_PROFILES_ACTIVE appropriately
- Have MariaDB running for production deployment

### Database Configuration

The application uses different database setups based on the active profile:

#### Development/Test (H2)

```shell
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=password
```

#### Production (MariaDB)

```shell
spring.datasource.url=${MYSQL_CONNECTION_STRING}/${MYSQL_DATABASE}
spring.datasource.username=${MYSQL_USER}
spring.datasource.password=${MYSQL_PASSWORD}
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
```

## Features

- User authentication and authorization
- Task management for construction projects
- Resource allocation
- Project tracking
- File management
- System settings configuration

## Database Migrations

Database schema changes are managed through Liquibase:

- Migrations are stored in `src/main/resources/db/changelog/`
- Production runs migrations automatically
- Development can recreate schema with `spring.jpa.hibernate.ddl-auto=create-drop`

## Troubleshooting

1. Database connection issues:
   - Verify MariaDB is running
   - Check credentials in `.env`
   - Confirm database exists

2. Application won't start:
   - Check logs for detailed errors
   - Verify Java version
   - Ensure all environment variables are set

3. Profile issues:
   - Check SPRING_PROFILES_ACTIVE in `.env`
   - Verify profile-specific properties files
