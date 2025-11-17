package vv.pms.auth;

import org.springframework.stereotype.Service;


import java.util.Optional;

import vv.pms.professor.Professor;
import vv.pms.professor.ProfessorService;
import vv.pms.student.Student;
import vv.pms.student.StudentService;

@Service
public class AuthenticationService {

    private final ProfessorService professorService;
    private final StudentService studentService;

    public AuthenticationService(ProfessorService professorService, StudentService studentService) {
        this.professorService = professorService;
        this.studentService = studentService;
    }

    /**
     * Authentication by email only. Returns an auth-local LoginRecord when an account with the email exists.
     * 
     * @param email The email to authenticate.
     * @return An Optional LoginRecord if authentication is successful, otherwise empty.
     */
    public Optional<LoginRecord> authenticateByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        String cleaned = email.trim();

        Optional<Professor> profOpt = professorService.findByEmail(cleaned);
        if (profOpt.isPresent()) {
            Professor p = profOpt.get();
            return Optional.of(new LoginRecord(p.getId(), p.getName(), p.getEmail(), "PROFESSOR"));
        }

        Optional<Student> studentOpt = studentService.findByEmail(cleaned);
        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();

            return Optional.of(new LoginRecord(s.getId(), s.getName(), s.getEmail(), "STUDENT"));
        }

        return Optional.empty();
    }
}
