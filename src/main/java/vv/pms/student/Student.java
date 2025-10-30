package vv.pms.student;

import jakarta.persistence.*;
import vv.pms.common.Program;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String studentId; // e.g., University ID number

    @Column(nullable = false, unique = true)
    private String email;

    @Enumerated(EnumType.STRING) // Stores the enum name (e.g., "SOFTWARE_ENGINEERING")
    @Column(nullable = false)
    private Program program;

    // Tracks if a student is officially assigned to a project (used by the Allocation module)
    private boolean hasProject = false;

    /**
     * Default constructor for JPA
     */
    public Student() {
    }

    /**
     * Parameterized constructor
     * 
     * @param name The name of the student
     * @param studentId The student ID
     * @param email The email address
     * @param program The program the student is enrolled in
     */
    public Student(String name, String studentId, String email, Program program) {
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.program = program;
        this.hasProject = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Program getProgram() {
        return program;
    }

    public void setProgram(Program program) {
        this.program = program;
    }

    public boolean isHasProject() {
        return hasProject;
    }

    // This setter is used by the StudentService based on actions in the Allocation module
    public void setHasProject(boolean hasProject) {
        this.hasProject = hasProject;
    }

    /**
     * Encapsulates the state change for the email address.
     */
    public void updateEmail(String newEmail) {
        if (newEmail == null || newEmail.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        this.email = newEmail;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", studentId='" + studentId + '\'' +
                ", email='" + email + '\'' +
                ", program=" + program +
                ", hasProject=" + hasProject +
                '}';
    }
}
