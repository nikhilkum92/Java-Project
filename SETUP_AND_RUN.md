# CampusGrid — Project Setup & Execution Guide
> **Branch:** `priyanshu` | **Current Milestone:** Module 1 (Authentication, Authorization & User Management)

This document provides complete instructions for developers and collaborators to clone/pull this branch, configure their local development environment, and run the CampusGrid platform.

---

## 1. Prerequisites

Before running the application, make sure you have the following installed:

| Requirement | Recommended Version | Verification Command |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | **JDK 21** or later | `java -version` |
| **Apache Maven** | **3.8+** / **3.9+** | `mvn -version` |
| **MySQL Server** | **8.0+** | `mysql --version` |
| **Git** | **2.30+** | `git --version` |

---

## 2. Cloning & Pulling the `priyanshu` Branch

### Option A: If setting up the project for the first time
```bash
# 1. Clone the repository
git clone https://github.com/nikhilkum92/Java-Project.git

# 2. Navigate to the project directory
cd Java-Project

# 3. Check out the priyanshu branch
git checkout priyanshu

# 4. Pull the latest commits
git pull origin priyanshu
```

### Option B: If you already have the repository cloned
```bash
# Fetch latest remote references
git fetch origin

# Switch to the priyanshu branch
git checkout priyanshu

# Pull latest changes
git pull origin priyanshu
```

---

## 3. Database Configuration

CampusGrid uses MySQL for persistence managed via Spring Data JPA / Hibernate.

### Step 1: Create the Database
In your MySQL command line client or workbench, create the database:
```sql
CREATE DATABASE IF NOT EXISTS campusgrid CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Configure Credentials
Open `src/main/resources/application.properties` and verify your MySQL connection settings:
```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/campusgrid?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Settings
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```
> **Note:** Hibernate automatically creates and updates the database schema (`users`, `students`, `faculty`, `user_sessions`) on initial startup.

---

## 4. Environment Variables & Security Setup

CampusGrid reads the JWT HMAC secret from the environment to avoid hardcoding secrets into source control.

Set `CAMPUSGRID_JWT_SECRET` in your terminal prior to running the application:

### Windows (PowerShell):
```powershell
$env:CAMPUSGRID_JWT_SECRET = "CampusGridSecureSecretSigningKey2026AtLeast32Bytes"
```

### Windows (Command Prompt):
```cmd
set CAMPUSGRID_JWT_SECRET=CampusGridSecureSecretSigningKey2026AtLeast32Bytes
```

### Linux / macOS (Bash or Zsh):
```bash
export CAMPUSGRID_JWT_SECRET="CampusGridSecureSecretSigningKey2026AtLeast32Bytes"
```

> **Requirement:** The signing secret must be at least 32 characters long (256 bits).

---

## 5. Building & Running the Project

### Step 1: Run Automated Tests
```bash
mvn clean test
```
*Expected: `BUILD SUCCESS` with 27 passed tests (0 failures, 0 errors).*

### Step 2: Build the JAR Package
```bash
mvn clean package -DskipTests
```
*Produces `target/campusgrid-core-1.0.0.jar`.*

### Step 3: Start the Application
Run via Maven:
```bash
mvn spring-boot:run
```
Or run the packaged JAR:
```bash
java -jar target/campusgrid-core-1.0.0.jar
```

The application runs at **`http://localhost:8080`**.

---

## 6. Web Portal & Default Test Credentials

Open your web browser and navigate to:
**[http://localhost:8080/login](http://localhost:8080/login)**

The database seeder automatically initializes the following test accounts:

| Role | Username | Password | Accessible Views / Features |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | `/admin/users` (User Management, CSV Import/Export, Status Control) |
| **Faculty** | `meera.faculty` | `Faculty@123` | `/faculty/dashboard` (Faculty Portal) |
| **Student** | `rahul123` | `Student@123` | `/student/dashboard` (Student Portal) |

---

## 7. Project Architecture Overview

```text
Java-Project/
├── pom.xml                                  # Maven dependencies (Spring Boot 3, JJWT, MySQL, Validation)
├── README.md                                # Repository root README
├── SETUP_AND_RUN.md                         # Project setup and developer instructions (this file)
└── src/
    ├── main/
    │   ├── java/com/campusgrid/
    │   │   ├── CampusGridApplication.java   # Application entry point & initial user seeder
    │   │   ├── config/                      # WebMvcConfigurer, CORS, and JwtAuthFilter
    │   │   ├── controller/                  # REST controllers (AuthController, UserManagementController)
    │   │   ├── dto/                         # Request/Response data transfer objects
    │   │   ├── model/                       # JPA entities (User, Student, Faculty, UserSession, Role)
    │   │   ├── repository/                  # Spring Data JPA repositories
    │   │   └── service/                     # Business logic (AuthService, JwtService, UserService)
    │   └── resources/
    │       ├── application.properties       # Server, MySQL, and JWT configuration
    │       └── static/                      # Frontend views (HTML), styles (CSS), client scripts (JS)
    └── test/                                # 27 automated tests covering Auth, JWT, Users, CSV, and Security
```

---

## 8. Troubleshooting

1. **MySQL Connection Error:**
   - Verify that your MySQL server is running.
   - Confirm that the `campusgrid` database has been created.
   - Verify the password in `src/main/resources/application.properties`.

2. **Missing JWT Secret Error:**
   - If you encounter a JWT secret validation exception on startup, confirm that you set the environment variable `$env:CAMPUSGRID_JWT_SECRET` in your active terminal session.

3. **Port 8080 Conflict:**
   - If port 8080 is in use, modify `server.port=8080` in `application.properties` to an alternate port (e.g. `8081`).
