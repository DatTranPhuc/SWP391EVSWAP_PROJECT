package evswap.swp391to4.controller;

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

    @GetMapping
    public ResponseEntity<List<StationResponse>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    @GetMapping("/search")
    public ResponseEntity<List<StationResponse>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(stationService.searchByName(name));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<StationResponse>> findNearby(
            @RequestParam BigDecimal lat,
            @RequestParam BigDecimal lng,
            @RequestParam(defaultValue = "5") double radiusKm
    ) {
        return ResponseEntity.ok(stationService.findNearby(lat, lng, radiusKm));
    }
}
