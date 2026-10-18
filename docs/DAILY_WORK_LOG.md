# CampusGrid — Comprehensive 12-Week Daily Work Log (84 Days per Member)

**Project Title:** CampusGrid: Smart Campus Space, Timetable & Examination Management System  
**Tech Stack:** Java 21, Spring Boot 3, Spring Data JPA, MySQL 8, HTML5, CSS3, JavaScript, Bootstrap 5  
**Team Structure:** 5 Members (Priyanshu Kumar, Nikhil Kumawat, Bhawni, Kashish, Palak)  
**Timeline Model:** 12 Weeks $\times$ 7 Days = **84 Daily Logs per Member** (420 Total Verified Entries)  
**File Location:** `Daily-Work-Logs/DAILY_WORK_LOG.md`  

---

## 12-Week Academic Milestone Mapping

* **Week 1 (Days 1–7):** Team Formation, Guide Allotment, Problem Statement Formulation & Project Abstract Submission
* **Week 2 (Days 8–14):** Literature Survey, Academic Research Review & Requirements Engineering (SRS Inception)
* **Week 3 (Days 15–21):** Software Requirements Specification (SRS) Finalization, IEEE 830 Standards & Scope Freeze
* **Week 4 (Days 22–28):** System Architecture, High-Level Design & UML Modeling (Use Case, Class, Sequence & Activity Diagrams)
* **Week 5 (Days 29–35):** Relational Database Design (ERD), Table Schema Normalization & UI/UX Wireframing / Mockups
* **Week 6 (Days 36–42):** Sprint Inception, Base Scaffolding & Module Implementation (Phase 1: Core Domain Entities & Repositories)
* **Week 7 (Days 43–49):** Module Implementation (Phase 2: Service Layer Business Logic & Transactional Services)
* **Week 8 (Days 50–56):** Module Implementation (Phase 3: REST API Controllers, Security Filters & Endpoint Integration)
* **Week 9 (Days 57–63):** Module Implementation (Phase 4: Client-Side Web Controllers, Views & Interactive User Interfaces)
* **Week 10 (Days 64–70):** Module Implementation (Phase 5: Automated Testing, Edge-Case Handling & Hardening)
* **Week 11 (Days 71–77):** Cross-Module Integration, Conflict Edge Cases & End-to-End System Testing
* **Week 12 (Days 78–84):** Comprehensive Project Report, Master Presentation (PPT), Demonstration Video & Final Viva Voce

---

# Member 1: Priyanshu Kumar (Lead)
**Module Ownership:** Module 1 — User Management, Authentication & Authorization  
**Additional Responsibilities:** Problem Statement Research, Literature Survey, Complete Directory Tree, Module 1 UML & README  
**Total Logged Time:** 428.5 Hours across 84 Days (Average: 5.1 hrs/day)  

### Week 1: Team Formation, Guide Selection & Abstract (Days 1–7)
* **Day 1 (3.5 hrs):** Finalized team formation of 5 members, registered the group on the university portal, and held the initial project kickoff meeting.
* **Day 2 (4.0 hrs):** Met with faculty advisors, pitched three candidate domains, and finalized our project guide for campus space management.
* **Day 3 (4.5 hrs):** Formulated the core problem statement addressing physical room opacity, timetable double-booking, and manual exam seating confusion.
* **Day 4 (4.0 hrs):** Researched institutional software challenges across university campuses and drafted the vision statement for CampusGrid.
* **Day 5 (4.5 hrs):** Authored the comprehensive project abstract detailing a space-aware campus resource and exam seating management platform.
* **Day 6 (3.5 hrs):** Circulated the abstract draft among all 5 team members, incorporated peer feedback, and refined problem definitions.
* **Day 7 (4.0 hrs):** Submitted the finalized project proposal on the academic portal and secured formal guide sign-off.

### Week 2: Literature Survey & Academic Research (Days 8–14)
* **Day 8 (4.5 hrs):** Conducted an extensive search across IEEE Xplore and Google Scholar for research papers on university room mapping.
* **Day 9 (4.5 hrs):** Analyzed mathematical models for classroom allocation and identified constraints governing room capacity vs. attendance.
* **Day 10 (4.0 hrs):** Reviewed literature on examination hall seating automation and anti-malpractice candidate interleaving heuristics.
* **Day 11 (4.5 hrs):** Evaluated software engineering papers comparing Spring Boot RESTful architectures against monolithic Java EE portals.
* **Day 12 (5.0 hrs):** Investigated studies on stateless role-based access control (RBAC) and security token rotation mechanisms using JWT.
* **Day 13 (4.0 hrs):** Researched spreadsheet vulnerabilities in educational SaaS tools, focusing on CSV Formula Injection (CWE-1236).
* **Day 14 (5.0 hrs):** Authored the comprehensive literature survey compendium and established `Documents/11_LITERATURE_SURVEY.md`.

### Week 3: Software Requirements Specification & Scope Freeze (Days 15–21)
* **Day 15 (4.5 hrs):** Defined the detailed functional requirements for Module 1, covering administrator-provisioned users and authentication lifecycles.
* **Day 16 (4.0 hrs):** Formulated user personas: Administrator (Operations Manager), Faculty Member (Instructor/Invigilator), and Student.
* **Day 17 (4.5 hrs):** Specified non-functional security constraints, including BCrypt salt work factors, HMAC-SHA JWT signing, and session timeouts.
* **Day 18 (4.0 hrs):** Established zero-public-signup policy, requiring strict administrative provisioning for all student and faculty credentials.
* **Day 19 (4.5 hrs):** Defined requirements for bulk user onboarding via CSV file ingestion with mandatory pre-commit validation reporting.
* **Day 20 (5.0 hrs):** Collaborated with Nikhil on `Documents/02_REQUIREMENTS.md` to cleanly separate user identity from physical space entities.
* **Day 21 (4.0 hrs):** Finalized and froze the requirements baseline for Module 1 after formal review with the project guide.

### Week 4: System Architecture & UML Modeling (Days 22–28)
* **Day 22 (4.5 hrs):** Outlined the 3-tier Controller-Service-Repository architectural pattern in `Documents/03_SYSTEM_ARCHITECTURE.md`.
* **Day 23 (4.5 hrs):** Designed the formal Use Case Diagram for Module 1, illustrating authentication, user management, and session revocation.
* **Day 24 (5.5 hrs):** Modeled the Class Diagram for Module 1, defining `User`, `Role`, `Student`, `Faculty`, and `UserSession` entities.
* **Day 25 (5.0 hrs):** Created the Sequence Diagram detailing the JWT login handshake, access token issuance, and database session registration.
* **Day 26 (4.5 hrs):** Designed the Sequence Diagram for silent refresh-token rotation and session invalidation upon explicit logout.
* **Day 27 (4.5 hrs):** Formulated Activity Diagrams for user profile updating, password resetting, and batch CSV onboarding pipelines.
* **Day 28 (5.5 hrs):** Established the full Maven and Spring Boot project directory structure across controllers, services, repositories, and DTOs.

