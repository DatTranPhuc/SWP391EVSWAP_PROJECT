package evswap.swp391to4.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.ReservationScheduleForm;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.SwapTransactionRepository;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.StationService;
import evswap.swp391to4.service.SwapService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final StationService stationService;
    private final ReservationService reservationService;
    private final SwapService swapService;
    private final SwapTransactionRepository swapTransactionRepository;

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

        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        Instant reservedStart = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        if (reservedStart.isBefore(Instant.now())) {
            redirect.addFlashAttribute("reservationError", "Thời gian đặt lịch phải ở tương lai");
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            return "redirect:/reservations/book";
        }

        try {
            Reservation saved = reservationService.createReservation(driver.getDriverId(), form.getStationId(), reservedStart);
            // Tạo QR cho đặt lịch vừa tạo
            Reservation withQr = swapService.generateQrForReservation(saved.getReservationId(), driver);
            redirect.addFlashAttribute("reservationSuccess", "Đặt lịch đổi pin thành công! Mã QR đã sẵn sàng để check-in.");
            redirect.addFlashAttribute("currentStep", "payment");
            redirect.addFlashAttribute("qrToken", withQr.getQrToken());
            redirect.addFlashAttribute("reservationId", withQr.getReservationId());
            redirect.addAttribute("stationId", form.getStationId());
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
        }

        return "redirect:/reservations/book";
    }

    @GetMapping("/swap")
    public String swapStep(@RequestParam("reservationId") Integer reservationId,
                           HttpSession session,
                           Model model,
                           RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem bước đổi pin");
            return "redirect:/login";
        }
        try {
            evswap.swp391to4.entity.Reservation reservation = reservationService
                    .findByIdOrThrow(reservationId);
            if (!reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
                redirect.addFlashAttribute("reservationError", "Bạn không có quyền xem đặt lịch này");
                return "redirect:/reservations/schedule";
            }
            // Bảo đảm có QR hợp lệ
            if (reservation.getQrToken() == null || reservation.getQrExpiresAt() == null || reservation.getQrExpiresAt().isBefore(java.time.Instant.now())) {
                reservation = swapService.generateQrForReservation(reservationId, driver);
            }
            model.addAttribute("qrToken", reservation.getQrToken());
            model.addAttribute("qrExpiresAt", reservation.getQrExpiresAt());
            model.addAttribute("stationName", reservation.getStation().getName());
            model.addAttribute("stationAddress", reservation.getStation().getAddress());
            model.addAttribute("currentStep", "swap");
            return "reservation-swap";
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
            return "redirect:/reservations/schedule";
        }
    }

    @GetMapping("/{id}")
    public String showDetail(@PathVariable("id") Integer reservationId,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem chi tiết đặt lịch");
            return "redirect:/login";
        }
        try {
            Reservation reservation = reservationService.findByIdOrThrow(reservationId);
            if (!reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
                redirect.addFlashAttribute("reservationError", "Bạn không có quyền xem đặt lịch này");
                return "redirect:/reservations/schedule";
            }
            
            // Load swap transaction if exists
            evswap.swp391to4.entity.SwapTransaction swapTransaction = swapTransactionRepository
                    .findByReservation_ReservationId(reservationId).orElse(null);
            
            model.addAttribute("reservation", reservation);
            model.addAttribute("swapTransaction", swapTransaction);
            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
            return "reservation-detail";
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
            return "redirect:/reservations/schedule";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancelReservation(@PathVariable("id") Integer reservationId,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để hủy đặt lịch");
            return "redirect:/login";
        }
        try {
            reservationService.cancelReservation(reservationId, driver.getDriverId());
            redirect.addFlashAttribute("reservationSuccess", "Đã hủy đặt lịch thành công");
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
        }
        return "redirect:/reservations/schedule";
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }
}
