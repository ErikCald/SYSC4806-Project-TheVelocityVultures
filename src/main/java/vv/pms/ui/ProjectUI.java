package vv.pms.ui;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

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

//import vv.pms.project.UnauthorizedAccessException;

@Controller
@RequestMapping("/projects")
public class ProjectUI {

    private final ProjectService projectService;
    private final ProfessorService professorService;
    private final AllocationService allocationService;
    private final StudentService studentService;

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
    public String projectDetails(@PathVariable Long id, Model model, HttpSession session) {

        // Get the Project
        Project project = projectService.findProjectById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid project ID: " + id));

        // Find the Allocation
        Optional<ProjectAllocation> allocationOpt = allocationService.findAllocationByProjectId(id);

        ProjectDetailsDTO.ProfessorDTO professorDTO;
        List<ProjectDetailsDTO.StudentDTO> studentDTOs;
        int spotsAvailable;
        Long assignedProfessorId = null; // <--- 1. Initialize variable

        if (allocationOpt.isPresent()) {
            // Allocation EXISTS
            ProjectAllocation allocation = allocationOpt.get();
            assignedProfessorId = allocation.getProfessorId(); // <--- 2. Capture the ID

            // Get Professor
            Professor profEntity = professorService.findProfessorById(allocation.getProfessorId())
                    .orElseThrow(() -> new IllegalArgumentException("Professor not found, but allocation exists!"));
            professorDTO = new ProjectDetailsDTO.ProfessorDTO(
                    profEntity.getId(), profEntity.getName(), profEntity.getEmail());

            // Get Students
            List<Long> studentIds = allocation.getAssignedStudentIds();
            List<Student> studentEntities = (studentIds == null || studentIds.isEmpty()) ? Collections.emptyList() :
                    List.copyOf(studentService.findByIds(Set.copyOf(studentIds)).values());
            studentDTOs = studentEntities.stream()
                    .map(student -> new ProjectDetailsDTO.StudentDTO(
                            student.getId(), student.getName(), student.getStudentId(),
                            student.getEmail(), student.getProgram().toString()
                    )).toList();

            spotsAvailable = project.getRequiredStudents() - studentDTOs.size();

        } else {
            // Allocation is MISSING
            professorDTO = new ProjectDetailsDTO.ProfessorDTO(null, "Unassigned", "");
            studentDTOs = Collections.emptyList();
            spotsAvailable = project.getRequiredStudents();
            assignedProfessorId = null; // Ensure it's null if no allocation
        }

        // Create the final DTO
        ProjectDetailsDTO detailsDTO = new ProjectDetailsDTO(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                project.getStatus().toString(),
                professorDTO,
                studentDTOs,
                spotsAvailable,
                assignedProfessorId // <--- 3. Pass it to the constructor
        );

        // Create the ProjectForm for the Edit Modal
        ProjectForm form = new ProjectForm(
                project.getId(),
                project.getTitle(),
                project.getDescription(),
                new ArrayList<>(project.getProgramRestrictions()),
                project.getRequiredStudents()
        );

        // Determine current user's assignment state
        boolean currentUserIsStudent = false;
        Long currentUserId = null;
        if (session != null && session.getAttribute("currentUserRole") != null) {
            Object roleObj = session.getAttribute("currentUserRole");
            if (roleObj != null && "STUDENT".equalsIgnoreCase(roleObj.toString())) {
                currentUserIsStudent = true;
            }
            Object idObj = session.getAttribute("currentUserId");
            if (idObj != null) {
                try {
                    if (idObj instanceof Number) currentUserId = ((Number) idObj).longValue();
                    else currentUserId = Long.parseLong(idObj.toString());
                } catch (Exception ignored) {
                }
            }
        }

        boolean currentUserAssigned = false;
        if (currentUserId != null) {
            for (ProjectDetailsDTO.StudentDTO s : studentDTOs) {
                if (currentUserId.equals(s.id())) { currentUserAssigned = true; break; }
            }
        }

        // Add all data to the Model
        model.addAttribute("project", detailsDTO);
        model.addAttribute("projectForm", form);
        model.addAttribute("programs", Program.values());
        model.addAttribute("currentUserIsStudent", currentUserIsStudent);
        model.addAttribute("currentUserAssigned", currentUserAssigned);
        model.addAttribute("spotsAvailable", spotsAvailable);

        return "project-details";
    }

