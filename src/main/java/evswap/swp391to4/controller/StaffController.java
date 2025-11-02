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


import org.springframework.web.multipart.MultipartFile;

import evswap.swp391to4.dto.TicketSupportResponse;
import evswap.swp391to4.dto.TicketUpdateRequest;
import evswap.swp391to4.dto.AvailableBatteryResponse;

import evswap.swp391to4.service.TicketSupportService;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.PaymentService;
import evswap.swp391to4.service.QRCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;



@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BatteryService batteryService;
    private final TicketSupportService ticketService;
    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final QRCodeService qrCodeService;


    // (Hàm checkStaffLogin giữ nguyên)
    private Staff checkStaffLogin(HttpSession session) {
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            throw new IllegalStateException("Bạn chưa đăng nhập! Vui lòng đăng nhập với tư cách Staff.");
        }
        if (staff.getStation() == null) {
            throw new IllegalStateException("Tài khoản Staff của bạn chưa được gán vào trạm nào. Vui lòng liên hệ Admin.");
        }
        return staff;
    }

    /**
     * Trang Dashboard (ĐÃ SỬA LẠI DÒNG ĐẾM TỔNG)
     */
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            Station station = staff.getStation();

            model.addAttribute("staffName", staff.getFullName());
            model.addAttribute("stationName", staff.getStation().getName());
            model.addAttribute("stationAddress", staff.getStation().getAddress());
            model.addAttribute("stationId", staff.getStation().getStationId());

            // 3. Lấy thống kê tickets cho staff
            List<TicketSupportResponse> staffTickets = ticketService.getTicketsByStaff(staff.getStaffId());
            long openTicketsCount = staffTickets.stream()
                    .filter(ticket -> "open".equals(ticket.getStatus()) || "in_progress".equals(ticket.getStatus()))
                    .count();
            long resolvedTicketsCount = staffTickets.stream()
                    .filter(ticket -> "resolved".equals(ticket.getStatus()) || "closed".equals(ticket.getStatus()))
                    .count();

            model.addAttribute("openTicketsCount", openTicketsCount);
            model.addAttribute("resolvedTicketsCount", resolvedTicketsCount);

            model.addAttribute("stationName", station.getName());
            model.addAttribute("stationAddress", station.getAddress());
            model.addAttribute("stationId", station.getStationId());

            // Lấy số liệu thống kê
            model.addAttribute("fullCount", batteryService.countBatteriesByState(station, "full"));
            model.addAttribute("chargingCount", batteryService.countBatteriesByState(station, "charging"));
            model.addAttribute("maintenanceCount", batteryService.countBatteriesByState(station, "maintenance"));
            model.addAttribute("retiredCount", batteryService.countBatteriesByState(station, "retired"));

            // ===== SỬA DÒNG NÀY =====
            model.addAttribute("totalCount", batteryService.getAllBatteriesForStation(station).size());

            return "staff/dashboard";

        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Không thể tải dữ liệu dashboard: " + e.getMessage());
            return "staff/dashboard";
        }
    }

    /**
     * Trang Quản lý Pin 
     */
    //abc
    @GetMapping("/batteries")
    public String manageBatteriesPage(
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {

        try {
            Staff staff = checkStaffLogin(session);

            List<Battery> batteryList = batteryService.searchBatteriesForStation(staff.getStation(), searchType, searchTerm);
            model.addAttribute("batteryList", batteryList);
            model.addAttribute("stationName", staff.getStation().getName());
            model.addAttribute("currentSearchType", searchType);
            model.addAttribute("currentSearchTerm", searchTerm);

            if (!model.containsAttribute("newBattery")) {
                model.addAttribute("newBattery", new BatteryCreateRequest());
            }

            return "staff/manage-batteries";

        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            if (e instanceof IllegalStateException) {
                return "redirect:/login";
            }
            return "redirect:/staff/dashboard";
        }
    }

    /**
     * Xử lý Thêm Pin Mới 
     */
    @PostMapping("/batteries/add")
    public String handleCreateBattery(
            @Valid @ModelAttribute("newBattery") BatteryCreateRequest dto,
            BindingResult bindingResult,
            HttpSession session, Model model, RedirectAttributes redirect) {

        Staff staff;
        try {
            staff = checkStaffLogin(session);
        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return loadPageForError(model, staff, "Thông tin nhập không hợp lệ. Vui lòng kiểm tra lại.");
        }

        try {
            batteryService.createBatteries(dto, staff);
            redirect.addFlashAttribute("successMessage", "Đã thêm " + dto.getQuantity() + " pin (Model: " + dto.getModel() + ") thành công!");
            return "redirect:/staff/batteries";

        } catch (Exception logicError) {
            return loadPageForError(model, staff, logicError.getMessage());
        }
    }

    /**
     * HÀM HELPER 
     */
    private String loadPageForError(Model model, Staff staff, String errorMessage) {
        // Hàm này giờ sẽ chạy đúng vì batteryService đã có getAllBatteriesForStation
        List<Battery> batteryList = batteryService.getAllBatteriesForStation(staff.getStation());
        model.addAttribute("batteryList", batteryList);
        model.addAttribute("stationName", staff.getStation().getName());
        model.addAttribute("createError", errorMessage);

        return "staff/manage-batteries";
    }

    /**
     * Xử lý Cập nhật Trạng thái 
     */
    @PostMapping("/batteries/update")
    public String handleUpdateBatteryState(
            @RequestParam("batteryId") Integer batteryId,
            @RequestParam("newState") String newState,
            HttpSession session, RedirectAttributes redirect) {

        try {
            checkStaffLogin(session);
            batteryService.updateBatteryState(batteryId, newState, (Staff) session.getAttribute("loggedInStaff"));
            redirect.addFlashAttribute("successMessage", "Đã cập nhật Pin #" + batteryId + " thành công!");

        } catch (IllegalStateException authError) {
            redirect.addFlashAttribute("loginError", authError.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/batteries";
    }


    /**
     * Trang quản lý tickets của staff
     * URL: GET /staff/tickets
     */
    @GetMapping("/tickets")
    public String listTickets(@RequestParam(value = "status", required = false) String status,
                              Model model, HttpSession session, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);

            List<TicketSupportResponse> ticketList;
            if (status == null || status.isEmpty() || "all".equals(status)) {
                ticketList = ticketService.getTicketsByStaff(staff.getStaffId());
            } else {
                ticketList = ticketService.getTicketsByStatus(status);
                // Filter by staff
                ticketList = ticketList.stream()
                        .filter(ticket -> ticket.getStaffId() != null && ticket.getStaffId().equals(staff.getStaffId()))
                        .collect(java.util.stream.Collectors.toList());
            }

            model.addAttribute("ticketList", ticketList);
            model.addAttribute("currentStatus", status);
            return "staff/tickets";

        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Xem chi tiết ticket
     * URL: GET /staff/tickets/{id}
     */
    @GetMapping("/tickets/{id}")
    public String viewTicket(@PathVariable Integer id, Model model, HttpSession session, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);

            TicketSupportResponse ticket = ticketService.getTicketById(id);

            // Kiểm tra quyền xem ticket (chỉ staff được assign)
            if (ticket.getStaffId() == null || !ticket.getStaffId().equals(staff.getStaffId())) {
                redirect.addFlashAttribute("error", "Bạn không có quyền xem ticket này");
                return "redirect:/staff/tickets";
            }

            model.addAttribute("ticket", ticket);
            model.addAttribute("ticketUpdate", new TicketUpdateRequest());

            // Load comments for display
            try {
                List<TicketSupportService.Comment> comments = ticketService.getCommentsByTicketId(id);
                model.addAttribute("comments", comments);
            } catch (Exception e) {
                System.err.println("Error loading comments for ticket " + id + ": " + e.getMessage());
                model.addAttribute("comments", new java.util.ArrayList<>());
            }

            return "staff/ticket-detail";

        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/staff/tickets";
        }
    }

    /**
     * API: Lấy danh sách comments (JSON) cho staff
     */
    @GetMapping("/tickets/{id}/comments")
    @ResponseBody
    public List<TicketSupportService.Comment> getCommentsJson(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            TicketSupportResponse ticket = ticketService.getTicketById(id);
            if (ticket.getStaffId() == null || !ticket.getStaffId().equals(staff.getStaffId())) {
                return java.util.Collections.emptyList();
            }
            return ticketService.getCommentsByTicketId(id);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    /**
     * Cập nhật ticket
     * URL: POST /staff/tickets/{id}/update
     */
    @PostMapping("/tickets/{id}/update")
    public String updateTicket(@PathVariable Integer id,
                               @ModelAttribute("ticketUpdate") TicketUpdateRequest ticketUpdate,
                               @RequestParam(required = false) String message,
                               @RequestParam(value = "attachment", required = false) MultipartFile attachment,
                               HttpSession session,
                               RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            
            // Cập nhật ticket
            ticketService.updateTicket(id, ticketUpdate, staff);

            // Nếu có message, thêm comment
            if (message != null && !message.trim().isEmpty()) {
                ticketService.addComment(id, "staff", staff.getFullName(), message);
            }

            // Nếu có file đính kèm, upload file
            if (attachment != null && !attachment.isEmpty()) {
                try {
                    // Validate file size (5MB)
                    if (attachment.getSize() > 5 * 1024 * 1024) {
                        redirect.addFlashAttribute("error", "File quá lớn! Tối đa 5MB.");
                        return "redirect:/staff/tickets/" + id;
                    }

                    // Validate file type
                    String contentType = attachment.getContentType();
                    if (contentType == null || (!contentType.startsWith("image/") &&
                            !contentType.equals("application/pdf") &&
                            !contentType.equals("application/msword") &&
                            !contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))) {
                        redirect.addFlashAttribute("error", "Chỉ chấp nhận file ảnh, PDF hoặc Word!");
                        return "redirect:/staff/tickets/" + id;
                    }

                    // Upload file
                    ticketService.saveAttachment(id, attachment);
                } catch (Exception e) {
                    redirect.addFlashAttribute("error", "Lỗi upload file: " + e.getMessage());
                    return "redirect:/staff/tickets/" + id;
                }
            }

            redirect.addFlashAttribute("success", "Cập nhật ticket thành công!");

        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/tickets/" + id;
    }

    /**
     * Thêm comment vào ticket
     * URL: POST /staff/tickets/{id}/comment
     */
    @PostMapping("/tickets/{id}/comment")
    public String addComment(@PathVariable Integer id,
                           @RequestParam String message,
                           HttpSession session,
                           RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            
            ticketService.addComment(id, "staff", staff.getFullName(), message);
            redirect.addFlashAttribute("success", "Đã thêm bình luận thành công!");
            
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        
        return "redirect:/staff/tickets/" + id;
    }

    /**
     * Danh sách reservations tại trạm của staff
     * URL: GET /staff/reservations
     */
    @GetMapping("/reservations")
    public String listReservations(@RequestParam(value = "status", required = false) String status,
                                   @RequestParam(value = "date", required = false) String date,
                                   Model model, HttpSession session, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            Station station = staff.getStation();

            // Get reservations for this station
            List<evswap.swp391to4.service.ReservationService.ReservationSummary> reservations =
                    reservationService.getUpcomingReservationsForStation(station.getStationId());

            // Filter by status if provided
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                reservations = reservations.stream()
                        .filter(r -> status.equalsIgnoreCase(r.status()))
                        .collect(java.util.stream.Collectors.toList());
            }

            model.addAttribute("reservations", reservations);
            model.addAttribute("currentStatus", status);
            model.addAttribute("stationName", station.getName());
            return "staff/reservation-list";

        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Không tải được danh sách đặt lịch: " + e.getMessage());
            model.addAttribute("reservations", java.util.Collections.emptyList());
            model.addAttribute("currentStatus", status);
            return "staff/reservation-list";
        }
    }

    /**
     * Chi tiết reservation
     * URL: GET /staff/reservations/{id}
     */
    @GetMapping("/reservations/{id}")
    public String viewReservationDetail(@PathVariable Integer id, 
                                       @RequestParam(value = "paymentSuccess", required = false) Boolean paymentSuccess,
                                       Model model, HttpSession session, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);

            // Get reservation details
            evswap.swp391to4.entity.Reservation reservation =
                    reservationService.getReservationById(id);

            // Check if reservation belongs to this station
            if (!reservation.getStation().getStationId().equals(staff.getStation().getStationId())) {
                redirect.addFlashAttribute("error", "Reservation không thuộc trạm của bạn");
                return "redirect:/staff/reservations";
            }

            model.addAttribute("reservation", reservation);
            model.addAttribute("stationName", staff.getStation().getName());
            
            // Show payment success message if redirected from PayOS (use model attribute instead of flash to avoid double redirect)
            if (Boolean.TRUE.equals(paymentSuccess)) {
                model.addAttribute("paymentSuccessMessage", "Thanh toán thành công! Trang sẽ tự động làm mới để hiển thị trạng thái thanh toán cập nhật.");
            }
            
            // Load available batteries for this vehicle at this station if status is checked_in
            if ("checked_in".equals(reservation.getStatus()) || "confirmed".equals(reservation.getStatus())) {
                if (reservation.getVehicle() != null) {
                    List<AvailableBatteryResponse> availableBatteries = 
                        batteryService.findEligibleBatteriesForVehicle(
                            reservation.getStation().getStationId(), 
                            reservation.getVehicle().getVehicleId());
                    model.addAttribute("availableBatteries", availableBatteries);
                }
            }
            
            return "staff/reservation-detail";

        } catch (IllegalStateException e) {
            // Preserve the redirect URL with paymentSuccess parameter if present
            String redirectUrl = "/staff/reservations/" + id;
            if (Boolean.TRUE.equals(paymentSuccess)) {
                redirectUrl += "?paymentSuccess=true";
            }
            redirect.addFlashAttribute("loginError", e.getMessage());
            // Pass redirect URL as query parameter so it persists through redirect
            try {
                return "redirect:/login?redirect=" + java.net.URLEncoder.encode(redirectUrl, java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                return "redirect:/login";
            }
        }
    }

    /**
     * Xác nhận reservation
     * URL: POST /staff/reservations/{id}/confirm
     */
    @PostMapping("/reservations/{id}/confirm")
    public String confirmReservation(@PathVariable Integer id,
                                     HttpSession session,
                                     RedirectAttributes redirect) {
        try {
            checkStaffLogin(session);

            reservationService.confirmReservation(id);

            redirect.addFlashAttribute("success", "Đã xác nhận reservation thành công!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/reservations/" + id;
    }

    /**
     * Hoàn tất đổi pin
     * URL: POST /staff/reservations/{id}/complete
     */
    @PostMapping("/reservations/{id}/complete")
    public String completeReservation(@PathVariable Integer id,
                                      @RequestParam(name = "batteryId", required = false) Integer batteryId,
                                      HttpSession session,
                                      RedirectAttributes redirect) {
        try {
            checkStaffLogin(session);

            // Chỉ đảm bảo reservation thuộc trạm của staff thông qua view page đã kiểm tra; ở đây cứ chạy logic
            reservationService.completeSwap(id, batteryId);

            redirect.addFlashAttribute("success", "Đã hoàn tất đổi pin!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/reservations/" + id;
    }

    /**
     * Gán pin thủ công trước khi hoàn tất
     * URL: POST /staff/reservations/{id}/assign-battery
     */
    @PostMapping("/reservations/{id}/assign-battery")
    public String assignBattery(@PathVariable Integer id,
                                @RequestParam("batteryId") Integer batteryId,
                                HttpSession session,
                                RedirectAttributes redirect) {
        try {
            checkStaffLogin(session);

            reservationService.reassignBattery(id, batteryId);
            redirect.addFlashAttribute("success", "Đã gán pin thành công!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/staff/reservations/" + id;
    }

    /**
     * Check-in thủ công
     * URL: POST /staff/reservations/{id}/check-in
     */
    @PostMapping("/reservations/{id}/check-in")
    public String checkInReservation(@PathVariable Integer id,
                                     HttpSession session,
                                     RedirectAttributes redirect) {
        try {
            checkStaffLogin(session);

            reservationService.checkInReservation(id);

            redirect.addFlashAttribute("success", "Check-in thành công!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/reservations/" + id;
    }

    /**
     * Xác nhận thanh toán tiền mặt tại trạm
     * URL: POST /staff/reservations/{id}/confirm-cash-payment
     */
    @PostMapping("/reservations/{id}/confirm-cash-payment")
    public String confirmCashPayment(@PathVariable Integer id,
                                     HttpSession session,
                                     RedirectAttributes redirect) {
        try {
            checkStaffLogin(session);

            reservationService.confirmCashPayment(id);

            redirect.addFlashAttribute("success", "Đã xác nhận thanh toán tiền mặt!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/reservations/" + id;
    }

    /**
     * Tạo QR chuyển khoản PayOS cho reservation
     * URL: POST /staff/reservations/{id}/create-transfer-qr
     */
    @PostMapping("/reservations/{id}/create-transfer-qr")
    public String createTransferQr(@PathVariable Integer id,
                                   HttpSession session,
                                   RedirectAttributes redirect) {
        try {
            checkStaffLogin(session);

            evswap.swp391to4.entity.Reservation reservation = reservationService.getReservationById(id);
            evswap.swp391to4.entity.Driver driver = reservation.getDriver();
            java.math.BigDecimal amount = reservation.getPriceAmount();

            paymentService.createSwapPaymentRequest(driver, reservation, amount);

            redirect.addFlashAttribute("success", "Đã tạo QR code thanh toán! QR code sẽ hiển thị trong trang chi tiết.");
        } catch (IllegalStateException e) {
            // Check if it's an authentication error (contains "chưa đăng nhập" or "Bạn chưa")
            if (e.getMessage() != null && (e.getMessage().contains("chưa đăng nhập") || e.getMessage().contains("Bạn chưa"))) {
                redirect.addFlashAttribute("loginError", e.getMessage());
                return "redirect:/login";
            }
            // Otherwise it's a business logic error (like PayOS error)
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/reservations/" + id;
    }

    /**
     * Hiển thị QR code PayOS cho reservation
     * URL: GET /staff/reservations/{id}/payment-qr
     */
    @GetMapping("/reservations/{id}/payment-qr")
    @ResponseBody
    public java.util.Map<String, Object> getPaymentQr(@PathVariable Integer id,
                                                       HttpSession session) {
        try {
            checkStaffLogin(session);

            evswap.swp391to4.entity.Reservation reservation = reservationService.getReservationById(id);
            
            // Check if reservation belongs to this station
            Staff staff = (Staff) session.getAttribute("loggedInStaff");
            if (!reservation.getStation().getStationId().equals(staff.getStation().getStationId())) {
                return java.util.Map.of("error", "Reservation không thuộc trạm của bạn");
            }
            
            // Tìm payment record cho reservation này
            java.util.List<evswap.swp391to4.entity.Payment> payments = paymentService.getPaymentsByReservation(reservation);
            
            evswap.swp391to4.entity.Payment payment = payments.stream()
                .filter(p -> p.getCheckoutUrl() != null && !p.getCheckoutUrl().isBlank())
                .filter(p -> "pending".equals(p.getStatus()) || "succeed".equals(p.getStatus()))
                .findFirst()
                .orElse(null);
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            if (payment != null) {
                result.put("qrUrl", payment.getCheckoutUrl());
                result.put("paymentStatus", payment.getStatus());
                result.put("reservationPaymentStatus", reservation.getPaymentStatus());
            } else {
                result.put("error", "Chưa có QR code thanh toán cho reservation này");
            }
            
            return result;
        } catch (IllegalStateException e) {
            // Check if it's an authentication error
            if (e.getMessage() != null && (e.getMessage().contains("chưa đăng nhập") || e.getMessage().contains("Bạn chưa"))) {
                return java.util.Map.of("error", "Authentication required", "authError", true);
            }
            return java.util.Map.of("error", e.getMessage());
        } catch (Exception e) {
            return java.util.Map.of("error", "Lỗi: " + e.getMessage());
        }
    }
    
    /**
     * API để check payment status (nhẹ hơn, chỉ trả về status)
     * URL: GET /staff/reservations/{id}/payment-status
     */
    @GetMapping("/reservations/{id}/payment-status")
    @ResponseBody
    public java.util.Map<String, Object> getPaymentStatus(@PathVariable Integer id,
                                                           HttpSession session) {
        try {
            checkStaffLogin(session);

            evswap.swp391to4.entity.Reservation reservation = reservationService.getReservationById(id);
            
            // Check if reservation belongs to this station
            Staff staff = (Staff) session.getAttribute("loggedInStaff");
            if (!reservation.getStation().getStationId().equals(staff.getStation().getStationId())) {
                return java.util.Map.of("error", "Reservation không thuộc trạm của bạn");
            }
            
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("paymentStatus", reservation.getPaymentStatus());
            result.put("paymentMethod", reservation.getPaymentMethod());
            
            // Check latest payment status
            java.util.List<evswap.swp391to4.entity.Payment> payments = paymentService.getPaymentsByReservation(reservation);
            if (!payments.isEmpty()) {
                evswap.swp391to4.entity.Payment latestPayment = payments.stream()
                    .max(java.util.Comparator.comparing(evswap.swp391to4.entity.Payment::getPaidAt, 
                        java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                    .orElse(null);
                if (latestPayment != null) {
                    result.put("latestPaymentStatus", latestPayment.getStatus());
                }
            }
            
            return result;
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && (e.getMessage().contains("chưa đăng nhập") || e.getMessage().contains("Bạn chưa"))) {
                return java.util.Map.of("error", "Authentication required", "authError", true);
            }
            return java.util.Map.of("error", e.getMessage());
        } catch (Exception e) {
            return java.util.Map.of("error", "Lỗi: " + e.getMessage());
        }
    }

    /**
     * Trang check-in với QR code scanner
     * URL: GET /staff/check-in
     */
    @GetMapping("/check-in")
    public String checkInPage(@RequestParam(value = "reservationId", required = false) Integer reservationId,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            model.addAttribute("stationName", staff.getStation().getName());
            
            // Nếu có reservationId, pre-load thông tin reservation
            if (reservationId != null) {
                try {
                    evswap.swp391to4.entity.Reservation reservation = reservationService.getReservationById(reservationId);
                    // Kiểm tra reservation thuộc trạm
                    if (reservation.getStation().getStationId().equals(staff.getStation().getStationId())) {
                        model.addAttribute("preloadReservation", reservation);
                    }
                } catch (Exception e) {
                    // Ignore error, chỉ không pre-load
                }
            }
            
            return "staff/check-in";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    /**
     * Xử lý check-in từ QR token hoặc manual token
     * URL: POST /staff/check-in
     */
    @PostMapping("/check-in")
    public String processCheckIn(@RequestParam(value = "qrToken", required = false) String qrToken,
                                 HttpSession session,
                                 RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            
            if (qrToken == null || qrToken.isBlank()) {
                redirect.addFlashAttribute("error", "Vui lòng nhập QR token hoặc quét QR code");
                return "redirect:/staff/check-in";
            }
            
            // Validate và lấy reservation
            evswap.swp391to4.entity.Reservation reservation = 
                reservationService.getReservationByQrToken(qrToken, staff.getStation().getStationId());
            
            // Check-in reservation
            reservationService.checkInReservation(reservation.getReservationId());
            
            redirect.addFlashAttribute("success", "Check-in thành công! Reservation #" + reservation.getReservationId());
            return "redirect:/staff/reservations/" + reservation.getReservationId();
            
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && (e.getMessage().contains("chưa đăng nhập") || e.getMessage().contains("Bạn chưa"))) {
                redirect.addFlashAttribute("loginError", e.getMessage());
                return "redirect:/login";
            }
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/staff/check-in";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
            return "redirect:/staff/check-in";
        }
    }

    /**
     * API để serve QR code image
     * URL: GET /api/qr-code/{token}
     */
    @GetMapping("/api/qr-code/{token}")
    @ResponseBody
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable String token) {
        try {
            byte[] qrImage = qrCodeService.generateQrCodeImage(token);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentLength(qrImage.length);
            headers.setCacheControl("public, max-age=3600"); // Cache 1 giờ
            
            return new ResponseEntity<>(qrImage, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * API để check-in từ QR code scan (camera hoặc upload ảnh)
     * URL: POST /api/check-in/qr-scan
     */
    @PostMapping("/api/check-in/qr-scan")
    @ResponseBody
    public java.util.Map<String, Object> checkInFromQrScan(@RequestParam(value = "qrToken", required = false) String qrToken,
                                                           @RequestParam(value = "image", required = false) MultipartFile imageFile,
                                                           HttpSession session) {
        try {
            Staff staff = checkStaffLogin(session);
            
            String token = null;
            
            // Nếu có qrToken trực tiếp, dùng luôn
            if (qrToken != null && !qrToken.isBlank()) {
                token = qrToken.trim();
            } 
            // Nếu có upload ảnh, decode QR code từ ảnh
            else if (imageFile != null && !imageFile.isEmpty()) {
                byte[] imageBytes = imageFile.getBytes();
                token = qrCodeService.decodeQrCodeFromImage(imageBytes);
            } 
            else {
                return java.util.Map.of("success", false, "error", "Vui lòng cung cấp QR token hoặc upload ảnh QR code");
            }
            
            // Validate và lấy reservation
            evswap.swp391to4.entity.Reservation reservation = 
                reservationService.getReservationByQrToken(token, staff.getStation().getStationId());
            
            // Trả về thông tin reservation để staff xem trước khi confirm
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("reservationId", reservation.getReservationId());
            result.put("driverName", reservation.getDriver().getFullName());
            result.put("vehicleInfo", reservation.getVehicle().getModel() + " - " + 
                      (reservation.getVehicle().getPlateNumber() != null ? 
                       reservation.getVehicle().getPlateNumber() : reservation.getVehicle().getVin()));
            result.put("reservedStart", reservation.getReservedStart().toString());
            result.put("status", reservation.getStatus());
            result.put("qrToken", token);
            
            return result;
            
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && (e.getMessage().contains("chưa đăng nhập") || e.getMessage().contains("Bạn chưa"))) {
                return java.util.Map.of("success", false, "error", "Authentication required", "authError", true);
            }
            return java.util.Map.of("success", false, "error", e.getMessage());
        } catch (Exception e) {
            return java.util.Map.of("success", false, "error", "Lỗi: " + e.getMessage());
        }
    }

    /**
     * API để confirm check-in sau khi verify QR code
     * URL: POST /api/check-in/confirm
     */
    @PostMapping("/api/check-in/confirm")
    @ResponseBody
    public java.util.Map<String, Object> confirmCheckIn(@RequestParam("qrToken") String qrToken,
                                                        HttpSession session) {
        try {
            Staff staff = checkStaffLogin(session);
            
            // Validate và lấy reservation
            evswap.swp391to4.entity.Reservation reservation = 
                reservationService.getReservationByQrToken(qrToken, staff.getStation().getStationId());
            
            // Check-in reservation
            reservationService.checkInReservation(reservation.getReservationId());
            
            return java.util.Map.of(
                "success", true,
                "message", "Check-in thành công!",
                "reservationId", reservation.getReservationId()
            );
            
        } catch (IllegalStateException e) {
            if (e.getMessage() != null && (e.getMessage().contains("chưa đăng nhập") || e.getMessage().contains("Bạn chưa"))) {
                return java.util.Map.of("success", false, "error", "Authentication required", "authError", true);
            }
            return java.util.Map.of("success", false, "error", e.getMessage());
        } catch (Exception e) {
            return java.util.Map.of("success", false, "error", "Lỗi: " + e.getMessage());
        }
    }

}