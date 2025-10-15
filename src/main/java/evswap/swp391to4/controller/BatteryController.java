package evswap.swp391to4.controller;

import evswap.swp391to4.dto.BatteryRequest;
import evswap.swp391to4.dto.BatteryResponse;
import evswap.swp391to4.dto.BatteryUpdateRequest;
import evswap.swp391to4.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryService batteryService;

    @PostMapping
    public ResponseEntity<BatteryResponse> create(@RequestBody BatteryRequest request) {
        return ResponseEntity.status(201).body(batteryService.createBattery(request));
    }

    @PatchMapping("/{batteryId}")
    public ResponseEntity<BatteryResponse> update(@PathVariable Integer batteryId,
                                                  @RequestBody BatteryUpdateRequest request) {
        return ResponseEntity.ok(batteryService.updateBattery(batteryId, request));
    }

    @GetMapping
    public ResponseEntity<List<BatteryResponse>> listAll() {
        return ResponseEntity.ok(batteryService.listAll());
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<BatteryResponse>> listByStation(@PathVariable Integer stationId) {
        return ResponseEntity.ok(batteryService.listByStation(stationId));
    }
}
