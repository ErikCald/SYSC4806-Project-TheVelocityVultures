package org.velocity.vultures;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProjectTopicRepository extends JpaRepository<ProjectTopic, Long> {
    List<ProjectTopic> findByStatus(ProjectStatus status);
    List<ProjectTopic> findByStatusAndProgramRestrictionsIn(ProjectStatus status, List<Program> programs);
    List<ProjectTopic> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);
}
