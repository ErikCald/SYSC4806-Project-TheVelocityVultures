package vv.pms;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Set;

@Entity
@Table(name = "project_topics")
public class ProjectTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    static private Long id = 0L;

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
    private int currentStudents = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.OPEN; // OPEN, FULL, ARCHIVED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor; // The professor offering the project

    @OneToMany(mappedBy = "projectTopic", cascade = CascadeType.ALL, orphanRemoval = true)
    private ArrayList<Student> students = new ArrayList<>();


    public ProjectTopic() {}


    public ProjectTopic(String title, String description,Set<Program> programRestrictions, int requiredStudents, Professor professor) {
        this.title = title;
        this.description = description;
        this.programRestrictions = programRestrictions;
        this.requiredStudents = requiredStudents;
        this.professor = professor;
        this.id = getId();
        incrementID();

    }



    /**
     * Increments the count of accepted students and updates status to FULL if capacity is reached.
     */
    public void addStudentToProject(Student student) {

        if (student.isHasProject()) {
            throw new IllegalStateException("Student already has an assigned project and cannot be added to a new one.");
        }

        if (this.students.size() >= this.requiredStudents) {
            this.status = ProjectStatus.FULL;
            throw new IllegalStateException("Project topic is already full. Cannot add more students.");
        }

        if (!this.programRestrictions.contains(student.getProgram())) {
            throw new IllegalArgumentException(
                    "Student's program (" + student.getProgram() + ") does not match project restrictions."
            );
        }


        this.students.add(student);

        if (this.students.size() >= this.requiredStudents) {
            this.status = ProjectStatus.FULL;
        }

        // NOTE: The actual linking (student.setProjectTopic(this)) and saving
        // of BOTH entities MUST happen in the Service layer.
    }


    public Long getId() {
        return id;
    }

    public void incrementID(){
        id+=1;
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

    public boolean isProgramAllowed(Program studentProgram) {
        // Returns true if the set of restrictions contains the student's program
        return this.programRestrictions.contains(studentProgram);
    }

    public int getRequiredStudents() {
        return requiredStudents;
    }


    public int getCurrentStudents() {
        return currentStudents;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public Professor getProfessor() {
        return professor;
    }

}