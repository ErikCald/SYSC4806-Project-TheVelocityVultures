package vv.pms.professor.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import vv.pms.professor.Professor;

import java.util.Optional;

public interface ProfessorRepository extends JpaRepository<Professor, Long> {
    Optional<Professor> findByEmail(String email);
}