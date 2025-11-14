package vv.pms.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.professor.ProfessorService;
import vv.pms.project.ProjectService;
import vv.pms.student.StudentService;

@Controller
@RequestMapping("/allocations")
public class AllocationUI {

    private final AllocationService allocationService;
    private final ProfessorService professorService;
    private final ProjectService projectService;
    private final StudentService studentService;

    public AllocationUI(AllocationService allocationService,
                        ProfessorService professorService,
                        ProjectService projectService,
                        StudentService studentService) {
        this.allocationService = allocationService;
        this.professorService = professorService;
        this.projectService = projectService;
        this.studentService = studentService;
    }

    @GetMapping
    public String listAllocations(Model model) {
        model.addAttribute("allocations", allocationService.findAllAllocations());
        model.addAttribute("projects", projectService.findAllProjects());
        model.addAttribute("professors", professorService.findAllProfessors());
        model.addAttribute("students", studentService.findAllStudents());
        return "allocations";
    }

    @PostMapping("/create")
    public String createAllocation(@RequestParam Long projectId,
                                   @RequestParam Long professorId,
                                   Model model) {
        try {
            allocationService.assignProfessorToProject(projectId, professorId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/remove")
    public String removeAllocation(@RequestParam Long projectId, Model model) {
        try {
            allocationService.removeProfessorAllocation(projectId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/assign-student")
    public String assignStudent(@RequestParam Long projectId,
                                @RequestParam Long studentId,
                                Model model) {
        try {
            allocationService.assignStudentToProject(projectId, studentId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/unassign-student")
    public String unassignStudent(@RequestParam Long projectId,
                                  @RequestParam Long studentId,
                                  Model model) {
        try {
            allocationService.unassignStudentFromProject(projectId, studentId);
            return "redirect:/allocations";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return listAllocations(model);
        }
    }

    @PostMapping("/best-effort")
    public String runBestEffort(Model model) {
        allocationService.runBestEffortAllocation();
        return "redirect:/allocations";
    }
}
