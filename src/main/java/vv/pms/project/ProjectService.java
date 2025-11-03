package vv.pms.project;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import vv.pms.project.internal.ProjectRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;

    @PersistenceContext
    private EntityManager em;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    /** Return all projects using JPA repository */
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    /** Return all projects using EntityManager (optional) */
    public List<Project> findAllProjects() {
        return em.createQuery("SELECT p FROM Project p ORDER BY p.id", Project.class)
                .getResultList();
    }

    /** Find by id */
    public Optional<Project> findProjectById(Long id) {
        if (id == null) return Optional.empty();
        Project p = em.find(Project.class, id);
        return Optional.ofNullable(p);
    }

    /** Create + persist new project */
    public Project addProject(String title, String description, java.util.Set<Program> programs, int requiredStudents) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        Project p = new Project(title, description == null ? "" : description, programs, requiredStudents);
        em.persist(p);
        return p;
    }

    /** Update an existing project */
    public Project updateProject(Project project) {
        if (project.getId() == null) {
            throw new IllegalArgumentException("Project id required for update");
        }
        return em.merge(project);
    }

    /** Delete by id */
    public void deleteProject(Long id) {
        Project p = em.find(Project.class, id);
        if (p != null) {
            em.remove(p);
        } else {
            throw new IllegalArgumentException("Project not found: " + id);
        }
    }

    /** Archive project */
    public void archiveProject(Long id) {
        Project p = em.find(Project.class, id);
        if (p == null) {
            throw new IllegalArgumentException("Project not found: " + id);
        }
        p.archive();
        em.merge(p);
    }
}



