package evswap.swp391to4.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.dto.TicketSupportResponse;
import evswap.swp391to4.dto.TicketUpdateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import evswap.swp391to4.service.BatteryService;
import evswap.swp391to4.service.SwapService;
import evswap.swp391to4.service.TicketSupportService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller DÀNH CHO STAFF 👨‍🔧
 * Hiển thị các trang giao diện (dashboard, quản lý pin...)
 * Yêu cầu: Staff phải đăng nhập (được check bằng hàm checkStaffLogin).
 */

@Controller
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BatteryService batteryService;
    private final SwapService swapService;
    // Ví dụ: Bạn sẽ cần tiêm (Inject) Service để lấy số liệu pin
    // private final BatteryService batteryService;
    private final TicketSupportService ticketService;
    private final ReservationRepository reservationRepo;
    private final BatteryRepository batteryRepo;
    private final SwapTransactionRepository swapRepo;

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
     * API: Lấy comments mới hơn mốc thời gian (ms) cho staff có quyền
     */
    @GetMapping("/tickets/{id}/comments")
    @ResponseBody
    public Object getCommentsSince(@PathVariable Integer id,
                                   @RequestParam(value = "since", required = false) Long since,
                                   HttpSession session) {
        try {
            Staff staff = checkStaffLogin(session);
            TicketSupportResponse ticket = ticketService.getTicketById(id);
            if (ticket.getStaffId() == null || !ticket.getStaffId().equals(staff.getStaffId())) {
                return java.util.Map.of("error", "FORBIDDEN");
            }
            java.time.Instant sinceInstant = since != null ? java.time.Instant.ofEpochMilli(since) : java.time.Instant.EPOCH;
            java.util.List<evswap.swp391to4.service.TicketSupportService.Comment> comments = ticketService.getCommentsSince(id, sinceInstant);
            return comments;
        } catch (IllegalStateException e) {
            return java.util.Map.of("error", "UNAUTHORIZED");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
    }

    // ===================== SWAP MANAGEMENT =====================
    @GetMapping("/swaps")
    public String manageSwapsPage(HttpSession session, Model model, RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            Station station = staff.getStation();

            java.time.Instant startOfDay = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();
            java.time.Instant endOfDay = java.time.LocalDate.now().plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant();

            java.util.List<Reservation> rawReservations = reservationRepo
                .findByStation_StationIdAndReservedStartBetweenOrderByReservedStartAsc(station.getStationId(), startOfDay, endOfDay);

            // Sắp xếp: ưu tiên xử lý (in_progress → confirmed → pending) lên đầu, đã xong (completed/canceled/failed) xuống dưới
            java.util.List<Reservation> reservations = rawReservations.stream()
                .sorted((r1, r2) -> {
                    String s1 = r1.getStatus() != null ? r1.getStatus().toLowerCase() : "";
                    String s2 = r2.getStatus() != null ? r2.getStatus().toLowerCase() : "";
                    boolean r1Done = "completed".equals(s1) || "canceled".equals(s1) || "failed".equals(s1);
                    boolean r2Done = "completed".equals(s2) || "canceled".equals(s2) || "failed".equals(s2);
                    if (r1Done != r2Done) {
                        return r1Done ? 1 : -1; // Chưa xong lên trước
                    }
                    if (!r1Done) {
                        // Trong nhóm chưa xong: in_progress > confirmed > pending
                        int p1 = "in_progress".equals(s1) ? 3 : ("confirmed".equals(s1) ? 2 : 1);
                        int p2 = "in_progress".equals(s2) ? 3 : ("confirmed".equals(s2) ? 2 : 1);
                        if (p1 != p2) return Integer.compare(p2, p1);
                    }
                    // Cùng trạng thái: sắp theo thời gian (sớm hơn lên trước)
                    if (r1.getReservedStart() != null && r2.getReservedStart() != null) {
                        return r1.getReservedStart().compareTo(r2.getReservedStart());
                    }
                    return 0;
                })
                .collect(java.util.stream.Collectors.toList());
            model.addAttribute("reservations", reservations);

            // Queue priority: allow starting only the earliest reservation waiting today
            Integer currentInProgressId = reservations.stream()
                .filter(r -> "in_progress".equalsIgnoreCase(r.getStatus()))
                .map(Reservation::getReservationId)
                .findFirst().orElse(null);
            Integer firstEligibleId = null;
            if (currentInProgressId == null) {
                for (Reservation r : reservations) {
                    String st = r.getStatus() != null ? r.getStatus().toLowerCase() : "";
                    if (!"completed".equals(st) && !"canceled".equals(st) && !"failed".equals(st)) {
                        firstEligibleId = r.getReservationId();
                        break;
                    }
                }
            }
            model.addAttribute("currentInProgressId", currentInProgressId);
            model.addAttribute("firstEligibleId", firstEligibleId);

            java.util.List<Battery> fullBatteries = batteryRepo.findByStationAndStateContainingIgnoreCase(station, "full");
            java.util.List<Battery> chargingBatteries = batteryRepo.findByStationAndStateContainingIgnoreCase(station, "charging");
            model.addAttribute("fullBatteries", fullBatteries);
            model.addAttribute("chargingBatteries", chargingBatteries);
            model.addAttribute("stationName", station.getName());
            model.addAttribute("stationId", station.getStationId());
            return "staff/manage-swaps";
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        }
    }

    @PostMapping("/swaps/checkin/qr")
    public String checkInByQr(@RequestParam("token") String token,
                              HttpSession session,
                              RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            swapService.checkInByQr(token, staff);
            redirect.addFlashAttribute("successMessage", "Check-in bằng QR thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/swaps";
    }

    @PostMapping("/swaps/start")
    public String startSwap(@RequestParam("reservationId") Integer reservationId,
                            HttpSession session,
                            RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            swapService.startSwap(reservationId, staff);
            redirect.addFlashAttribute("successMessage", "Bắt đầu đổi pin");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/swaps";
    }

    @PostMapping("/swaps/complete")
    public String completeSwap(@RequestParam("reservationId") Integer reservationId,
                               @RequestParam(value = "batteryOutId", required = false) Integer batteryOutId,
                               @RequestParam(value = "batteryInId", required = false) Integer batteryInId,
                               @RequestParam(value = "result", required = false) String result,
                               @RequestParam(value = "recordCash", required = false) boolean recordCash,
                               HttpSession session,
                               RedirectAttributes redirect) {
        try {
            Staff staff = checkStaffLogin(session);
            SwapTransaction tx = swapService.completeSwap(reservationId, batteryOutId, batteryInId, result, staff);
            if (recordCash) {
                // record a succeed cash payment for the driver
                evswap.swp391to4.entity.Driver driver = tx.getReservation().getDriver();
                swapService.recordCashPaymentIfNeeded(reservationId, driver);
            }
            redirect.addFlashAttribute("successMessage", "Hoàn tất giao dịch đổi pin");
        } catch (Exception e) {
            redirect.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/staff/swaps";
    }

    /**
     * API: Thêm comment (JSON) và trả về comment vừa thêm cho staff
     */
    @PostMapping("/tickets/{id}/comments")
    @ResponseBody
    public Object postCommentJson(@PathVariable Integer id,
                                  @RequestBody java.util.Map<String, String> body,
                                  HttpSession session) {
        try {
            Staff staff = checkStaffLogin(session);
            TicketSupportResponse ticket = ticketService.getTicketById(id);
            if (ticket.getStaffId() == null || !ticket.getStaffId().equals(staff.getStaffId())) {
                return java.util.Map.of("error", "FORBIDDEN");
            }
            String message = body != null ? body.get("message") : null;
            evswap.swp391to4.service.TicketSupportService.Comment saved = ticketService.addCommentAndReturn(id, "staff", staff.getFullName(), message);
            return saved;
        } catch (IllegalStateException e) {
            return java.util.Map.of("error", "UNAUTHORIZED");
        } catch (Exception e) {
            return java.util.Map.of("error", e.getMessage());
        }
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
     * Trang Quản lý Pin (Code của bạn đã đúng)
     */
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
     * Xử lý Thêm Pin Mới (Code của bạn đã đúng)
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
     * HÀM HELPER (Code của bạn đã đúng)
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
     * Xử lý Cập nhật Trạng thái (Code của bạn đã đúng)
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

        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("loginError", e.getMessage());
            return "redirect:/login";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/staff/tickets/" + id;
    }
}