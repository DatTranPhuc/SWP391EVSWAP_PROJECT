package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final DriverRepository driverRepository;
    private final StationRepository stationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getReservations(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Reservation> reservations = reservationRepository.findAll();
            List<Map<String, Object>> reservationList = reservations.stream()
                .map(reservation -> {
                    Map<String, Object> reservationData = new HashMap<>();
                    reservationData.put("reservationId", reservation.getReservationId());
                    reservationData.put("status", reservation.getStatus());
                    reservationData.put("reservedStart", reservation.getReservedStart());
                    reservationData.put("createdAt", reservation.getCreatedAt());
                    reservationData.put("qrToken", reservation.getQrToken());
                    reservationData.put("qrStatus", reservation.getQrStatus());
                    
                    if (reservation.getDriver() != null) {
                        reservationData.put("driverId", reservation.getDriver().getDriverId());
                        reservationData.put("driverName", reservation.getDriver().getFullName());
                    }
                    
                    if (reservation.getStation() != null) {
                        reservationData.put("stationId", reservation.getStation().getStationId());
                        reservationData.put("stationName", reservation.getStation().getName());
                        reservationData.put("stationAddress", reservation.getStation().getAddress());
                    }
                    
                    return reservationData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("reservations", reservationList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReservation(@RequestHeader("Authorization") String authHeader,
                                                                @RequestBody Map<String, Object> request) {
        try {
            Integer stationId = (Integer) request.get("stationId");
            String reservedStartStr = (String) request.get("reservedStart");
            Integer driverId = (Integer) request.get("driverId");

            if (driverId == null) {
                driverId = 1; // Default driver for demo
            }

            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

            Instant reservedStart = Instant.parse(reservedStartStr);

            Reservation reservation = Reservation.builder()
                .driver(driver)
                .station(station)
                .reservedStart(reservedStart)
                .status("pending")
                .createdAt(Instant.now())
                .qrNonce(UUID.randomUUID().toString())
                .qrExpiresAt(Instant.now().plusSeconds(3600)) // 1 hour
                .qrStatus("active")
                .qrToken(UUID.randomUUID().toString())
                .build();

            Reservation savedReservation = reservationRepository.save(reservation);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("reservation", Map.of(
                "reservationId", savedReservation.getReservationId(),
                "qrToken", savedReservation.getQrToken(),
                "status", savedReservation.getStatus()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> getReservation(@PathVariable Integer reservationId,
                                                              @RequestHeader("Authorization") String authHeader) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("reservationId", reservation.getReservationId());
            reservationData.put("status", reservation.getStatus());
            reservationData.put("reservedStart", reservation.getReservedStart());
            reservationData.put("createdAt", reservation.getCreatedAt());
            reservationData.put("qrToken", reservation.getQrToken());
            reservationData.put("qrStatus", reservation.getQrStatus());
            reservationData.put("qrExpiresAt", reservation.getQrExpiresAt());
            reservationData.put("checkedInAt", reservation.getCheckedInAt());
            
            if (reservation.getDriver() != null) {
                reservationData.put("driverId", reservation.getDriver().getDriverId());
                reservationData.put("driverName", reservation.getDriver().getFullName());
            }
            
            if (reservation.getStation() != null) {
                reservationData.put("stationId", reservation.getStation().getStationId());
                reservationData.put("stationName", reservation.getStation().getName());
                reservationData.put("stationAddress", reservation.getStation().getAddress());
            }

            return ResponseEntity.ok(reservationData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> updateReservation(@PathVariable Integer reservationId,
                                                                 @RequestHeader("Authorization") String authHeader,
                                                                 @RequestBody Map<String, Object> request) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

            String status = (String) request.get("status");
            if (status != null) {
                reservation.setStatus(status);
                if ("checked_in".equals(status)) {
                    reservation.setCheckedInAt(Instant.now());
                }
            }

            reservationRepository.save(reservation);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reservation updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> cancelReservation(@PathVariable Integer reservationId,
                                                                @RequestHeader("Authorization") String authHeader) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

            reservation.setStatus("canceled");
            reservationRepository.save(reservation);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Reservation canceled successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