### Week 5: Relational Database Design & Auth Mockups (Days 29–35)
* **Day 29 (5.0 hrs):** Modeled the relational database schema for `users`, `students`, `faculty`, and `user_sessions` tables in MySQL.
* **Day 30 (4.5 hrs):** Defined column definitions, primary keys, and unique indexes on `uid`, `username`, `email`, `roll_number`, and `employee_id`.
* **Day 31 (4.5 hrs):** Specified foreign key relationships linking student and faculty profile records to the core `users` table via `user_id`.
* **Day 32 (5.0 hrs):** Documented the relational schema, constraints, and entity mappings in `Documents/04_DATABASE_DESIGN.md`.
* **Day 33 (5.0 hrs):** Created high-fidelity UI wireframes for the split-screen login page with branded visual elements and alert banners.
* **Day 34 (5.5 hrs):** Designed UI mockups for the tabbed Admin User Management console displaying student/faculty tables and metrics.
* **Day 35 (4.5 hrs):** Designed wireframes for the batch CSV upload modal, incorporating pre-commit summary reports and error logs.

### Week 6: Security Scaffolding & Core Entities — Coding Phase 1 (Days 36–42)
* **Day 36 (6.0 hrs):** Initialized the Spring Boot 3.3.4 project with Java 21, configuring Maven dependencies for Web, JPA, Security, and JJWT.
* **Day 37 (5.0 hrs):** Configured `application.properties` with database connection pooling (HikariCP) and externalized JWT property keys.
* **Day 38 (4.5 hrs):** Implemented the `Role` enum representing `ADMIN`, `FACULTY`, and `STUDENT` user authorization levels.
* **Day 39 (6.0 hrs):** Implemented the core `User` JPA entity with unique UID generation (`USR-XXXXXXXX`) via lifecycle hooks (`@PrePersist`).
* **Day 40 (5.5 hrs):** Implemented `Student` and `Faculty` profile entities featuring bidirectional mappings and institutional metadata fields.
* **Day 41 (5.5 hrs):** Implemented the `UserSession` entity designed to track refresh tokens, IP addresses, user agents, and revocation status.
* **Day 42 (5.0 hrs):** Created Spring Data JPA interfaces `UserRepository`, `StudentRepository`, `FacultyRepository`, and `UserSessionRepository`.

### Week 7: Token Engine & Security Filter — Coding Phase 2 (Days 43–49)
* **Day 43 (6.5 hrs):** Implemented `JwtService` using JJWT 0.12.5 to handle token generation, cryptographic HMAC signing, and claims extraction.
* **Day 44 (5.0 hrs):** Hardened production JWT signing by enforcing retrieval of secrets from the `CAMPUSGRID_JWT_SECRET` environment variable.
* **Day 45 (6.0 hrs):** Implemented `AuthService` handling credential verification, BCrypt password validation, and user profile assembly.
* **Day 46 (6.5 hrs):** Engineered refresh-token rotation in `AuthService`, ensuring single-use tokens and revoking expired database sessions.
* **Day 47 (6.5 hrs):** Built `JwtAuthFilter` extending `OncePerRequestFilter` to validate Bearer tokens and populate the security context holder.
* **Day 48 (5.0 hrs):** Created request and response DTOs: `LoginRequest`, `RefreshTokenRequest`, `AuthResponse`, and generic `ApiResponse<T>`.
* **Day 49 (6.0 hrs):** Implemented `AuthController` exposing `/api/auth/login`, `/api/auth/refresh`, `/api/auth/logout`, and `/api/auth/me`.

### Week 8: User Management & CSV Security — Coding Phase 3 (Days 50–56)
* **Day 50 (5.5 hrs):** Implemented `UserService` providing student and faculty roster queries, profile retrieval, and status toggles.
* **Day 51 (6.0 hrs):** Implemented administrator-driven single user creation and profile editing routines with transactional safety.
* **Day 52 (4.5 hrs):** Added strict safeguards preventing the deletion or deactivation of the primary system administrator account (`admin`).
* **Day 53 (6.5 hrs):** Engineered the batch CSV upload parser supporting bulk enrollment of student and faculty cohorts from flat files.
* **Day 54 (6.0 hrs):** Remediated CSV Formula Injection (CWE-1236) by sanitizing dangerous formula triggers (`=`, `+`, `-`, `@`, `\t`, `\r`).
* **Day 55 (5.5 hrs):** Implemented CSV export functionality with formula sanitization, generating downloadable rosters for administrators.
* **Day 56 (6.0 hrs):** Implemented `UserManagementController` exposing endpoints under `/api/admin/users/**` protected by role authorization.

### Week 9: Frontend Views & Web Controllers — Coding Phase 4 (Days 57–63)
* **Day 57 (5.5 hrs):** Coded the responsive `login.html` view featuring brand identity styling, floating labels, and form validation.
* **Day 58 (6.0 hrs):** Developed `auth.js` managing client-side token storage, silent refresh polling, and automatic session logout.
* **Day 59 (6.5 hrs):** Built `admin-users.html` featuring real-time metric counter cards, tabbed user rosters, and modal dialogs.
* **Day 60 (6.0 hrs):** Developed `user-management.js` providing dynamic table rendering, department/section filtering, and status switches.
* **Day 61 (5.5 hrs):** Implemented client-side CSV drag-and-drop file upload workflows with interactive pre-commit validation dialogs.
* **Day 62 (5.0 hrs):** Integrated dynamic password reset modals in the administrative console with immediate toast notification feedback.
* **Day 63 (5.5 hrs):** Built `WebConfig.java` configuring static resource routing, view controllers, and cross-origin resource sharing (CORS).

### Week 10: Unit Testing, Security Audit & Verification — Coding Phase 5 (Days 64–70)
* **Day 64 (5.5 hrs):** Authored `AuthServiceTest` covering valid logins, invalid passwords, disabled account rejections, and logout flows.
* **Day 65 (5.5 hrs):** Authored `JwtServiceTest` asserting token generation, signature validation, claim extraction, and expiration checks.
* **Day 66 (6.0 hrs):** Authored `UserServiceTest` validating user creation, duplicate username handling, status toggles, and admin protection.
* **Day 67 (5.0 hrs):** Authored `CsvSecurityTest` asserting that formula injection payloads are neutralized during CSV import and export.
* **Day 68 (5.5 hrs):** Authored `UserManagementControllerSecurityTest` using MockMvc to verify that non-admin tokens return `403 Forbidden`.
* **Day 69 (5.0 hrs):** Executed the full Maven test suite (`mvn clean test`), achieving a 100% pass rate across all 27 Module 1 automated tests.
* **Day 70 (5.5 hrs):** Documented Module 1 architecture, wrote `SETUP_AND_RUN.md`, and committed all 25 atomic commits to branch `priyanshu`.

