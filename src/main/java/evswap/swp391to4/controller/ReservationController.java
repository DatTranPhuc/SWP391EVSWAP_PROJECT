package evswap.swp391to4.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.ReservationScheduleForm;
import evswap.swp391to4.dto.AvailableBatteryResponse;
import evswap.swp391to4.dto.QrResponse;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.BatteryService;
import evswap.swp391to4.service.StationService;
import evswap.swp391to4.service.VehicleService;
import evswap.swp391to4.service.WalletService;
import evswap.swp391to4.service.FeedbackService;
import evswap.swp391to4.service.PaymentService;
import evswap.swp391to4.service.QRCodeService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final StationService stationService;
    private final ReservationService reservationService;
    private final VehicleService vehicleService;
    private final WalletService walletService;
    private final BatteryService batteryService;
    private final FeedbackService feedbackService;
    private final PaymentService paymentService;
    private final QRCodeService qrCodeService;

    @GetMapping("/schedule")
    public String showSchedulePage(@RequestParam(value = "q", required = false) String query,
                                   @RequestParam(value = "tab", required = false) String tab,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        List<StationResponse> stations = (query == null || query.isBlank())
                ? stationService.getAllStations()
                : stationService.searchByName(query);
        model.addAttribute("stations", stations);
        model.addAttribute("searchQuery", query);
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        
        // Lấy 2 danh sách: lịch đặt (scheduled) và lịch sử đổi (history)
        List<evswap.swp391to4.service.ReservationService.ReservationSummary> scheduledReservations = 
            reservationService.getScheduledReservations(driver.getDriverId());
        List<evswap.swp391to4.service.ReservationService.ReservationSummary> swapHistory = 
            reservationService.getSwapHistory(driver.getDriverId());
        
        model.addAttribute("scheduledReservations", scheduledReservations);
        model.addAttribute("swapHistory", swapHistory);
        model.addAttribute("activeTab", tab != null ? tab : "scheduled"); // Default to "scheduled" tab

        if (!model.containsAttribute("currentStep")) {
            model.addAttribute("currentStep", "search");
        }

        return "reservation-schedule";
    }

    @GetMapping("/book")
    public String showBookingPage(@RequestParam("stationId") Integer stationId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        StationResponse selectedStation;
        try {
            selectedStation = stationService.findById(stationId);
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy trạm đã chọn");
            return "redirect:/reservations/schedule";
        }

        if (!model.containsAttribute("reservationForm")) {
            ReservationScheduleForm form = new ReservationScheduleForm();
            form.setStationId(stationId);
            model.addAttribute("reservationForm", form);
        }

        model.addAttribute("selectedStation", selectedStation);
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));

        // Load vehicles & wallet balance & availability hint
        model.addAttribute("vehicles", vehicleService.getVehiclesForDriver(driver.getDriverId()));
        model.addAttribute("walletBalance", walletService.getBalance(driver.getDriverId()));

        if (!model.containsAttribute("currentStep")) {
            model.addAttribute("currentStep", "schedule");
        }

        return "reservation-book";
    }

    @PostMapping("/book")
    public String submitReservation(@ModelAttribute("reservationForm") ReservationScheduleForm form,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        if (form.getStationId() == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn trạm đổi pin");
            redirect.addFlashAttribute("reservationForm", form);
            return "redirect:/reservations/schedule";
        }

        LocalDate date = form.getDate();
        LocalTime time = form.getTime();
        if (date == null || time == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn ngày và giờ đặt lịch");
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            return "redirect:/reservations/book";
        }

        if (form.getVehicleId() == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn phương tiện để đề xuất pin phù hợp");
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            return "redirect:/reservations/book";
        }

        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        Instant reservedStart = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        if (reservedStart.isBefore(Instant.now())) {
            redirect.addFlashAttribute("reservationError", "Thời gian đặt lịch phải ở tương lai");
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            return "redirect:/reservations/book";
        }

        try {
            String paymentMethod = form.getPaymentMethod();
            if (paymentMethod == null || paymentMethod.isBlank()) {
                paymentMethod = "wallet"; // Default to wallet
            }
            
            Reservation reservation = reservationService.createReservationWithPaymentMethod(
                driver.getDriverId(), 
                form.getStationId(), 
                form.getVehicleId(), 
                reservedStart,
                paymentMethod,
                false); // Not instant swap
            
            // Determine success message based on new status flow
            String successMsg;
            String currentStep;
            if ("confirmed".equals(reservation.getStatus())) {
                // Wallet payment: auto-confirmed
                successMsg = "Đặt lịch và thanh toán thành công! Lịch đã được xác nhận tự động. Vui lòng đến trạm đúng giờ.";
                currentStep = "swap"; // Ready for check-in
            } else {
                // Cash/transfer: pending staff confirmation
                successMsg = "Đặt lịch thành công! Vui lòng đợi staff xác nhận và thanh toán tại trạm.";
                currentStep = "payment";
            }
            
            redirect.addFlashAttribute("reservationSuccess", successMsg);
            redirect.addFlashAttribute("currentStep", currentStep);
            redirect.addAttribute("stationId", form.getStationId());
            return "redirect:/reservations/payment-success?reservationId=" + reservation.getReservationId();
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
        }

        return "redirect:/reservations/book";
    }

    @GetMapping("/payment-success")
    public String paymentSuccess(@RequestParam("reservationId") Integer reservationId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem thông tin đặt lịch");
            return "redirect:/login";
        }

        try {
            Reservation reservation = reservationService.getReservationById(reservationId);
            
            // Validate reservation belongs to driver
            if (!reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
                redirect.addFlashAttribute("error", "Không có quyền xem reservation này");
                return "redirect:/reservations/my-reservations";
            }
            
            model.addAttribute("reservation", reservation);
            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
            model.addAttribute("currentStep", "payment");
            
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không tìm thấy reservation: " + e.getMessage());
            return "redirect:/reservations/my-reservations";
        }
        
        return "reservation-payment-success";
    }

    @GetMapping("/my-reservations")
    public String myReservations(@RequestParam(value = "tab", required = false) String tab,
                                HttpSession session, Model model, RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem lịch đổi pin");
            return "redirect:/login";
        }
        
        // Lấy 2 danh sách: lịch đặt (scheduled) và lịch sử đổi (history)
        List<evswap.swp391to4.service.ReservationService.ReservationSummary> scheduledReservations = 
            reservationService.getScheduledReservations(driver.getDriverId());
        List<evswap.swp391to4.service.ReservationService.ReservationSummary> swapHistory = 
            reservationService.getSwapHistory(driver.getDriverId());
        
        // Mặc định hiển thị tab "scheduled" nếu không có tab được chỉ định
        if (tab == null || tab.isEmpty()) {
            tab = "scheduled";
        }
        
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("scheduledReservations", scheduledReservations);
        model.addAttribute("swapHistory", swapHistory);
        model.addAttribute("currentTab", tab);
        return "reservation-my-list";
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable Integer id,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để hủy đặt lịch");
            return "redirect:/login";
        }
        try {
            reservationService.cancelReservationWithRefund(id);
            redirect.addFlashAttribute("success", "Đã hủy lịch và xử lý hoàn tiền theo chính sách.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations/my-reservations";
    }

    @PostMapping("/{id}/delete")
    public String deleteReservation(@PathVariable Integer id,
                                    @RequestParam(value = "redirectTo", required = false) String redirectTo,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xóa lịch sử");
            return "redirect:/login";
        }
        try {
            reservationService.deleteReservationFromHistory(id, driver.getDriverId());
            redirect.addFlashAttribute("success", "Đã xóa lịch sử thành công.");
        } catch (IllegalStateException e) {
            // Business logic errors - hiển thị message gốc
            redirect.addFlashAttribute("error", e.getMessage());
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // Database constraint errors - hiển thị message thân thiện
            redirect.addFlashAttribute("error", "Không thể xóa lịch sử này vì vẫn còn thông tin thanh toán liên quan. Vui lòng liên hệ quản trị viên nếu cần hỗ trợ.");
        } catch (Exception e) {
            // Các lỗi khác
            String errorMsg = e.getMessage();
            if (errorMsg != null && (errorMsg.contains("FK") || errorMsg.contains("constraint") || errorMsg.contains("REFERENCE"))) {
                redirect.addFlashAttribute("error", "Không thể xóa lịch sử này vì vẫn còn dữ liệu liên quan. Vui lòng thử lại sau hoặc liên hệ hỗ trợ.");
            } else {
                redirect.addFlashAttribute("error", "Đã xảy ra lỗi khi xóa lịch sử: " + (errorMsg != null ? errorMsg : "Lỗi không xác định"));
            }
        }
        
        // Xác định redirect URL - ưu tiên redirectTo parameter, sau đó mặc định về history tab
        if (redirectTo != null && !redirectTo.isEmpty()) {
            // Kiểm tra redirectTo hợp lệ để tránh open redirect vulnerability
            if (redirectTo.equals("schedule")) {
                return "redirect:/reservations/schedule?tab=history";
            }
        }
        return "redirect:/reservations/my-reservations?tab=history";
    }

    @GetMapping("/{id}")
    public String viewReservationDetail(@PathVariable Integer id,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem chi tiết đặt lịch");
            return "redirect:/login";
        }

        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (!reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
                redirect.addFlashAttribute("error", "Không có quyền xem reservation này");
                return "redirect:/reservations/my-reservations";
            }

            model.addAttribute("reservation", reservation);
            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
            
            // Determine current step based on status and reservation type
            String currentStep;
            boolean isInstant = reservation.getIsInstantSwap() != null && reservation.getIsInstantSwap();
            String status = reservation.getStatus() == null ? "" : reservation.getStatus();
            
            if ("completed".equals(status)) {
                currentStep = "done";
            } else if ("checked_in".equals(status)) {
                currentStep = "swap"; // Ready for staff to process
            } else if ("confirmed".equals(status)) {
                currentStep = "swap"; // Ready for check-in
            } else if ("pending".equals(status)) {
                // Pending: waiting for staff confirmation (cash/transfer scheduled)
                currentStep = "payment";
            } else {
                currentStep = "schedule";
            }
            
            model.addAttribute("currentStep", currentStep);
            model.addAttribute("isInstantSwap", isInstant);

            // Check if feedback was already created for this station
            if ("completed".equals(reservation.getStatus())) {
                boolean hasFeedback = feedbackService.hasFeedbackForStation(
                    driver.getDriverId(), 
                    reservation.getStation().getStationId());
                model.addAttribute("hasFeedback", hasFeedback);
            }

            return "reservation-detail";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không tìm thấy reservation: " + e.getMessage());
            return "redirect:/reservations/my-reservations";
        }
    }

    /**
     * Xử lý callback từ PayOS sau khi thanh toán
     * URL: GET /reservations/payment-callback
     */
    @GetMapping("/payment-callback")
    public String paymentCallback(@RequestParam("reservationId") Integer reservationId,
                                  @RequestParam(value = "code", required = false) String code,
                                  RedirectAttributes redirect) {
        try {
            // Verify payment status if code is provided
            // code = "00" means success in PayOS
            boolean paymentSuccess = code != null && "00".equals(code);
            
            if (paymentSuccess) {
                // Tìm payment và verify status, sau đó confirm reservation nếu cần
                try {
                    Reservation reservation = reservationService.getReservationById(reservationId);
                    
                    // Tìm payment record cho reservation này
                    java.util.List<evswap.swp391to4.entity.Payment> payments = 
                        paymentService.getPaymentsByReservation(reservation);
                    
                    // Tìm payment PayOS gần nhất chưa thành công
                    evswap.swp391to4.entity.Payment payosPayment = payments.stream()
                        .filter(p -> "payos".equalsIgnoreCase(p.getMethod()))
                        .filter(p -> !"succeed".equalsIgnoreCase(p.getStatus()))
                        .max(java.util.Comparator.comparing(evswap.swp391to4.entity.Payment::getPaidAt, 
                            java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder())))
                        .orElse(null);
                    
                    if (payosPayment != null) {
                        // Reconcile payment status từ PayOS API
                        evswap.swp391to4.entity.Payment reconciled = 
                            paymentService.reconcilePayOsPaymentById(payosPayment.getPaymentId());
                        
                        // Reload reservation để lấy status mới nhất (có thể đã được webhook cập nhật)
                        reservation = reservationService.getReservationById(reservationId);
                        
                        // Nếu payment đã thành công và reservation vẫn pending, tự động confirm
                        if ("succeed".equalsIgnoreCase(reconciled.getStatus()) && 
                            "pending".equalsIgnoreCase(reservation.getStatus())) {
                            try {
                                reservationService.confirmReservation(reservationId);
                            } catch (Exception e) {
                                // Log lỗi nhưng không fail callback
                                System.err.println("Failed to confirm reservation: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    // Log lỗi nhưng vẫn redirect
                    System.err.println("Error processing payment callback: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // Redirect to staff reservation detail page with auto refresh
            // This allows staff to see updated payment status immediately
            redirect.addFlashAttribute("paymentSuccess", paymentSuccess);
            return "redirect:/staff/reservations/" + reservationId + "?paymentSuccess=true&autoRefresh=true";
        } catch (Exception e) {
            // If error, still redirect to staff page but with error message
            redirect.addFlashAttribute("error", "Có lỗi xảy ra khi xử lý thanh toán: " + e.getMessage());
            return "redirect:/staff/reservations/" + reservationId;
        }
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }

    // ===== JSON APIs moved under ReservationController for consistency =====
    @GetMapping("/api/eligible-batteries")
    public ResponseEntity<java.util.List<AvailableBatteryResponse>> apiEligibleBatteries(
            @RequestParam Integer stationId,
            @RequestParam Integer vehicleId,
            HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            java.util.List<AvailableBatteryResponse> batteries = batteryService.findEligibleBatteriesForVehicle(stationId, vehicleId);
            return ResponseEntity.ok(batteries);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/api/{id}/qr")
    public ResponseEntity<QrResponse> apiReservationQr(@PathVariable Integer id, HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            Reservation reservation = reservationService.getReservationById(id);
            if (!reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
                return ResponseEntity.status(403).build();
            }
            QrResponse response = QrResponse.builder()
                    .qrToken(reservation.getQrToken())
                    .qrStatus(reservation.getQrStatus())
                    .expiresAt(reservation.getQrExpiresAt() == null ? null : reservation.getQrExpiresAt().toEpochMilli())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Serve QR code image cho driver
     * URL: GET /reservations/qr-code/{token}
     */
    @GetMapping("/qr-code/{token}")
    public ResponseEntity<byte[]> getQrCodeImage(@PathVariable String token,
                                                  HttpSession session) {
        try {
            Driver driver = (Driver) session.getAttribute("loggedInDriver");
            if (driver == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            
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
     * Trang instant swap payment (hiển thị sau khi user đến trạm)
     * URL: GET /reservations/instant-swap?stationId={id}
     */
    @GetMapping("/instant-swap")
    public String showInstantSwapPayment(@RequestParam("stationId") Integer stationId,
                                        HttpSession session,
                                        Model model,
                                        RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đổi pin ngay");
            return "redirect:/login";
        }

        StationResponse selectedStation;
        try {
            selectedStation = stationService.findById(stationId);
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy trạm đã chọn");
            return "redirect:/reservations/schedule";
        }

        model.addAttribute("selectedStation", selectedStation);
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("vehicles", vehicleService.getVehiclesForDriver(driver.getDriverId()));
        model.addAttribute("walletBalance", walletService.getBalance(driver.getDriverId()));

        ReservationScheduleForm form = new ReservationScheduleForm();
        form.setStationId(stationId);
        model.addAttribute("reservationForm", form);

        return "reservation-instant-swap-payment";
    }

    /**
     * Submit instant swap reservation
     * URL: POST /reservations/instant-swap
     */
    @PostMapping("/instant-swap")
    public String submitInstantSwap(@ModelAttribute("reservationForm") ReservationScheduleForm form,
                                   HttpSession session,
                                   RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đổi pin ngay");
            return "redirect:/login";
        }

        if (form.getStationId() == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn trạm đổi pin");
            return "redirect:/reservations/schedule";
        }

        if (form.getVehicleId() == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn phương tiện");
            redirect.addAttribute("stationId", form.getStationId());
            return "redirect:/reservations/instant-swap";
        }

        try {
            String paymentMethod = form.getPaymentMethod();
            if (paymentMethod == null || paymentMethod.isBlank()) {
                paymentMethod = "cash"; // Default to cash for instant swap
            }
            
            // Create instant swap reservation with current time
            Reservation reservation = reservationService.createReservationWithPaymentMethod(
                driver.getDriverId(), 
                form.getStationId(), 
                form.getVehicleId(), 
                Instant.now(), // Current time for instant swap
                paymentMethod,
                true); // isInstantSwap = true
            
            // Instant swap goes directly to checked_in status
            String successMsg;
            if ("checked_in".equals(reservation.getStatus())) {
                successMsg = "Đổi pin ngay đã được tạo! Bạn đã được check-in tự động. Vui lòng chờ staff xử lý.";
            } else {
                successMsg = "Đặt lịch thành công! Vui lòng chờ staff xử lý.";
            }
            
            redirect.addFlashAttribute("reservationSuccess", successMsg);
            return "redirect:/reservations/" + reservation.getReservationId();
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
            redirect.addAttribute("stationId", form.getStationId());
            return "redirect:/reservations/instant-swap";
        }
    }
}
