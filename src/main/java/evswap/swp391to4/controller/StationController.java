package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @GetMapping("/stations")
    public String showStations(Model model, HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            model.addAttribute("loginRequired", "Vui lòng đăng nhập để sử dụng chức năng tìm trạm.");
            return "login";
        }

        model.addAttribute("driverName", driver.getFullName());
        return "stations";
    }

    @GetMapping("/api/stations")
    @ResponseBody
    public List<StationResponse> getStations() {
        return stationService.getAllStations();
    }
}
