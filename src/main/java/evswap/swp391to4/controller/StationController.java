package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    /**
     * Tìm kiếm trạm theo tên (hoặc trả về tất cả nếu không có filter)
     * GET /api/stations?name=...
     */
    @GetMapping
    public ResponseEntity<List<StationResponse>> getStations(@RequestParam(value = "name", required = false) String name) {
        if (name != null && !name.isBlank()) {
            return ResponseEntity.ok(stationService.searchByName(name));
        }
        return ResponseEntity.ok(stationService.getAllStations());
    }

    /**
     * Lấy chi tiết 1 trạm theo id
     * GET /api/stations/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<StationResponse> getStationDetails(@PathVariable Integer id) {
        return ResponseEntity.ok(stationService.findById(id));
    }
}
