package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.service.SystemConfigService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/system-config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * Lấy tất cả cấu hình hệ thống
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllSystemConfig() {
        try {
            Map<String, Object> config = systemConfigService.getAllSystemConfig();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("config", config);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách trạng thái pin
     */
    @GetMapping("/battery-statuses")
    public ResponseEntity<Map<String, Object>> getBatteryStatuses() {
        try {
            Map<String, Object> statuses = systemConfigService.getBatteryStatuses();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statuses);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách tình trạng pin
     */
    @GetMapping("/battery-conditions")
    public ResponseEntity<Map<String, Object>> getBatteryConditions() {
        try {
            Map<String, Object> conditions = systemConfigService.getBatteryConditions();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", conditions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách trạng thái trạm
     */
    @GetMapping("/station-statuses")
    public ResponseEntity<Map<String, Object>> getStationStatuses() {
        try {
            Map<String, Object> statuses = systemConfigService.getStationStatuses();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", statuses);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách model xe
     */
    @GetMapping("/vehicle-models")
    public ResponseEntity<Map<String, Object>> getVehicleModels() {
        try {
            Map<String, Object> models = systemConfigService.getVehicleModels();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", models);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy danh sách tên trạm mẫu
     */
    @GetMapping("/station-name-templates")
    public ResponseEntity<Map<String, Object>> getStationNameTemplates() {
        try {
            Map<String, Object> templates = systemConfigService.getStationNameTemplates();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", templates);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
