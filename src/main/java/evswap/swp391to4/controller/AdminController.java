package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.AdminRepository;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;
    private final DriverRepository driverRepository;
    private final StaffRepository staffRepository;
    private final AdminRepository adminRepository;
    private final StationRepository stationRepository;

    @PostMapping("/add-staff")
    public ResponseEntity<?> addStaff(@RequestBody StaffCreateRequest req) {
        try {
            StaffResponse resp = staffService.createStaff(req);
            return ResponseEntity.status(201).body(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    @PostMapping("/add-station")
    public ResponseEntity<?> addStation(@RequestBody StationCreateRequest req) {
        try {
            StationResponse resp = stationService.createStation(req);
            return ResponseEntity.status(201).body(resp);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAdminUsers(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Driver> drivers = driverRepository.findAll();
            List<Staff> staff = staffRepository.findAll();
            List<Admin> admins = adminRepository.findAll();

            List<Map<String, Object>> userList = new ArrayList<>();

            // Add drivers
            drivers.forEach(driver -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", driver.getDriverId());
                userData.put("email", driver.getEmail());
                userData.put("fullName", driver.getFullName());
                userData.put("role", "driver");
                userData.put("isActive", driver.getEmailVerified());
                userData.put("createdAt", driver.getCreatedAt());
                userList.add(userData);
            });

            // Add staff
            staff.forEach(s -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", s.getStaffId());
                userData.put("email", s.getEmail());
                userData.put("fullName", s.getFullName());
                userData.put("role", "staff");
                userData.put("isActive", s.getIsActive());
                userData.put("createdAt", Instant.now()); // Mock creation time
                userList.add(userData);
            });

            // Add admins
            admins.forEach(admin -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("userId", admin.getAdminId());
                userData.put("email", admin.getEmail());
                userData.put("fullName", admin.getFullName());
                userData.put("role", "admin");
                userData.put("isActive", true);
                userData.put("createdAt", admin.getCreatedAt());
                userList.add(userData);
            });

            Map<String, Object> response = new HashMap<>();
            response.put("users", userList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping("/staff")
    public ResponseEntity<Map<String, Object>> createStaff(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody Map<String, Object> request) {
        try {
            String email = (String) request.get("email");
            String fullName = (String) request.get("fullName");
            Integer stationId = (Integer) request.get("stationId");

            Station station = null;
            if (stationId != null) {
                station = stationRepository.findById(stationId)
                    .orElse(null);
            }

            Staff staff = Staff.builder()
                .email(email)
                .fullName(fullName)
                .station(station)
                .isActive(true)
                .build();

            Staff savedStaff = staffRepository.save(staff);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("staff", Map.of(
                "staffId", savedStaff.getStaffId(),
                "email", savedStaff.getEmail(),
                "fullName", savedStaff.getFullName()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/stations")
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
                .latitude(new java.math.BigDecimal(latitude.toString()))
                .longitude(new java.math.BigDecimal(longitude.toString()))
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
}