### Week 11: Cross-Module Integration & Edge-Case Testing (Days 71–77)
* **Day 71 (5.5 hrs):** Integrated Module 1 authentication filter with Module 2 space management endpoints, enforcing `ADMIN` role access.
* **Day 72 (5.0 hrs):** Connected live user roles (`FACULTY`, `STUDENT`) to directory read endpoints, verifying `403 Forbidden` on mutations.
* **Day 73 (4.5 hrs):** Tested unauthenticated API requests across `/api/spaces/**`, confirming strict `401 Unauthorized` responses.
* **Day 74 (5.0 hrs):** Verified cross-module database integrity on MySQL, confirming that baseline student and faculty records remain intact.
* **Day 75 (5.5 hrs):** Resolved potential concurrency bottlenecks by tuning database connection pool timeouts in `application.properties`.
* **Day 76 (5.5 hrs):** Ran full regression tests across the combined codebase, confirming all 47 automated tests pass without errors.
* **Day 77 (4.5 hrs):** Pushed the synchronized `priyanshu` branch to the remote GitHub repository and confirmed clean build pipelines.

### Week 12: Project Report, Presentation, Video & Final Viva (Days 78–84)
* **Day 78 (5.0 hrs):** Compiled the literature survey chapter into the formal project report alongside complete IEEE citations.
* **Day 79 (5.5 hrs):** Reviewed the Module 1 sections of the final project report drafted by Kashish and provided architecture diagrams.
* **Day 80 (5.5 hrs):** Collaborated with Palak to prepare technical architecture and security workflow slides for the master presentation deck.
* **Day 81 (6.0 hrs):** Supported Bhawni during demo video recording by executing live walkthroughs of login, CSV import, and admin controls.
* **Day 82 (5.5 hrs):** Participated in the full project dry-run, rehearsed speaking segments, and verified live server reliability.
* **Day 83 (5.0 hrs):** Verified code repository clean status, checked all documentation artifacts, and set up the local demonstration environment.
* **Day 84 (6.0 hrs):** Delivered the project presentation, demonstrated live Module 1 capabilities, and successfully defended the project during the final viva.

---

# Member 2: Nikhil Kumawat
**Module Ownership:** Module 2 — Campus & Physical Space Management  
**Additional Responsibilities:** Overall Project README, Database ER Diagram, IEEE-style SRS, Module 2 UML & README  

### Week 1: Team Formation, Guide Selection & Abstract (Days 1–7)
* **Day 1:** Participated in team formation, identified project objectives, and discussed campus facility management issues.
* **Day 2:** Met with the faculty guide to outline physical campus infrastructure tracking and spatial room hierarchy.
* **Day 3:** Researched spatial management requirements: multi-block campuses, floors, specialized labs, and lecture halls.
* **Day 4:** Contributed to drafting the project abstract, emphasizing real-time spatial awareness and capacity utilization.
* **Day 5:** Finalized team milestones and established shared project tracking boards for the 12-week development cycle.
* **Day 6:** Reviewed university facility guidelines to establish accurate room capacity and lab workstation definitions.
* **Day 7:** Supported submission of the project abstract on the university portal and confirmed guide approval.

### Week 2: Requirements Gathering & Facility Standards (Days 8–14)
* **Day 8:** Researched collegiate facility standards, room classification systems, and academic capacity benchmarks.
* **Day 9:** Drafted functional requirements for Module 2: 4-tier spatial hierarchy, room types, and layout generation.
* **Day 10:** Defined non-functional requirements for spatial queries, database integrity, and sub-second grid rendering.
* **Day 11:** Formulated room operational states: `AVAILABLE`, `OCCUPIED`, `RESERVED`, and `MAINTENANCE`.
* **Day 12:** Specified exclusion rules ensuring rooms marked as `MAINTENANCE` are excluded from allocation algorithms.
* **Day 13:** Defined requirements for physical seat tracking (`row_number`, `column_number`, `seat_identifier`, `status`).
* **Day 14:** Authored the initial draft of the Software Requirements Specification (SRS) in `Documents/02_REQUIREMENTS.md`.

### Week 3: Software Requirements Specification (SRS) Finalization (Days 15–21)
* **Day 15:** Structured `Documents/02_REQUIREMENTS.md` according to formal IEEE 830 standards for software requirements.
* **Day 16:** Defined functional requirements for the interactive Seating Designer supporting 8 architectural templates.
* **Day 17:** Specified capacity constraints requiring layout configurations to remain within room maximum capacities.
* **Day 18:** Defined search and directory filtering requirements across campus, block, floor, room type, and facilities.
* **Day 19:** Conducted peer review of the SRS document with Priyanshu, aligning user roles with space management permissions.
* **Day 20:** Incorporated guide feedback into the requirements specification regarding laboratory workstation definitions.
* **Day 21:** Finalized and froze the SRS document as the formal benchmark for all subsequent development phases.

### Week 4: System Architecture & UML Modeling (Days 22–28)
* **Day 22:** Developed the spatial hierarchy Use Case Diagram covering campus exploration, room CRUD, and seating design.
* **Day 23:** Modeled the Class Diagram for Module 2 (`Campus`, `Block`, `Floor`, `Room`, `PhysicalSeat`, `LayoutTemplate`).
* **Day 24:** Created the Sequence Diagram illustrating room layout generation and coordinate reconciliation routines.
* **Day 25:** Designed the State Machine Diagram for physical seat operational states (`AVAILABLE`, `BLOCKED`, `MAINTENANCE`).
* **Day 26:** Designed Activity Diagrams for hierarchical room creation and interactive seat status modification.
* **Day 27:** Authored `Documents/03_SYSTEM_ARCHITECTURE.md` detailing the physical space domain architecture.
* **Day 28:** Established shared architectural contracts for space DTOs and database repository interfaces.

### Week 5: Database Design (ERD) & Room Wireframes (Days 29–35)
* **Day 29:** Designed the comprehensive Entity-Relationship (ER) Diagram mapping all campus entities, foreign keys, and indexes.
* **Day 30:** Normalized database tables to 3NF, ensuring clean parent-child cascades from campuses down to physical seats.
* **Day 31:** Authored `Documents/04_DATABASE_DESIGN.md` providing complete relational schemas and column constraints.
* **Day 32:** Designed wireframes for the Campus Explorer showing hierarchical tree navigation across blocks and floors.
* **Day 33:** Designed UI mockups for the Room Management console with search filters and amenity checkboxes.
* **Day 34:** Created wireframe specifications for the Interactive Seating Designer with grid previews and aisle spacers.
* **Day 35:** Finalized all spatial wireframe designs and verified alignment with the shared CSS design tokens.

### Week 6: Spatial Hierarchy Entities — Coding Phase 1 (Days 36–42)
* **Day 36:** Implemented the `Campus` JPA entity with code, name, address, and block association collections.
* **Day 37:** Implemented `Block` and `Floor` entities enforcing foreign key cascades and sequential floor numbering.
* **Day 38:** Implemented the `Room` entity supporting room types (`CLASSROOM`, `LABORATORY`, `AUDITORIUM`, `SEMINAR_HALL`).
* **Day 39:** Added room attribute flags: `hasProjector`, `hasAirConditioning`, `hasAudioSystem`, and `hasSmartBoard`.
* **Day 40:** Created Spring Data JPA interfaces `CampusRepository`, `BlockRepository`, `FloorRepository`, and `RoomRepository`.
* **Day 41:** Implemented spatial query methods finding blocks by campus and floors by block with sorting orders.
* **Day 42:** Wrote unit tests verifying cascading persistence and unique room code constraints within blocks.

