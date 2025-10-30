package vv.pms.allocation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.allocation.internal.ProjectAllocationRepository;
import vv.pms.professor.ProfessorService; // Import other module's API
import vv.pms.project.ProjectService;   // Import other module's API

@Service
@Transactional
public class AllocationService {

    private final ProjectAllocationRepository repository;
    private final ProfessorService professorService;
    private final ProjectService projectService;

    // Inject dependencies on repository and other module's services
    public AllocationService(
            ProjectAllocationRepository repository,
            ProfessorService professorService,
            ProjectService projectService) {
        this.repository = repository;
        this.professorService = professorService;
        this.projectService = projectService;
    }

    /**
     * CORE USE CASE: Assigns a Professor to an existing Project.
     */
    public ProjectAllocation assignProfessorToProject(Long projectId, Long professorId) {

        // 1. Module Boundary Check (Project Module API)
        projectService.findProjectById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project ID " + projectId + " not found."));

        // 2. Module Boundary Check (Professor Module API)
        professorService.findProfessorById(professorId)
                .orElseThrow(() -> new IllegalArgumentException("Professor ID " + professorId + " not found."));

        // 3. Allocation Business Logic (Self-contained)
        if (repository.findByProjectId(projectId).isPresent()) {
            throw new IllegalStateException("Project " + projectId + " is already allocated to a professor.");
        }

        // 4. Create and save the new allocation relationship
        ProjectAllocation allocation = new ProjectAllocation(projectId, professorId);
        return repository.save(allocation);
    }

    // Add methods for removing assignments, finding allocations, etc.
}