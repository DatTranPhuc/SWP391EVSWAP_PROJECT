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

    // ====================== STAFF ======================

    // Hiển thị danh sách nhân viên
    @GetMapping("/staff")
    public String viewAllStaff(Model model) {
        model.addAttribute("staffList", staffService.getAllStaff());
        return "admin/staff-list"; // trỏ đến file templates/admin/staff-list.html
    }

    // Hiển thị form thêm nhân viên
    @GetMapping("/staff/add")
    public String showAddStaffForm(Model model) {
        model.addAttribute("staff", new StaffCreateRequest());
        return "admin/staff-add"; // templates/admin/staff-add.html
    }

    // Xử lý thêm nhân viên (POST)
    @PostMapping("/staff/add")
    public String createStaff(@ModelAttribute("staff") StaffCreateRequest req, Model model) {
        try {
            staffService.createStaff(req);
            return "redirect:/admin/staff"; // quay lại danh sách
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/staff-add";
        }
    }

    // ====================== STATION ======================

    // Hiển thị danh sách trạm
    @GetMapping("/station")
    public String viewAllStations(Model model) {
        model.addAttribute("stationList", stationService.getAllStations());
        return "admin/station-list"; // templates/admin/station-list.html
    }

    // Hiển thị form thêm trạm
    @GetMapping("/station/add")
    public String showAddStationForm(Model model) {
        model.addAttribute("station", new StationCreateRequest());
        return "admin/station-add";
    }

    // Xử lý thêm trạm
    @PostMapping("/station/add")
    public String createStation(@ModelAttribute("station") StationCreateRequest req, Model model) {
        try {
            stationService.createStation(req);
            return "redirect:/admin/station";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "admin/station-add";
        }
    }
}
