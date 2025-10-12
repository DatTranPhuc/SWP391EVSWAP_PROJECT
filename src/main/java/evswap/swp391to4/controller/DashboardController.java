package evswap.swp391to4.controller;

import evswap.swp391to4.dto.DashboardStats;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DashboardService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final StationService stationService;

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(HttpSession session, Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        DashboardStats stats = dashboardService.getDashboardStats();
        List<StationResponse> highlightStations = stationService.getAllStations()
                .stream()
                .filter(station -> station.getStatus() == null || !"closed".equalsIgnoreCase(station.getStatus()))
                .limit(3)
                .toList();

        model.addAttribute("stats", stats);
        model.addAttribute("highlightStations", highlightStations);
        model.addAttribute("hasStations", !highlightStations.isEmpty());

        if (driver != null) {
            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("loggedIn", true);
            model.addAttribute("vehicleCount", dashboardService.countVehiclesForDriver(driver.getDriverId()));
            model.addAttribute("driverId", driver.getDriverId());
        } else {
            model.addAttribute("loggedIn", false);
            model.addAttribute("vehicleCount", 0);
        }
        return "dashboard";
    }

    @PostMapping("/dashboard/action")
    public String handleDashboardAction(@RequestParam("feature") String feature,
                                        HttpSession session,
                                        RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để sử dụng chức năng.");
            return "redirect:/login";
        }

        redirect.addFlashAttribute("dashboardMessage", "Bạn đã chọn chức năng: " + feature);
        return "redirect:/dashboard";
    }
}

