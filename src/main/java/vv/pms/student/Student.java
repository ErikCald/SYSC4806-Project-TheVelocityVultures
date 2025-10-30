package vv.pms.student;

import jakarta.persistence.*;
import vv.pms.project.Program;

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

    @Enumerated(EnumType.STRING) // Stores the enum name (e.g., "SOFTWARE_ENGINEERING") as a string
    @Column(nullable = false)
    private Program program;

    // Field to easily track if a student has an accepted project
    private boolean hasProject = false;

    // --- Relationships ---

    // One-to-One relationship with the User entity for login (not shown, but assumed)
    // @OneToOne
    // @JoinColumn(name = "user_id")
    // private User user;

    // --- Constructors ---

    public Student() {
    }

    public Student(String name, String studentId, String email, Program program) {
        this.name = name;
        this.studentId = studentId;
        this.email = email;
        this.program = program;
        this.hasProject = false;
    }

    // --- Getters and Setters ---

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

    public void setHasProject(boolean hasProject) {
        this.hasProject = hasProject;
    }
}
