package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ReservationScheduleForm;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal; // << THÊM IMPORT NÀY
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final StationService stationService;
    private final ReservationService reservationService;

    @GetMapping("/schedule")
    public String showSchedulePage(@RequestParam(value = "stationId", required = false) Integer stationId,
                                   @RequestParam(value = "q", required = false) String query,
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

        if (!model.containsAttribute("reservationForm")) {
            ReservationScheduleForm form = new ReservationScheduleForm();
            form.setStationId(stationId);
            model.addAttribute("reservationForm", form);
        } else if (stationId != null) {
            Object formObj = model.asMap().get("reservationForm");
            if (formObj instanceof ReservationScheduleForm existing && existing.getStationId() == null) {
                existing.setStationId(stationId);
            }
        }

        if (!model.containsAttribute("currentStep")) {
            model.addAttribute("currentStep", stationId != null ? "schedule" : "search");
        }

        if (stationId != null) {
            try {
                StationResponse selected = stationService.findById(stationId);
                model.addAttribute("selectedStation", selected);
            } catch (Exception e) {
                redirect.addFlashAttribute("reservationError", "Không tìm thấy trạm đã chọn");
                return "redirect:/reservations/schedule";
            }
        }

        return "reservation-schedule";
    }

    // <<============================================================>>
    // << HÀM MỚI CHO CHỨC NĂNG "TÌM TRẠM GẦN TÔI" >>
    // <<============================================================>>
    @GetMapping("/nearby")
    public String findNearbyStations(
            @RequestParam("lat") BigDecimal lat,
            @RequestParam("lng") BigDecimal lng,
            @RequestParam(value = "radiusKm", defaultValue = "5.0") double radiusKm,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {

        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        // 1. GỌI HÀM TÌM KIẾM LÂN CẬN (thay vì getAll)
        List<StationResponse> stations = stationService.findNearby(lat, lng, radiusKm);
        model.addAttribute("stations", stations);

        // 2. Thêm cờ để báo cho HTML biết đây là tìm kiếm lân cận
        model.addAttribute("isNearbySearch", true);
        model.addAttribute("searchQuery", "Các trạm gần vị trí của bạn"); // Hiển thị tiêu đề tìm kiếm

        // 3. Thêm tọa độ user để JS bản đồ đọc và zoom vào
        model.addAttribute("userLat", lat);
        model.addAttribute("userLng", lng);

        // 4. (QUAN TRỌNG) Thêm TẤT CẢ các model attributes khác
        // mà trang này cần (lấy từ hàm showSchedulePage)
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));
        if (!model.containsAttribute("reservationForm")) {
            model.addAttribute("reservationForm", new ReservationScheduleForm());
        }
        if (!model.containsAttribute("currentStep")) {
            model.addAttribute("currentStep", "search");
        }

        // 5. Trả về đúng file HTML
        return "reservation-schedule";
    }
    // <<============================================================>>
    // << KẾT THÚC HÀM MỚI >>
    // <<============================================================>>


    @PostMapping("/schedule")
    public String submitReservation(@ModelAttribute("reservationForm") ReservationScheduleForm form,
                                    @RequestParam(value = "q", required = false) String query,
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
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
            return "redirect:/reservations/schedule";
        }

        LocalDate date = form.getDate();
        LocalTime time = form.getTime();
        if (date == null || time == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn ngày và giờ đặt lịch");
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
            return "redirect:/reservations/schedule";
        }

        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        Instant reservedStart = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        if (reservedStart.isBefore(Instant.now())) {
            redirect.addFlashAttribute("reservationError", "Thời gian đặt lịch phải ở tương lai");
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
            return "redirect:/reservations/schedule";
        }

        try {
            reservationService.createReservation(driver.getDriverId(), form.getStationId(), reservedStart);
            redirect.addFlashAttribute("reservationSuccess", "Đặt lịch đổi pin thành công! Hãy chuẩn bị cho bước thanh toán.");
            redirect.addFlashAttribute("currentStep", "payment");
            redirect.addAttribute("stationId", form.getStationId());
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
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