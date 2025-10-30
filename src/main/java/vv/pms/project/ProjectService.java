package vv.pms.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.project.internal.ProjectRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository repository;

    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and saves a new project.
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
     * NOTE: In a real system, the Allocation module should be checked before deletion.
     */
    public void deleteProject(Long id) {
        repository.deleteById(id);
    }

    /**
     * Archives a project, changing its status.
     */
    public void archiveProject(Long id) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new ProjectNotFoundException("Project not found: " + id));
        project.archive();
        repository.save(project);
    }

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