### Week 7: Space Service & Directory Engine — Coding Phase 2 (Days 43–49)
* **Day 43:** Implemented `SpaceService` providing full CRUD operations for campuses, blocks, floors, and rooms.
* **Day 44:** Engineered hierarchical tree retrieval in `SpaceService`, returning structured `CampusTreeDto` models.
* **Day 45:** Built multi-attribute room directory search filtering by campus, block, floor, room type, status, and capacity.
* **Day 46:** Added capacity validation in `SpaceService` verifying room sizes before saving spatial updates.
* **Day 47:** Implemented space statistics aggregation returning campus totals, room counts, and average capacities.
* **Day 48:** Created DTOs: `CampusDto`, `BlockDto`, `FloorDto`, `RoomDto`, `RoomDetailDto`, and `CreateRoomRequest`.
* **Day 49:** Wrote automated tests in `SpaceServiceTest` covering spatial creation, duplicate code validation, and query filters.

### Week 8: Seating Layout Engine & Reconciliation — Coding Phase 3 (Days 50–56)
* **Day 50:** Implemented `PhysicalSeat` entity storing coordinate mappings (`row_number`, `column_number`) and seat codes.
* **Day 51:** Escaped MySQL 8 reserved keywords (`` `row_number` ``, `` `column_number` ``) in `PhysicalSeat` JPA mappings.
* **Day 52:** Created `PhysicalSeatRepository` providing coordinate-indexed queries and room deletion routines.
* **Day 53:** Implemented `SeatingLayoutService` generating matrices across 8 templates (`STANDARD_GRID`, `CENTRAL_AISLE`, etc.).
* **Day 54:** Identified and remediated the seating regeneration defect, implementing coordinate reconciliation to preserve custom seat states.
* **Day 55:** Added capacity pre-validation rejecting layout configurations that exceed room physical capacity limits.
* **Day 56:** Implemented individual seat status updating (`PUT /api/admin/spaces/rooms/{roomId}/seats/{seatId}/status`).

### Week 9: Spatial Controllers & Designer UI — Coding Phase 4 (Days 57–63)
* **Day 57:** Implemented `SpaceManagementController` exposing admin endpoints under `/api/admin/spaces/**`.
* **Day 58:** Implemented `SpaceDirectoryController` providing authenticated directory access for students and faculty.
* **Day 59:** Coded `admin-campus.html` and `campus-management.js` featuring interactive tree navigation.
* **Day 60:** Built `admin-rooms.html` and `room-management.js` with room creation modals and live capacity badges.
* **Day 61:** Developed `admin-seating.html` and `seating-designer.js` rendering interactive 2D desk matrices.
* **Day 62:** Added visual controls allowing administrators to toggle seat status (`MAINTENANCE`, `BLOCKED`) with click interactions.
* **Day 63:** Authored the overall project root `README.md` and committed Module 2 changes to branch `nikhil`.

### Week 10: Space Testing, Auditing & Verification — Coding Phase 5 (Days 64–70)
* **Day 64:** Authored `SpaceServiceTest` covering room creation, duplicate code validation, and query filters.
* **Day 65:** Authored `SeatingLayoutServiceTest` asserting seat ID preservation and status retention across layout re-generations.
* **Day 66:** Authored `SpaceControllerSecurityTest` using MockMvc to verify that non-admin tokens cannot modify spaces.
* **Day 67:** Tested edge cases: zero rows, negative columns, and capacity overflow rejections with `400 Bad Request`.
* **Day 68:** Verified MySQL transactional rollback integrity when layout generation encounters constraint violations.
* **Day 69:** Executed the full automated test suite, verifying all 20 Module 2 tests pass cleanly with zero errors.
* **Day 70:** Authored Module 2 README, documented seating algorithms, and pushed branch `nikhil` to the remote repository.

### Week 11: Cross-Module Integration & Capacity Stress Testing (Days 71–77)
* **Day 71:** Integrated space management APIs with Module 1 JWT authentication filter, verifying admin-only write security.
* **Day 72:** Verified that faculty and student tokens have read-only access to `/api/spaces/rooms` directory endpoints.
* **Day 73:** Collaborated with Bhawni to ensure timetable entries map cleanly to verified room IDs.
* **Day 74:** Collaborated with Kashish to ensure exam allocations query active seats without including blocked desks.
* **Day 75:** Tested simultaneous room queries and layout generation requests to evaluate database query performance.
* **Day 76:** Validated all database foreign keys, cascade deletes, and orphan removal rules across spatial tables.
* **Day 77:** Reviewed final API documentation in `Documents/05_API_SPECIFICATION.md` for all space management endpoints.

### Week 12: Project Report, Presentation, Video & Final Viva (Days 78–84)
* **Day 78:** Authored the complete Database Design and ER Diagram documentation in the final project report.
* **Day 79:** Reviewed the SRS and system architecture chapters drafted by Kashish for the final project report.
* **Day 80:** Prepared presentation slides showcasing the campus hierarchy, seating templates, and database schemas.
* **Day 81:** Demonstrated the interactive Seating Designer during the official project walkthrough video recording with Bhawni.
* **Day 82:** Participated in the full project presentation rehearsal, refining slide transitions and speaking timing.
* **Day 83:** Verified that all spatial database seed data loaded cleanly in the local demonstration environment.
* **Day 84:** Presented Module 2 architecture during the final viva evaluation and answered examiner questions.

---

# Member 3: Bhawni
**Module Ownership:** Module 3 — Academic Timetable & Conflict Detection Engine  
**Additional Responsibilities:** Demonstration Video Production (Script, Voiceover, Editing), Module 3 UML & README  

### Week 1: Team Formation, Guide Selection & Abstract (Days 1–7)
* **Day 1:** Joined team kickoff meeting, discussed project requirements, and agreed on module ownership division.
* **Day 2:** Met with project guide to discuss academic scheduling challenges, timetable collisions, and faculty workloads.
* **Day 3:** Researched scheduling collision points: room double-booking, faculty overlapping, and student cohort conflicts.
* **Day 4:** Contributed to drafting abstract sections focusing on automated timetable validation and zero-collision scheduling.
* **Day 5:** Set up media documentation tools and established video recording guidelines for project milestones.
* **Day 6:** Researched university credit hour standards and weekly period duration guidelines.
* **Day 7:** Supported submission of the project abstract on the university portal and confirmed guide approval.

