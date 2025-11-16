# 4th Year Project Management System
A web-based system designed to manage 4th-year project topics, student applications, oral presentations, and final report submissions. This system streamlines project coordination for professors, students, and the 4th-year project coordinator.  

## Milestone 2 Features
- Project Creation UI for Profs https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/18
- Project Detail and availability, and search UI https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/32
- Allocation Management backend https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/31
- Student Registration and UI https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/28
- Login/Logout with site-wide header https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/29
- Student project application flow https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/30
- Unit and Integration Test to test allocations of Students and Profs to Projects https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/34

## Milestone 1 Features
- Setup Spring Modulith Design Structure https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/6
- Initial Class Creation https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/7
- Azure Hosting https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/4
- Simple Professor's Web UI https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/3

*(Future milestones will add more functionality.)*

## Full Project Vision

### For Professors
- Add, delete, or archive 4th-year project topics.  
- Enter project details including description, program restrictions, and the number of students required.  
- Track applications to their projects and view which students have applied.  
- Enter availability for oral presentations.  

### For Students
- Browse and search available 4th-year projects.  
- Apply for projects until they reach maximum capacity.  
- Enter availability for oral presentations.  
- Submit final project reports online before a system-enforced deadline.  

### For the 4th-Year Project Coordinator
- Search for students who have not yet been assigned a project and send reminders.  
- Allocate oral presentation sessions to rooms based on professor and student availability.  
  - Allocation can be manual or use a simple “best effort” algorithm.  
- Manage project deadlines and enforce submission timelines for final reports.  

### System Features
- User authentication for professors, students, and the coordinator.  
- Real-time validation for project capacities and submission deadlines.  
- Web interface built with HTML and styled using CSS for an intuitive user experience.  
- Java backend handling business logic and data management.

## 🚀 Sprint Summary

### 1. Authentication & User Accounts
- Implement email-based login for Students and Professors  
- Add session-based role tracking (Student / Professor)  
- Create a signup flow with validation  
- Build login/signup UI pages (Thymeleaf)

**Deliverables:**  
- ✔ `/login`, `/signup`, `/auth/login`, `/auth/signup` endpoints  
- ✔ `LoginRecord` DTO  
- ✔ Header with role-aware rendering

---

### 2. Student Module
- Build Student CRUD service  
- Add `StudentController` with REST + Thymeleaf endpoints  
- Create Student Profile page (view/edit)  
- Integrate student roles into authentication  
- Validate uniqueness of `studentId` & `email`

**Deliverables:**  
- ✔ `/student/profile`  
- ✔ `/student/profile/edit`  
- ✔ Student forms + validation

---

### 3. Professor Module
- Create professor management UI  
- Enable CRUD operations via `/professors`  
- Ensure UI prevents students from accessing professor pages  

**Deliverables:**  
- ✔ Professor list page  
- ✔ Add/Edit/Delete forms  
- ✔ Role-based UI access checks

---

### 4. Project Module
- Build project creation/editing UI  
- Add filtering by program or status  
- Integrate project restrictions (program eligibility)  
- Surface professor assignment & capacity rules  

**Deliverables:**  
- ✔ `/projects` dashboard (Thymeleaf)  
- ✔ Project create/edit forms  
- ✔ Program selection & restriction filters

---

### 5. Allocation Module
- Implement `AllocationService` for:  
  - Assigning professor to project  
  - Assigning student to project  
  - Capacity checks  
  - Program compatibility  
- Add integration tests for allocation flows  

**Deliverables:**  
- ✔ Full allocation workflow  
- ✔ Unit + integration tests  
- ✔ Controller + UI hooks (optional)

---

### 6. Architecture & Module Structure
- Apply Spring Modulith conventions  
- Separate code into modules:  
  `auth`, `student`, `professor`, `project`, `allocation`, `ui`  
- Create clear domain boundaries

**Deliverables:**  
- ✔ Updated package layout  
- ✔ Module diagram  
- ✔ Clean separation between modules

---

## 📦 Overall Sprint Deliverables
- Full authentication + registration (student & professor)  
- Student profile management UI  
- Professor directory and management tools  
- Project management + filtering UI  
- Fully implemented allocation logic (tested)  
- Modulith-style architecture  
- Working end-to-end prototype

## UML Diagrams (Updated for Milestone 2

#### Module Diagram
<img width="478" height="683" alt="ModulesDiagram" src="https://github.com/user-attachments/assets/e14b0a7e-4347-4501-a9ea-8d088d4f9edc" />

#### Database Schema Diagram
<img width="721" height="775" alt="DBDiagram" src="https://github.com/user-attachments/assets/0456d845-7ea1-45aa-b27c-3124c53d9832" />

#### Class UML Diagrams
<img width="1583" height="671" alt="project_class_diagram" src="https://github.com/user-attachments/assets/003ee62f-9e8e-49f8-808c-762c54600f74" />
<img width="1634" height="614" alt="student_class_diagram" src="https://github.com/user-attachments/assets/73277c3e-f5dd-43bd-ab8f-c3dabb6bc44f" />
<img width="1872" height="744" alt="allocation_class_diagram" src="https://github.com/user-attachments/assets/ebde894d-c885-41ff-a569-4e3ead28c2bd" />
<img width="774" height="561" alt="professor_class_diagram" src="https://github.com/user-attachments/assets/0a767a24-c82b-4118-be58-0b3fd4c77baa" />
<img width="837" height="442" alt="auth_class_diagram" src="https://github.com/user-attachments/assets/a0c3cb79-e2a9-4627-beb1-ef71cc732e0e" />
<img width="4096" height="1249" alt="ui_class_diagram" src="https://github.com/user-attachments/assets/f02a052e-137b-482b-93a7-9d243c7f289f" />


## Technology Stack
Backend Language
- Java 21
Framework
- Spring Boot 3.5.x
- Spring Modules
- Spring Web (MVC) – Controllers, routing, HTTP handling
- Spring Data JPA – ORM abstraction
- Spring Validation (Jakarta Validation) – @Valid, @Email, @NotBlank
- Spring Transaction Management – via @Transactional
- Spring Boot Starter Thymeleaf – HTML templating
- Spring Test – integration with JUnit
- Persistence
- Hibernate ORM (via JPA)
- Jakarta Persistence API

Database
- H2 (in-memory) for development & tests
  
Frontend (Server-Side Rendering)
- Thymeleaf (SSR HTML)

UI Components
- Bootstrap 5.3 (CDN)
- Custom CSS (inline within templates)
- Project logo under /static/images

JavaScript
- Minimal vanilla JS
- Toggle signup student fields
- Bootstrap UI behaviour

Authentication & Authorization
- Custom email-only login
- No passwords (MVP)
- Auto-login after signup

Session Management
- HttpSession (Spring MVC)

Testing Stack
- JUnit 5 (Jupiter)
- Mockito (inline mock-maker)
- Spring Boot Test
- Integration Testing
- @SpringBootTest
- TestRestTemplate
- H2 testing database

## 📌 Kanban Board Snapshot (Milestone 2)
This project uses a GitHub Projects Kanban workflow to track tasks across the sprint.
Below is the snapshot of our board at the end of Milestone 2:
<img width="1910" height="863" alt="KanbanSS" src="https://github.com/user-attachments/assets/002cd4da-5b3d-4826-9938-5274f7b695d6" />


## Contributors
- Darren Wallace  
- Maahir Muhammad  
- Ayoub Hassan  
- Erik Cald  
- Lakshman Chelliah  

## Installation & Setup
1. Clone the repository:  
   ```bash
   git clone https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures.git
   javac -d bin src/**/*.java
   java -cp bin Application
