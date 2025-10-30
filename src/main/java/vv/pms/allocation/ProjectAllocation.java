package vv.pms.allocation;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "project_allocations")
public class ProjectAllocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use Long to reference the Project entity from the 'project' module
    @Column(nullable = false, unique = true)
    private Long projectId;

    // Use Long to reference the Professor entity from the 'professor' module
    @Column(nullable = false)
    private Long professorId;

    // TODO: For future milestones: Tracks assigned students using IDs
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "allocation_assigned_students", joinColumns = @JoinColumn(name = "allocation_id"))
    private Set<Long> assignedStudentIds = new HashSet<>();

    // TODO: For future milestones: Tracks the current count for quick checks
    @Column(nullable = false)
    private int currentStudentCount = 0;

    /**
     * Default constructor for JPA
     */
    public ProjectAllocation() {}

    /**
     * Parameterized constructor
     * 
     * @param projectId The ID of the project
     * @param professorId The ID of the professor
     */
    public ProjectAllocation(Long projectId, Long professorId) {
        this.projectId = projectId;
        this.professorId = professorId;
    }

    /**
     * Assigns a student to this project allocation.
     * Validation logic for fullness/duplicates goes in the AllocationService.
     */
    public void assignStudent(Long studentId) {
        // Validation logic for fullness/duplicates goes in the AllocationService
        this.assignedStudentIds.add(studentId);
        this.currentStudentCount = this.assignedStudentIds.size();
    }

    public Long getId() {
        return id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public Long getProfessorId() {
        return professorId;
    }

    public void setProfessorId(Long professorId) {
        this.professorId = professorId;
    }

    public Set<Long> getAssignedStudentIds() {
        return assignedStudentIds;
    }

    public int getCurrentStudentCount() {
        return currentStudentCount;
    }
}