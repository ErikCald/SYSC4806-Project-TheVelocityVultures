package vv.pms.project;

import jakarta.persistence.*;
import vv.pms.common.Program;

import java.util.Set;

@Entity
@Table(name = "project_topics")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob // Used for storing large amounts of text
    @Column(nullable = false)
    private String description;

    @ElementCollection(targetClass = Program.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "topic_program_restrictions", joinColumns = @JoinColumn(name = "topic_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "program")
    private Set<Program> programRestrictions;

    @Column(nullable = false)
    private int requiredStudents; // Maximum number of students allowed

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.OPEN;

    public Project() {}

    /***
     * Parameterized constructor
     *
     * @param title The title of the project
     * @param description The description of the project
     * @param programRestrictions The program restrictions for the project
     * @param requiredStudents The number of students required for the project
     */
    public Project(String title, String description, Set<Program> programRestrictions, int requiredStudents) {
        this.title = title;
        this.description = description;
        this.programRestrictions = programRestrictions;
        this.requiredStudents = requiredStudents;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<Program> getProgramRestrictions() {
        return programRestrictions;
    }

    public void setProgramRestrictions(Set<Program> programRestrictions) {
        this.programRestrictions = programRestrictions;
    }

    public int getRequiredStudents() {
        return requiredStudents;
    }

    public void setRequiredStudents(int requiredStudents) {
        this.requiredStudents = requiredStudents;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public void archive() {
        this.status = ProjectStatus.ARCHIVED;
    }

    public boolean isProgramAllowed(Program studentProgram) {
        return this.programRestrictions.contains(studentProgram);
    }
}