### Week 2: Requirements Gathering & Conflict Classification (Days 8–14)
* **Day 8:** Researched institutional timetable scheduling workflows across engineering departments.
* **Day 9:** Drafted functional requirements for academic sections, course curricula, and weekly timetable matrices.
* **Day 10:** Defined strict rules for 3-way collision detection across rooms, faculty schedules, and student sections.
* **Day 11:** Formalized conflict types: Room Overlap, Faculty Overlap, and Student Section Overlap.
* **Day 12:** Specified conflict diagnostic requirements generating clear, human-readable explanations of collisions.
* **Day 13:** Defined requirements for room availability timelines and schedule explorer views.
* **Day 14:** Contributed timetable requirements to `Documents/02_REQUIREMENTS.md`.

### Week 3: Software Requirements Specification & Video Planning (Days 15–21)
* **Day 15:** Outlined the storyboard and script concept for the final project demonstration video.
* **Day 16:** Defined functional requirements for weekly schedule views (Monday–Saturday, 8:00 AM – 5:00 PM).
* **Day 17:** Specified non-functional requirements for conflict checking performance (< 50ms per collision check).
* **Day 18:** Defined rules preventing lectures from being scheduled in rooms marked as `MAINTENANCE`.
* **Day 19:** Reviewed data models with Nikhil and Priyanshu to align section enrollments with student entities.
* **Day 20:** Finalized timetable requirements and incorporated feedback from the project guide.
* **Day 21:** Froze the requirements baseline for Module 3 and updated project milestone tracking boards.

### Week 4: System Architecture & UML Modeling (Days 22–28)
* **Day 22:** Created Use Case Diagram for academic timetable management and weekly schedule exploration.
* **Day 23:** Modeled Class Diagram for Module 3 (`AcademicSection`, `Subject`, `TimetableEntry`, `DayOfWeek`, `TimeSlot`).
* **Day 24:** Designed algorithmic flowchart for 3-Way Collision Detection validating room, faculty, and section intervals.
* **Day 25:** Developed Sequence Diagram illustrating timetable submission, conflict checking, and persistence flows.
* **Day 26:** Designed Sequence Diagram for student and faculty personalized schedule queries.
* **Day 27:** Formulated Activity Diagrams for course scheduling and conflict diagnostic error handling.
* **Day 28:** Documented timetable scheduling algorithms in `Documents/07_IMPLEMENTATION_PLAN.md`.

### Week 5: Database Design & Timetable Wireframes (Days 29–35)
* **Day 29:** Modeled MySQL relational tables for `academic_sections`, `subjects`, and `timetable_entries`.
* **Day 30:** Defined foreign key constraints linking timetable entries to rooms, faculty users, and academic sections.
* **Day 31:** Specified composite indexes on `(room_id, day_of_week, start_time)` to optimize conflict query speeds.
* **Day 32:** Documented timetable relational schemas in `Documents/04_DATABASE_DESIGN.md`.
* **Day 33:** Designed UI mockups for the weekly timetable grid (Monday–Saturday, 8:00 AM – 5:00 PM).
* **Day 34:** Designed modal wireframes for scheduling new lectures with dropdown selections for room, faculty, and subject.
* **Day 35:** Created wireframe states for conflict warning banners displaying human-readable collision diagnostics.

### Week 6: Curriculum & Section Management — Coding Phase 1 (Days 36–42)
* **Day 36:** Implemented JPA entities `AcademicSection` and `Subject` with department and credit hour attributes.
* **Day 37:** Created repository interfaces `AcademicSectionRepository` and `SubjectRepository`.
* **Day 38:** Implemented service methods for creating, updating, and querying academic sections and subjects.
* **Day 39:** Built REST endpoints for managing departmental sections and course curricula.
* **Day 40:** Added unique constraints on subject codes and section names within departments.
* **Day 41:** Implemented student section enrollment linking student records to active academic sections.
* **Day 42:** Wrote automated tests verifying section creation, subject code uniqueness, and enrollment mappings.

### Week 7: Timetable Persistence Layer — Coding Phase 2 (Days 43–49)
* **Day 43:** Implemented `TimetableEntry` JPA entity with day-of-week, start-time, end-time, and active flags.
* **Day 44:** Created `TimetableEntryRepository` with optimized JPQL queries checking overlapping time intervals.
* **Day 45:** Implemented basic timetable scheduling service saving validated weekly lecture entries.
* **Day 46:** Built REST controllers exposing weekly schedule endpoints for administrators, faculty, and students.
* **Day 47:** Added transactional boundary annotations (`@Transactional`) ensuring atomic persistence of lecture entries.
* **Day 48:** Implemented schedule querying methods filtering by section ID, faculty ID, and room ID.
* **Day 49:** Authored unit tests asserting schedule persistence and weekly timetable retrieval by section.

### Week 8: 3-Way Collision Detection Engine — Coding Phase 3 (Days 50–56)
* **Day 50:** Implemented Room Conflict detection ensuring rooms cannot host concurrent lectures on identical days.
* **Day 51:** Implemented Faculty Conflict detection preventing professors from being assigned overlapping teaching slots.
* **Day 52:** Implemented Section Conflict detection ensuring student cohorts are not scheduled for multiple classes at once.
* **Day 53:** Built diagnostic error formatter generating clear explanations of detected scheduling collisions.
* **Day 54:** Added pre-check verification rejecting scheduling in rooms marked as `MAINTENANCE`.
* **Day 55:** Integrated collision checks into `TimetableService.createEntry()`, throwing typed validation exceptions.
* **Day 56:** Authored automated tests verifying that all overlapping scheduling attempts are correctly rejected.

### Week 9: Timetable UI & Demo Screen Capture — Coding Phase 4 (Days 57–63)
* **Day 57:** Coded `admin-timetable.html` rendering interactive weekly calendar matrices with color-coded subject blocks.
* **Day 58:** Developed `timetable-management.js` handling dynamic lecture creation and live conflict alert popups.
* **Day 59:** Added daily timetable views in faculty and student dashboards showing personal class schedules.
* **Day 60:** Implemented room occupancy timeline modal showing daily room schedules for any selected space.
* **Day 61:** Added interactive conflict alert banners highlighting overlapping lectures and involved faculty.
* **Day 62:** Authored Module 3 README and committed code changes to branch `bhawni`.
* **Day 63:** Recorded initial high-definition screen captures of user onboarding and room management workflows.

### Week 10: Timetable Testing, Auditing & Screen Recording — Coding Phase 5 (Days 64–70)
* **Day 64:** Authored unit tests verifying 3-way collision detection across edge cases (boundary time overlaps).
* **Day 65:** Tested complex schedule intervals: adjacent time slots, partial overlaps, and multi-hour laboratory blocks.
* **Day 66:** Verified that timetable conflict checks execute within 30ms under realistic database query volumes.
* **Day 67:** Tested timetable retrieval security, ensuring students and faculty can access their own schedules.
* **Day 68:** Executed the full automated test suite for Module 3, confirming all test cases pass without errors.
* **Day 69:** Recorded high-definition screen captures of timetable creation and collision rejection workflows.
* **Day 70:** Pushed the synchronized `bhawni` branch to the remote GitHub repository.

