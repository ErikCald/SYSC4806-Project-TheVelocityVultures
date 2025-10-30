package vv.pms.project.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.project.Project;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByTitleContainingIgnoreCase(String title);
}