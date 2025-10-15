package evswap.swp391to4.controller;

import evswap.swp391to4.dto.dashboard.DashboardSnapshot;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DashboardDataService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardDataService dashboardDataService;

    @GetMapping
    public ResponseEntity<DashboardSnapshot> current(HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(dashboardDataService.buildSnapshot(driver.getDriverId()));
    }
}
