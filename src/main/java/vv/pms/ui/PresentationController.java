package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vv.pms.allocation.AllocationService;
import vv.pms.allocation.ProjectAllocation;
import vv.pms.presentation.PresentationService;
import vv.pms.presentation.PresentationService.SlotOption;
import vv.pms.presentation.PresentationSlot;
import vv.pms.presentation.Room;
import vv.pms.presentation.RoomService;
import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.project.Project;
import vv.pms.project.ProjectService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/presentations")
public class PresentationController {

    private final RoomService roomService;
    private final PresentationService presentationService;
    private final AllocationService allocationService;
    private final ProjectService projectService;
    private final ProfessorService professorService;
    private final StudentService studentService;

    public PresentationController(RoomService roomService,
                                  PresentationService presentationService,
                                  AllocationService allocationService,
                                  ProjectService projectService,
                                  ProfessorService professorService,
                                  StudentService studentService) {
        this.roomService = roomService;
        this.presentationService = presentationService;
        this.allocationService = allocationService;
        this.projectService = projectService;
        this.professorService = professorService;
        this.studentService = studentService;
    }

    // ----------------------------------------------------
    // GET main page (Coordinator only)
    // ----------------------------------------------------

    @GetMapping
    public String presentationsPage(HttpSession session, Model model) {
        String role = (String) session.getAttribute("currentUserRole");
        if (role == null) {
            return "redirect:/login";
        }
        if (!"COORDINATOR".equalsIgnoreCase(role)) {
            return "redirect:/home";
        }

        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);

        // Build rows: only projects with valid allocation (professor + ≥1 student)
        List<ProjectAllocation> allocations = allocationService.findAllAllocations().stream()
                .filter(a -> !a.getAssignedStudentIds().isEmpty())
                .collect(Collectors.toList());

        Set<Long> projectIds = allocations.stream()
                .map(ProjectAllocation::getProjectId)
                .collect(Collectors.toSet());

