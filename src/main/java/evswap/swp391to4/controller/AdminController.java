package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;

    // ====================== STAFF ======================

    // GET mẫu để tạo nhân viên (template)
    @GetMapping("/staff/template")
    public ResponseEntity<StaffCreateRequest> getStaffTemplate() {
        return ResponseEntity.ok(new StaffCreateRequest());
    }

    // Thêm nhân viên mới
    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody StaffCreateRequest req) {
        try {
            staffService.createStaff(req);
            return ResponseEntity.ok("Staff created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ====================== STATION ======================

    @GetMapping("/stations/template")
    public ResponseEntity<StationCreateRequest> getStationTemplate() {
        return ResponseEntity.ok(new StationCreateRequest());
    }

    // Thêm trạm mới
    @PostMapping("/stations")
    public ResponseEntity<?> createStation(@RequestBody StationCreateRequest req) {
        try {
            stationService.createStation(req);
            return ResponseEntity.ok("Station created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
