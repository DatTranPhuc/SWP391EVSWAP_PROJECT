package evswap.swp391to4.controller;

import evswap.swp391to4.dto.VehicleBatteryCompatibilityResponse;
import evswap.swp391to4.dto.VehicleBatteryLinkRequest;
import evswap.swp391to4.service.VehicleBatteryCompatibilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-batteries")
@RequiredArgsConstructor
public class VehicleBatteryCompatibilityController {

    private final VehicleBatteryCompatibilityService compatibilityService;

    @PostMapping
    public ResponseEntity<VehicleBatteryCompatibilityResponse> link(@RequestBody VehicleBatteryLinkRequest request) {
        return ResponseEntity.status(201).body(compatibilityService.link(request));
    }

    @DeleteMapping
    public ResponseEntity<Void> unlink(@RequestBody VehicleBatteryLinkRequest request) {
        compatibilityService.unlink(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public ResponseEntity<List<VehicleBatteryCompatibilityResponse>> listForVehicle(@PathVariable Integer vehicleId) {
        return ResponseEntity.ok(compatibilityService.listForVehicle(vehicleId));
    }

    @GetMapping("/battery/{batteryId}")
    public ResponseEntity<List<VehicleBatteryCompatibilityResponse>> listForBattery(@PathVariable Integer batteryId) {
        return ResponseEntity.ok(compatibilityService.listForBattery(batteryId));
    }
}
