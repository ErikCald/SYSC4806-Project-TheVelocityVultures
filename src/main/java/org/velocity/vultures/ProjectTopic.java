package org.velocity.vultures;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "project_topics", indexes = {
        @Index(name = "idx_project_topics_status", columnList = "status")
})
public class ProjectTopic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @Lob
    @NotBlank
    @Column(nullable = false)
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "topic_program_restrictions", joinColumns = @JoinColumn(name = "topic_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "program", nullable = false)
    private Set<Program> programRestrictions = new HashSet<>();

    @Min(1)
    @Column(nullable = false)
    private int requiredStudents;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status = ProjectStatus.OPEN;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor;

    @OneToMany(mappedBy = "projectTopic")
    private List<Student> students = new ArrayList<>();

    public ProjectTopic() {}

    public ProjectTopic(String title, String description, Set<Program> programRestrictions, int requiredStudents, Professor professor) {
        this.title = title;
        this.description = description;
        if (programRestrictions != null) {
            this.programRestrictions.addAll(programRestrictions);
        }
        this.requiredStudents = requiredStudents;
        this.professor = professor;
        this.status = ProjectStatus.OPEN;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Set<Program> getProgramRestrictions() { return programRestrictions; }
    public void setProgramRestrictions(Set<Program> programRestrictions) {
        this.programRestrictions = programRestrictions != null ? programRestrictions : new HashSet<>();
    }
    public int getRequiredStudents() { return requiredStudents; }
    public void setRequiredStudents(int requiredStudents) { this.requiredStudents = requiredStudents; }
    public ProjectStatus getStatus() { return status; }
    public void setStatus(ProjectStatus status) { this.status = status; }
    public Professor getProfessor() { return professor; }
    public void setProfessor(Professor professor) { this.professor = professor; }
    public List<Student> getStudents() { return students; }

    public int getCurrentSize() { return students == null ? 0 : students.size(); }

    public boolean acceptsProgram(Program program) {
        return programRestrictions == null || programRestrictions.isEmpty() || programRestrictions.contains(program);
    }

    public boolean isFull() { return getCurrentSize() >= requiredStudents; }

    public void refreshStatus() {
        if (status == ProjectStatus.ARCHIVED) return;
        this.status = isFull() ? ProjectStatus.FULL : ProjectStatus.OPEN;
    }
}
