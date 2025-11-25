package vv.pms.ui;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vv.pms.availability.Availability;
import vv.pms.availability.AvailabilityService;

@Controller
@RequestMapping("/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    @GetMapping
    public String showAvailability(HttpSession session, Model model) {
        // 1. Security Check
        Long userId = (Long) session.getAttribute("currentUserId");
        String userRole = (String) session.getAttribute("currentUserRole");

        if (userId == null || userRole == null) {
            return "redirect:/login";
        }

        // 2. Fetch Data
        Availability availability = availabilityService.getAvailability(userId, userRole);
        model.addAttribute("availability", availability);

        // 3. Setup View Helpers
        model.addAttribute("days", new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"});
        model.addAttribute("timeSlots", generateTimeLabels());

        return "availability"; // Refers to templates/availability.html
    }

    @PostMapping
    public String updateAvailability(HttpSession session, @ModelAttribute Availability availabilityForm) {
        Long userId = (Long) session.getAttribute("currentUserId");
        String userRole = (String) session.getAttribute("currentUserRole");

        if (userId == null || userRole == null) {
            return "redirect:/login";
        }

        // 4. Save Updates
        availabilityService.updateAvailability(userId, userRole, availabilityForm.getTimeslots());

        return "redirect:/availability?success";
    }

    // Helper to generate the 32 time labels (08:00 - 04:00)
    private String[] generateTimeLabels() {
        String[] labels = new String[32];
        int startHour = 8;
        int startMin = 0;

        for (int i = 0; i < 32; i++) {
            int endMin = startMin + 15;
            int endHour = startHour;
            if (endMin == 60) {
                endMin = 0;
                endHour++;
            }

            // Format: "08:00-08:15"
            labels[i] = String.format("%02d:%02d-%02d:%02d", startHour, startMin, endHour, endMin);

            startMin += 15;
            if (startMin == 60) {
                startMin = 0;
                startHour++;
            }
        }
        return labels;
    }
}