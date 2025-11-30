package vv.pms.project;

import org.springframework.context.annotation.Lazy; //IMPORT TO FIX CIRCULAR DEPENDENCY ISSUE
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.project.internal.ProjectRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final AllocationService allocationService;

    @PersistenceContext
    private EntityManager em;

    // Added @Lazy to fix circular dependency loop error. This tells Spring: "Start ProjectService now,
    // but wait to create AllocationService until we actually use it."
    public ProjectService(ProjectRepository projectRepository, @Lazy AllocationService allocationService) {
        this.projectRepository = projectRepository;
        this.allocationService = allocationService;
    }

    /** Helper method to check authorization */
    private void checkModificationAuthorization(Long projectId, Long requestingProfessorId, boolean isCoordinator) {
        // Coordinators bypass all checks
        if (isCoordinator) {
            return;
        }

        // Retrieve allocation to find the owner
        Optional<ProjectAllocation> allocation = allocationService.findAllocationByProjectId(projectId);

        if (allocation.isEmpty()) {
            // No owner assigned: Only Coordinator can modify
            throw new UnauthorizedAccessException("Project has no owner. Only Coordinators can modify it.");
        }

        // Check if requester matches the owner
        if (!allocation.get().getProfessorId().equals(requestingProfessorId)) {
            throw new UnauthorizedAccessException("Only the Project Owner or a Coordinator can modify this project.");
        }
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public List<Project> findAllProjects() {
        return em.createQuery("SELECT p FROM Project p ORDER BY p.id", Project.class).getResultList();
    }

    public Optional<Project> findProjectById(Long id) {
        if (id == null) return Optional.empty();
        Project p = em.find(Project.class, id);
        return Optional.ofNullable(p);
    }

    public Project addProject(String title, String description, java.util.Set<Program> programs, int requiredStudents, Long professorId) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("Description is required");
        if (professorId == null) throw new IllegalArgumentException("Professor ID is required to create a project");

        Project p = new Project(title, description, programs, requiredStudents);
        em.persist(p);

        // Flush to ensure Project ID is generated before allocation
        em.flush();

        // Modulith Requirement: Call AllocationService Public API
        allocationService.assignProfessorToProject(p.getId(), professorId);

        return p;
    }

    /** Update an existing project with Auth Check */
    public Project updateProject(Project project, Long requestingProfessorId, boolean isCoordinator) {
        if (project.getId() == null) throw new IllegalArgumentException("Project id required for update");

        checkModificationAuthorization(project.getId(), requestingProfessorId, isCoordinator);

        return em.merge(project);
    }

    /** Delete by id with Auth Check */
    public void deleteProject(Long id, Long requestingProfessorId, boolean isCoordinator) {
        checkModificationAuthorization(id, requestingProfessorId, isCoordinator);

        Project p = em.find(Project.class, id);
        if (p != null) {
            em.remove(p);
        } else {
            throw new IllegalArgumentException("Project not found: " + id);
        }
    }

    /** Archive project with Auth Check */
    public void archiveProject(Long id, Long requestingProfessorId, boolean isCoordinator) {
        checkModificationAuthorization(id, requestingProfessorId, isCoordinator);

        Project p = em.find(Project.class, id);
        if (p == null) throw new IllegalArgumentException("Project not found: " + id);

        p.archive();
        em.merge(p);
    }

    @Transactional(readOnly = true)
    public Page<Project> findProjects(String program, String status, Pageable pageable) {
        Program programEnum = (program != null && !program.isBlank()) ? Program.valueOf(program.toUpperCase()) : null;
        ProjectStatus statusEnum = (status != null && !status.isBlank()) ? ProjectStatus.valueOf(status.toUpperCase()) : null;

        Map<String, Object> parameters = new HashMap<>();
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

        TypedQuery<Project> dataQuery = em.createQuery(sb.toString(), Project.class);
        parameters.forEach(dataQuery::setParameter);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        StringBuilder countSb = new StringBuilder("SELECT COUNT(DISTINCT p) FROM Project p ");
        countSb.append("LEFT JOIN p.programRestrictions pr WHERE 1=1 ");
        if (programEnum != null) countSb.append("AND pr = :program ");
        if (statusEnum != null) countSb.append("AND p.status = :status ");

        TypedQuery<Long> countQuery = em.createQuery(countSb.toString(), Long.class);
        parameters.forEach(countQuery::setParameter);

        List<Project> projects = dataQuery.getResultList();
        long totalCount = countQuery.getSingleResult();

        return new PageImpl<>(projects, pageable, totalCount);
    }
}