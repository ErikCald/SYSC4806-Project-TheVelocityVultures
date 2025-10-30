package vv.pms.project.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.project.Project;
import java.util.List;

// Placed in 'internal' to restrict direct access from other modules

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTitleContainingIgnoreCase(String title);
}