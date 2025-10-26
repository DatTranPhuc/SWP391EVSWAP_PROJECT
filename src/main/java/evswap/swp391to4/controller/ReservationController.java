package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ReservationScheduleForm;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Reservation; // << Cần import
import evswap.swp391to4.service.PaymentService; // << Cần import
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpServletRequest; // << Cần import
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

import java.math.BigDecimal;
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
    private final PaymentService paymentService; // << Tiêm (Inject) PaymentService

    /**
     * Hàm hiển thị trang đặt lịch
     */
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

    /**
     * Hàm xử lý chức năng "Tìm trạm gần tôi"
     */
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

        // 1. GỌI HÀM TÌM KIẾM LÂN CẬN
        List<StationResponse> stations = stationService.findNearby(lat, lng, radiusKm);
        model.addAttribute("stations", stations);

        // 2. Thêm cờ để báo cho HTML biết đây là tìm kiếm lân cận
        model.addAttribute("isNearbySearch", true);
        model.addAttribute("searchQuery", "Các trạm gần vị trí của bạn");

        // 3. Thêm tọa độ user để JS bản đồ đọc và zoom vào
        model.addAttribute("userLat", lat);
        model.addAttribute("userLng", lng);

        // 4. Thêm TẤT CẢ các model attributes khác
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


    /**
     * Hàm xử lý submit đặt lịch (ĐÃ CẬP NHẬT ĐỂ GỌI VNPay)
     */
    @PostMapping("/schedule")
    public String submitReservation(@ModelAttribute("reservationForm") ReservationScheduleForm form,
                                    @RequestParam(value = "q", required = false) String query,
                                    HttpServletRequest httpReq, // << Thêm tham số này
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
            // 1. Tạo Reservation với status "pending"
            Reservation reservation = reservationService.createReservation(
                    driver.getDriverId(),
                    form.getStationId(),
                    reservedStart
            );

            // 2. TẠO THANH TOÁN
            // Gọi PaymentService để tạo Payment và lấy URL
            String paymentUrl = paymentService.createPaymentForReservation(httpReq, reservation, driver);

            // 3. CHUYỂN HƯỚNG NGƯỜI DÙNG sang VNPay
            return "redirect:" + paymentUrl;

        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", "Lỗi khi tạo thanh toán: " + e.getMessage());
            redirect.addFlashAttribute("reservationForm", form);
            redirect.addAttribute("stationId", form.getStationId());
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
        }

        return "redirect:/reservations/schedule";
    }

    /**
     * Hàm tiện ích
     */
    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }
}