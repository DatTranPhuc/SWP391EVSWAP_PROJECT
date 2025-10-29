package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ApiResponse;
import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.service.BatteryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final BatteryService batteryService;

    private Staff checkStaffLogin(HttpSession session) {
        Staff staff = (Staff) session.getAttribute("loggedInStaff");
        if (staff == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Bạn chưa đăng nhập! Vui lòng đăng nhập với tư cách Staff.");
        }
        if (staff.getStation() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tài khoản Staff của bạn chưa được gán vào trạm nào. Vui lòng liên hệ Admin.");
        }
        return staff;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(HttpSession session) {
        Staff staff = checkStaffLogin(session);
        Station station = staff.getStation();

        Map<String, Object> data = new HashMap<>();
        data.put("staffName", staff.getFullName());
        data.put("stationName", station.getName());
        data.put("stationAddress", station.getAddress());
        data.put("stationId", station.getStationId());
        data.put("fullCount", batteryService.countBatteriesByState(station, "full"));
        data.put("chargingCount", batteryService.countBatteriesByState(station, "charging"));
        data.put("maintenanceCount", batteryService.countBatteriesByState(station, "maintenance"));
        data.put("retiredCount", batteryService.countBatteriesByState(station, "retired"));
        data.put("totalCount", batteryService.getAllBatteriesForStation(station).size());

        return ResponseEntity.ok(ApiResponse.success("Thông tin dashboard staff.", data));
    }

    @GetMapping("/batteries")
    public ResponseEntity<ApiResponse<Map<String, Object>>> manageBatteriesPage(
            @RequestParam(name = "searchType", required = false) String searchType,
            @RequestParam(name = "searchTerm", required = false) String searchTerm,
            HttpSession session) {

        Staff staff = checkStaffLogin(session);

        List<Battery> batteryList = batteryService.searchBatteriesForStation(staff.getStation(), searchType, searchTerm);
        Map<String, Object> data = new HashMap<>();
        data.put("batteryList", batteryList);
        data.put("stationName", staff.getStation().getName());
        data.put("currentSearchType", searchType);
        data.put("currentSearchTerm", searchTerm);

        return ResponseEntity.ok(ApiResponse.success("Danh sách pin tại trạm", data));
    }

    @PostMapping("/batteries")
    public ResponseEntity<ApiResponse<List<Battery>>> handleCreateBattery(
            @Valid @RequestBody BatteryCreateRequest dto,
            HttpSession session) {

        Staff staff = checkStaffLogin(session);
        batteryService.createBatteries(dto, staff);

        List<Battery> batteryList = batteryService.getAllBatteriesForStation(staff.getStation());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Đã thêm pin thành công!", batteryList));
    }

    @PatchMapping("/batteries/{batteryId}")
    public ResponseEntity<ApiResponse<Void>> handleUpdateBatteryState(
            @PathVariable("batteryId") Integer batteryId,
            @RequestBody Map<String, String> payload,
            HttpSession session) {

        Staff staff = checkStaffLogin(session);
        String newState = payload.getOrDefault("newState", "");
        if (newState.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trạng thái mới là bắt buộc.");
        }

        batteryService.updateBatteryState(batteryId, newState, staff);
        return ResponseEntity.ok(ApiResponse.<Void>success("Đã cập nhật pin thành công!"));
    }
}
