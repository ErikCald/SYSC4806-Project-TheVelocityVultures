package vv.pms.student.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.student.Student;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByEmail(String email);
}