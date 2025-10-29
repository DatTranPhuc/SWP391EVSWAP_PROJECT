package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ApiResponse;
import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StaffUpdateRequest;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;

    private Admin checkAdminLogin(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("loggedInAdmin");
        if (admin == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Bạn chưa đăng nhập! Vui lòng đăng nhập với tư cách Admin.");
        }
        return admin;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard(HttpSession session) {
        Admin admin = checkAdminLogin(session);
        Map<String, Object> data = Map.of(
                "adminName", admin.getFullName(),
                "next", List.of("/api/admin/staff", "/api/admin/stations")
        );
        return ResponseEntity.ok(ApiResponse.success("Thông tin dashboard admin.", data));
    }

    @GetMapping("/staff")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> listStaff(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session) {
        checkAdminLogin(session);
        List<StaffResponse> staffList = staffService.getAllStaff(search);
        return ResponseEntity.ok(ApiResponse.success("Danh sách nhân viên", staffList));
    }

    @GetMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<StaffUpdateRequest>> getStaff(@PathVariable Integer id,
                                                                     HttpSession session) {
        checkAdminLogin(session);
        StaffUpdateRequest staff = staffService.getStaffDetails(id);
        return ResponseEntity.ok(ApiResponse.success("Thông tin chi tiết nhân viên", staff));
    }

    @PostMapping("/staff")
    public ResponseEntity<ApiResponse<StaffResponse>> addStaff(@Valid @RequestBody StaffCreateRequest staff,
                                                               HttpSession session) {
        checkAdminLogin(session);
        StaffResponse created = staffService.createStaff(staff);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo nhân viên thành công!", created));
    }

    @PutMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> editStaff(@PathVariable Integer id,
                                                                 @Valid @RequestBody StaffUpdateRequest staff,
                                                                 HttpSession session) {
        checkAdminLogin(session);
        StaffResponse updated = staffService.updateStaff(id, staff);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhân viên thành công!", updated));
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable Integer id,
                                                         HttpSession session) {
        checkAdminLogin(session);
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Xóa nhân viên thành công!"));
    }

    @GetMapping("/stations")
    public ResponseEntity<ApiResponse<List<StationResponse>>> listStations(
            @RequestParam(value = "search", required = false) String search,
            HttpSession session) {
        checkAdminLogin(session);
        List<StationResponse> stationList = (search == null || search.isBlank())
                ? stationService.getAllStations()
                : stationService.searchByName(search);
        return ResponseEntity.ok(ApiResponse.success("Danh sách trạm", stationList));
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<ApiResponse<StationResponse>> getStation(@PathVariable Integer id,
                                                                    HttpSession session) {
        checkAdminLogin(session);
        return ResponseEntity.ok(ApiResponse.success("Thông tin trạm", stationService.findById(id)));
    }

    @PostMapping("/stations")
    public ResponseEntity<ApiResponse<StationResponse>> addStation(@Valid @RequestBody StationCreateRequest station,
                                                                   HttpSession session) {
        checkAdminLogin(session);
        StationResponse created = stationService.createStation(station);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo trạm thành công!", created));
    }

    @PutMapping("/stations/{id}")
    public ResponseEntity<ApiResponse<StationResponse>> editStation(@PathVariable Integer id,
                                                                    @Valid @RequestBody StationCreateRequest stationRequest,
                                                                    HttpSession session) {
        checkAdminLogin(session);
        StationResponse updated = stationService.updateStation(id, stationRequest);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật trạm thành công!", updated));
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStation(@PathVariable Integer id,
                                                           HttpSession session) {
        checkAdminLogin(session);
        stationService.deleteStation(id);
        return ResponseEntity.ok(ApiResponse.<Void>success("Xóa trạm thành công!"));
    }
}
