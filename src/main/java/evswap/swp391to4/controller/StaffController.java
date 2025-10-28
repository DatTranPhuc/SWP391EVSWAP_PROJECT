package evswap.swp391to4.controller;

import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.service.BatteryService;
import evswap.swp391to4.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BatteryService batteryService;
    private final StaffService staffService;

    /**
     * Dashboard của staff, trả về số liệu pin theo từng trạng thái và thông tin trạm
     */
    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(@RequestHeader(name = "Staff-Id") Integer staffId) {
        if (staffId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn chưa đăng nhập!"));
        }
        // Giả sử StaffService có hàm lấy Staff entity từ StaffId
        Optional<Staff> staffOpt = staffService.login(getEmailFromId(staffId), "dummy"); // mock login để lấy Staff
        Staff staff = staffOpt.orElse(null);
        if (staff == null || staff.getStation() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản Staff chưa gán trạm!"));
        }
        Station station = staff.getStation();
        long fullCount = batteryService.countBatteriesByState(station, "full");
        long chargingCount = batteryService.countBatteriesByState(station, "charging");
        long maintenanceCount = batteryService.countBatteriesByState(station, "maintenance");
        long retiredCount = batteryService.countBatteriesByState(station, "retired");
        int totalCount = batteryService.getAllBatteriesForStation(station).size();

        return ResponseEntity.ok(Map.of(
                "staffName", staff.getFullName(),
                "stationName", station.getName(),
                "stationAddress", station.getAddress(),
                "stationId", station.getStationId(),
                "fullCount", fullCount,
                "chargingCount", chargingCount,
                "maintenanceCount", maintenanceCount,
                "retiredCount", retiredCount,
                "totalCount", totalCount
        ));
    }

    /**
     * Quản lý/thống kê/hủy/tìm kiếm danh sách pin tại trạm của staff
     */
    @GetMapping("/batteries")
    public ResponseEntity<List<Battery>> manageBatteries(
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            @RequestHeader(name = "Staff-Id") Integer staffId) {
        if (staffId == null) {
            return ResponseEntity.status(401).build();
        }
        Optional<Staff> staffOpt = staffService.login(getEmailFromId(staffId), "dummy");
        Staff staff = staffOpt.orElse(null);
        if (staff == null || staff.getStation() == null) {
            return ResponseEntity.badRequest().build();
        }
        List<Battery> batteryList = batteryService.searchBatteriesForStation(staff.getStation(), searchType, searchTerm);
        return ResponseEntity.ok(batteryList);
    }

    /**
     * Thêm pin mới vào trạm
     */
    @PostMapping("/batteries")
    public ResponseEntity<?> handleCreateBattery(
            @Validated @RequestBody BatteryCreateRequest dto,
            @RequestHeader(name = "Staff-Id") Integer staffId) {
        if (staffId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn chưa đăng nhập!"));
        }
        Optional<Staff> staffOpt = staffService.login(getEmailFromId(staffId), "dummy");
        Staff staff = staffOpt.orElse(null);
        if (staff == null || staff.getStation() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản Staff chưa gán trạm!"));
        }
        try {
            batteryService.createBatteries(dto, staff);
            return ResponseEntity.ok(Map.of("success", "Đã thêm " + dto.getQuantity() + " pin (Model: " + dto.getModel() + ") thành công!"));
        } catch (Exception logicError) {
            return ResponseEntity.badRequest().body(Map.of("error", logicError.getMessage()));
        }
    }

    /**
     * Cập nhật trạng thái pin
     */
    @PutMapping("/batteries/{batteryId}")
    public ResponseEntity<?> handleUpdateBatteryState(
            @PathVariable("batteryId") Integer batteryId,
            @RequestParam("newState") String newState,
            @RequestHeader(name = "Staff-Id") Integer staffId) {
        if (staffId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Bạn chưa đăng nhập!"));
        }
        Optional<Staff> staffOpt = staffService.login(getEmailFromId(staffId), "dummy");
        Staff staff = staffOpt.orElse(null);
        if (staff == null || staff.getStation() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tài khoản Staff chưa gán trạm!"));
        }
        try {
            batteryService.updateBatteryState(batteryId, newState, staff);
            return ResponseEntity.ok(Map.of("success", "Đã cập nhật Pin #" + batteryId + " trạng thái thành công!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Helper: cần sửa lại, lấy email thực hoặc dùng token auth ở production
    private String getEmailFromId(Integer staffId) {
        // TODO: Service lấy email staff theo id (hoặc dùng token tại frontend)
        return "";
    }
}
