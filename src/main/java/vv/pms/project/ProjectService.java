package vv.pms.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.common.Program;
import vv.pms.project.internal.ProjectRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repository;

    /**
     * Constructor for ProjectService
     * 
     * @param repository The ProjectRepository instance (injected)
     */
    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and saves a new project.
     * 
     * @param title            The title of the project
     * @param description      The description of the project
     * @param restrictions     The program restrictions for the project
     * @param requiredStudents The number of students required for the project
     * @return The created Project entity
     */
    public Project addProject(String title, String description, Set<Program> restrictions, int requiredStudents) {
        Project newProject = new Project(title, description, restrictions, requiredStudents);
        return repository.save(newProject);
    }

    /**
     * Retrieves a project by ID.
     * This is the API used by the Allocation module to check if a Project ID is valid.
     */
    @Transactional(readOnly = true)
    public Optional<Project> findProjectById(Long id) {
        return repository.findById(id);
    }

    /**
     * Deletes a project by ID.
     * TODO: Consider cascading effects required when deleting a project
     */
    public void deleteProject(Long id) {
        repository.deleteById(id);
    }

    /**
     * Archives a project, changing its status.
     * 
     * @param id The ID of the project to archive
     */
    public void archiveProject(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));
        project.archive();
        repository.save(project);
    }

    /**
     * Retrieves all projects.
     * 
     * @return A list of all projects
     */
    @Transactional(readOnly = true)
    public List<Project> findAllProjects() {
        return repository.findAll();
    }

    // Custom exception defined here or in an internal package
    public static class ProjectNotFoundException extends RuntimeException {
        public ProjectNotFoundException(String message) {
            super(message);
        }
    }
}