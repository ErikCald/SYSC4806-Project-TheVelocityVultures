package vv.pms.ui;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import vv.pms.project.ProjectService;
import vv.pms.project.Project;
import vv.pms.ui.records.ProjectForm;
import vv.pms.ui.records.ProjectRecord;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/projects")
public class ProjectWebController {

    private final ProjectService projectService;

    public ProjectWebController(ProjectService projectService) {
        this.projectService = projectService;
    }

    private ProjectRecord toRecord(Project p) {
        return new ProjectRecord(
                p.getId(),
                p.getTitle(),
                p.getDescription(),
                p.getProgramRestrictions(),
                p.getRequiredStudents(),
                p.getStatus()
        );
    }

    @GetMapping
    public List<ProjectRecord> findAll() {
        return projectService.findAllProjects().stream()
                .map(this::toRecord)
                .collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<ProjectRecord> createProject(@Valid @RequestBody ProjectRecord dto) {
        try {
            Project project = projectService.addProject(
                    dto.title(),
                    dto.description(),
                    dto.programRestrictions(),
                    dto.requiredStudents()
            );
            return ResponseEntity.status(201).body(toRecord(project));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        try {
            projectService.deleteProject(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<ProjectRecord> archiveProject(@PathVariable Long id) {
        try {
            projectService.archiveProject(id);
            Project updated = projectService.findProjectById(id)
                    .orElseThrow(() -> new RuntimeException("Unexpected: Project not found after archive"));
            return ResponseEntity.ok(toRecord(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/projects")
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("currentProject", new Project()); // required for Thymeleaf form
        return "projects";
    }
}



