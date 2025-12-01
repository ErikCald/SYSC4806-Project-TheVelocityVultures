# 4th Year Project Management System
A web-based system designed to manage 4th-year project topics, student applications, oral presentations, and final report submissions. This system streamlines project coordination for professors, students, and the 4th-year project coordinator.  

## Milestone 3 Features
- Enforce report submission endpoint + deadline enforcement https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/37  
- Create Coordinator account type and coordinator home page https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/49  
- Define behaviour for projects with no description https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/51  
- Remove student ability to delete projects https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/52  
- Fix login header not updating after username change https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/53  
- Professor and student availability input for oral presentations https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/56  
- Room Assignment Module for oral presentations https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/57  
- Project ownership rules & edit restrictions https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/58  
- Student Profile: Add team information section https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/59  
- Fix coordinator header behaviour when logged in https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/67  
- Integration tests for new Milestone 3 features https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/61  

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

## Full Project

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


## UML Diagrams

#### Module Diagram
<img width="478" height="683" alt="ModulesDiagram" src="https://github.com/user-attachments/assets/e14b0a7e-4347-4501-a9ea-8d088d4f9edc" />

#### Database Schema Diagram
<img width="1543" height="531" alt="image" src="https://github.com/user-attachments/assets/bdf47d0c-eda9-4e9d-8b62-bfc0659225f0" />

#### Class UML Diagrams
<img width="1872" height="744" alt="image" src="https://github.com/user-attachments/assets/3385742d-921d-4441-99d9-0bef3605dc01" />
<img width="774" height="561" alt="image" src="https://github.com/user-attachments/assets/804e9566-a1a8-4f1f-ab3f-5a2ec1b8a7e3" />
<img width="1583" height="671" alt="image" src="https://github.com/user-attachments/assets/fc95f779-9f8f-443a-951d-b765a4263cd9" />
<img width="1634" height="614" alt="image" src="https://github.com/user-attachments/assets/1b3aa394-74d5-41f6-b19d-765470de9203" />
<img width="4096" height="1249" alt="image" src="https://github.com/user-attachments/assets/96c1159c-ae56-49b0-ab5f-b4ee107cc39d" />


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

## 📌 Kanban Board Snapshot (Milestone 3)
Below is the snapshot of our board for our plan for Milestone 3:
<img width="1803" height="719" alt="image" src="https://github.com/user-attachments/assets/659353e4-4c17-4201-a9a9-210aab63ce65" />

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
