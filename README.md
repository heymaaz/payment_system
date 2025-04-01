# Payment Processing System

## Overview

This project implements a simplified payment processing system. 

The system allows users to register accounts, manage balances, make payments to other users, and view transaction history via a RESTful API.

The core requirements met include:
* User registration with an initial balance.
* Processing payments between users, including validation for sufficient funds, positive amounts and unique sender and receiver.
* Maintaining user balances and transaction history.
* Providing API endpoints for user and payment operations.
* Basic logging and structured error handling.
* In-memory data storage (using H2 database via Spring Data JPA).

## Prerequisites

* Java JDK 21 or later
* Apache Maven 3.9.9+ (The Maven Wrapper `./mvnw` is included)

## Build Instructions

You can build the project using the included Maven wrapper:

```bash
./mvnw clean package
```

This command will compile the code, run tests, and package the application into an executable JAR file located in the target/ directory (target/payment_system-0.0.1-SNAPSHOT.jar).

## Running the Application

Once built, you can run the application using:

```bash
java -jar target/payment_system-*.jar
```

The application will start, and the API will be available at http://localhost:8080

## API Documentation

OpenAPI specification can be found at OpenAPI. They were auto-generated using Redocly in IntelliJ.

## Error Handling

API errors are handled centrally and return a standardized JSON response body, with a 400 Bad Request or 404 Not Found status code. The response structure follows the following pattern based on the implemented GlobalExceptionHandler:

```JSON
{
    "timestamp": "iso-timestamp",
    "status": 400,
    "error": "Bad Request",
    "message": "Specific error message (e.g., 'Sender and receiver user IDs must be different', 'Not enough balance', 'amount is required')",
    "field": "fieldName included for field-level validation errors"
}
```
Common error scenarios include invalid input data, non-existent users, and insufficient funds during payment processing.

## Design Choices & Technology
- ![Spring Initializer.jpeg](Spring%20Initializer.jpeg "Spring Initializer.jpeg")
- Framework: Java 21 with Spring Boot 3.4.4
- Key Dependencies:
  - spring-boot-starter-web: For building the REST API.
  - spring-boot-starter-data-jpa: For data persistence (configured with H2 in-memory database).
  - spring-boot-starter-validation: For request validation using Hibernate Bean Validation annotations.
  - lombok: To reduce boilerplate code (getters, setters, etc.).
  - Storage: Uses Spring Data JPA repositories backed by an H2 in-memory database. Data is not persisted between application restarts.
  - Concurrency: Transaction management is handled using Spring's `@Transactional` annotation on service methods like PaymentService.processPayment. This ensures the atomicity of database operations (e.g., debiting sender, crediting receiver, creating payment record) – they either all succeed or all fail together. The implementation relies on H2's default transaction isolation level to manage concurrent access. While `@Transactional` ensures atomic updates, preventing certain race conditions under high concurrency (like concurrent balance checks and subsequent debits leading to overdrafts) might require more advanced strategies like pessimistic locking (`@Lock`) or optimistic locking (`@Version`) in a production environment.
  
## Testing
  The project includes unit and integration tests to ensure the core functionality works as expected. Tests cover service logic, validation, and API endpoint interactions.

You can run the tests using Maven:

```Bash
./mvnw test
```
