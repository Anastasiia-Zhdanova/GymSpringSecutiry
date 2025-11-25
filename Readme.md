# 🏋️ Gym Management REST API (Spring Boot)

## 📝 Project Overview

This project is an enterprise-ready, multi-layered Java application built with **Spring Boot 3** and **Hibernate/JPA**. It provides a comprehensive RESTful API for managing Trainees, Trainers, and Training sessions.

The system is designed for high availability and observability, featuring:

* **Security:** Spring Security with **Redis** for stateful session management. Stateless authentication using **JWT (JSON Web Tokens)** with **Brute Force Protection** (account locking).
* **Observability:** Integrated **Spring Boot Actuator** for health monitoring and **Prometheus** metrics via Micrometer for performance tracking.
* **Configuration:** Full support for multi-environment deployments using **Spring Profiles** (`local`, `dev`, `stg`, `prod`).
* **AOP:** Custom Aspect for request tracing (Transaction ID/MDC logging).
* **Data Layer:** JPA/EntityManager-based DAOs (migrated from manual `HibernateUtil`) for clean and efficient data access.

---

## 🏗️ Project Structure & Key Components

This project follows a strict layered architecture to ensure separation of concerns.

| Layer | Package/File | Key Components (Files)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| :--- | :--- |:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **Core** | `GymApplication.java` | Main Spring Boot application entry point (`@SpringBootApplication`).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| **Configuration** | `config` | `WebSecurityConfig.java` (Manages HTTP security, protected routes).<br> `JwtAuthenticationFilter.java` (Intercepts requests to validate JWT tokens).<br>`CustomUsernamePasswordAuthenticationFilter.java` (Handles login logic).<br>`LoggingAspect.java` (AOP for request/response logging via MDC).<br>`SwaggerConfig.java` (OpenAPI documentation setup).<br>`TrainingTypeInitialLoadHealthIndicator.java` (Custom Actuator check 2).                                                                                                                        |
| **Security** | `security` | `JwtService.java` (Generates, signs, and validates JWT tokens).<br>`UserDetailsServiceImpl.java` (Loads user data from DB for Spring Security).                                                                                                                                                                                                                                                                                                                                                                                                                |
| **API (Controllers)** | `controller` | `AuthenticationController.java` (Handles registration, login, password change).<br>`TraineeController.java` (Manages Trainee profiles, trainers, and trainings).<br>`TrainerController.java` (Manages Trainer profiles, trainees, and trainings).<br>`TrainingController.java` (Handles creation of new training sessions).                                                                                                                                                                                                                                    |
| **Business Logic** | `service` | `AuthService.java` (Password hashing, user authentication, username generation).<br>`TraineeService.java` / `TrainerService.java` (Core profile logic).<br>`TraineeServiceFacade.java` / `TrainerServiceFacade.java` (Facades for controllers).<br>`TrainingService.java` (Business logic for creating trainings).<br>`TrainingTypeService.java` (Manages training specializations).                                                                                                                                                                           |
| **Data Contracts (DTO)** | `dto/request` | `LoginRequest.java`<br>`ChangePasswordRequest.java`<br>`TraineeRegistrationRequest.java`<br>`TrainerRegistrationRequest.java`<br>`TraineeProfileUpdateRequest.java`<br>`TrainerProfileUpdateRequest.java`<br>`UserStatusUpdateRequest.java`<br>`UpdateTraineeTrainersRequest.java`<br>`TrainingRequest.java`                                                                                                                                                                                                                                                   |
| | `dto/response` | `AuthResponse.java`<br>`UserCredentialsResponse.java`<br>`TraineeProfileResponse.java`<br>`TraineeShortResponse.java`<br>`TrainerProfileResponse.java`<br>`TrainerShortResponse.java`<br>`TrainingListResponse.java`<br>`TrainingTypeResponse.java`                                                                                                                                                                                                                                                                                                            |
| **Mapping** | `mapper` | `TraineeMapper.java` / `TrainerMapper.java` (MapStruct interfaces for DTO/Entity conversion).                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| **Data Access** | `dao` | `GenericDAO.java` (JPA `EntityManager` base class).<br>`UserDAO.java`, `TraineeDAO.java`, `TrainerDAO.java`, `TrainingDAO.java`, `TrainingTypeDAO.java`.                                                                                                                                                                                                                                                                                                                                                                                                       |
| **Persistence** | `entity` | `User.java` (Base profile), `Trainee.java`, `Trainer.java`, `Training.java`, `TrainingType.java`.                                                                                                                                                                                                                                                                                                                                                                                                                                                              |
| **Error Handling** | `exception` | `GlobalExceptionHandler.java` (`@ControllerAdvice` to handle exceptions).<br>`AuthenticationException.java`, `NotFoundException.java`, `ValidationException.java`.                                                                                                                                                                                                                                                                                                                                                                                             |
| **Utilities** | `util` | `PasswordUtil.java` (BCrypt hashing), `UsernameUtil.java`, `UserCredentialGenerator.java`, `QueryUtil.java`.                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| **Resources** | `resources` | `application.yml` (Base config), `application-{profile}.yml`, `data.sql`, `logback.xml`.                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |

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
```
### Running with Profiles

This application uses Spring Profiles to manage configurations across different environments. You activate a profile using the `--spring.profiles.active=...` argument.

|Profile   |  File | Database                | ddl-auto  |  Purpose |
|---|---|-------------------------|---|---|
| local  | application-local.yml  | H2 In-Memory            | create-drop  |  Local development & testing. |
|  dev |  application-dev.yml | PostgreSQL (Dev DB)     | update  | Development integration environment.  |
| stg  |  application-stg.yml | PostgreSQL (Staging DB) | validate  |  Pre-production testing. |
| prod  |  application-prod.yml | PostgreSQL (Prod DB)    | validate  | Live production environment.  |

#### 1. How to run (`local` profile)

The `local` profile is designed to run "out-of-the-box" using an H2 in-memory database. It automatically loads `data.sql`.
```bash
# Run using the local profile
java -jar target/gym-management-rest-1.0-SNAPSHOT.jar --spring.profiles.active=local
```

#### 2. How to run (`dev`, `stg`, `prod` profiles)

These profiles require an external PostgreSQL database and a password passed as an Environment Variable (to avoid storing secrets in code).

Example: Launching the `stg` (Staging) Environment

##### 1.Set the Environment Variable (macOS/Linux):
```bash
export STG_DB_PASSWORD=your_secure_staging_password
```
(For Windows, use: `set STG_DB_PASSWORD=your_secure_staging_password`)

##### 2.Run the Application with the `stg` profile active:
```bash
java -jar target/gym-management-rest-1.0-SNAPSHOT.jar --spring.profiles.active=stg
```

The application will start, read the database password from the `STG_DB_PASSWORD` variable, and connect to the Staging PostgreSQL database.

### 📊 Observability (Actuator & Metrics)
This project uses **Spring Boot Actuator** and **Micrometer** to provide deep insights into the application's health and performance.

#### 1. Spring Boot Actuator
   Actuator provides production-ready endpoints for monitoring.

1) **How it's implemented:** Enabled by adding the `spring-boot-starter-actuator` dependency in pom.xml.

2) How it's configured: Endpoints are exposed in `application.yml` via `management.endpoints.web.exposure.include: "*" `.

Key Endpoint: `GET /actuator/health`

#### 2. Custom Health Indicators
   I have implemented two custom health indicators that integrate with the `/actuator/health` endpoint:

##### 1. `TrainingTypeInitialLoadHealthIndicator`

1) Purpose: Ensures that the reference data (e.g., from `data.sql`) has been successfully loaded into the database, which is critical for creating Trainers.

2) Implementation: Injected `TrainingTypeDAO` and checks if `findAll()` returns an empty list.

3) Location: `com.company.gym.config.TrainingTypeInitialLoadHealthIndicator`

4) Status: `DOWN` if the `training_type` table is empty.

#### 2. Custom Metrics (Prometheus)
   I use **"Micrometer"** to define custom metrics, which are then exposed at the `/actuator/prometheus` endpoint for scraping.

##### 1. Metric: User Registrations (Counter)

1) Name: `app.user.registrations.total`

2) Purpose: Tracks the total number of new Trainee and Trainer profiles created.

3) Implementation: A `Counter` is initialized in the `AuthService` constructor (using `MeterRegistry`).

4) Location (Where): It is incremented (`via .increment()`) in the `assignUniqueUsernameAndPassword` method of `AuthService`.

##### 2. Metric: Training Creation Time (Timer)

1) Name: `app.training.creation.time`

2) Purpose: Measures the latency (duration) of the `createTraining` method, including database validation and insertion.

3) Implementation: A `Timer` is initialized in the `TrainingService` constructor (using `MeterRegistry`).

4) Location (Where): The entire business logic of the `createTraining` method is wrapped in a `Timer.record(...)` lambda block.

## 🗺️ API Usage and Documentation

### Swagger Documentation

The full interactive API documentation is available here for testing all **17 endpoints**:

* **URL:** `http://localhost:8080/swagger-ui.html`

