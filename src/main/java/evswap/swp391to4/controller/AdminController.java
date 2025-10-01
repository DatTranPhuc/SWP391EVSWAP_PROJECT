package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Station;
import evswap.swp391to4.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {

    private final StaffService staffService;

    /**
     * API tạo Staff
     * POST http://localhost:8080/api/admin/add-staff
     * Body: JSON
     * {
     *   "fullName": "John Doe",
     *   "email": "john@example.com",
     *   "password": "123456",
     *   "stationId": 1
     * }
     */
    @PostMapping("/add-staff")
    public Map<String, String> addStaff(@RequestBody Map<String, Object> body) {
        Map<String, String> res = new HashMap<>();
        try {
            String fullName = (String) body.get("fullName");
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            Integer stationId = (Integer) body.get("stationId");

            // tạo Station dummy, chỉ cần id để liên kết
            Station station = new Station();
            station.setStationId(stationId);

            staffService.createStaff(station, fullName, email, password);

            res.put("status", "success");
            res.put("message", "Staff account created!");
        } catch (Exception e) {
            res.put("status", "error");
            res.put("message", e.getMessage());
        }
        return res;
    }
}
