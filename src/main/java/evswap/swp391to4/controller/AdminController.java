package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StaffUpdateRequest;
import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StaffService;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final StaffService staffService;
    private final StationService stationService;

    // ====================== PHẦN QUẢN LÝ STAFF ======================

    @GetMapping("/staff")
    public ResponseEntity<List<StaffResponse>> listStaff(@RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(staffService.getAllStaff(search));
    }

    @GetMapping("/staff/{id}")
    public ResponseEntity<StaffUpdateRequest> getStaffDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(staffService.getStaffDetails(id));
    }

    @PostMapping("/staff")
    public ResponseEntity<StaffResponse> createStaff(@Validated @RequestBody StaffCreateRequest req) {
        return ResponseEntity.ok(staffService.createStaff(req));
    }

    @PutMapping("/staff/{id}")
    public ResponseEntity<StaffResponse> updateStaff(@PathVariable Integer id, @Validated @RequestBody StaffUpdateRequest req) {
        return ResponseEntity.ok(staffService.updateStaff(id, req));
    }

    @DeleteMapping("/staff/{id}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Integer id) {
        staffService.deleteStaff(id);
        return ResponseEntity.noContent().build();
    }

    // ====================== PHẦN QUẢN LÝ STATION ======================

    @GetMapping("/stations")
    public ResponseEntity<List<StationResponse>> listStations(@RequestParam(value = "search", required = false) String search) {
        if (search == null || search.isBlank())
            return ResponseEntity.ok(stationService.getAllStations());
        return ResponseEntity.ok(stationService.searchByName(search));
    }

    @GetMapping("/stations/{id}")
    public ResponseEntity<StationResponse> getStation(@PathVariable Integer id) {
        return ResponseEntity.ok(stationService.findById(id));
    }

    @PostMapping("/stations")
    public ResponseEntity<StationResponse> createStation(@Validated @RequestBody StationCreateRequest req) {
        return ResponseEntity.ok(stationService.createStation(req));
    }

    @PutMapping("/stations/{id}")
    public ResponseEntity<StationResponse> updateStation(@PathVariable Integer id, @Validated @RequestBody StationCreateRequest req) {
        return ResponseEntity.ok(stationService.updateStation(id, req));
    }

    @DeleteMapping("/stations/{id}")
    public ResponseEntity<Void> deleteStation(@PathVariable Integer id) {
        stationService.deleteStation(id);
        return ResponseEntity.noContent().build();
    }
}