### Key Endpoints

Authentication is handled via **Redis Sessions**. After a successful `POST /login`, a session cookie (`JSESSIONID`) must be included in all protected requests.

| Req.   | Description                          | Method   | Path                                                       | Auth Required |
|:-------|:-------------------------------------|:---------|:-----------------------------------------------------------| :--- |
| 1, 2   | **Registration** (Trainee/Trainer)   | `POST`   | `/api/v1/auth/{type}/register`                             | ❌ |
| 3      | **Login** (Establish Session)        | `POST`   | `/api/v1/auth/login`                                       | ❌ |
| 4      | **Change Password**                  | `PUT`    | `/api/v1/auth/change-password`                             | ✅ |
| 7      | **Delete Trainee** (Cascade)         | `DELETE` | `/api/v1/trainees/{username}`                              | ✅ |
| 14     | **Add Training**                     | `POST`   | `/api/v1/trainings`                                        | ✅ |
| 15, 16 | **Activate/Deactivate**              | `PATCH`  | `/api/v1/{type}s/{username}/status`                        | ✅ |
| 17     | **Get Training Types**               | `GET`    | `/api/v1/training-types`                                   | ❌ |
| 5,8    | **Get Profile** (Trainee/Trainer)    | `GET`    | `/api/v1/{type}s/{username}`                               | ✅ |
| 6,9    | **Update Profile** (Trainee/Trainer) | `PUT`    | `/api/v1/{type}s/{username}`                               | ✅ |
| 11     | **Update Profile** (Trainee/Trainer) | `PUT`    | `/api/v1/trainees/{username}/trainers `                    |	✅ |
| 10     | **Get Unassigned Trainers**          | `GET`    | `	/api/v1/trainees/{traineeUsername}/unassigned-trainers ` |	✅ |
| 12, 13 | **Get Trainings List**               | `GET`    | `	/api/v1/{type}s/{username}/trainings `                   |	✅ |
| 14     | **Add Training**                     | `POST`    | `	/api/v1/trainings `                   |	✅|

---

### 🛡️ Engineering Excellence
1. **Security**: Authentication relies on **Spring Security** and **BCrypt** hashing for password storage.

2. **Traceability (AOP)**: A unique **Transaction ID (TID)** is generated (via `LoggingAspect`) and logged for every REST request, enabling end-to-end tracing.

3. **Error Handling**: Custom exceptions are centrally managed by `GlobalExceptionHandler`, ensuring predictable and standardized HTTP status responses.

4. **Code Quality**: Adherence to SOLID principles, DTO validation via **Jakarta Validation**, and efficient object mapping using **MapStruct**.

## Task
### Spring Boot
**1. Based on the codebase created during the previous module, implement follow functionality:**

1) Convert existing application to be `Spring boot Application`.

2) Enable `actuator`. 
* Implement a few custom `health indicators`.
* Implement a few custom metrics using `Prometheus`.
3) Implement support for different environments (`local`, `dev`, `stg`, `prod`). Use Spring profiles.

### Notes:
1. Cover code with unit tests. Code should contain proper logging.
2. Pay attention that each environment - different db properties.
3. All functions except Create Trainer/Trainee profile. Should be executed only after Trainee/Trainer authentication (on this step should be checked
username and password matching). 