package vv.pms.ui.records;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for Professor information, used for communication 
 * between the Web Controller and the HTTP client.
 */
public record ProfessorRecord(
    Long id,
    
    @NotBlank(message = "Name is required.")
    @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters.")
    String name,
    
    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be a valid format.")
    String email
) {
    // This is a record, so boilerplate like getters/setters is handled automatically.
}
