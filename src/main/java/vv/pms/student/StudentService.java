package vv.pms.student;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.student.internal.StudentRepository;
import java.util.List;
import java.util.Optional;
import vv.pms.common.Program;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repository;

    public StudentService(StudentRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates and saves a new student record.
     * 
     * @param name      The name of the student
     * @param studentId The unique student ID
     * @param email     The email address of the student
     * @param program   The program the student is enrolled in
     * @return The created Student entity
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
     * @param id The primary ID of the student
     * @return An Optional containing the Student if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Student> findStudentById(Long id) {
        return repository.findById(id);
    }

    /**
     * Toggles the project status. Used internally or by the Allocation module after a successful assignment.
     * @param studentId The ID of the student
     * @param hasProject The new project status
     */
    public void updateProjectStatus(Long studentId, boolean hasProject) {
        Student student = repository.findById(studentId)
                .orElseThrow(() -> new StudentNotFoundException("Student ID " + studentId + " not found."));

        student.setHasProject(hasProject);
        repository.save(student);
    }

    /**
     * Finds all students without an assigned project.
     * @return A list of students who do not have a project assigned
     */
    @Transactional(readOnly = true)
    public List<Student> findStudentsWithoutProject() {
        // Example of a custom business query
        return repository.findAll().stream()
                .filter(s -> !s.isHasProject())
                .toList();
    }

    public static class StudentNotFoundException extends RuntimeException {
        public StudentNotFoundException(String message) {
            super(message);
        }
    }
}