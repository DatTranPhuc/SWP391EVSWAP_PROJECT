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

    @GetMapping("/schedule")
    public String showSchedulePage(@RequestParam(value = "q", required = false) String query,
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
        model.addAttribute("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));

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
            Reservation reservation = reservationService.createReservationWithPayment(driver.getDriverId(), form.getStationId(), form.getVehicleId(), reservedStart);
            redirect.addFlashAttribute("reservationSuccess", "Đặt lịch và thanh toán thành công!");
            redirect.addFlashAttribute("currentStep", "payment");
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
    public String myReservations(@RequestParam(value = "status", required = false) String status,
                                HttpSession session, Model model, RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem lịch đổi pin");
            return "redirect:/login";
        }
        
        List<evswap.swp391to4.service.ReservationService.ReservationSummary> reservations = 
            reservationService.getUpcomingReservations(driver.getDriverId());
        
        // Filter by status if provided
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            reservations = reservations.stream()
                .filter(r -> status.equalsIgnoreCase(r.status()))
                .collect(java.util.stream.Collectors.toList());
        }
        
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("upcomingReservations", reservations);
        model.addAttribute("currentStatus", status);
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
            model.addAttribute("currentStep", switch (reservation.getStatus() == null ? "" : reservation.getStatus()) {
                case "pending" -> "payment"; // sau khi thanh toán, đợi xác nhận
                case "confirmed" -> "swap";
                case "checked_in" -> "swap";
                case "completed" -> "done";
                default -> "schedule";
            });

            return "reservation-detail";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không tìm thấy reservation: " + e.getMessage());
            return "redirect:/reservations/my-reservations";
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
}
