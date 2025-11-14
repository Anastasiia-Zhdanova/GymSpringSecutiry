# 🏋️ Gym Management REST API (Spring Boot)

## 📝 Project Overview

This project is an enterprise-ready, multi-layered Java application built with **Spring Boot 3** and **Hibernate/JPA**. It provides a comprehensive RESTful API for managing Trainees, Trainers, and Training sessions.

The system is designed for high availability and observability, featuring:

* **Security:** Spring Security with **Redis** for stateful session management.
* **Observability:** Integrated **Spring Boot Actuator** for health monitoring and **Prometheus** metrics via Micrometer for performance tracking.
* **Configuration:** Full support for multi-environment deployments using **Spring Profiles** (`local`, `dev`, `stg`, `prod`).
* **AOP:** Custom Aspect for request tracing (Transaction ID/MDC logging).
* **Data Layer:** JPA/EntityManager-based DAOs (migrated from manual `HibernateUtil`) for clean and efficient data access.

---

## 🏗️ Project Structure & Key Components

This project follows a strict layered architecture to ensure separation of concerns.

| Layer | Package/File | Key Components (Files) |
| :--- | :--- | :--- |
| **Core** | `GymApplication.java` | Main Spring Boot application entry point (`@SpringBootApplication`). |
| **Configuration** | `config` | `WebSecurityConfig.java` (Manages HTTP security, protected routes).<br>`CustomUsernamePasswordAuthenticationFilter.java` (Handles login logic).<br>`RedisConfig.java` (Enables `@EnableRedisHttpSession`).<br>`LoggingAspect.java` (AOP for request/response logging via MDC).<br>`SwaggerConfig.java` (OpenAPI documentation setup).<br>`RedisSessionHealthIndicator.java` (Custom Actuator check 1).<br>`TrainingTypeInitialLoadHealthIndicator.java` (Custom Actuator check 2). |
| **API (Controllers)** | `controller` | `AuthenticationController.java` (Handles registration, login, password change).<br>`TraineeController.java` (Manages Trainee profiles, trainers, and trainings).<br>`TrainerController.java` (Manages Trainer profiles, trainees, and trainings).<br>`TrainingController.java` (Handles creation of new training sessions). |
| **Business Logic** | `service` | `AuthService.java` (Password hashing, user authentication, username generation).<br>`TraineeService.java` / `TrainerService.java` (Core profile logic).<br>`TraineeServiceFacade.java` / `TrainerServiceFacade.java` (Facades for controllers).<br>`TrainingService.java` (Business logic for creating trainings).<br>`TrainingTypeService.java` (Manages training specializations). |
| **Data Contracts (DTO)** | `dto/request` | `LoginRequest.java`<br>`ChangePasswordRequest.java`<br>`TraineeRegistrationRequest.java`<br>`TrainerRegistrationRequest.java`<br>`TraineeProfileUpdateRequest.java`<br>`TrainerProfileUpdateRequest.java`<br>`UserStatusUpdateRequest.java`<br>`UpdateTraineeTrainersRequest.java`<br>`TrainingRequest.java` |
| | `dto/response` | `AuthResponse.java`<br>`UserCredentialsResponse.java`<br>`TraineeProfileResponse.java`<br>`TraineeShortResponse.java`<br>`TrainerProfileResponse.java`<br>`TrainerShortResponse.java`<br>`TrainingListResponse.java`<br>`TrainingTypeResponse.java` |
| **Mapping** | `mapper` | `TraineeMapper.java` / `TrainerMapper.java` (MapStruct interfaces for DTO/Entity conversion). |
| **Data Access** | `dao` | `GenericDAO.java` (JPA `EntityManager` base class).<br>`UserDAO.java`, `TraineeDAO.java`, `TrainerDAO.java`, `TrainingDAO.java`, `TrainingTypeDAO.java`. |
| **Persistence** | `entity` | `User.java` (Base profile), `Trainee.java`, `Trainer.java`, `Training.java`, `TrainingType.java`. |
| **Error Handling** | `exception` | `GlobalExceptionHandler.java` (`@ControllerAdvice` to handle exceptions).<br>`AuthenticationException.java`, `NotFoundException.java`, `ValidationException.java`. |
| **Utilities** | `util` | `PasswordUtil.java` (BCrypt hashing), `UsernameUtil.java`, `UserCredentialGenerator.java`. |
| **Resources** | `resources` | `application.yml` (Base config), `application-{profile}.yml`, `data.sql`, `logback.xml`. |

---

## 🚀 Running the Application & Profiles

### Prerequisites

1.  **Java Development Kit (JDK) 17+**
2.  **Maven 3.6+**
3.  **PostgreSQL Server** (Required for `dev/stg/prod` profiles)
4.  **Redis Server** (Required for session management, e.g., `localhost:6379` for local)

### Build and Package

First, build the executable JAR file:
```bash
mvn clean install