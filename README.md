# 4th Year Project Management System

A web-based system designed to manage 4th-year project topics, student applications, oral presentations, and final report submissions. This system streamlines project coordination for professors, students, and the 4th-year project coordinator.  

## Current Features (Milestone 1)
- Professors can be added to the system.  

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

## UML Diagrams

#### Module Diagram
<img width="354" height="549" alt="SYSC4806-Project-Milestone1-ModuleDiagram" src="https://github.com/user-attachments/assets/69188335-c7be-49d0-ad75-c7ffe08b9e76" />

#### Database Schema Diagram
<img width="623" height="900" alt="Image" src="https://github.com/user-attachments/assets/f003f8ee-1fdd-418d-a851-259befe7c773" />

#### Class UML Diagrams
<img width="1417" height="674" alt="project_class_diagram" src="https://github.com/user-attachments/assets/599dc444-f06f-4b75-a802-002f6aa4059d" />
<img width="1548" height="593" alt="student_class_diagram" src="https://github.com/user-attachments/assets/1b8af9ba-00c8-45d6-b59e-f928b28b3542" />
<img width="2574" height="875" alt="ui_class_diagram" src="https://github.com/user-attachments/assets/17eb0349-704b-4075-88c6-598bcc0b8bcc" />
<img width="1872" height="658" alt="allocation_class_diagram" src="https://github.com/user-attachments/assets/182650c6-39a9-4b51-986e-b27fb9766a11" />
<img width="774" height="537" alt="professor_class_diagram" src="https://github.com/user-attachments/assets/112cdfcc-f989-45df-9519-7e573e9ec09a" />


## Technology Stack
- **Backend:** Java  
- **Frontend:** HTML, CSS  

## Milestones
- **Milestone 1:** Add professors.  

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
