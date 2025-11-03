package vv.pms.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.ui.records.ProjectForm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping("/projects")
public class ProjectUI {

    private final ProjectService projectService;

    public ProjectUI(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("programs", Arrays.asList(Program.values()));
        model.addAttribute("projectForm", new ProjectForm());
        return "projects";
    }

    @GetMapping("/edit/{id}")
    public String editProject(@PathVariable Long id, Model model) {
        Project project = projectService.findProjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + id));

        List<Program> list = new ArrayList<>(project.getProgramRestrictions());

        ProjectForm form = new ProjectForm(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                list,
                project.getRequiredStudents()
        );

        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("projectForm", form);
        model.addAttribute("programs", Arrays.asList(Program.values()));
        return "projects";
    }

    @PostMapping
    public String handleProjectForm(@Valid @ModelAttribute("projectForm") ProjectForm form,
                                    BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projects", projectService.getAllProjects());
            model.addAttribute("programs", Arrays.asList(Program.values()));
            return "projects";
        }

        if (form.getId() == null) {
            projectService.addProject(
                    form.getTitle(),
                    form.getDescription(),
                    new HashSet<>(form.getProgramRestrictions()),
                    form.getRequiredStudents()
            );
        } else {
            Project p = projectService.findProjectById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getId()));
            p.setTitle(form.getTitle());
            p.setDescription(form.getDescription());
            p.setRequiredStudents(form.getRequiredStudents());
            p.setProgramRestrictions(new HashSet<>(form.getProgramRestrictions()));
            projectService.updateProject(p);
        }

        return "redirect:/projects";
    }

    @PostMapping("/delete")
    public String deleteProject(@RequestParam Long id) {
        projectService.deleteProject(id);
        return "redirect:/projects";
    }

    @PostMapping("/archive")
    public String archiveProject(@RequestParam Long id) {
        projectService.archiveProject(id);
        return "redirect:/projects";
    }
}



