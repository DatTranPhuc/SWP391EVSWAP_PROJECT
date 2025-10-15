package evswap.swp391to4.controller;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/stations")
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    // Hiển thị giao diện trạm kèm danh sách (route mặc định: /stations)
    @GetMapping
    public String listStations(Model model) {
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("showSearch", true);
        return "station-manage";
    }

    // Hiển thị form THÊM mới
    @GetMapping("/add-form")
    public String showAddForm(Model model) {
        model.addAttribute("addOrEdit", true);
        model.addAttribute("editMode", false);
        model.addAttribute("stationForm", new StationCreateRequest());
        return "station-manage";
    }

    // Xử lý thêm mới
    @PostMapping("/add")
    public String addStation(@ModelAttribute("stationForm") StationCreateRequest req, RedirectAttributes redirect) {
        stationService.createStation(req);
        return "redirect:/stations";
    }

    // Form sửa trạm
    @GetMapping("/edit-form/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        StationResponse s = stationService.findById(id);
        model.addAttribute("addOrEdit", true);
        model.addAttribute("editMode", true);
        model.addAttribute("stationForm", s);
        return "station-manage";
    }

    // Xử lý cập nhật
    @PostMapping("/update/{id}")
    public String updateStation(@PathVariable Integer id, @ModelAttribute("stationForm") StationCreateRequest req) {
        stationService.updateStation(id, req);
        return "redirect:/stations";
    }

    // Xoá trạm
    @PostMapping("/delete/{id}")
    public String deleteStation(@PathVariable Integer id) {
        stationService.deleteStation(id);
        return "redirect:/stations";
    }

    // Xem chi tiết
    @GetMapping("/{id}")
    public String viewStation(@PathVariable Integer id, Model model) {
        model.addAttribute("station", stationService.findById(id));
        return "station-manage";
    }

    // Tìm kiếm trạm theo tên
    @GetMapping("/search")
    public String searchByName(@RequestParam(name = "name", required = false) String name, Model model) {
        model.addAttribute("stations", stationService.searchByName(name));
        model.addAttribute("showSearch", true);
        return "station-manage";
    }

    // Tìm trạm gần vị trí
    @GetMapping("/nearby")
    public String findNearby(@RequestParam BigDecimal lat,
                             @RequestParam BigDecimal lng,
                             @RequestParam(defaultValue = "5") double radiusKm,
                             Model model) {
        model.addAttribute("nearbyStations", stationService.findNearby(lat, lng, radiusKm));
        return "station-manage";
    }
}
