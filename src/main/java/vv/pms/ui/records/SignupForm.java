package vv.pms.ui.records;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import vv.pms.project.Program;

public class SignupForm {

    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Role is required")
    private String role; // STUDENT or PROFESSOR

    // Student-only fields
    private String studentId;
    private Program program;

    public SignupForm() {}

    // getters/setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
}
