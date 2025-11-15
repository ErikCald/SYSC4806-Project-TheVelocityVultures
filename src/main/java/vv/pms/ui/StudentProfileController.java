package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import vv.pms.project.Program;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

@Controller
public class StudentProfileController {

    private final StudentService studentService;

    public StudentProfileController(StudentService studentService) {
        this.studentService = studentService;
    }

    // --- Display student profile ---
    @GetMapping("/student/profile")
    public String viewProfile(HttpSession session, Model model) {
        String role = (String) session.getAttribute("currentUserRole");

        if (role == null || !role.equals("STUDENT"))
            return "redirect:/login";

        Long studentId = (Long) session.getAttribute("currentUserId");

        Student student = studentService.findStudentById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        model.addAttribute("student", student);
        model.addAttribute("programs", Program.values());
        return "student_profile";
    }

    // --- Update student profile ---
    @PostMapping("/student/profile")
    public String updateProfile(@Valid @ModelAttribute("student") Student form,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {

        String role = (String) session.getAttribute("currentUserRole");

        if (role == null || !role.equals("STUDENT"))
            return "redirect:/login";

        if (bindingResult.hasErrors()) {
            model.addAttribute("programs", Program.values());
            return "student_profile";
        }

        Long studentId = (Long) session.getAttribute("currentUserId");

        try {
            studentService.updateStudent(
                    studentId,
                    form.getName(),
                    form.getStudentId(),
                    form.getEmail(),
                    form.getProgram()
            );

            model.addAttribute("success", "Profile updated successfully!");
        } catch (Exception ex) {
            model.addAttribute("error", ex.getMessage());
        }

        model.addAttribute("programs", Program.values());
        return "student_profile";
    }
}