        Map<Long, Project> projects = projectIds.stream()
                .map(projectService::findProjectById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Project::getId, p -> p));

        Set<Long> professorIds = allocations.stream()
                .map(ProjectAllocation::getProfessorId)
                .collect(Collectors.toSet());

        Map<Long, Professor> professors = professorService.findByIds(professorIds);

        Set<Long> studentIds = allocations.stream()
                .flatMap(a -> a.getAssignedStudentIds().stream())
                .collect(Collectors.toSet());

        Map<Long, Student> students = studentService.findByIds(studentIds);

        List<RowView> rows = new ArrayList<>();

        for (ProjectAllocation alloc : allocations) {
            Project project = projects.get(alloc.getProjectId());
            if (project == null) {
                continue;
            }
            Professor prof = professors.get(alloc.getProfessorId());
            if (prof == null) {
                continue;
            }

            List<Student> assignedStudents = alloc.getAssignedStudentIds().stream()
                    .map(students::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            String studentNames = assignedStudents.stream()
                    .map(Student::getName)
                    .collect(Collectors.joining(", "));

            PresentationSlot slot = presentationService.findByProjectId(project.getId())
                    .orElse(null);

            Long selectedRoomId = (slot != null ? slot.getRoomId() :
                    (rooms.isEmpty() ? null : rooms.get(0).getId()));

            List<SlotOption> availableSlots =
                    (selectedRoomId != null
                            ? presentationService.getAvailableSlots(project.getId(), selectedRoomId)
                            : List.of());

            String selectedTimeLabel = null;
            int selectedDayIndex = -1;
            int selectedStartBinIndex = -1;
            if (slot != null) {
                selectedDayIndex = slot.getDayIndex();
                selectedStartBinIndex = slot.getStartBinIndex();
                // If this exact slot is still in availableSlots, reuse its label; otherwise rebuild label via service
                for (SlotOption opt : availableSlots) {
                    if (opt.dayIndex() == selectedDayIndex && opt.startBinIndex() == selectedStartBinIndex) {
                        selectedTimeLabel = opt.label();
                        break;
                    }
                }
                // If it's not in the new availability list, we don't add a selected label; user may need to update.
            }

            RowView row = new RowView();
            row.setProjectId(project.getId());
            row.setProjectTitle(project.getTitle());
            row.setProfessorName(prof.getName());
            row.setStudentNames(studentNames);
            row.setSelectedRoomId(selectedRoomId);
            row.setAvailableSlots(availableSlots);
            row.setSelectedDayIndex(selectedDayIndex);
            row.setSelectedStartBinIndex(selectedStartBinIndex);
            row.setHasAssignedSlot(slot != null);

            rows.add(row);
        }

        model.addAttribute("rows", rows);
        return "presentations";
    }

    // ----------------------------------------------------
    // Room actions
    // ----------------------------------------------------

    @PostMapping("/rooms/add")
    public String addRoom(@RequestParam("name") String name) {
        try {
            roomService.createRoom(name);
        } catch (RuntimeException ignored) {
            // Best-effort, ignore for now or add flash error if you like
        }
        return "redirect:/presentations";
    }

    @PostMapping("/rooms/update")
    public String updateRoom(@RequestParam("id") Long id,
                             @RequestParam("name") String name) {
        try {
            roomService.updateRoom(id, name);
        } catch (RuntimeException ignored) {
        }
        return "redirect:/presentations";
    }

    @PostMapping("/rooms/delete")
    public String deleteRoom(@RequestParam("id") Long id) {
        roomService.deleteRoom(id);
        return "redirect:/presentations";
    }

    // ----------------------------------------------------
    // Assign / Unassign
    // ----------------------------------------------------

    @PostMapping("/assign")
    public String assignPresentation(@RequestParam("projectId") Long projectId,
                                     @RequestParam("roomId") Long roomId,
                                     @RequestParam("slotKey") String slotKey) {
        try {
            String[] parts = slotKey.split("-");
            int dayIndex = Integer.parseInt(parts[0]);
            int startBinIndex = Integer.parseInt(parts[1]);
            presentationService.assignPresentation(projectId, roomId, dayIndex, startBinIndex);
        } catch (RuntimeException ignored) {
        }
        return "redirect:/presentations";
    }

    @PostMapping("/unassign")
    public String unassignPresentation(@RequestParam("projectId") Long projectId) {
        presentationService.unassignPresentation(projectId);
        return "redirect:/presentations";
    }

    // ----------------------------------------------------
    // Best-effort auto assignment
    // ----------------------------------------------------

    @PostMapping("/auto-assign")
    public String autoAssign() {
        presentationService.runBestEffortAllocation();
        return "redirect:/presentations";
    }

    // ----------------------------------------------------
    // View model for the Thymeleaf page
    // ----------------------------------------------------

    public static class RowView {
        private Long projectId;
        private String projectTitle;
        private String professorName;
        private String studentNames;
        private Long selectedRoomId;
        private List<SlotOption> availableSlots;
        private int selectedDayIndex;
        private int selectedStartBinIndex;
        private boolean hasAssignedSlot;

        public Long getProjectId() {
            return projectId;
        }

        public void setProjectId(Long projectId) {
            this.projectId = projectId;
        }

        public String getProjectTitle() {
            return projectTitle;
        }

        public void setProjectTitle(String projectTitle) {
            this.projectTitle = projectTitle;
        }

        public String getProfessorName() {
            return professorName;
        }

        public void setProfessorName(String professorName) {
            this.professorName = professorName;
        }

        public String getStudentNames() {
            return studentNames;
        }

        public void setStudentNames(String studentNames) {
            this.studentNames = studentNames;
        }

        public Long getSelectedRoomId() {
            return selectedRoomId;
        }

        public void setSelectedRoomId(Long selectedRoomId) {
            this.selectedRoomId = selectedRoomId;
        }

        public List<SlotOption> getAvailableSlots() {
            return availableSlots;
        }

        public void setAvailableSlots(List<SlotOption> availableSlots) {
            this.availableSlots = availableSlots;
        }

        public int getSelectedDayIndex() {
            return selectedDayIndex;
        }

        public void setSelectedDayIndex(int selectedDayIndex) {
            this.selectedDayIndex = selectedDayIndex;
        }

        public int getSelectedStartBinIndex() {
            return selectedStartBinIndex;
        }

        public void setSelectedStartBinIndex(int selectedStartBinIndex) {
            this.selectedStartBinIndex = selectedStartBinIndex;
        }

        public boolean isHasAssignedSlot() {
            return hasAssignedSlot;
        }

        public void setHasAssignedSlot(boolean hasAssignedSlot) {
            this.hasAssignedSlot = hasAssignedSlot;
        }
    }
}
