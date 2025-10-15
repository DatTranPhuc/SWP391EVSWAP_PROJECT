package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ReservationRequest;
import evswap.swp391to4.dto.ReservationResponse;
import evswap.swp391to4.dto.ReservationStatusUpdateRequest;
import evswap.swp391to4.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@RequestBody ReservationRequest request) {
        return ResponseEntity.status(201).body(reservationService.createReservation(request));
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ReservationResponse> updateStatus(@PathVariable Integer reservationId,
                                                            @RequestBody ReservationStatusUpdateRequest request) {
        return ResponseEntity.ok(reservationService.updateStatus(reservationId, request));
    }

    @PostMapping("/{reservationId}/qr")
    public ResponseEntity<ReservationResponse> refreshQr(@PathVariable Integer reservationId) {
        return ResponseEntity.ok(reservationService.refreshQrCode(reservationId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<ReservationResponse>> listForDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(reservationService.getReservationsForDriver(driverId));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<ReservationResponse>> listForStation(
            @PathVariable Integer stationId,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant end) {
        return ResponseEntity.ok(reservationService.getReservationsForStation(stationId, start, end));
    }
}
