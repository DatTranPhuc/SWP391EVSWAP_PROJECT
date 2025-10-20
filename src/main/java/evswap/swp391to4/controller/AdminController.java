package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import jakarta.validation.Valid; // Import cho @Valid
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult; // Import để hứng lỗi
import org.springframework.validation.FieldError; // Import để lấy thông tin lỗi
import org.springframework.web.bind.annotation.*;

import java.util.List; // Import cho List

/**
 * Lớp Controller (Người Phục Vụ 🧑‍✈️)
 * "Bắt" tất cả các URL bắt đầu bằng /admin
 * và ra lệnh cho các Service (Bộ não) tương ứng.
 */
@Controller
@RequestMapping("/admin") // Tất cả URL trong file này đều bắt đầu bằng /admin
@RequiredArgsConstructor
public class AdminController {

    // Controller "ra lệnh" cho 2 Service này
    private final StaffService staffService;
    private final StationService stationService;

    // ====================== VIEW DASHBOARD ======================
    /**
     * Hiển thị trang Dashboard chính của Admin.
     * Bắt URL: GET /admin/dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard"; // Trả về file admin/dashboard.html
    }

    // ====================== STAFF ======================

    /**
     * CHỨC NĂNG MỚI: Xử lý xem danh sách VÀ tìm kiếm nhân viên.
     * Bắt URL: GET /admin/staff
     * (hoặc GET /admin/staff?search=tên_cần_tìm)
     */
    @GetMapping("/staff")
    public String listStaff(@RequestParam(value = "search", required = false) String search, Model model) {

        // 1. Ra lệnh cho Service: "Lấy danh sách nhân viên (có tìm kiếm)"
        List<StaffResponse> staffList = staffService.getAllStaff(search);

        // 2. Bỏ danh sách vào "túi" (Model) để gửi cho HTML
        model.addAttribute("staffList", staffList);
        // 3. Bỏ từ khóa tìm kiếm vào "túi" (để hiển thị lại trên ô search)
        model.addAttribute("search", search);

        // 4. Trả về file admin/list-staff.html
        return "admin/list-staff";
    }

    /**
     * Hiển thị form "Thêm nhân viên".
     * Bắt URL: GET /admin/staff/add
     */
    @GetMapping("/staff/add")
    public String addStaffForm(Model model) {
        // Đưa 1 đối tượng rỗng ra form
        model.addAttribute("staff", new StaffCreateRequest());
        return "admin/add-staff"; // Trả về file admin/add-staff.html
    }

    /**
     * CHỨC NĂNG CẬP NHẬT: Xử lý khi bấm nút "Submit" trên form thêm Staff.
     * Đã thêm @Valid và BindingResult để kiểm tra lỗi.
     * Bắt URL: POST /admin/staff/add
     */
    @PostMapping("/staff/add")
    public String addStaffSubmit(
            @Valid @ModelAttribute("staff") StaffCreateRequest staff, // Bật @Valid
            BindingResult bindingResult, // Hứng lỗi (nếu có)
            Model model
    ) {

        // 1. KIỂM TRA LỖI VALIDATION TRƯỚC (lỗi @NotBlank, @Email...)
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi -> Gửi lỗi về lại form HTML
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            model.addAttribute("staff", staff); // Giữ lại dữ liệu người dùng đã gõ
            return "admin/add-staff"; // Trả về lại trang form, KHÔNG gọi Service
        }

        // 2. NẾU KHÔNG CÓ LỖI VALIDATION -> MỚI GỌI SERVICE
        try {
            // Ra lệnh cho Service: "Tạo nhân viên mới"
            staffService.createStaff(staff);
            model.addAttribute("success", "Tạo nhân viên thành công!");
            model.addAttribute("staff", new StaffCreateRequest()); // Xóa form
        } catch (Exception e) {
            // Đây là lỗi từ Service (ví dụ: "Email đã tồn tại")
            model.addAttribute("error", e.getMessage());
            model.addAttribute("staff", staff); // Giữ lại dữ liệu
        }

        return "admin/add-staff"; // Trả về lại trang form (để hiển thị success/error)
    }

    // ====================== STATION ======================

    @GetMapping("/station")
    public String listStations(@RequestParam(value = "search", required = false) String search, Model model) {

        List<StationResponse> stationList;

        // Logic tìm kiếm (dùng các hàm bạn đã có)
        if (search == null || search.isBlank()) {
            // Nếu không tìm -> Lấy tất cả
            stationList = stationService.getAllStations();
        } else {
            // Nếu có tìm -> Gọi hàm searchByName
            stationList = stationService.searchByName(search);
        }

        model.addAttribute("stationList", stationList);
        model.addAttribute("search", search);

        return "admin/list-station"; // Trả về file admin/list-station.html
    }

    /**
     * Hiển thị form "Thêm trạm".
     * Bắt URL: GET /admin/station/add
     */
    @GetMapping("/station/add")
    public String addStationForm(Model model) {
        model.addAttribute("station", new StationCreateRequest());
        return "admin/add-station";
    }

    /**
     * CHỨC NĂNG CẬP NHẬT: Xử lý khi bấm nút "Submit" trên form thêm Station.
     * Đã thêm @Valid và BindingResult để kiểm tra lỗi.
     * Bắt URL: POST /admin/station/add
     */
    @PostMapping("/station/add")
    public String addStationSubmit(
            @Valid @ModelAttribute("station") StationCreateRequest station, // Bật @Valid
            BindingResult bindingResult, // Hứng lỗi
            Model model
    ) {

        // 1. KIỂM TRA LỖI VALIDATION TRƯỚC (tên trống, tọa độ sai...)
        if (bindingResult.hasErrors()) {
            // Nếu có lỗi -> Gửi lỗi về lại form HTML
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            model.addAttribute("station", station); // Giữ lại dữ liệu đã gõ
            return "admin/add-station"; // Trả về lại trang form, KHÔNG gọi Service
        }

        // 2. NẾU KHÔNG CÓ LỖI -> MỚI GỌI SERVICE
        try {
            stationService.createStation(station);
            model.addAttribute("success", "Tạo trạm thành công!");
            model.addAttribute("station", new StationCreateRequest()); // Xóa form
        } catch (Exception e) {
            // Lỗi từ Service (ví dụ: tên trạm đã tồn tại)
            model.addAttribute("error", e.getMessage());
            model.addAttribute("station", station); // Giữ lại dữ liệu
        }

        return "admin/add-station"; // Trả về lại trang form (để hiển thị success/error)
    }
}