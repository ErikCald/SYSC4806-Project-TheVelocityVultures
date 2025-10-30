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

    /**
     * Constructor for ProfessorService
     */
    public ProfessorService(ProfessorRepository repository) {
        this.repository = repository;
    }

    /**
     * Adds a new professor to the system.
     * @param name  The name of the professor
     * @param email The email of the professor
     * @return The created Professor entity
     */
    public Professor addProfessor(String name, String email) {
        if (repository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Professor with email " + email + " already exists.");
        }
        Professor newProfessor = new Professor(name, email);
        return repository.save(newProfessor);
    }

    /**
     * Retrieves a professor by their ID.
     * This is crucial for cross-module communication (e.g., by the Allocation module).
     * @param id The ID of the professor
     * @return An Optional containing the Professor if found, or empty if not found
     */
    @Transactional(readOnly = true)
    public Optional<Professor> findProfessorById(Long id) {
        return repository.findById(id);
    }

    /**
     * Retrieves all professors.
     * @return A list of all Professor entities
     */
    @Transactional(readOnly = true)
    public List<Professor> findAllProfessors() {
        return repository.findAll();
    }

    /**
     * Removes a professor by ID.
     * TODO: Consider cascading effects required when deleting a professor
     * 
     * @param id The ID of the professor to remove
     * @throws ProfessorNotFoundException if the professor with the given ID does not exist
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