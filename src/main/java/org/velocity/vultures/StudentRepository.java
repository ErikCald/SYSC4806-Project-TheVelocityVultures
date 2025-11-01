package org.velocity.vultures;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findByHasProjectFalse();
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByEmail(String email);
}