    @PostMapping("/apply")
    public String applyToProject(@RequestParam Long projectId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session == null) {
            redirectAttributes.addFlashAttribute("applyError", "You must be logged in to apply.");
            return "redirect:/projects/details/" + projectId;
        }
        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null || !"STUDENT".equalsIgnoreCase(roleObj.toString())) {
            redirectAttributes.addFlashAttribute("applyError", "Only students may apply to projects.");
            return "redirect:/projects/details/" + projectId;
        }
        Object idObj = session.getAttribute("currentUserId");
        if (idObj == null) {
            redirectAttributes.addFlashAttribute("applyError", "Missing user id in session.");
            return "redirect:/projects/details/" + projectId;
        }
        Long studentId;
        try {
            if (idObj instanceof Number) studentId = ((Number) idObj).longValue();
            else studentId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("applyError", "Invalid user id in session.");
            return "redirect:/projects/details/" + projectId;
        }

        try {
            allocationService.assignStudentToProject(projectId, studentId);
            return "redirect:/projects/details/" + projectId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("applyError", e.getMessage());
            return "redirect:/projects/details/" + projectId;
        }
    }

    @PostMapping("/unapply")
    public String unapplyFromProject(@RequestParam Long projectId, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session == null) {
            redirectAttributes.addFlashAttribute("applyError", "You must be logged in to unapply.");
            return "redirect:/projects/details/" + projectId;
        }
        Object roleObj = session.getAttribute("currentUserRole");
        if (roleObj == null || !"STUDENT".equalsIgnoreCase(roleObj.toString())) {
            redirectAttributes.addFlashAttribute("applyError", "Only students may unapply from projects.");
            return "redirect:/projects/details/" + projectId;
        }
        Object idObj = session.getAttribute("currentUserId");
        if (idObj == null) {
            redirectAttributes.addFlashAttribute("applyError", "Missing user id in session.");
            return "redirect:/projects/details/" + projectId;
        }
        Long studentId;
        try {
            if (idObj instanceof Number) studentId = ((Number) idObj).longValue();
            else studentId = Long.parseLong(idObj.toString());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("applyError", "Invalid user id in session.");
            return "redirect:/projects/details/" + projectId;
        }

        try {
            allocationService.unassignStudentFromProject(projectId, studentId);
            return "redirect:/projects/details/" + projectId;
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("applyError", e.getMessage());
            return "redirect:/projects/details/" + projectId;
        }
    }

    // --- HELPER METHODS FOR SESSION ---
    private Long getCurrentUserId(HttpSession session) {
        Object idObj = session.getAttribute("currentUserId");
        if (idObj instanceof Number) return ((Number) idObj).longValue();
        if (idObj != null) return Long.parseLong(idObj.toString());
        return null;
    }

    private boolean isCoordinator(HttpSession session) {
        Object roleObj = session.getAttribute("currentUserRole");
        return roleObj != null && "COORDINATOR".equalsIgnoreCase(roleObj.toString());
    }

    @PostMapping
    public String handleProjectForm(@Valid @ModelAttribute("projectForm") ProjectForm form,
                                    BindingResult result, Model model, HttpSession session) {
        // Security Check
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return "redirect:/login"; // Or handle error

        if (result.hasErrors()) {
            model.addAttribute("projectsPage", projectService.findProjects(null, null, Pageable.unpaged()));
            model.addAttribute("programs", Program.values());
            model.addAttribute("statuses", ProjectStatus.values());
            return "projects";
        }

        if (form.getId() == null) {
            // CREATE: Pass currentUserId as the owner
            Project p = projectService.addProject(
                    form.getTitle(),
                    form.getDescription(),
                    new HashSet<>(form.getProgramRestrictions()),
                    form.getRequiredStudents(),
                    currentUserId // <-- ADDED: Assign Owner
            );
            return "redirect:/projects/details/" + p.getId();

        } else {
            // UPDATE: Check Authorization via Service
            Project p = projectService.findProjectById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Project not found: " + form.getId()));

            p.setTitle(form.getTitle());
            p.setDescription(form.getDescription());
            p.setRequiredStudents(form.getRequiredStudents());
            p.setProgramRestrictions(new HashSet<>(form.getProgramRestrictions()));

            // Pass requesting ID and Coordinator status
            projectService.updateProject(p, currentUserId, isCoordinator(session));

            return "redirect:/projects/details/" + p.getId();
        }
    }

    @PostMapping("/delete")
    public String deleteProject(@RequestParam Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return "redirect:/login";

        projectService.deleteProject(id, currentUserId, isCoordinator(session));
        return "redirect:/projects";
    }

    @PostMapping("/archive")
    public String archiveProject(@RequestParam Long id, HttpSession session) {
        Long currentUserId = getCurrentUserId(session);
        if (currentUserId == null) return "redirect:/login";

        projectService.archiveProject(id, currentUserId, isCoordinator(session));
        return "redirect:/projects/details/" + id;
    }
}