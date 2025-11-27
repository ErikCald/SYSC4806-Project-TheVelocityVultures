package vv.pms.allocation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.internal.ProjectAllocationRepository;
import vv.pms.professor.ProfessorService;
import vv.pms.project.ProjectService;
import vv.pms.student.StudentService; // Need StudentService for student assignments
import vv.pms.project.Project;
import vv.pms.student.Student;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    // --- Professor Allocation Methods ---

    /**
     * Assigns a Professor to an existing Project.
     */
    public ProjectAllocation assignProfessorToProject(Long projectId, Long professorId) {

        // Boundary Checks: Verify IDs exist via module APIs
        projectService.findProjectById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project ID " + projectId + " not found."));

        professorService.findProfessorById(professorId)
                .orElseThrow(() -> new ProfessorNotFoundException("Professor ID " + professorId + " not found."));

        // Allocation Business Logic: Check for duplicates
        if (repository.findByProjectId(projectId).isPresent()) {
            throw new AllocationStateException("Project " + projectId + " is already allocated to a professor.");
        }

        // Create and save the new allocation relationship
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        return repository.save(allocation);
    }

    /**
     * Removes the professor allocation for a project.
     * Note: This might need to cascade to student unassignments in a production system.
     */
    public void removeProfessorAllocation(Long projectId) {
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Allocation for Project ID " + projectId + " not found."));

        repository.delete(allocation);
    }

    // --- Student Allocation Methods ---

    /**
     * Assigns a Student to an allocated Project, performing all necessary checks.
     */
    public ProjectAllocation assignStudentToProject(Long projectId, Long studentId) {

        // 1. Retrieve Allocation and Entity Details
        ProjectAllocation allocation = repository.findByProjectId(projectId)
                .orElseThrow(() -> new AllocationNotFoundException("Project " + projectId + " is not yet allocated to a professor."));

        Project project = projectService.findProjectById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException("Project ID " + projectId + " not found.")); // Should already exist

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student ID " + studentId + " not found."));

        // 2. Allocation Business Logic Checks
        if (allocation.getAssignedStudentIds().contains(studentId)) {
            throw new AllocationStateException("Student " + studentId + " is already assigned to this project.");
        }

        if (student.isHasProject()) {
            throw new AllocationStateException("Student " + studentId + " already has an assigned project.");
        }

        if (allocation.getAssignedStudentIds().size() >= project.getRequiredStudents()) {
            throw new AllocationStateException("Project " + projectId + " is already full.");
        }

        if (!project.isProgramAllowed(student.getProgram())) {
            throw new AllocationStateException("Student's program (" + student.getProgram() + ") does not match project restrictions.");
        }

        // 3. Update State in Allocation Module and Student Module
        allocation.addStudent(studentId); // Updates allocation student list and count
        studentService.updateProjectStatus(studentId, true); // Updates Student's hasProject status

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
        allocation.unassignStudent(studentId);
        studentService.updateProjectStatus(studentId, false);

        return repository.save(allocation);
    }

    // --- Read/Query Methods ---

    /**
     * Retrieves the allocation record for a specific project.
     */
    @Transactional(readOnly = true)
    public Optional<ProjectAllocation> findAllocationByProjectId(Long projectId) {
        return repository.findByProjectId(projectId);
    }

    /**
     * Finds the allocation that contains the given studentId (if any).
     */
    @Transactional(readOnly = true)
    public Optional<ProjectAllocation> findAllocationByStudentId(Long studentId) {
        return repository.findAll().stream()
                .filter(a -> a.getAssignedStudentIds().contains(studentId))
                .findFirst();
    }

    /**
     * Returns the list of student IDs assigned to the given project.
     */
    @Transactional(readOnly = true)
    public java.util.List<Long> findStudentsByProjectId(Long projectId) {
        return repository.findByProjectId(projectId)
                .map(ProjectAllocation::getAssignedStudentIds)
                .orElse(java.util.List.of());
    }

    /**
     * Returns the list of projects assigned to the given professor.
     */
    @Transactional(readOnly = true)
    public List<Project> findProjectsByProfessorId(Long professorId) {
        List<ProjectAllocation> allocations = repository.findByProfessorId(professorId);
        Set<Long> projectIds = allocations.stream()
                .map(ProjectAllocation::getProjectId)
                .collect(Collectors.toSet());
        
        if (projectIds.isEmpty()) {
            return List.of();
        }
        
        return projectService.findProjectsByIds(projectIds);
    }

    @Transactional
    public void runBestEffortAllocation() {
        // Best-effort: for each project, ensure allocation and try to fill students
        List<Project> projects = projectService.findAllProjects();
        List<Student> students = studentService.findAllStudents();

        // First, ensure each project that should have an allocation has one with a professor.
        for (Project project : projects) {
            Optional<ProjectAllocation> existing =
                    repository.findByProjectId(project.getId());
            if (existing.isEmpty()) {
                // naive: pick any professor (or skip if none)
                professorService.findAllProfessors().stream().findFirst().ifPresent(prof -> {
                    try {
                        assignProfessorToProject(project.getId(), prof.getId());
                    } catch (RuntimeException ignored) {
                        // best-effort: ignore failures
                    }
                });
            }
        }

        // Refresh allocations and students after possible changes
        students = studentService.findAllStudents();
        List<ProjectAllocation> allocations = repository.findAll();

        for (ProjectAllocation allocation : allocations) {
            Project project = projectService.findProjectById(allocation.getProjectId())
                    .orElse(null);
            if (project == null) {
                continue;
            }
            int capacity = project.getRequiredStudents();
            for (Student student : students) {
                if (student.isHasProject()) {
                    continue;
                }
                if (!project.getProgramRestrictions().isEmpty()
                        && !project.getProgramRestrictions().contains(student.getProgram())) {
                    continue;
                }
                if (allocation.getAssignedStudentIds().size() >= capacity) {
                    break;
                }
                try {
                    assignStudentToProject(project.getId(), student.getId());
                } catch (RuntimeException ignored) {
                    // best-effort: ignore failures for individual students
                }
            }
        }
    }


    /**
     * Retrieves all allocation records.
     */
    @Transactional(readOnly = true)
    public List<ProjectAllocation> findAllAllocations() {
        return repository.findAll();
    }

    /** Finds all Allocations for a given set of Project IDs and returns them in a Map for fast lookups. */
    @Transactional(readOnly = true)
    public Map<Long, ProjectAllocation> findAllocationsByProjectIds(Set<Long> projectIds) {
        return repository.findByProjectIdIn(projectIds).stream()
                .collect(Collectors.toMap(ProjectAllocation::getProjectId, Function.identity()));
    }

    /**
     * Public API for other modules (e.g., coordinator dashboard) to obtain a mapping of studentId -> projectId
     * without exposing internal repository details. Only includes students currently assigned.
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> mapStudentToProjectIds() {
        return repository.findAll().stream()
            .flatMap(a -> a.getAssignedStudentIds().stream()
                .map(sid -> new java.util.AbstractMap.SimpleEntry<>(sid, a.getProjectId())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