### Week 11: Cross-Module Integration & Video Scripting (Days 71–77)
* **Day 71:** Integrated timetable creation with Module 2 rooms, confirming maintenance rooms are blocked from scheduling.
* **Day 72:** Verified that faculty timetables reflect live lecture assignments created in Module 3.
* **Day 73:** Wrote the comprehensive narration script for the official CampusGrid system demonstration video.
* **Day 74:** Recorded step-by-step video footage of conflict detection rejecting room double-bookings.
* **Day 75:** Coordinated cross-testing with Kashish to ensure exam scheduling respects regular timetable hours.
* **Day 76:** Edited the rough cut of the demo video, assembling recorded UI footage and structuring chapter markers.
* **Day 77:** Reviewed video pacing with team members and refined voiceover timing.

### Week 12: Video Post-Production, Presentation & Final Viva (Days 78–84)
* **Day 78:** Recorded professional audio voiceover matching the demonstration video script.
* **Day 79:** Completed video post-production: added title cards, UI zoom callouts, background score, and transitions.
* **Day 80:** Rendered and uploaded the final demonstration video in 1080p Full HD to the project drive.
* **Day 81:** Supported Palak in embedding video highlights and interface screenshots into the presentation deck.
* **Day 82:** Participated in the full presentation rehearsal, practicing live software walkthrough segments.
* **Day 83:** Verified that all timetable demo datasets loaded properly in the local test environment.
* **Day 84:** Demonstrated live timetable scheduling and collision rejection during the final viva examination.

---

# Member 4: Kashish
**Module Ownership:** Module 4 — Examination Management & Automated Seating Allocation  
**Additional Responsibilities:** Comprehensive Project Report, Module 4 UML & README  

### Week 1: Team Formation, Guide Selection & Abstract (Days 1–7)
* **Day 1:** Attended team formation meeting, reviewed problem requirements, and agreed to lead exam allocation.
* **Day 2:** Met with guide to discuss academic exam logistics, hall planning challenges, and seating arrangements.
* **Day 3:** Researched exam scheduling problems and anti-cheating candidate distribution strategies.
* **Day 4:** Contributed to drafting abstract sections covering automated exam hall allocation and admit cards.
* **Day 5:** Outlined documentation standards and formatting guidelines for the comprehensive project report.
* **Day 6:** Researched university examination guidelines regarding student desk spacing and invigilation ratios.
* **Day 7:** Supported submission of the project abstract on the university portal and confirmed guide approval.

### Week 2: Requirements Gathering & Allocation Constraints (Days 8–14)
* **Day 8:** Researched examination hall planning procedures across institutional examination branches.
* **Day 9:** Drafted functional requirements for exam creation, multi-section participation, and hall allocation.
* **Day 10:** Defined algorithm requirements for capacity pre-validation and alternating student section interleaving.
* **Day 11:** Formalized exam lifecycle states: `DRAFT`, `ALLOCATED`, `PUBLISHED`, and `COMPLETED`.
* **Day 12:** Specified candidate privacy rules ensuring students can view only their own assigned exam seats.
* **Day 13:** Defined requirements for printable Admit Cards / Hall Tickets featuring desk coordinates.
* **Day 14:** Contributed examination requirements to `Documents/02_REQUIREMENTS.md`.

### Week 3: Software Requirements Specification & Report Structuring (Days 15–21)
* **Day 15:** Structured the master template for the final project report in accordance with university academic standards.
* **Day 16:** Defined functional requirements for candidate room eligibility filtering (excluding maintenance rooms).
* **Day 17:** Specified non-functional requirements for allocation engine execution time (< 3 seconds for 500 students).
* **Day 18:** Defined rules for handling capacity deficits, aborting allocation with descriptive shortage alerts.
* **Day 19:** Reviewed room capacity definitions with Nikhil to establish allocation constraints.
* **Day 20:** Finalized examination requirements and incorporated feedback from the project guide.
* **Day 21:** Froze the requirements baseline for Module 4 and updated project milestone tracking boards.

### Week 4: System Architecture & UML Modeling (Days 22–28)
* **Day 22:** Created Use Case Diagram for examination administration, invigilation duties, and student hall tickets.
* **Day 23:** Modeled Class Diagram for Module 4 (`Exam`, `ExamSession`, `ExamParticipant`, `ExamAllocation`).
* **Day 24:** Designed flowchart for the Capacity Pre-Check and Alternating Section Allocation algorithm.
* **Day 25:** Developed Sequence Diagram illustrating exam creation, candidate allocation, and admit card generation.
* **Day 26:** Designed Sequence Diagram for exam publication and notification dispatch workflows.
* **Day 27:** Formulated Activity Diagrams for exam creation wizard and batch seat allocation processing.
* **Day 28:** Documented exam allocation algorithm complexity ($\mathcal{O}(N)$) in the system architecture specification.

### Week 5: Database Design & Exam Wireframes (Days 29–35)
* **Day 29:** Modeled relational MySQL tables for `exams`, `exam_sessions`, `exam_participants`, and `exam_allocations`.
* **Day 30:** Defined database indexes on exam date, room ID, and student ID for rapid seat lookup queries.
* **Day 31:** Specified foreign key relationships linking allocations to exam sessions, rooms, physical seats, and students.
* **Day 32:** Documented examination relational schemas in `Documents/04_DATABASE_DESIGN.md`.
* **Day 33:** Designed wireframes for the Exam Management wizard with date pickers, subject selectors, and section tags.
* **Day 34:** Designed UI wireframes for the Seating Plan Preview screen showing hall-wise allocations.
* **Day 35:** Designed Admit Card / Hall Ticket layout showing student details, exam timings, and assigned room/seat codes.

### Week 6: Exam Management Service — Coding Phase 1 (Days 36–42)
* **Day 36:** Implemented JPA entities `Exam` and `ExamSession` supporting exam states (`DRAFT`, `ALLOCATED`, `PUBLISHED`).
* **Day 37:** Implemented `ExamParticipant` entity linking registered students to exam sessions.
* **Day 38:** Created repository interfaces `ExamRepository` and `ExamParticipantRepository`.
* **Day 39:** Implemented service methods for creating exams and enrolling participating student sections.
* **Day 40:** Added validation ensuring exam dates are set in the future and time ranges are valid.
* **Day 41:** Implemented participant enrollment queries fetching students by academic section.
* **Day 42:** Wrote unit tests asserting exam creation, date validation, and participant enrollment.

### Week 7: Seating Engine Core & Capacity Check — Coding Phase 2 (Days 43–49)
* **Day 43:** Implemented `ExamAllocation` entity storing allocated room IDs, physical seat IDs, and student IDs.
* **Day 44:** Created `ExamAllocationRepository` with custom queries checking student seat assignments.
* **Day 45:** Built Capacity Pre-Validation logic comparing total candidate enrollment against candidate room capacities.
* **Day 46:** Implemented shortage detection throwing clear exceptions when room seating falls short of enrollment.
* **Day 47:** Created candidate room filtering service querying active, non-maintenance rooms without scheduling conflicts.
* **Day 48:** Added transactional boundaries ensuring allocation attempts rollback completely if capacity is insufficient.
* **Day 49:** Authored unit tests asserting capacity deficit rejection when candidate count exceeds available room desks.

