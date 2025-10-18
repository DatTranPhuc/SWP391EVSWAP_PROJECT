package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    /**
     * Hiển thị trang quản lý trạm chính.
     * Luôn cung cấp danh sách tất cả trạm và một form trống cho modal.
     */
    @GetMapping
    public String listStations(Model model) {
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("stationForm", new StationCreateRequest());
        // Thêm một danh sách rỗng để Thymeleaf không báo lỗi khi render lần đầu
        model.addAttribute("nearbyStations", Collections.emptyList());
        return "station-manage";
    }

    /**
     * Xử lý yêu cầu thêm một trạm mới từ modal.
     */
    @PostMapping("/add")
    public String addStation(@ModelAttribute("stationForm") StationCreateRequest req, RedirectAttributes redirect) {
        try {
            stationService.createStation(req);
            redirect.addFlashAttribute("stationSuccess", "Thêm trạm mới thành công!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("stationError", e.getMessage());
        }
        return "redirect:/stations";
    }

    /**
     * Xử lý yêu cầu xóa một trạm.
     */
    @PostMapping("/delete/{id}")
    public String deleteStation(@PathVariable Integer id, RedirectAttributes redirect) {
        try {
            stationService.deleteStation(id);
            redirect.addFlashAttribute("stationSuccess", "Đã xoá trạm thành công!");
        } catch (IllegalStateException e) {
            redirect.addFlashAttribute("stationError", e.getMessage());
        }
        return "redirect:/stations";
    }

    /**
     * Hiển thị trang chi tiết một trạm.
     */
    @GetMapping("/{id}")
    public String viewStation(@PathVariable Integer id, Model model) {
        model.addAttribute("station", stationService.findById(id));
        // Cần thêm các thuộc tính này để các phần khác của trang không bị lỗi
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("stationForm", new StationCreateRequest());
        return "station-manage";
    }

    /**
     * Xử lý yêu cầu tìm kiếm trạm theo tên.
     */
    @GetMapping("/search")
    public String searchByName(@RequestParam(name = "name", required = false) String name, Model model) {
        model.addAttribute("stations", stationService.searchByName(name));
        model.addAttribute("stationForm", new StationCreateRequest());
        return "station-manage";
    }

    /**
     * Xử lý yêu cầu tìm các trạm gần một vị trí tọa độ cho trước.
     * Đây là phương thức quan trọng nhất cần kiểm tra.
     */
    @GetMapping("/nearby")
    public String findNearby(@RequestParam BigDecimal lat,
                             @RequestParam BigDecimal lng,
                             @RequestParam(defaultValue = "5") double radiusKm,
                             Model model) {
        // 1. Gọi service để lấy danh sách các trạm gần đó
        List<StationResponse> nearbyStations = stationService.findNearby(lat, lng, radiusKm);

        // 2. Thêm danh sách kết quả tìm kiếm vào model
        model.addAttribute("nearbyStations", nearbyStations);

        // 3. Thêm tọa độ người dùng vào model để JavaScript có thể vẽ bản đồ
        model.addAttribute("userLat", lat);
        model.addAttribute("userLng", lng);

        // 4. Thêm các thuộc tính phụ để các phần khác của trang (danh sách chính, modal) không bị lỗi
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("stationForm", new StationCreateRequest());

        return "station-manage";
    }
}