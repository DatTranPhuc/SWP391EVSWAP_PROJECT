package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ApiResponse;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<StationResponse>>> searchStations(
            @RequestParam(value = "name", required = false) String name) {
        List<StationResponse> stations = (name == null || name.isBlank())
                ? stationService.getAllStations()
                : stationService.searchByName(name);
        return ResponseEntity.ok(ApiResponse.success("Danh sách trạm", stations));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StationResponse>> getStation(@PathVariable Integer id) {
        StationResponse station = stationService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Thông tin trạm", station));
    }
}