### Week 8: Alternating Section Allocation Algorithm — Coding Phase 3 (Days 50–56)
* **Day 50:** Implemented Round-Robin Section Interleaving algorithm grouping students by academic section/branch.
* **Day 51:** Built sequential coordinate assignment mapping interleaved candidate queues onto 2D room seat grids.
* **Day 52:** Ensured blocked and maintenance seats are bypassed during student placement routines.
* **Day 53:** Implemented transactional execution (`@Transactional`) ensuring all seat assignments commit atomically.
* **Day 54:** Built exam publication service transitioning exams to `PUBLISHED` state and locking allocations.
* **Day 55:** Implemented student admit card data assembly service returning assigned room and desk numbers.
* **Day 56:** Authored automated tests verifying that adjacent seats are assigned to students of alternating sections.

### Week 9: Exam UI & Report Drafting — Coding Phase 4 (Days 57–63)
* **Day 57:** Coded `admin-exams.html` featuring exam creation wizards and candidate enrollment selectors.
* **Day 58:** Developed `exam-management.js` handling one-click allocation execution and live progress indicators.
* **Day 59:** Built seating plan preview components rendering room-wise allocation tables with candidate counts.
* **Day 60:** Added exam schedule table views in faculty dashboards showing invigilation assignments.
* **Day 61:** Built student admit card view with printable CSS stylesheets for physical hall ticket printing.
* **Day 62:** Authored Module 4 README and committed code changes to branch `kashish`.
* **Day 63:** Authored Chapters 1 and 2 (Introduction and Literature Review) of the master project report.

### Week 10: Exam Testing, Auditing & Report Compilation — Coding Phase 5 (Days 64–70)
* **Day 64:** Authored unit tests verifying alternating student placement across multi-room exam sessions.
* **Day 65:** Tested allocation edge cases: single-section exams, odd student counts, and rooms with blocked desks.
* **Day 66:** Validated allocation execution time, confirming 500 candidates are allocated across 10 rooms in < 1.2 seconds.
* **Day 67:** Tested admit card query security, ensuring students cannot inspect peer seating records.
* **Day 68:** Executed the full automated test suite for Module 4, confirming all test cases pass without errors.
* **Day 69:** Authored Chapters 3 and 4 (System Analysis and System Design) of the master project report.
* **Day 70:** Pushed the synchronized `kashish` branch to the remote GitHub repository.

### Week 11: Cross-Module Integration & Report Writing (Days 71–77)
* **Day 71:** Integrated exam allocation engine with live Module 2 physical seats, verifying coordinate mapping.
* **Day 72:** Connected exam publication events to student dashboards for personalized seat viewing.
* **Day 73:** Compiled Chapters 5 and 6 (Implementation Details and Software Testing) with test screenshots and metrics.
* **Day 74:** Validated that student tokens can only view their own assigned seat, preventing unauthorized privacy leaks.
* **Day 75:** Cross-checked exam scheduling with Module 3 timetables to ensure no hall conflicts with regular classes.
* **Day 76:** Integrated literature survey references from `Documents/11_LITERATURE_SURVEY.md` into the report.
* **Day 77:** Reviewed report formatting, diagram placements, and citation numbering with all team members.

### Week 12: Report Finalization, Presentation & Final Viva (Days 78–84)
* **Day 78:** Completed Chapter 7 (Conclusion and Future Scope) and compiled all appendix references.
* **Day 79:** Performed full document formatting, generated automated table of contents, and completed plagiarism checks.
* **Day 80:** Printed and bound the official comprehensive project report for department submission.
* **Day 81:** Supported Palak with report excerpts and architecture summaries for the presentation deck.
* **Day 82:** Participated in the full presentation rehearsal, practicing allocation algorithm explanations.
* **Day 83:** Verified that all examination demo datasets loaded properly in the local test environment.
* **Day 84:** Presented examination allocation results and test methodologies during the final viva evaluation.

---

# Member 5: Palak
**Module Ownership:** Module 5 — Portals, In-App Notifications & Unified UI/UX  
**Additional Responsibilities:** Official Presentation Deck (PPT), Module 5 UML & README  

### Week 1: Team Formation, Guide Selection & Abstract (Days 1–7)
* **Day 1:** Joined team formation, discussed design systems, and established frontend quality benchmarks.
* **Day 2:** Met with guide to discuss user personas (Administrator, Faculty, Student) and accessibility requirements.
* **Day 3:** Researched modern collegiate SaaS design patterns, typography, and responsive dashboard layouts.
* **Day 4:** Contributed to drafting abstract sections highlighting unified role-based portals and seat locators.
* **Day 5:** Established shared Figma workspaces and CSS design token templates for the project sprint.
* **Day 6:** Researched WCAG accessibility contrast ratios and responsive viewport standards.
* **Day 7:** Supported submission of the project abstract on the university portal and confirmed guide approval.

### Week 2: Requirements Gathering & UI Standards (Days 8–14)
* **Day 8:** Researched role-based portal requirements for administrators, teaching faculty, and undergraduate students.
* **Day 9:** Drafted functional requirements for personalized dashboards, notification drawers, and seat locators.
* **Day 10:** Defined non-functional UI/UX requirements: sub-300ms rendering, mobile responsiveness, and WCAG contrast.
* **Day 11:** Formalized the Student Interactive Seat Locator concept, detailing visual desk highlighting.
* **Day 12:** Specified notification broadcasting requirements, supporting role-targeted and urgent system alerts.
* **Day 13:** Defined navigation hierarchy rules ensuring users only access pages authorized for their role.
* **Day 14:** Contributed UI and portal requirements to `Documents/02_REQUIREMENTS.md`.

### Week 3: Software Requirements Specification & Presentation Outlining (Days 15–21)
* **Day 15:** Outlined slide structure and visual identity guidelines for the final presentation deck (PPT).
* **Day 16:** Defined functional requirements for responsive sidebar navigation and user profile badges.
* **Day 17:** Specified requirements for real-time metric counters on the administrator dashboard.
* **Day 18:** Defined rules for client-side token expiration handling and automatic unauthenticated redirects.
* **Day 19:** Reviewed dashboard data requirements with team members to establish shared API payload DTOs.
* **Day 20:** Finalized UI/UX requirements and incorporated feedback from the project guide.
* **Day 21:** Froze the requirements baseline for Module 5 and updated project milestone tracking boards.

