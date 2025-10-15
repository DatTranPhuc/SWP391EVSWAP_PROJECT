package evswap.swp391to4.controller;

import evswap.swp391to4.dto.dashboard.DashboardSnapshot;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DashboardDataService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardDataService dashboardDataService;

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(HttpSession session, Model model) {
        boolean adminLoggedIn = session.getAttribute("loggedInAdmin") != null;
        boolean staffLoggedIn = session.getAttribute("loggedInStaff") != null;
        model.addAttribute("canAccessOperations", adminLoggedIn || staffLoggedIn);
        model.addAttribute("canAccessAdmin", adminLoggedIn);

        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver != null) {
            DashboardSnapshot snapshot = dashboardDataService.buildSnapshot(driver.getDriverId());

            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
            model.addAttribute("driverEmail", driver.getEmail());
            model.addAttribute("driverPhone", driver.getPhone() != null ? driver.getPhone() : "Chưa cập nhật");
            model.addAttribute("driverId", driver.getDriverId());
            model.addAttribute("driverSince", driver.getCreatedAt());
            model.addAttribute("snapshot", snapshot);

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            if (snapshot.overview().totalPaid() != null) {
                model.addAttribute("totalPaidFormatted", currencyFormat.format(snapshot.overview().totalPaid()));
            } else {
                model.addAttribute("totalPaidFormatted", currencyFormat.format(0));
            }
            if (snapshot.overview().lastPayment() != null && snapshot.overview().lastPayment().amount() != null) {
                model.addAttribute("lastPaymentAmount", currencyFormat.format(snapshot.overview().lastPayment().amount()));
            } else {
                model.addAttribute("lastPaymentAmount", null);
            }

            model.addAttribute("loggedIn", true);
            model.addAttribute("activePage", "overview");
        } else {
            model.addAttribute("loggedIn", false);
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

        String normalizedFeature = feature == null ? "" : feature.trim();

        if ("Tổng quan".equalsIgnoreCase(normalizedFeature)) {
            redirect.addFlashAttribute("dashboardMessage", "Bạn đang ở trang tổng quan EV SWAP.");
            return "redirect:/dashboard";
        }

        if ("Phương tiện".equalsIgnoreCase(normalizedFeature)) {
            return "redirect:/vehicles/manage";
        }

        if ("Vận hành".equalsIgnoreCase(normalizedFeature)
                || "Trung tâm vận hành".equalsIgnoreCase(normalizedFeature)
                || "Operations".equalsIgnoreCase(normalizedFeature)) {
            if (!hasOperationsAccess(session)) {
                redirect.addFlashAttribute("dashboardMessage", "Chức năng này chỉ dành cho nhân viên hoặc quản trị viên.");
                return "redirect:/dashboard";
            }
            return "redirect:/operations/console";
        }

        if ("Tìm trạm".equalsIgnoreCase(normalizedFeature)) {
            redirect.addFlashAttribute("dashboardMessage", "Chức năng Tìm trạm đang được phát triển.");
            return "redirect:/dashboard";
        }

        if ("Báo cáo".equalsIgnoreCase(normalizedFeature)) {
            redirect.addFlashAttribute("dashboardMessage", "Chức năng Báo cáo sẽ sớm ra mắt.");
            return "redirect:/dashboard";
        }

        if ("Tài khoản".equalsIgnoreCase(normalizedFeature)) {
            redirect.addFlashAttribute("dashboardMessage", "Truy cập trang tài khoản trong phiên bản sắp tới.");
            return "redirect:/dashboard";
        }

        if ("Hỗ trợ".equalsIgnoreCase(normalizedFeature)) {
            redirect.addFlashAttribute("dashboardMessage", "Đội ngũ hỗ trợ sẽ sẵn sàng sau khi bạn đăng nhập.");
            return "redirect:/dashboard";
        }

        redirect.addFlashAttribute("dashboardMessage", "Bạn đã chọn chức năng: " + normalizedFeature);
        return "redirect:/dashboard";
    }

    private boolean hasOperationsAccess(HttpSession session) {
        return session.getAttribute("loggedInAdmin") != null || session.getAttribute("loggedInStaff") != null;
    }

    private String extractInitial(String fullName) {
        if (fullName == null) {
            return "U";
        }
        String trimmed = fullName.trim();
        if (trimmed.isEmpty()) {
            return "U";
        }
        return trimmed.substring(0, 1).toUpperCase();
    }
}

