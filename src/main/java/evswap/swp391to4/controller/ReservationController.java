package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ReservationScheduleForm;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final StationService stationService;
    private final ReservationService reservationService;

    /**
     * Trả về danh sách trạm cho việc đặt lịch, có thể lọc theo query hoặc lấy tất cả
     */
    @GetMapping("/stations")
    public ResponseEntity<List<StationResponse>> getStations(@RequestParam(value = "search", required = false) String query) {
        if (query == null || query.isBlank()) {
            return ResponseEntity.ok(stationService.getAllStations());
        }
        return ResponseEntity.ok(stationService.searchByName(query));
    }

    /**
     * Lấy lịch đặt pin sắp tới của 1 tài xế (driverId lấy từ header hoặc JWT trong thực tế)
     */
    @GetMapping("/upcoming/{driverId}")
    public ResponseEntity<List<ReservationService.ReservationSummary>> getUpcomingReservations(@PathVariable Integer driverId) {
        return ResponseEntity.ok(reservationService.getUpcomingReservations(driverId));
    }

    /**
     * API đặt lịch đổi pin, nhận về DTO với stationId, date, time, và driverId (client lấy từ token khi triển khai auth)
     */
    @PostMapping("/book")
    public ResponseEntity<?> submitReservation(@Validated @RequestBody ReservationScheduleForm form,
                                               @RequestHeader(name = "Driver-Id") Integer driverId) {
        // Validate đầu vào
        if (driverId == null) {
            return ResponseEntity.status(401).body("Bạn chưa đăng nhập!");
        }
        if (form.getStationId() == null) {
            return ResponseEntity.badRequest().body("Vui lòng chọn trạm đổi pin");
        }
        LocalDate date = form.getDate();
        LocalTime time = form.getTime();
        if (date == null || time == null) {
            return ResponseEntity.badRequest().body("Vui lòng chọn ngày và giờ đặt lịch");
        }
        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        Instant reservedStart = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        if (reservedStart.isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body("Thời gian đặt lịch phải ở tương lai");
        }
        try {
            var reservation = reservationService.createReservation(driverId, form.getStationId(), reservedStart);
            return ResponseEntity.ok(reservation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