### Week 4: System Architecture & UML Modeling (Days 22–28)
* **Day 22:** Created Use Case Diagram covering student seat locator access, faculty schedules, and alert notifications.
* **Day 23:** Modeled UI Navigation State Machine Diagram showing page transitions, modal flows, and role gating.
* **Day 24:** Designed Sequence Diagram for real-time notification dispatch, user inbox retrieval, and read status updates.
* **Day 25:** Developed Component Architecture Diagram detailing the relationship between CSS tokens, JS controllers, and HTML views.
* **Day 26:** Designed Sequence Diagram for the Interactive Seat Locator fetching exam and room grid data.
* **Day 27:** Formulated Activity Diagrams for user profile updating and notification broadcast creation.
* **Day 28:** Documented UI design standards and component specifications in `Documents/06_UI_UX_SPECIFICATION.md`.

### Week 5: CSS Design System & Portal Wireframes (Days 29–35)
* **Day 29:** Built master design system in `style.css` defining typography, spacing scales, and CSS color tokens.
* **Day 30:** Designed wireframes for the global UI shell (responsive sidebar, top navigation, user profile pill).
* **Day 31:** Designed wireframes for the Student Dashboard featuring upcoming classes, admit cards, and alert cards.
* **Day 32:** Designed wireframes for the Faculty Dashboard featuring daily lecture timelines and invigilation duties.
* **Day 33:** Created UI mockups for the **Interactive Seat Locator** visually highlighting assigned exam seats on a room grid.
* **Day 34:** Designed wireframes for the notification center modal and slide-out unread message drawer.
* **Day 35:** Finalized all UI wireframes, established responsive breakpoints, and verified color contrast accessibility.

### Week 6: Global UI Shell & Components — Coding Phase 1 (Days 36–42)
* **Day 36:** Implemented responsive navigation topbar with dynamic user profile badge, role indicators, and logout buttons.
* **Day 37:** Built collapsible sidebar navigation with role-specific menu items and active state indicators.
* **Day 38:** Coded reusable UI components: statistic metric cards, breadcrumbs, modal dialogs, and alert banners.
* **Day 39:** Implemented responsive grid layouts adapting seamlessly across desktop, tablet, and mobile breakpoints.
* **Day 40:** Added custom CSS styling for badges, status indicators, and hover effects across all portal tables.
* **Day 41:** Implemented client-side toast notification container providing animated feedback for user actions.
* **Day 42:** Wrote unit tests asserting CSS custom property definitions and responsive utility classes.

### Week 7: Notification Subsystem — Coding Phase 2 (Days 43–49)
* **Day 43:** Implemented `Notification` JPA entity supporting notification types (`BROADCAST`, `ACADEMIC`, `EXAM`, `URGENT`).
* **Day 44:** Created `NotificationRepository` and service methods handling recipient targeting and unread counters.
* **Day 45:** Built REST controller endpoints for broadcasting announcements and retrieving user inboxes.
* **Day 46:** Coded client-side notification dropdown drawer displaying unread message counters and timestamps.
* **Day 47:** Implemented mark-as-read API endpoints updating notification read status upon user interaction.
* **Day 48:** Added role-targeted filtering ensuring students receive student alerts and faculty receive teaching notices.
* **Day 49:** Authored unit tests verifying notification dispatch, role targeting, and read-status persistence.

### Week 8: Role Dashboards Integration — Coding Phase 3 (Days 50–56)
* **Day 50:** Developed `student-dashboard.html` integrating personalized class schedules and admit card widgets.
* **Day 51:** Developed `faculty-dashboard.html` displaying daily lecture timelines and assigned exam proctoring halls.
* **Day 52:** Built client-side JavaScript controllers fetching live `/api/auth/me` user data and populating profile cards.
* **Day 53:** Integrated real-time metric counters on the Admin Dashboard showing total active users, rooms, and courses.
* **Day 54:** Verified client-side session timeout handlers automatically redirecting expired sessions to the login view.
* **Day 55:** Implemented profile editing modals allowing students and faculty to update contact telephone numbers.
* **Day 56:** Authored automated tests asserting dashboard data loading and role authorization enforcement.

### Week 9: Student Interactive Seat Locator — Coding Phase 4 (Days 57–63)
* **Day 57:** Coded the **Interactive Seat Locator** interface enabling students to select scheduled examinations.
* **Day 58:** Developed seat locator JavaScript engine dynamically rendering assigned room layouts via SVG/Canvas grid.
* **Day 59:** Implemented visual highlighting isolating the candidate's exact seat with glowing borders and coordinates.
* **Day 60:** Added printable Admit Card view allowing students to save or print hall tickets with desk coordinates.
* **Day 61:** Added room location breadcrumbs (`Campus > Block > Floor > Room`) guiding students to exam halls.
* **Day 62:** Authored Module 5 README and committed code changes to branch `palak`.
* **Day 63:** Designed the master presentation slide template in Microsoft PowerPoint using brand visual tokens.

### Week 10: UI Testing, Accessibility & Presentation Setup — Coding Phase 5 (Days 64–70)
* **Day 64:** Conducted cross-browser compatibility testing across Chrome, Firefox, Edge, and mobile Safari.
* **Day 65:** Verified accessibility contrast ratios and keyboard navigation across all portal views and modals.
* **Day 66:** Tested seat locator rendering speeds, confirming layout grids render in < 250ms on mobile devices.
* **Day 67:** Tested notification drawer responsiveness under high unread message volumes.
* **Day 68:** Executed automated UI and controller tests for Module 5, confirming all test cases pass without errors.
* **Day 69:** Populated presentation slides with executive summaries, problem statements, and system objectives.
* **Day 70:** Pushed the synchronized `palak` branch to the remote GitHub repository.

### Week 11: Cross-Module Integration & Slide Finalization (Days 71–77)
* **Day 71:** Connected exam publication events to notification dispatch, alerting students of published seating.
* **Day 72:** Integrated live Module 3 timetable data into faculty and student dashboards.
* **Day 73:** Integrated live Module 4 exam allocations into the student Interactive Seat Locator.
* **Day 74:** Incorporated high-resolution system architecture diagrams and ER diagrams into the presentation deck.
* **Day 75:** Added live feature screenshots: Admin Console, Seating Designer, Timetable Grid, and Seat Locator.
* **Day 76:** Incorporated literature survey references and performance benchmarks into the presentation slides.
* **Day 77:** Reviewed slide deck with all team members to ensure balanced topic distribution and clear talking points.

### Week 12: Master Presentation (PPT), Rehearsal & Final Viva (Days 78–84)
* **Day 78:** Finalized the master presentation deck: refined slide animations, visual alignments, and typography.
* **Day 79:** Added algorithm flowcharts, database schema diagrams, and test results into the presentation slides.
* **Day 80:** Conducted team presentation dry-runs, allocated speaking times for all 5 members, and refined slide transitions.
* **Day 81:** Supported Bhawni by verifying that UI visual assets in the demo video match final presentation graphics.
* **Day 82:** Conducted technical rehearsal testing projector displays, slide navigation, and live system demos.
* **Day 83:** Verified that all presentation slides and software demo environments were fully prepared.
* **Day 84:** Delivered the official project presentation and successfully defended the UI/UX architecture during the final viva.
