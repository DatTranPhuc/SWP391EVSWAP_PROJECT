package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

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
}
