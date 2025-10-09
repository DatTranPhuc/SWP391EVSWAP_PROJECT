package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;

    /**
     * 🧭 Trang dashboard admin
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<StaffResponse> staffList = staffService.getAllStaff();
        List<StationResponse> stationList = stationService.getAllStations();
        model.addAttribute("staffList", staffList);
        model.addAttribute("stationList", stationList);
        return "admin-dashboard"; // Tên file HTML trong /templates/
    }

    /**
     * 🧑‍💼 Form thêm nhân viên (giao diện web)
     */
    @GetMapping("/add-staff")
    public String showAddStaffForm(Model model) {
        model.addAttribute("staffForm", new StaffCreateRequest());
        return "admin-add-staff";
    }

    /**
     * 🧑‍💼 Submit form thêm nhân viên (giao diện web)
     */
    @PostMapping("/add-staff")
    public String handleAddStaffForm(@ModelAttribute("staffForm") StaffCreateRequest req,
                                     RedirectAttributes redirect) {
        try {
            staffService.createStaff(req);
            redirect.addFlashAttribute("success", "Thêm nhân viên thành công!");
            return "redirect:/admin/dashboard";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/add-staff";
        }
    }

    /**
     * 🌐 API thêm nhân viên (cho Postman)
     * POST /api/admin/add-staff
     */
    @PostMapping("/api/add-staff")
    @ResponseBody
    public ResponseEntity<?> addStaffApi(@RequestBody StaffCreateRequest req) {
        try {
            StaffResponse resp = staffService.createStaff(req);
            return ResponseEntity.status(201).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    /**
     * 🌐 API thêm trạm (cho Postman)
     * POST /api/admin/add-station
     */
    @PostMapping("/api/add-station")
    @ResponseBody
    public ResponseEntity<?> addStationApi(@RequestBody StationCreateRequest req) {
        try {
            StationResponse resp = stationService.createStation(req);
            return ResponseEntity.status(201).body(resp);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }
}
