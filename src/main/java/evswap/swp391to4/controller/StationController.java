package evswap.swp391to4.controller;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;
    private final StationRepository stationRepository;

    /**
     * 📋 Xem tất cả trạm
     */
    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    /**
     * 🔎 Tìm trạm theo tên (rỗng → hiện tất cả)
     * /api/stations/search?name=Station A
     */
    @GetMapping("/search")
    public ResponseEntity<List<StationResponse>> searchStationsByName(@RequestParam(required = false) String name) {
        return ResponseEntity.ok(stationService.searchByName(name));
    }

    /**
     * 📍 Tìm trạm gần vị trí hiện tại
     * /api/stations/nearby?lat=10.7626&lng=106.6822&radiusKm=5
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<StationResponse>> findNearbyStations(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "5") double radiusKm
    ) {
        return ResponseEntity.ok(stationService.findNearby(lat, lng, radiusKm));
    }

    /**
     * 🕐 Lấy khung giờ trống của trạm
     */
    @GetMapping("/{stationId}/timeslots")
    public ResponseEntity<Map<String, Object>> getTimeSlots(@PathVariable Integer stationId,
                                                           @RequestParam String date,
                                                           @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            // Generate mock time slots for the given date
            List<Map<String, Object>> slots = new ArrayList<>();
            LocalDate localDate = LocalDate.parse(date);
            
            // Generate slots from 8 AM to 10 PM, every 30 minutes
            for (int hour = 8; hour < 22; hour++) {
                for (int minute = 0; minute < 60; minute += 30) {
                    LocalTime time = LocalTime.of(hour, minute);
                    Instant slotTime = localDate.atTime(time).atZone(ZoneId.systemDefault()).toInstant();
                    
                    Map<String, Object> slot = new HashMap<>();
                    slot.put("time", slotTime.toString());
                    slot.put("available", Math.random() > 0.3); // 70% chance of being available
                    slot.put("price", 50000 + (int)(Math.random() * 20000)); // Random price between 50k-70k
                    slots.add(slot);
                }
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("slots", slots);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    /**
     * ➕ Tạo trạm mới (Admin only)
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createStation(@RequestHeader("Authorization") String authHeader,
                                                            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String address = (String) request.get("address");
            Double latitude = ((Number) request.get("latitude")).doubleValue();
            Double longitude = ((Number) request.get("longitude")).doubleValue();

            Station station = Station.builder()
                .name(name)
                .address(address)
                .latitude(new BigDecimal(latitude.toString()))
                .longitude(new BigDecimal(longitude.toString()))
                .status("active")
                .build();

            Station savedStation = stationRepository.save(station);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("station", Map.of(
                "stationId", savedStation.getStationId(),
                "name", savedStation.getName(),
                "address", savedStation.getAddress()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 🔍 Lấy thông tin trạm theo ID
     */
    @GetMapping("/{stationId}")
    public ResponseEntity<Map<String, Object>> getStation(@PathVariable Integer stationId,
                                                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException("Station not found"));

            Map<String, Object> stationData = new HashMap<>();
            stationData.put("stationId", station.getStationId());
            stationData.put("name", station.getName());
            stationData.put("address", station.getAddress());
            stationData.put("latitude", station.getLatitude());
            stationData.put("longitude", station.getLongitude());
            stationData.put("status", station.getStatus());

            return ResponseEntity.ok(stationData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
