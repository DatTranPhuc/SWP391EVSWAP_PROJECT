package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StaffUpdateRequest; // <-- Import DTO Sửa Staff
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <-- Import RedirectAttributes

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;

    // ====================== VIEW DASHBOARD ======================
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // ====================== STAFF (XEM VÀ TẠO) ======================

    @GetMapping("/staff")
    public String listStaff(@RequestParam(value = "search", required = false) String search, Model model) {
        List<StaffResponse> staffList = staffService.getAllStaff(search);
        model.addAttribute("staffList", staffList);
        model.addAttribute("search", search);
        return "admin/list-staff";
    }

    @GetMapping("/staff/add")
    public String addStaffForm(Model model) {
        model.addAttribute("staff", new StaffCreateRequest());
        return "admin/add-staff";
    }

    @PostMapping("/staff/add")
    public String addStaffSubmit(
            @Valid @ModelAttribute("staff") StaffCreateRequest staff,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            model.addAttribute("staff", staff);
            return "admin/add-staff";
        }
        try {
            staffService.createStaff(staff);
            model.addAttribute("success", "Tạo nhân viên thành công!");
            model.addAttribute("staff", new StaffCreateRequest());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("staff", staff);
        }
        return "admin/add-staff";
    }

    // ====================== STAFF (SỬA VÀ XÓA) ======================

    @GetMapping("/staff/edit/{id}")
    public String editStaffForm(@PathVariable Integer id, Model model) {
        try {
            StaffUpdateRequest staff = staffService.getStaffDetails(id);
            model.addAttribute("staff", staff);
            return "admin/edit-staff"; // Trả về trang edit-staff.html
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/staff";
        }
    }

    @PostMapping("/staff/edit/{id}")
    public String editStaffSubmit(@PathVariable Integer id,
                                  @Valid @ModelAttribute("staff") StaffUpdateRequest staff,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            model.addAttribute("staff", staff);
            return "admin/edit-staff"; // Trả về trang edit nếu lỗi validation
        }
        try {
            staffService.updateStaff(id, staff);
            redirect.addFlashAttribute("success", "Cập nhật nhân viên thành công!");
            return "redirect:/admin/staff"; // Về trang danh sách
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("staff", staff);
            return "admin/edit-staff"; // Ở lại trang edit
        }
    }

    @GetMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            staffService.deleteStaff(id);
            redirect.addFlashAttribute("success", "Xóa nhân viên thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/staff"; // Về trang danh sách
    }


    // ====================== STATION (XEM VÀ TẠO) ======================

    @GetMapping("/station")
    public String listStations(@RequestParam(value = "search", required = false) String search, Model model) {
        List<StationResponse> stationList;
        if (search == null || search.isBlank()) {
            stationList = stationService.getAllStations();
        } else {
            stationList = stationService.searchByName(search);
        }
        model.addAttribute("stationList", stationList);
        model.addAttribute("search", search);
        return "admin/list-station";
    }

    @GetMapping("/station/add")
    public String addStationForm(Model model) {
        model.addAttribute("station", new StationCreateRequest());
        return "admin/add-station";
    }

    @PostMapping("/station/add")
    public String addStationSubmit(
            @Valid @ModelAttribute("station") StationCreateRequest station,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            model.addAttribute("station", station);
            return "admin/add-station";
        }
        try {
            stationService.createStation(station);
            model.addAttribute("success", "Tạo trạm thành công!");
            model.addAttribute("station", new StationCreateRequest());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("station", station);
        }
        return "admin/add-station";
    }

    // ====================== STATION (SỬA VÀ XÓA) ======================

    @GetMapping("/station/edit/{id}")
    public String editStationForm(@PathVariable Integer id, Model model) {
        try {
            StationResponse station = stationService.findById(id);
            model.addAttribute("station", station);
            return "admin/edit-station"; // Trả về trang edit-station.html
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/admin/station";
        }
    }

    @PostMapping("/station/edit/{id}")
    public String editStationSubmit(@PathVariable Integer id,
                                    @Valid @ModelAttribute("station") StationCreateRequest station,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            model.addAttribute("station", station);
            return "admin/edit-station";
        }
        try {
            stationService.updateStation(id, station);
            redirect.addFlashAttribute("success", "Cập nhật trạm thành công!");
            return "redirect:/admin/station"; // Về trang danh sách
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("station", station);
            return "admin/edit-station"; // Ở lại trang edit
        }
    }

    @GetMapping("/station/delete/{id}")
    public String deleteStation(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            stationService.deleteStation(id);
            redirect.addFlashAttribute("success", "Xóa trạm thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/station"; // Về trang danh sách
    }
}