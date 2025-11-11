package vv.pms.auth;

import org.springframework.stereotype.Service;
import vv.pms.professor.ProfessorService;
import vv.pms.student.StudentService;

import java.util.Optional;

@Service
public class AuthenticationService {

    private final ProfessorService professorService;
    private final StudentService studentService;

    public AuthenticationService(ProfessorService professorService, StudentService studentService) {
        this.professorService = professorService;
        this.studentService = studentService;
    }

    /**
     * MVP authentication by email only. Returns an auth-local LoginRecord when an account with the email exists.
     * Role is "PROFESSOR" or "STUDENT".
     */
    public Optional<LoginRecord> authenticateByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();

        String cleaned = email.trim();

        return professorService.findByEmail(cleaned)
                .map(p -> new LoginRecord(p.getId(), p.getName(), p.getEmail(), "PROFESSOR"))
                .or(() -> studentService.findByEmail(cleaned)
                        .map(s -> new LoginRecord(s.getId(), s.getName(), s.getEmail(), "STUDENT")));
    }
}
