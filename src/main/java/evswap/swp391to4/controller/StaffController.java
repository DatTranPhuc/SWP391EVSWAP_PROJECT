package evswap.swp391to4.controller;

import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.service.BatteryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BatteryService batteryService;

    /**
     * Security Check Helper: Ensures staff is logged in and assigned to a station.
     * Throws IllegalStateException if checks fail.
     */
    private Staff checkLogin(HttpSession session) { // Renamed for brevity
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            throw new IllegalStateException("You are not logged in! Please log in as Staff.");
        }
        if (staff.getStation() == null) {
            throw new IllegalStateException("Your Staff account is not assigned to a station. Please contact an Admin.");
        }
        return staff;
    }

    /**
     * Staff Dashboard Page (GET /staff/dashboard)
     * Displays overview and battery statistics for the staff's station.
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Staff staff = checkLogin(session); // Authentication/Authorization check
            Station station = staff.getStation();

            // Basic Info
            model.addAttribute("staffName", staff.getFullName());
            model.addAttribute("stationName", station.getName());
            model.addAttribute("stationAddress", station.getAddress());
            model.addAttribute("stationId", station.getStationId());

            // Battery Statistics
            model.addAttribute("fullCount", batteryService.countBatteriesByState(station, "full"));
            model.addAttribute("chargingCount", batteryService.countBatteriesByState(station, "charging"));
            model.addAttribute("maintenanceCount", batteryService.countBatteriesByState(station, "maintenance"));
            model.addAttribute("retiredCount", batteryService.countBatteriesByState(station, "retired"));
            model.addAttribute("totalCount", batteryService.getAllBatteriesForStation(station).size()); // Get total count

            return "staff/dashboard";

        } catch (IllegalStateException authError) { // Catch login/assignment errors
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Catch other errors (e.g., database issues)
            model.addAttribute("errorMessage", "Could not load dashboard data: " + e.getMessage());
            // Attempt to still render the basic dashboard layout
            try {
                Staff staff = (Staff) session.getAttribute("loggedInStaff");
                if(staff != null && staff.getStation() != null) {
                    model.addAttribute("staffName", staff.getFullName());
                    model.addAttribute("stationName", staff.getStation().getName());
                }
            } catch (Exception ignored) {} // Ignore potential errors here
            return "staff/dashboard"; // Stay on dashboard but show error
        }
    }

    /**
     * Battery Management Page (GET /staff/batteries)
     * Displays the battery list (with search/filter) and the 'Add Battery' form.
     */
    @GetMapping("/batteries")
    public String manageBatteriesPage(
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            HttpSession session, Model model, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Auth check

            // Fetch battery list based on search/filter
            List<Battery> batteryList = batteryService.searchBatteriesForStation(staff.getStation(), searchType, searchTerm);
            model.addAttribute("batteryList", batteryList);

            // Add necessary info for the view
            model.addAttribute("stationName", staff.getStation().getName());
            model.addAttribute("currentSearchType", searchType);
            model.addAttribute("currentSearchTerm", searchTerm);

            // Prepare empty DTO for the 'Add Battery' form (if not already present due to error)
            if (!model.containsAttribute("newBattery")) {
                model.addAttribute("newBattery", new BatteryCreateRequest());
            }

            return "staff/manage-batteries";

        } catch (IllegalStateException authError) { // Catch login/assignment errors
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Catch other errors
            redirect.addFlashAttribute("errorMessage", "Error loading battery management page: " + e.getMessage());
            return "redirect:/staff/dashboard"; // Redirect to dashboard on other errors
        }
    }

    /**
     * Handle Bulk Battery Creation (POST /staff/batteries/add)
     * Processes the 'Add Battery' form submission.
     */
    @PostMapping("/batteries/add")
    public String handleCreateBattery(
            @Valid @ModelAttribute("newBattery") BatteryCreateRequest dto,
            BindingResult bindingResult,
            HttpSession session, Model model, RedirectAttributes redirect) {

        Staff staff;
        try {
            staff = checkLogin(session); // Auth check first
        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        }

        // Check for validation errors
        if (bindingResult.hasErrors()) {
            // If errors, return to the SAME page to display errors and retain form data
            return loadPageForError(model, staff, "Invalid input. Please check the form again.");
        }

        try {
            // If validation passes, attempt to create batteries
            batteryService.createBatteries(dto, staff);
            redirect.addFlashAttribute("successMessage", "Successfully added " + dto.getQuantity() + " batteries (Model: " + dto.getModel() + ")!");
            return "redirect:/staff/batteries"; // Redirect on success (PRG pattern)

        } catch (Exception logicError) {
            // If service layer throws an error (e.g., invalid state)
            // Return to the SAME page to display the error and retain form data
            return loadPageForError(model, staff, logicError.getMessage());
        }
    }

    /**
     * Helper Method: Reloads the manage-batteries page data when a form error occurs.
     * Ensures the battery list is still displayed alongside the error message.
     */
    private String loadPageForError(Model model, Staff staff, String errorMessage) {
        List<Battery> batteryList = batteryService.getAllBatteriesForStation(staff.getStation()); // Load all batteries
        model.addAttribute("batteryList", batteryList);
        model.addAttribute("stationName", staff.getStation().getName());
        model.addAttribute("createError", errorMessage); // Specific error message for the create form
        // The DTO ('newBattery') with its errors is automatically added back to the model by Spring
        return "staff/manage-batteries"; // Return the view name
    }

    /**
     * Handle Battery State Update (POST /staff/batteries/update)
     * Processes updates to set state to 'charging', 'maintenance', or 'retired'.
     */
    @PostMapping("/batteries/update")
    public String handleUpdateBatteryState(
            @RequestParam("batteryId") Integer batteryId,
            @RequestParam("newState") String newState,
            HttpSession session, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Auth check
            batteryService.updateBatteryState(batteryId, newState, staff);
            redirect.addFlashAttribute("successMessage", "Successfully updated Battery #" + batteryId + " state!");

        } catch (IllegalStateException authError) { // Catch login/assignment errors
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Catch logic errors (invalid state, battery not found, etc.)
            redirect.addFlashAttribute("errorMessage", "Error updating battery: " + e.getMessage());
        }

        return "redirect:/staff/batteries"; // Always redirect back
    }

    /**
     * Handle Simulate Full Charge (POST /staff/batteries/mark-full)
     * Simulates IoT marking a 'charging' battery as 'full'.
     */
    @PostMapping("/batteries/mark-full")
    public String handleMarkAsFull(
            @RequestParam("batteryId") Integer batteryId,
            HttpSession session, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Auth check
            batteryService.markBatteryAsFull(batteryId, staff);
            redirect.addFlashAttribute("successMessage", "Simulated full charge for Battery #" + batteryId + "!");

        } catch (IllegalStateException authError) { // Catch login/assignment errors
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Catch logic errors (not charging, not found, etc.)
            redirect.addFlashAttribute("errorMessage", "Error marking as full: " + e.getMessage());
        }

        return "redirect:/staff/batteries"; // Always redirect back
    }

    /**
     * Handle Simulate Battery Usage (POST /staff/batteries/simulate-usage)
     * Allows staff to manually set the SOC percentage.
     */
    @PostMapping("/batteries/simulate-usage")
    public String handleSimulateUsage(
            @RequestParam("batteryId") Integer batteryId,
            @RequestParam("newSocPercent") Integer newSocPercent,
            HttpSession session, RedirectAttributes redirect) {

        try {
            Staff staff = checkLogin(session); // Auth check
            batteryService.simulateBatteryUsage(batteryId, newSocPercent, staff);
            redirect.addFlashAttribute("successMessage", "Simulated usage for Battery #" + batteryId + " (New SOC: " + newSocPercent + "%)!");

        } catch (IllegalStateException authError) { // Catch login/assignment errors
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) { // Catch logic errors (invalid SOC, not found, etc.)
            redirect.addFlashAttribute("errorMessage", "Error simulating usage: " + e.getMessage());
        }

        return "redirect:/staff/batteries"; // Always redirect back
    }
}