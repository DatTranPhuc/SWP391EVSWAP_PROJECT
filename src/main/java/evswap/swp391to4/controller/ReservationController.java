package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ApiResponse;
import evswap.swp391to4.dto.ReservationScheduleForm;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final StationService stationService;
    private final ReservationService reservationService;

    @GetMapping("/schedule")
    public ResponseEntity<ApiResponse<Map<String, Object>>> showSchedulePage(
            @RequestParam(value = "q", required = false) String query,
            HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            Map<String, Object> data = Map.of("next", "/api/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>failure("Vui lòng đăng nhập để đặt lịch đổi pin.").withData(data));
        }

        List<StationResponse> stations = (query == null || query.isBlank())
                ? stationService.getAllStations()
                : stationService.searchByName(query);

        Map<String, Object> data = new HashMap<>();
        data.put("stations", stations);
        data.put("searchQuery", query);
        data.put("driverName", driver.getFullName());
        data.put("driverInitial", extractInitial(driver.getFullName()));
        data.put("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));
        data.put("currentStep", "search");

        return ResponseEntity.ok(ApiResponse.success("Thông tin lịch đổi pin", data));
    }

    @GetMapping("/book")
    public ResponseEntity<ApiResponse<Map<String, Object>>> showBookingPage(@RequestParam("stationId") Integer stationId,
                                                                            HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            Map<String, Object> data = Map.of("next", "/api/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>failure("Vui lòng đăng nhập để đặt lịch đổi pin.").withData(data));
        }

        StationResponse selectedStation = stationService.findById(stationId);

        Map<String, Object> data = new HashMap<>();
        data.put("selectedStation", selectedStation);
        data.put("driverName", driver.getFullName());
        data.put("driverInitial", extractInitial(driver.getFullName()));
        data.put("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));
        data.put("currentStep", "schedule");

        return ResponseEntity.ok(ApiResponse.success("Thông tin đặt lịch cho trạm đã chọn", data));
    }

    @PostMapping("/book")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitReservation(@RequestBody ReservationScheduleForm form,
                                                                              HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            Map<String, Object> data = Map.of("next", "/api/auth/login");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.<Map<String, Object>>failure("Vui lòng đăng nhập để đặt lịch đổi pin.").withData(data));
        }

        if (form.getStationId() == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Vui lòng chọn trạm đổi pin."));
        }

        LocalDate date = form.getDate();
        LocalTime time = form.getTime();
        if (date == null || time == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Vui lòng chọn ngày và giờ đặt lịch."));
        }

        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        Instant reservedStart = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        if (reservedStart.isBefore(Instant.now())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.failure("Thời gian đặt lịch phải ở tương lai."));
        }

        reservationService.createReservation(driver.getDriverId(), form.getStationId(), reservedStart);

        Map<String, Object> data = new HashMap<>();
        data.put("next", "payment");
        data.put("stationId", form.getStationId());
        data.put("message", "Đặt lịch đổi pin thành công! Hãy chuẩn bị cho bước thanh toán.");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đặt lịch thành công", data));
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }
}
