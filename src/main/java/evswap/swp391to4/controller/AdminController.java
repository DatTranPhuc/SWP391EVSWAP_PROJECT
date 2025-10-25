package evswap.swp391to4.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.FeedbackResponse;
import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StaffUpdateRequest;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.dto.TicketSupportResponse;
import evswap.swp391to4.dto.TicketUpdateRequest;
import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.service.FeedbackService;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import evswap.swp391to4.service.TicketSupportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller DÀNH CHO ADMIN
 * ĐÃ ĐƯỢC BẢO MẬT: Mọi hàm đều yêu cầu đăng nhập.
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;
    private final FeedbackService feedbackService;
    private final TicketSupportService ticketService;

    /**
     * HÀM HELPER (NỘI BỘ)
     * Kiểm tra xem Admin đã đăng nhập hay chưa (Người gác cửa)
     */
    private Admin checkAdminLogin(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            // Ném lỗi nếu chưa đăng nhập
            throw new IllegalStateException("Bạn chưa đăng nhập! Vui lòng đăng nhập với tư cách Admin.");
        }
        return admin;
    }

    // ====================== VIEW DASHBOARD ======================
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        return "admin/dashboard";
    }

    // ====================== STAFF (XEM VÀ TẠO) ======================

    @GetMapping("/staff")
    public String listStaff(@RequestParam(value = "search", required = false) String search, Model model, HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        List<StaffResponse> staffList = staffService.getAllStaff(search);
        model.addAttribute("staffList", staffList);
        model.addAttribute("search", search);
        return "admin/list-staff";
    }

    @GetMapping("/staff/add")
    public String addStaffForm(Model model, HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        model.addAttribute("staff", new StaffCreateRequest());
        return "admin/add-staff";
    }

    @PostMapping("/staff/add")
    public String addStaffSubmit(
            @Valid @ModelAttribute("staff") StaffCreateRequest staff,
            BindingResult bindingResult,
            Model model,
            HttpSession session
    ) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
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
    public String editStaffForm(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            StaffUpdateRequest staff = staffService.getStaffDetails(id);
            model.addAttribute("staff", staff);
            return "admin/edit-staff";
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
                                  RedirectAttributes redirect,
                                  HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            model.addAttribute("staff", staff);
            return "admin/edit-staff";
        }
        try {
            staffService.updateStaff(id, staff);
            redirect.addFlashAttribute("success", "Cập nhật nhân viên thành công!");
            return "redirect:/admin/staff";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("staff", staff);
            return "admin/edit-staff";
        }
    }

    // SỬA: Đổi sang @PostMapping cho an toàn
    @PostMapping("/staff/delete/{id}")
    public String deleteStaff(@PathVariable Integer id, RedirectAttributes redirect, HttpSession session) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            staffService.deleteStaff(id);
            redirect.addFlashAttribute("success", "Xóa nhân viên thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/staff";
    }


    // ==========================================================
    // ====================== PHẦN QUẢN LÝ STATION ==================
    // ==========================================================

    // SỬA: Đổi sang "/stations" (số nhiều)
    @GetMapping("/stations")
    public String listStations(@RequestParam(value = "search", required = false) String search, Model model, HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
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

    // SỬA: Đổi sang "/stations/add"
    @GetMapping("/stations/add")
    public String addStationForm(Model model, HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        model.addAttribute("station", new StationCreateRequest());
        return "admin/add-station";
    }

    // SỬA: Đổi sang "/stations/add"
    @PostMapping("/stations/add")
    public String addStationSubmit(
            @Valid @ModelAttribute("station") StationCreateRequest station,
            BindingResult bindingResult,
            Model model,
            HttpSession session
    ) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
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

    // SỬA: Đổi sang "/stations/edit/{id}"
    @GetMapping("/stations/edit/{id}")
    public String editStationForm(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            StationResponse station = stationService.findById(id);
            model.addAttribute("station", station);
            return "admin/edit-station";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // SỬA: Redirect về "/admin/stations"
            return "redirect:/admin/stations";
        }
    }

    /**
     * SỬA LỖI QUAN TRỌNG:
     * Đã sửa lỗi crash khi validation (nhập sai) lúc edit.
     */
    @PostMapping("/stations/edit/{id}") // Keep path variable name as "id"
    public String editStationSubmit(@PathVariable Integer id, // This 'id' is used by the HTML now
                                    @Valid @ModelAttribute("station") StationCreateRequest stationRequest,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirect,
                                    HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP

        // Exactly like editStaffSubmit: Check for validation errors
        if (bindingResult.hasErrors()) {
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            // Return the DTO with errors - HTML no longer crashes!
            model.addAttribute("station", stationRequest);
            return "admin/edit-station"; // Stay on the edit page
        }

        // Exactly like editStaffSubmit: Try to update
        try {
            stationService.updateStation(id, stationRequest);
            redirect.addFlashAttribute("success", "Cập nhật trạm thành công!");
            return "redirect:/admin/stations"; // Go back to the list

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            // Return the DTO with errors
            model.addAttribute("station", stationRequest);
            return "admin/edit-station"; // Stay on the edit page
        }
    }

    // SỬA: Đổi sang @PostMapping cho an toàn
    @PostMapping("/stations/delete/{id}")
    public String deleteStation(@PathVariable Integer id, RedirectAttributes redirect, HttpSession session) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            stationService.deleteStation(id);
            redirect.addFlashAttribute("success", "Xóa trạm thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        // SỬA: Redirect về "/admin/stations"
        return "redirect:/admin/stations";
    }

    // ====================== FEEDBACK (XEM VÀ TẠO) ======================

    @GetMapping("/feedback")
    public String listFeedback(Model model, HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        List<FeedbackResponse> feedbackList = feedbackService.getAllFeedback();
        model.addAttribute("feedbackList", feedbackList);
        return "admin/list-feedback";
    }

    @GetMapping("/feedback/station/{stationId}")
    public String listFeedbackByStation(@PathVariable Long stationId, Model model, HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByStationId(stationId);
        String stationName = stationService.findById(stationId.intValue()).getName();
        model.addAttribute("feedbackList", feedbackList);
        model.addAttribute("stationName", stationName);
        model.addAttribute("stationId", stationId);
        return "admin/list-feedback";
    }

    // ====================== TICKET SUPPORT (XEM VÀ QUẢN LÝ) ======================

    @GetMapping("/tickets")
    public String listTickets(@RequestParam(value = "status", required = false) String status, Model model, HttpSession session) {
        checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
        List<TicketSupportResponse> ticketList;
        if (status == null || status.isEmpty() || "all".equals(status)) {
            ticketList = ticketService.getAllTickets();
        } else {
            ticketList = ticketService.getTicketsByStatus(status);
        }
        model.addAttribute("ticketList", ticketList);
        model.addAttribute("currentStatus", status);
        return "admin/list-tickets";
    }

    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Integer id, Model model, HttpSession session) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            TicketSupportResponse ticket = ticketService.getTicketById(id);
            model.addAttribute("ticket", ticket);
            
            // Lấy danh sách staff để assign
            List<StaffResponse> staffList = staffService.getAllStaff(null);
            model.addAttribute("staffList", staffList);
            
            // Load comments for display
            try {
                List<TicketSupportService.Comment> comments = ticketService.getCommentsByTicketId(id);
                model.addAttribute("comments", comments);
            } catch (Exception e) {
                System.err.println("Error loading comments for ticket " + id + ": " + e.getMessage());
                e.printStackTrace();
                model.addAttribute("comments", new java.util.ArrayList<>());
            }
            
            // Form object cho update
            model.addAttribute("ticketUpdate", new TicketUpdateRequest());
            return "admin/ticket-detail";
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/admin/tickets";
        }
    }

    @PostMapping("/tickets/{id}/update")
    public String updateTicket(@PathVariable Integer id,
                              @Valid @ModelAttribute("ticketUpdate") TicketUpdateRequest ticketUpdate,
                              BindingResult bindingResult,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirect) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            if (bindingResult.hasErrors()) {
                // Load lại dữ liệu nếu có lỗi validation
                TicketSupportResponse ticket = ticketService.getTicketById(id);
                model.addAttribute("ticket", ticket);
                List<StaffResponse> staffList = staffService.getAllStaff(null);
                model.addAttribute("staffList", staffList);
                model.addAttribute("ticketUpdate", ticketUpdate);
                return "admin/ticket-detail";
            }

            // Tạo staff object để update (giả lập staff từ session)
            evswap.swp391to4.entity.Staff staff = new evswap.swp391to4.entity.Staff();
            staff.setStaffId(1); // Giả lập admin staff ID
            
            ticketService.updateTicket(id, ticketUpdate, staff);
            redirect.addFlashAttribute("success", "Cập nhật ticket thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/resolve")
    public String resolveTicket(@PathVariable Integer id,
                               @RequestParam String note,
                               HttpSession session,
                               RedirectAttributes redirect) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            // Tạo staff object để resolve (giả lập staff từ session)
            evswap.swp391to4.entity.Staff staff = new evswap.swp391to4.entity.Staff();
            staff.setStaffId(1); // Giả lập admin staff ID
            
            ticketService.resolveTicket(id, note, staff);
            redirect.addFlashAttribute("success", "Đã đánh dấu ticket là resolved!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/close")
    public String closeTicket(@PathVariable Integer id, RedirectAttributes redirect, HttpSession session) {
        try {
            checkAdminLogin(session); // <-- KIỂM TRA ĐĂNG NHẬP
            ticketService.closeTicket(id);
            redirect.addFlashAttribute("success", "Đã đóng ticket!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/tickets/" + id;
    }

    @PostMapping("/tickets/{id}/comment")
    public String addComment(@PathVariable Integer id,
                           @RequestParam String message,
                           HttpSession session,
                           RedirectAttributes redirect) {
        try {
            checkAdminLogin(session);
            
            // Tạo admin object để comment (giả lập admin staff ID)
            evswap.swp391to4.entity.Staff adminStaff = new evswap.swp391to4.entity.Staff();
            adminStaff.setStaffId(1); // Giả lập admin staff ID
            adminStaff.setFullName("Admin");
            
            ticketService.addComment(id, "admin", "Admin", message);
            redirect.addFlashAttribute("success", "Đã thêm bình luận thành công!");
            
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/admin/tickets/" + id;
    }
}