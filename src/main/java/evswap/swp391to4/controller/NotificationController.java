package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.NotificationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public String listNotifications(HttpSession session, Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        List<NotificationResponse> notifications = notificationService.getNotificationsByDriver(driver.getDriverId());
        long unreadCount = notificationService.getUnreadCount(driver.getDriverId());

        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));

        return "notifications";
    }

    @PostMapping("/{id}/read")
    @ResponseBody
    public Map<String, Object> markAsRead(@PathVariable("id") Integer notiId, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        
        if (driver == null) {
            response.put("success", false);
            response.put("error", "UNAUTHORIZED");
            return response;
        }

        try {
            notificationService.markAsRead(notiId, driver.getDriverId());
            response.put("success", true);
            response.put("unreadCount", notificationService.getUnreadCount(driver.getDriverId()));
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/mark-all-read")
    public String markAllAsRead(HttpSession session, RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        notificationService.markAllAsRead(driver.getDriverId());
        redirect.addFlashAttribute("success", "Đã đánh dấu tất cả thông báo đã đọc");
        return "redirect:/notifications";
    }

    @PostMapping("/delete-read")
    public String deleteReadNotifications(HttpSession session, RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        notificationService.deleteReadNotifications(driver.getDriverId());
        redirect.addFlashAttribute("success", "Đã xóa tất cả thông báo đã đọc");
        return "redirect:/notifications";
    }

    @GetMapping("/api/unread-count")
    @ResponseBody
    public Map<String, Object> getUnreadCount(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        
        if (driver == null) {
            response.put("count", 0);
            return response;
        }

        long count = notificationService.getUnreadCount(driver.getDriverId());
        response.put("count", count);
        return response;
    }

    @GetMapping("/api/list")
    @ResponseBody
    public Map<String, Object> getNotificationsApi(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        
        if (driver == null) {
            response.put("error", "UNAUTHORIZED");
            return response;
        }

        List<NotificationResponse> notifications = notificationService.getNotificationsByDriver(driver.getDriverId());
        long unreadCount = notificationService.getUnreadCount(driver.getDriverId());

        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);
        return response;
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }
}

