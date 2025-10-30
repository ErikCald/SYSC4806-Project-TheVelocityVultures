package vv.pms.professor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vv.pms.professor.internal.ProfessorRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProfessorService {

    private final ProfessorRepository repository;

    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    /**
     * CREATE: Adds a new professor to the system.
     */
    public Professor addProfessor(String name, String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Professor with email " + email + " already exists.");
        }
        Professor newProfessor = new Professor(name, email);
        return repository.save(newProfessor);
    }

    /**
     * READ: Retrieves a professor by their ID.
     * This is crucial for cross-module communication (e.g., by the Allocation module).
     */
    @Transactional(readOnly = true)
    public Optional<Professor> findProfessorById(Long id) {
        return repository.findById(id);
    }

    /**
     * READ: Retrieves all professors.
     */
    @Transactional(readOnly = true)
    public List<Professor> findAllProfessors() {
        return repository.findAll();
    }

    /**
     * DELETE: Removes a professor by ID.
     * NOTE: Real-world systems require complex checks before deletion (e.g., are they assigned to any projects?).
     */
    public void deleteProfessor(Long id) {
        if (!repository.existsById(id)) {
            throw new ProfessorNotFoundException("Professor with ID " + id + " not found.");
        }
        repository.deleteById(id);
    }
}

class ProfessorNotFoundException extends RuntimeException {
    public ProfessorNotFoundException(String message) {
        super(message);
    }
}