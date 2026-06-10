# UTC2 Web Server (Backend)

**UTC2 Web Server** is the central Backend (Core System) serving the application ecosystem of the University of Transport and Communications, Campus in Ho Chi Minh City (UTC2).

This system is built on the **Java Spring Boot 3** platform, acting as the backbone connecting the entire data flow between the students' mobile devices (**Mobile App**) and the university's administration dashboard (**Web Admin**).

**Related repositories:**
- [UTC2_App_Reborn](https://github.com/Piu-Empire/UTC2_App_Reborn) — Mobile App for Students
- [UTC2_Web_Admin](https://github.com/Piu-Empire/UTC2_Web_Admin) — Web Admin Dashboard

---

## Role Analysis in the Ecosystem

The Backend performs 3 core responsibilities:
1. **Business Logic Hub:** Controls the integrity of all data flows communicating between the Mobile App and the Web Admin before saving them to the Database.
2. **Notification Dispatcher:** Communicates with Firebase FCM to automatically push notifications to student phones upon updates from the university (approving forms, tuition payments).
3. **AI Core:** Processes Semantic Search via LangChain4j, helping to secure API Keys and offload AI processing from the Mobile App.

---

## Role-Based Access Control Analysis

The system utilizes Spring Security & JWT to secure and divide 4 specialized permission groups (Roles):

- **STUDENT:** Uses the Mobile App. Only has permission to view personal data (grades, schedules), submit public service forms, pay tuition fees, and interact with the AI. Cannot view other users' data.
- **ADMIN:** Uses the Web Admin. Possesses the highest privileges, administers the entire system, configures the application, issues and manages other system accounts.
- **STAFF:** Uses the Web Admin for operational tasks. The STAFF role is further divided into detailed management Levels:
  - `Level 1 (Class)`: Participates in evaluating and managing class-level training points.
  - `Level 3 (Department)` / `Level 4 (Faculty)`: Manages academic results within the scope of the respective faculty or department.
  - `Level 5 (School Level)`: The highest operational privilege, responsible for approving Public Services (card issuance, confirmations), managing Dormitories, and performing mass Excel Data Import/Export (grades, tuition).
- **ADVISOR:** Tracks academic results, assesses training points, and manages academic warnings for the list of students in their assigned classes.

---

## Architecture Diagram / ERD

| System Architecture | Database Diagram (ERD) |
| :---: | :---: |
| ![Architecture](docs/images/arch.png) | ![ERD](docs/images/erd.png) |

*(Note: You can add architecture and ERD diagram images to the `docs/images/` directory to display them on Github)*

---

## Key Features & Modules

The Backend provides a set of powerful RESTful APIs, divided into independent modules that closely align with actual business operations:

1. **Auth & Security (`auth`, `security`):** Manages login, token issuance (JWT), password encryption, and password recovery.
2. **Academic Management (`academic`, `schedule`, `assessment`):** 
   - Manages grades, calculates average GPA, and automatically issues academic warnings.
   - Processes and returns class and exam schedules for specific students.
   - Calculates and stores training points.
3. **Financial Management (`finance`):** Creates and tracks invoices, manages tuition debt for subjects and dormitory accommodations.
4. **Online Public Services (`public_services`):** Stores application forms (Card re-issuance, Student loans, Transcripts). Updates and returns form statuses (Pending -> Approved/Rejected).
5. **Dormitory (`dormitory`):** Handles logic for room allocation, bed assignment, and student accommodation registration.
6. **Course Registration (`enrollment`):** Manages opening classes, limiting student registration numbers, and handling schedule conflict logic to ensure safe credit registration.
7. **Import/Export (`imports`, `export`):** Processes mass Excel files from the Web Admin to import large datasets (thousands of students/grades) and automatically exports reports (PDF, Excel) with optimized resources.

---

## Tech Stack

- **Language:** Java 21
- **Core Framework:** Spring Boot 3
- **Database:** MySQL 8+
- **Security:** Spring Security, JWT (JSON Web Token)
- **Extended Integrations:** 
  - **Firebase Admin SDK** (Push Notification)
  - **LangChain4j** (AI Chat / Semantic Search)
- **ORM:** Spring Data JPA / Hibernate
- **Build Tool:** Maven 3.8+
- **Deployment:** Docker & Docker Compose
- **API Documentation:** Swagger UI / OpenAPI 3

---

## Installation & Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Piu-Empire/UTC2_Web_Server.git
   ```
2. **Initialize the Database (MySQL):**
   ```sql
   CREATE DATABASE utc2_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. **Configure the environment (`.env` or `application.properties`):**
   - Set `spring.datasource.username` and `password`.
   - Set `app.jwt.secret` (the secret string used to sign JWTs).
   - Provide the `firebase-service-account.json` configuration file (if you want to test Push Notifications).
   - Set API Keys for the AI Chat module (OpenAI/Gemini).
4. **Run the application (Using Maven Wrapper):**
   ```bash
   ./mvnw spring-boot:run
   ```
   *(Supports quick launch via Docker: `docker-compose up -d`)*
5. **Access API documentation (Swagger UI):**
   - Open a browser and navigate to: `http://localhost:8080/swagger-ui.html`

---

## Deployment

The project is pre-configured for easy deployment to Cloud services:
- **Web Server (Spring Boot):** Practically deployed on the **Render** platform as a Web Service. The source code already includes a `Dockerfile` compatible with Render.
- **Database (MySQL):** The database is entirely hosted on **Railway**, providing a secure Connection URI to connect with the Server on Render.

---

## Source Code Architecture

The project applies the standard Layered Architecture of Spring Boot, making the system easy to maintain and scale in the long run:

```text
com.utc2.appreborn.backend
├── common              (Constants, Enums, Utils, shared DTO/Response structures)
├── config              (System configurations: Security, CORS, Swagger, Database, Firebase)
├── exception           (Centralized exception handling - GlobalExceptionHandler)
├── middleware          (Filters, Interceptors catching and processing requests before reaching the Controller)
├── security            (JWT authentication logic, Custom UserDetails)
└── modules             (Core area - Divided by Domain Driven Design)
    ├── academic        (Academic results, classes)
    ├── aichat          (AI/Langchain4j integration)
    ├── assessment      (Training point assessment)
    ├── auth            (Login, Password recovery)
    ├── dormitory       (Dormitory)
    ├── enrollment      (Course registration)
    ├── finance         (Tuition, Invoices)
    ├── imports         (Excel file import processing from Admin)
    ├── export          (Excel/PDF file export processing)
    ├── notification    (Sending Push Notifications via Firebase)
    ├── public_services (Public administrative services)
    └── schedule        (Schedules)
```
