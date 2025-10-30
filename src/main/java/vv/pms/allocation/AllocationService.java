package vv.pms.allocation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.internal.ProjectAllocationRepository;
import vv.pms.professor.ProfessorService;
import vv.pms.project.ProjectService;
import vv.pms.student.StudentService;
import vv.pms.project.Project;
import vv.pms.student.Student;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AllocationService {

    private final ProjectAllocationRepository repository;
    private final ProfessorService professorService;
    private final ProjectService projectService;
    private final StudentService studentService;

    public AllocationService(
            ProjectAllocationRepository repository,
            ProfessorService professorService,
            ProjectService projectService,
            StudentService studentService) {
        this.repository = repository;
        this.professorService = professorService;
        this.projectService = projectService;
        this.studentService = studentService;
    }

    /**
     * Assigns a Professor to a Project, performing all necessary boundary checks and business logic.
     */
    public ProjectAllocation assignProfessorToProject(Long projectId, Long professorId) {

        // 1. Boundary Checks: Verify IDs exist via module APIs
        projectService.findProjectById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project ID " + projectId + " not found."));

        professorService.findProfessorById(professorId)
                .orElseThrow(() -> new ProfessorNotFoundException("Professor ID " + professorId + " not found."));

        // 2. Allocation Business Logic: Check for duplicates
        if (repository.findByProjectId(projectId).isPresent()) {
            throw new AllocationStateException("Project " + projectId + " is already allocated to a professor.");
        }

        // 3. Create and save the new allocation relationship
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        return repository.save(allocation);
    }

    /**
     * Removes the professor allocation for a project.
     * TODO: This might need to cascade to student unassignments in a production system.
     */
    public void removeProfessorAllocation(Long projectId) {
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Allocation for Project ID " + projectId + " not found."));

        repository.delete(allocation);
    }

    /**
     * Assigns a Student to an allocated Project, performing all necessary boundary checks and business logic.
     */
    public ProjectAllocation assignStudentToProject(Long projectId, Long studentId) {

        // Retrieve Allocation and Entity Details
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Project " + projectId + " is not yet allocated to a professor."));

        Project project = projectService.findProjectById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project ID " + projectId + " not found.")); // Should already exist

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student ID " + studentId + " not found."));

        // Allocation Business Logic Checks
        if (allocation.getAssignedStudentIds().contains(studentId)) {
            throw new AllocationStateException("Student " + studentId + " is already assigned to this project.");
        }

        if (student.isHasProject()) {
            throw new AllocationStateException("Student " + studentId + " already has an assigned project.");
        }

        if (allocation.getCurrentStudentCount() >= project.getRequiredStudents()) {
            throw new AllocationStateException("Project " + projectId + " is already full.");
        }

        if (!project.isProgramAllowed(student.getProgram())) {
            throw new AllocationStateException("Student's program (" + student.getProgram() + ") does not match project restrictions.");
        }

        // Update State in Allocation Module and Student Module
        allocation.assignStudent(studentId); 
        studentService.updateProjectStatus(studentId, true);

        return repository.save(allocation);
    }

    /**
     * Removes a Student from an allocated Project.
     */
    public ProjectAllocation unassignStudentFromProject(Long projectId, Long studentId) {
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Project " + projectId + " not allocated."));

        if (!allocation.getAssignedStudentIds().contains(studentId)) {
            throw new AllocationNotFoundException("Student " + studentId + " is not assigned to this project.");
        }

        // Update State
        allocation.getAssignedStudentIds().remove(studentId);
        studentService.updateProjectStatus(studentId, false);

        return repository.save(allocation);
    }

    /**
     * Retrieves the allocation record for a specific project.
     */
    @Transactional(readOnly = true)
    public Optional<ProjectAllocation> findAllocationByProjectId(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    /**
     * Retrieves all allocation records.
     */
    @Transactional(readOnly = true)
    public List<ProjectAllocation> findAllAllocations() {
        return repository.findAll();
    }

    // Define module-specific exceptions that provide clear context
    public static class AllocationNotFoundException extends RuntimeException {
        public AllocationNotFoundException(String message) { super(message); }
    }
    public static class AllocationStateException extends RuntimeException {
        public AllocationStateException(String message) { super(message); }
    }
    public static class ProjectNotFoundException extends RuntimeException {
        public ProjectNotFoundException(String message) { super(message); }
    }
    public static class ProfessorNotFoundException extends RuntimeException {
        public ProfessorNotFoundException(String message) { super(message); }
    }
    public static class StudentNotFoundException extends RuntimeException {
        public StudentNotFoundException(String message) { super(message); }
    }
}
