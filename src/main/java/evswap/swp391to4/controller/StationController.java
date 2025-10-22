package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StationCreateRequest; // Vẫn cần cho form rỗng
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/stations") // Bắt buộc là "/stations" để Driver truy cập
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    /**
     * SỬA LẠI: Gộp listStations và searchByName
     * Hiển thị trang tìm trạm chính, xử lý cả tìm kiếm theo tên.
     */
    @GetMapping
    public String listStations(
            @RequestParam(value = "name", required = false) String name,
            Model model
    ) {
        List<StationResponse> stationList;
        if (name == null || name.isBlank()) {
            stationList = stationService.getAllStations();
        } else {
            stationList = stationService.searchByName(name);
        }

        // SỬA: Luôn thêm TẤT CẢ các thuộc tính mà trang cần
        model.addAttribute("stations", stationList); // Cho bảng danh sách
        model.addAttribute("searchName", name);     // Giữ lại từ khóa tìm kiếm
        model.addAttribute("nearbyStations", Collections.emptyList()); // Cho phần nearby
        model.addAttribute("stationForm", new StationCreateRequest()); // Cho JS (nếu có)
        // model.addAttribute("station", null); // Không cần, vì th:if="${station != null}"

        return "station-manage"; // Trả về file station-manage.html
    }

    /**
     * Xử lý yêu cầu XEM chi tiết một trạm.
     */
    @GetMapping("/{id}")
    public String viewStation(@PathVariable Integer id, Model model) {
        try {
            // 1. Lấy thông tin trạm chi tiết
            StationResponse station = stationService.findById(id);
            model.addAttribute("station", station); // {station} dùng cho phần "Chi tiết"

            // 2. SỬA: Thêm TẤT CẢ các thuộc tính phụ để trang không lỗi
            model.addAttribute("stations", stationService.getAllStations()); // {stations} cho danh sách chính
            model.addAttribute("searchName", "");
            model.addAttribute("nearbyStations", Collections.emptyList());
            model.addAttribute("stationForm", new StationCreateRequest());

            return "station-manage";
        } catch (Exception e) {
            return "redirect:/stations?error=notFound";
        }
    }

    /**
     * Xử lý yêu cầu tìm trạm gần đây.
     */
    @GetMapping("/nearby")
    public String findNearby(@RequestParam BigDecimal lat,
                             @RequestParam BigDecimal lng,
                             @RequestParam(defaultValue = "5") double radiusKm,
                             Model model) {
        // 1. Lấy danh sách trạm gần đó
        List<StationResponse> nearbyStations = stationService.findNearby(lat, lng, radiusKm);
        model.addAttribute("nearbyStations", nearbyStations);

        // 2. Thêm tọa độ người dùng để vẽ bản đồ
        model.addAttribute("userLat", lat);
        model.addAttribute("userLng", lng);

        // 3. SỬA: Thêm TẤT CẢ các thuộc tính phụ để trang không lỗi
        model.addAttribute("stations", stationService.getAllStations()); // {stations} cho danh sách chính
        model.addAttribute("searchName", "");
        model.addAttribute("stationForm", new StationCreateRequest());

        return "station-manage";
    }
}