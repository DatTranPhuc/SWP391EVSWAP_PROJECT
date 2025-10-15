package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.ReportService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String showReports(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirectAttributes.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem báo cáo.");
            return "redirect:/login";
        }

        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("report", reportService.getReportSummary());
        return "report";
    }
}
