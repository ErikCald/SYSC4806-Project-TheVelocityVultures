package vv.pms.ui;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import vv.pms.allocation.ProjectAllocation;
import vv.pms.allocation.AllocationService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Program;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.project.ProjectStatus;
import vv.pms.student.Student;
import vv.pms.student.StudentService;
import vv.pms.ui.records.ProjectDetailsDTO;
import vv.pms.ui.records.ProjectForm;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
public class ProjectUI {

    private final ProjectService projectService;
    private final ProfessorService professorService;
    private final AllocationService allocationService;
    private final StudentService studentService;    //

    public ProjectUI(ProjectService projectService,
                     ProfessorService professorService,
                     AllocationService allocationService,
                     StudentService studentService) {
        this.projectService = projectService;
        this.professorService = professorService;
        this.allocationService = allocationService;
        this.studentService = studentService;
    }

    private record ProjectSummary(
            Long id,
            String title,
            String status,
            String professorName,
            int spotsAvailable
    ) {}

    @GetMapping
    public String listProjects(Model model,
                               @RequestParam(required = false) String program,
                               @RequestParam(required = false) String status,
                               Pageable pageable) {

        Page<Project> projectsPage = projectService.findProjects(program, status, pageable);

        List<Project> projects = projectsPage.getContent();
        Set<Long> projectIds = projects.stream().map(Project::getId).collect(Collectors.toSet());

        Map<Long, ProjectAllocation> allocMap = projectIds.isEmpty() ? Collections.emptyMap() :
                allocationService.findAllocationsByProjectIds(projectIds);

        Set<Long> profIds = allocMap.values().stream()
                .map(ProjectAllocation::getProfessorId)
                .collect(Collectors.toSet());

        Map<Long, Professor> profMap = profIds.isEmpty() ? Collections.emptyMap() :
                professorService.findByIds(profIds);

        Page<ProjectSummary> summaryPage = projectsPage.map(project -> {
            ProjectAllocation alloc = allocMap.get(project.getId());
            Professor prof = (alloc != null) ? profMap.get(alloc.getProfessorId()) : null;
            String profName = (prof != null) ? prof.getName() : "Unassigned";
            int allocCount = (alloc != null) ? alloc.getAssignedStudentIds().size() : 0;
            int spotsAvailable = project.getRequiredStudents() - allocCount;

            return new ProjectSummary(
                    project.getId(),
                    project.getTitle(),
                    project.getStatus().toString(),
                    profName,
                    spotsAvailable
            );
        });

        model.addAttribute("projectsPage", summaryPage); // Pass the Page
        model.addAttribute("programs", Program.values()); // For the filter
        model.addAttribute("statuses", ProjectStatus.values()); // For the filter
        model.addAttribute("selectedProgram", program);
        model.addAttribute("selectedStatus", status);

        model.addAttribute("projectForm", new ProjectForm());

        return "projects"; //
    }


    @GetMapping("/details/{id}")
    public String projectDetails(@PathVariable Long id, Model model) {

        // Get the Project
        Project project = projectService.findProjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + id));

        // Find the Allocation (but DON'T throw an error if it's missing)
        Optional<ProjectAllocation> allocationOpt = allocationService.findAllocationByProjectId(id);

        ProjectDetailsDTO.ProfessorDTO professorDTO;
        List<ProjectDetailsDTO.StudentDTO> studentDTOs;
        int spotsAvailable;

        if (allocationOpt.isPresent()) {
            // Allocation EXISTS
            ProjectAllocation allocation = allocationOpt.get();

            // Get Professor
            Professor profEntity = professorService.findProfessorById(allocation.getProfessorId())
                    .orElseThrow(() -> new IllegalArgumentException("Professor not found, but allocation exists!"));
            professorDTO = new ProjectDetailsDTO.ProfessorDTO(
                    profEntity.getId(), profEntity.getName(), profEntity.getEmail());

            // Get Students
            Set<Long> studentIds = (Set<Long>) allocation.getAssignedStudentIds();
            List<Student> studentEntities = studentIds.isEmpty() ? Collections.emptyList() :
                    List.copyOf(studentService.findByIds(studentIds).values());
            studentDTOs = studentEntities.stream()
                    .map(student -> new ProjectDetailsDTO.StudentDTO(
                            student.getId(), student.getName(), student.getStudentId(),
                            student.getEmail(), student.getProgram().toString()
                    )).toList();

            spotsAvailable = project.getRequiredStudents() - studentDTOs.size();

        } else {
            // Allocation is MISSING
            // Create "empty" or "unassigned" DTOs
            professorDTO = new ProjectDetailsDTO.ProfessorDTO(null, "Unassigned", "");
            studentDTOs = Collections.emptyList();
            spotsAvailable = project.getRequiredStudents();
        }

        // Create the final DTO
        ProjectDetailsDTO detailsDTO = new ProjectDetailsDTO(
                project.getId(), project.getTitle(), project.getDescription(),
                project.getStatus().toString(),
                professorDTO, studentDTOs, spotsAvailable);

        // Create the ProjectForm for the Edit Modal
        ProjectForm form = new ProjectForm(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                new ArrayList<>(project.getProgramRestrictions()),
                project.getRequiredStudents()
        );

        // Add all data to the Model
        model.addAttribute("project", detailsDTO); // For display
        model.addAttribute("projectForm", form); // For the Edit modal
        model.addAttribute("programs", Program.values()); // For the Edit modal

        return "project-details"; //
    }

    @PostMapping
    public String handleProjectForm(@Valid @ModelAttribute("projectForm") ProjectForm form,
                                    BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projectsPage", projectService.findProjects(null, null, Pageable.unpaged()));
            model.addAttribute("programs", Program.values());
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects";
        }

        if (form.getId() == null) {
            Project p = projectService.addProject(
                    form.getTitle(),
                    form.getDescription(),
                    new HashSet<>(form.getProgramRestrictions()),
                    form.getRequiredStudents()
            );
            return "redirect:/projects/details/" + p.getId();

        } else {
            Project p = projectService.findProjectById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getId()));
            p.setTitle(form.getTitle());
            p.setDescription(form.getDescription());
            p.setRequiredStudents(form.getRequiredStudents());
            p.setProgramRestrictions(new HashSet<>(form.getProgramRestrictions()));
            projectService.updateProject(p);

            return "redirect:/projects/details/" + p.getId();
        }
    }

    @PostMapping("/delete")
    public String deleteProject(@RequestParam Long id) {
        projectService.deleteProject(id);
        return "redirect:/projects";
    }

    @PostMapping("/archive")
    public String archiveProject(@RequestParam Long id) {
        projectService.archiveProject(id);
        return "redirect:/projects/details/" + id;
    }
}



