package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
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

    // ------------------ STAFF ------------------

    // Tạo nhân viên mới
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

    // Lấy thông tin 1 nhân viên theo ID
    @GetMapping("/staff/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable Integer id) {
        try {
            StaffResponse resp = staffService.getStaffById(id);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // Lấy danh sách tất cả nhân viên
    @GetMapping("/staff")
    public ResponseEntity<?> getAllStaff() {
        try {
            List<StaffResponse> list = staffService.getAllStaff();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // ------------------ STATION ------------------

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
}
