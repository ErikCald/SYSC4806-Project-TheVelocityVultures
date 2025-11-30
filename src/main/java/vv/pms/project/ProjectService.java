package vv.pms.project;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import vv.pms.project.internal.ProjectRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /** Find multiple projects by IDs */
    public List<Project> findProjectsByIds(java.util.Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return projectRepository.findAllById(ids);
    }

    /** Create + persist new project */
    public Project addProject(String title, String description, java.util.Set<Program> programs, int requiredStudents) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("Description is required");
        }
        Project p = new Project(title, description, programs, requiredStudents);
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

    /** Find Projects */
    @Transactional(readOnly = true)
    public Page<Project> findProjects(String program, String status, Pageable pageable) {

        // Convert String filters to Enums (or null)
        Program programEnum = (program != null && !program.isBlank()) ? Program.valueOf(program.toUpperCase()) : null;
        ProjectStatus statusEnum = (status != null && !status.isBlank()) ? ProjectStatus.valueOf(status.toUpperCase()) : null;

        // Use a map to hold named parameters
        Map<String, Object> parameters = new HashMap<>();

        // Build the main data query string (JPQL)
        StringBuilder sb = new StringBuilder("SELECT DISTINCT p FROM Project p ");
        sb.append("LEFT JOIN p.programRestrictions pr WHERE 1=1 ");

        if (programEnum != null) {
            sb.append("AND pr = :program ");
            parameters.put("program", programEnum);
        }
        if (statusEnum != null) {
            sb.append("AND p.status = :status ");
            parameters.put("status", statusEnum);
        }
        sb.append("ORDER BY p.title ASC");

        // Create and configure the data query
        TypedQuery<Project> dataQuery = em.createQuery(sb.toString(), Project.class);
        parameters.forEach(dataQuery::setParameter); // Set parameters

        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        // Build the separate COUNT query
        StringBuilder countSb = new StringBuilder("SELECT COUNT(DISTINCT p) FROM Project p ");
        countSb.append("LEFT JOIN p.programRestrictions pr WHERE 1=1 ");

        if (programEnum != null) {
            countSb.append("AND pr = :program ");
        }
        if (statusEnum != null) {
            countSb.append("AND p.status = :status ");
        }

        TypedQuery<Long> countQuery = em.createQuery(countSb.toString(), Long.class);
        parameters.forEach(countQuery::setParameter); // Use same parameters

        // Execute queries and build the Page object
        List<Project> projects = dataQuery.getResultList();
        long totalCount = countQuery.getSingleResult();

        return new PageImpl<>(projects, pageable, totalCount);
    }
}