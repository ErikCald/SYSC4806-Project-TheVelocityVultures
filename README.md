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
	@@ -16,13 +31,11 @@ A web-based system designed to manage 4th-year project topics, student applicati
- Azure Hosting https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/4
- Simple Professor's Web UI https://github.com/ErikCald/SYSC4806-Project-TheVelocityVultures/issues/3

## UML Diagrams 

#### Module Diagram
<img width="478" height="683" alt="ModulesDiagram" src="https://github.com/user-attachments/assets/e14b0a7e-4347-4501-a9ea-8d088d4f9edc" />

#### Database Schema Diagram

#### Class UML Diagrams
<img width="1872" height="744" alt="image" src="https://github.com/user-attachments/assets/3385742d-921d-4441-99d9-0bef3605dc01" />
<img width="774" height="561" alt="image" src="https://github.com/user-attachments/assets/804e9566-a1a8-4f1f-ab3f-5a2ec1b8a7e3" />
<img width="1583" height="671" alt="image" src="https://github.com/user-attachments/assets/fc95f779-9f8f-443a-951d-b765a4263cd9" />
<img width="1634" height="614" alt="image" src="https://github.com/user-attachments/assets/1b3aa394-74d5-41f6-b19d-765470de9203" />
<img width="4096" height="1249" alt="image" src="https://github.com/user-attachments/assets/96c1159c-ae56-49b0-ab5f-b4ee107cc39d" />

## Technology Stack
	@@ -206,9 +127,9 @@ This project uses a GitHub Projects Kanban workflow to track tasks across the sp
Below is the snapshot of our board at the end of Milestone 2:
<img width="1910" height="863" alt="KanbanSS" src="https://github.com/user-attachments/assets/002cd4da-5b3d-4826-9938-5274f7b695d6" />

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
