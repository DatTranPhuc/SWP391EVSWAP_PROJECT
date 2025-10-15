package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest; // Giả sử bạn có DTO cho Update
import evswap.swp391to4.dto.StationCreateRequest; // Giả sử bạn có DTO cho Update
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

    // ====================== STAFF MANAGEMENT (CRUD) ======================

    // 1. CREATE: Thêm nhân viên mới
    @PostMapping("/staff")
    public ResponseEntity<?> createStaff(@RequestBody StaffCreateRequest req) {
        try {
            StaffResponse newStaff = staffService.createStaff(req);
            return ResponseEntity.ok(newStaff);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. READ: Lấy danh sách tất cả nhân viên
    @GetMapping("/staff")
    public ResponseEntity<List<StaffResponse>> getAllStaff() {
        return ResponseEntity.ok(staffService.getAllStaff());
    }

    // 3. READ BY ID: Lấy thông tin một nhân viên theo ID
    @GetMapping("/staff/{id}")
    public ResponseEntity<?> getStaffById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(staffService.getStaffById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. UPDATE: Cập nhật thông tin nhân viên
    @PutMapping("/staff/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Integer id, @RequestBody StaffCreateRequest req) {
        try {
            StaffResponse updatedStaff = staffService.updateStaff(id, req);
            return ResponseEntity.ok(updatedStaff);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. DELETE: Xóa một nhân viên
    @DeleteMapping("/staff/{id}")
    public ResponseEntity<?> deleteStaff(@PathVariable Integer id) {
        try {
            staffService.deleteStaff(id);
            return ResponseEntity.ok("Staff with id " + id + " deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ====================== STATION MANAGEMENT (CRUD) ======================

    // 1. CREATE: Thêm trạm mới
    @PostMapping("/stations")
    public ResponseEntity<?> createStation(@RequestBody StationCreateRequest req) {
        try {
            StationResponse newStation = stationService.createStation(req);
            return ResponseEntity.ok(newStation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. READ: Lấy danh sách tất cả các trạm
    @GetMapping("/stations")
    public ResponseEntity<List<StationResponse>> getAllStations() {
        return ResponseEntity.ok(stationService.getAllStations());
    }

    // 3. READ BY ID: Lấy thông tin một trạm theo ID
    @GetMapping("/stations/{id}")
    public ResponseEntity<?> getStationById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(stationService.getStationById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. UPDATE: Cập nhật thông tin trạm
    @PutMapping("/stations/{id}")
    public ResponseEntity<?> updateStation(@PathVariable Integer id, @RequestBody StationCreateRequest req) {
        try {
            StationResponse updatedStation = stationService.updateStation(id, req);
            return ResponseEntity.ok(updatedStation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. DELETE: Xóa một trạm
    @DeleteMapping("/stations/{id}")
    public ResponseEntity<?> deleteStation(@PathVariable Integer id) {
        try {
            stationService.deleteStation(id);
            return ResponseEntity.ok("Station with id " + id + " deleted successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}