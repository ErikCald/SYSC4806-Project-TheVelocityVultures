package vv.pms.allocation;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "project_allocations")
public class ProjectAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long projectId;

    @Column(nullable = false)
    private Long professorId;

    @ElementCollection
    @CollectionTable(name = "allocation_students", joinColumns = @JoinColumn(name = "allocation_id"))
    @Column(name = "student_id", nullable = false)
    private List<Long> assignedStudentIds = new ArrayList<>();

    // --- UI helper fields (not persisted) ---
    @Transient
    private String projectTitle;

    @Transient
    private String professorName;

    public ProjectAllocation() {}

    public ProjectAllocation(Long projectId, Long professorId) {
        this.projectId = projectId;
        this.professorId = professorId;
    }

    // --- Business Logic focused on relationships ---
    public void assignStudent(Long studentId) {
        // Validation logic for fullness/duplicates goes in the AllocationService
        this.assignedStudentIds.add(studentId);
    }

    public void unassignStudent(Long studentId) {
        this.assignedStudentIds.remove(studentId);
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public List<Long> getAssignedStudentIds() {
        return assignedStudentIds;
    }

    public int getCurrentStudentCount() {
        return assignedStudentIds.size();
    }

    public void addStudent(Long studentId) {
        if (!assignedStudentIds.contains(studentId)) {
            assignedStudentIds.add(studentId);
        }
    }

    public void removeStudent(Long studentId) {
        assignedStudentIds.remove(studentId);
    }

    // --- UI helper accessors ---

    public String getProjectTitle() {
        return projectTitle;
    }

    public void setProjectTitle(String projectTitle) {
        this.projectTitle = projectTitle;
    }

    public String getProfessorName() {
        return professorName;
    }

    public void setProfessorName(String professorName) {
        this.professorName = professorName;
    }
}
