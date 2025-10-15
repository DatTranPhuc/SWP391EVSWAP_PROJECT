package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/batteries")
@RequiredArgsConstructor
public class BatteryController {

    private final BatteryRepository batteryRepository;
    private final StationRepository stationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getBatteries(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Battery> batteries = batteryRepository.findAll();
            List<Map<String, Object>> batteryList = batteries.stream()
                .map(battery -> {
                    Map<String, Object> batteryData = new HashMap<>();
                    batteryData.put("batteryId", battery.getBatteryId());
                    batteryData.put("model", battery.getModel());
                    batteryData.put("state", battery.getState());
                    batteryData.put("sohPercent", battery.getSohPercent());
                    batteryData.put("socPercent", battery.getSocPercent());
                    
                    if (battery.getStation() != null) {
                        batteryData.put("stationId", battery.getStation().getStationId());
                        batteryData.put("stationName", battery.getStation().getName());
                    }
                    
                    return batteryData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("batteries", batteryList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createBattery(@RequestHeader("Authorization") String authHeader,
                                                            @RequestBody Map<String, Object> request) {
        try {
            String model = (String) request.get("model");
            String state = (String) request.getOrDefault("state", "full");
            Integer sohPercent = (Integer) request.getOrDefault("sohPercent", 100);
            Integer socPercent = (Integer) request.getOrDefault("socPercent", 100);
            Integer stationId = (Integer) request.get("stationId");

            Station station = null;
            if (stationId != null) {
                station = stationRepository.findById(stationId)
                    .orElse(null);
            }

            Battery battery = Battery.builder()
                .model(model)
                .state(state)
                .sohPercent(sohPercent)
                .socPercent(socPercent)
                .station(station)
                .build();

            Battery savedBattery = batteryRepository.save(battery);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("battery", Map.of(
                "batteryId", savedBattery.getBatteryId(),
                "model", savedBattery.getModel(),
                "state", savedBattery.getState(),
                "sohPercent", savedBattery.getSohPercent(),
                "socPercent", savedBattery.getSocPercent()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{batteryId}")
    public ResponseEntity<Map<String, Object>> getBattery(@PathVariable Integer batteryId,
                                                         @RequestHeader("Authorization") String authHeader) {
        try {
            Battery battery = batteryRepository.findById(batteryId)
                .orElseThrow(() -> new RuntimeException("Battery not found"));

            Map<String, Object> batteryData = new HashMap<>();
            batteryData.put("batteryId", battery.getBatteryId());
            batteryData.put("model", battery.getModel());
            batteryData.put("state", battery.getState());
            batteryData.put("sohPercent", battery.getSohPercent());
            batteryData.put("socPercent", battery.getSocPercent());
            
            if (battery.getStation() != null) {
                batteryData.put("stationId", battery.getStation().getStationId());
                batteryData.put("stationName", battery.getStation().getName());
            }

            return ResponseEntity.ok(batteryData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{batteryId}")
    public ResponseEntity<Map<String, Object>> updateBattery(@PathVariable Integer batteryId,
                                                            @RequestHeader("Authorization") String authHeader,
                                                            @RequestBody Map<String, Object> request) {
        try {
            Battery battery = batteryRepository.findById(batteryId)
                .orElseThrow(() -> new RuntimeException("Battery not found"));

            String state = (String) request.get("state");
            Integer sohPercent = (Integer) request.get("sohPercent");
            Integer socPercent = (Integer) request.get("socPercent");

            if (state != null) {
                battery.setState(state);
            }
            if (sohPercent != null) {
                battery.setSohPercent(sohPercent);
            }
            if (socPercent != null) {
                battery.setSocPercent(socPercent);
            }

            batteryRepository.save(battery);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Battery updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{batteryId}")
    public ResponseEntity<Map<String, Object>> deleteBattery(@PathVariable Integer batteryId,
                                                            @RequestHeader("Authorization") String authHeader) {
        try {
            Battery battery = batteryRepository.findById(batteryId)
                .orElseThrow(() -> new RuntimeException("Battery not found"));

            batteryRepository.delete(battery);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Battery deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<Map<String, Object>> getBatteriesByStation(@PathVariable Integer stationId,
                                                                    @RequestHeader("Authorization") String authHeader) {
        try {
            List<Battery> batteries = batteryRepository.findAll().stream()
                .filter(battery -> battery.getStation() != null && 
                        battery.getStation().getStationId().equals(stationId))
                .collect(Collectors.toList());

            List<Map<String, Object>> batteryList = batteries.stream()
                .map(battery -> {
                    Map<String, Object> batteryData = new HashMap<>();
                    batteryData.put("batteryId", battery.getBatteryId());
                    batteryData.put("model", battery.getModel());
                    batteryData.put("state", battery.getState());
                    batteryData.put("sohPercent", battery.getSohPercent());
                    batteryData.put("socPercent", battery.getSocPercent());
                    return batteryData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("batteries", batteryList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}
