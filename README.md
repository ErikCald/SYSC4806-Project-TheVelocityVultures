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
