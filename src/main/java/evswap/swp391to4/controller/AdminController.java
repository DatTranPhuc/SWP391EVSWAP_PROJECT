package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;

    // ====================== VIEW DASHBOARD ======================
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard"; // Trả về admin/dashboard.html
    }

    // ====================== STAFF ======================
    @GetMapping("/staff/add")
    public String addStaffForm(Model model) {
        model.addAttribute("staff", new StaffCreateRequest());
        return "admin/add-staff"; // admin/add-staff.html
    }

    @PostMapping("/staff/add")
    public String addStaffSubmit(@ModelAttribute StaffCreateRequest staff, Model model) {
        try {
            staffService.createStaff(staff);
            model.addAttribute("success", "Staff created successfully");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "admin/add-staff";
    }

    // ====================== STATION ======================
    @GetMapping("/station/add")
    public String addStationForm(Model model) {
        model.addAttribute("station", new StationCreateRequest());
        return "admin/add-station"; // admin/add-station.html
    }

    @PostMapping("/station/add")
    public String addStationSubmit(@ModelAttribute StationCreateRequest station, Model model) {
        try {
            stationService.createStation(station);
            model.addAttribute("success", "Station created successfully");
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "admin/add-station";
    }
}
