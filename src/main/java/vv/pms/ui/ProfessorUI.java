package vv.pms.ui;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import vv.pms.professor.ProfessorService;
import vv.pms.professor.Professor;
import vv.pms.ui.records.ProfessorForm;
import jakarta.validation.Valid;

/**
 * Serves the application for Professor management.
 */
@Controller
public class ProfessorUI {

    private final ProfessorService professorService;

    @Autowired
    public ProfessorUI(ProfessorService professorService) {
        this.professorService = professorService;
    }

    /**
     * GET /professors : Lists all professors and displays the form for adding/editing.
     * @param model The Spring Model to pass data to the view.
     * @param form The ProfessorForm object for binding the creation/edit form.
     * @return The name of the HTML template (professors.html).
     */
    @GetMapping("/professors")
    public String listProfessors(Model model, ProfessorForm form) {
        // Add the list of all professors to the model for the table
        model.addAttribute("professors", professorService.findAllProfessors());

        // Add the ProfessorForm object to the model for form binding
        if (form.getId() == null) {
            // Provide a new, empty form if one wasn't just submitted
            model.addAttribute("professorForm", new ProfessorForm());
        }
        
        return "professors";
    }
    
    /**
     * GET /professors/edit/{id} : Loads a professor's details into the form for editing.
     * 
     * @param id The ID of the professor to edit.
     * @param model The Spring Model to pass data to the view.
     * @return The name of the HTML template (professors.html) with the form
     */
    @GetMapping("/professors/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Professor professor = professorService.findProfessorById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid professor Id:" + id));
        
        // Populate the form object with existing data
        ProfessorForm form = new ProfessorForm(professor.getId(), professor.getName(), professor.getEmail());
        
        // Load the list of professors again and the pre-populated form
        model.addAttribute("professors", professorService.findAllProfessors());
        model.addAttribute("professorForm", form);
        
        // We use a query parameter to signal to the template that the form should be displayed in 'edit' mode.
        return "professors"; 
    }

    /**
     * POST /professors : Handles the form submission for both creating and modifying a professor.
     * 
     * @param form The ProfessorForm object bound from the submitted form.
     * @param result The BindingResult containing validation results.
     * @param model The Spring Model to pass data to the view.
     * @return Redirects to /professors on success, or reloads the form with errors on failure.
     */
    @PostMapping("/professors")
    public String handleFormSubmission(@Valid ProfessorForm form, BindingResult result, Model model) {
        if (result.hasErrors()) {
            // Re-add the list of professors and the form with errors to the model
            model.addAttribute("professors", professorService.findAllProfessors());
            return "professors";
        }
        
        try {
            if (form.getId() == null) {
                // CREATE new professor
                professorService.addProfessor(form.getName(), form.getEmail());
            } else {
                // UPDATE existing professor using the modifyProfessor logic
                professorService.modifyProfessor(form.getId(), form.getName(), form.getEmail());
            }
        } catch (Exception e) {
            // Handle business logic errors (like duplicate email) by adding an error to the model
            model.addAttribute("professors", professorService.findAllProfessors());
            model.addAttribute("submissionError", "Submission failed: " + e.getMessage());
            return "professors";
        }

        // Redirect to prevent form resubmission on refresh
        return "redirect:/professors";
    }

    /**
     * POST /professors/delete : Handles deletion submitted via a hidden form.
     * @param id The ID of the professor to delete.
     * @return Redirects to /professors after deletion.
     */
    @PostMapping("/professors/delete")
    public String deleteProfessor(@RequestParam Long id) {
        try {
            professorService.deleteProfessor(id);
        } catch (Exception e) {
            // Log error but redirect back to list
            System.err.println("Error deleting professor: " + e.getMessage());
        }
        return "redirect:/professors";
    }
}
