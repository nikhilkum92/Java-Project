# CampusGrid — Academic Space & User Management Platform
> **Branch:** `priyanshu` | **Current Milestone:** Module 1 (Authentication, Authorization & User Management)

Welcome to **CampusGrid**! This guide details how to clone/pull this branch, set up your local development environment, configure the database and security variables, and run and test the project seamlessly.

---

## 1. Prerequisites

Before running the project, ensure you have installed:

| Prerequisite | Recommended Version | Verification Command |
| :--- | :--- | :--- |
| **Java Development Kit (JDK)** | **JDK 21** (or higher) | `java -version` |
| **Apache Maven** | **3.8+** / **3.9+** | `mvn -version` |
| **MySQL Database Server** | **8.0+** | `mysql --version` |
| **Git** | **2.30+** | `git --version` |
| **IDE** (optional) | IntelliJ IDEA / Eclipse / VS Code | — |

---

## 2. Cloning & Pulling the `priyanshu` Branch

### Option A: If you are setting up the repository from scratch
```bash
# 1. Clone the repository
git clone https://github.com/nikhilkum92/Java-Project.git

# 2. Enter the repository directory
cd Java-Project

# 3. Check out the 'priyanshu' branch
git checkout priyanshu

# 4. Pull the latest commits
git pull origin priyanshu
```

### Option B: If you already have the repository cloned
```bash
# Fetch all remote branches and objects
git fetch origin

# Switch to the priyanshu branch
git checkout priyanshu

# Pull the latest changes
git pull origin priyanshu
```

---

## 3. Database Configuration

CampusGrid uses MySQL for persistence with Spring Data JPA / Hibernate.

### Step 1: Create the MySQL Database
Open MySQL Workbench or your terminal shell (`mysql -u root -p`) and create the database:
```sql
CREATE DATABASE IF NOT EXISTS campusgrid CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Step 2: Configure Credentials in `application.properties`
Open `src/main/resources/application.properties` and verify or adjust your credentials:
```properties
# MySQL Connection
spring.datasource.url=jdbc:mysql://localhost:3306/campusgrid?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate Schema Auto-Creation
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```
> **Note:** With `spring.jpa.hibernate.ddl-auto=update`, Hibernate will automatically generate all necessary tables (`users`, `students`, `faculty`, `user_sessions`) on the first application startup.

---

## 4. Environment Variables & Security Setup

CampusGrid protects JWT signing keys through an environment variable to prevent credentials from being hardcoded.

Set `CAMPUSGRID_JWT_SECRET` in your terminal session before starting the application:

### On Windows (PowerShell):
```powershell
$env:CAMPUSGRID_JWT_SECRET = "CampusGridSecureSecretSigningKey2026AtLeast32Bytes"
```

### On Windows (CMD):
```cmd
set CAMPUSGRID_JWT_SECRET=CampusGridSecureSecretSigningKey2026AtLeast32Bytes
```

### On macOS / Linux (Bash or Zsh):
```bash
export CAMPUSGRID_JWT_SECRET="CampusGridSecureSecretSigningKey2026AtLeast32Bytes"
```

> **Requirement:** The signing secret must be at least 32 characters (256 bits) to satisfy HMAC-SHA standards.

---

## 5. Building & Running the Application

### Step 1: Run Automated Tests
Verify that all unit and security tests pass locally:
```bash
mvn clean test
```
*Expected output: `BUILD SUCCESS` with 27 passing tests (0 failures, 0 errors).*

### Step 2: Build the JAR Package
```bash
mvn clean package -DskipTests
```
*This generates `target/campusgrid-core-1.0.0.jar`.*

### Step 3: Run the Application
You can run the app using Maven:
```bash
mvn spring-boot:run
```
Or directly using the packaged JAR:
```bash
java -jar target/campusgrid-core-1.0.0.jar
```

The server starts on port `8080`.

---

## 6. Accessing the Web Portal & Default Credentials

Open your web browser and navigate to:
**[http://localhost:8080/login](http://localhost:8080/login)**

On first launch, the built-in database seeder populates the initial default test users:

| Role | Username | Password | Accessible Portal / Views |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` | `Admin@123` | `/admin/users` (User Management, CSV Import/Export, Lifecycle) |
| **Faculty** | `meera.faculty` | `Faculty@123` | `/faculty/dashboard` (Faculty Portal) |
| **Student** | `rahul123` | `Student@123` | `/student/dashboard` (Student Portal) |

---

## 7. Project Architecture Overview

```text
Java-Project/
├── pom.xml                                  # Maven dependencies (Spring Boot 3, JJWT, MySQL, Validation)
├── README.md                                # Setup & configuration instructions
└── src/
    ├── main/
    │   ├── java/com/campusgrid/
    │   │   ├── CampusGridApplication.java   # Spring Boot entry point & initial user seeder
    │   │   ├── config/                      # WebMvcConfigurer, CORS, and JwtAuthFilter
    │   │   ├── controller/                  # REST controllers (AuthController, UserManagementController)
    │   │   ├── dto/                         # Request/Response data transfer objects
    │   │   ├── model/                       # Entities (User, Student, Faculty, UserSession, Role)
    │   │   ├── repository/                  # Spring Data JPA repositories
    │   │   └── service/                     # Business logic (AuthService, JwtService, UserService)
    │   └── resources/
    │       ├── application.properties       # Server, MySQL, and JWT configuration
    │       └── static/                      # Static assets (HTML views, CSS styles, JavaScript controllers)
    └── test/                                # 27 automated tests covering Auth, JWT, Users, CSV, and Security
```

---

## 8. Common Troubleshooting

1. **MySQL Connection Refused / Access Denied:**
   - Ensure the MySQL service is actively running (`Get-Service MySQL*` or `services.msc`).
   - Double-check the password in `application.properties`.
   - Ensure the `campusgrid` database has been created.

2. **JWT Secret Missing:**
   - If you encounter a startup or token verification error regarding JWT keys, confirm you exported or set `$env:CAMPUSGRID_JWT_SECRET` in the active terminal window before running the app.

3. **Port 8080 Already in Use:**
   - Change `server.port=8080` in `src/main/resources/application.properties` to an open port (e.g., `8081`), or terminate the process holding port 8080 (`netstat -ano | findstr :8080`).

---

## 9. Branch Contribution Guidelines

- Keep all changes on your assigned feature branches.
- Always run `mvn test` before committing to ensure that no existing security or regression checks break.
- Preserve CSV formula injection guards when modifying user import/export routines.