// vv.pms.student.StudentService.java
package vv.pms.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.student.internal.StudentRepository; // Import from its own internal package
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import vv.pms.project.Program;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and saves a new student record.
     */
    public Student addStudent(String name, String studentId, String email, Program program) {
        if (repository.findByStudentId(studentId).isPresent()) {
            throw new IllegalArgumentException("Student ID " + studentId + " already exists.");
        }
        Student newStudent = new Student(name, studentId, email, program);
        return repository.save(newStudent);
    }

    /**
     * Retrieves a student by their primary ID. This is the API used by the Allocation module.
     */
    @Transactional(readOnly = true)
    public Optional<Student> findStudentById(Long id) {
        return repository.findById(id);
    }

    /**
     * Find a student by email. Public API used by other modules (e.g., auth).
     */
    @Transactional(readOnly = true)
    public Optional<Student> findByEmail(String email) {
        return repository.findByEmail(email);
    }

    /**
     * Toggles the project status. Used internally or by the Allocation module after a successful assignment.
     */
    public void updateProjectStatus(Long studentId, boolean hasProject) {
        Student student = repository.findById(studentId)
            .orElseThrow(() -> new StudentNotFoundException("Student ID " + studentId + " not found."));
        
        student.setHasProject(hasProject);
        repository.save(student);
    }

    /** Finds all Students for a given set of IDs and returns them in a Map for fast lookups. */
    @Transactional(readOnly = true)
    public Map<Long, Student> findByIds(Set<Long> ids) {
        return repository.findAllById(ids).stream()
                .collect(Collectors.toMap(Student::getId, Function.identity()));
    }
    
    @Transactional(readOnly = true)
    public List<Student> findStudentsWithoutProject() {
        return repository.findAll().stream()
                .filter(s -> !s.isHasProject())
                .toList();
    }
    
    // --- Custom Exception ---
    public static class StudentNotFoundException extends RuntimeException {
        public StudentNotFoundException(String message) {
            super(message);
        }
    }
}