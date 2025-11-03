package evswap.swp391to4.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.FeedbackRequest;
import evswap.swp391to4.dto.FeedbackResponse;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.FeedbackService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final evswap.swp391to4.service.ReservationService reservationService;

    /**
     * Hiển thị trang feedback (form + danh sách feedback của driver)
     */
    @GetMapping
    public String feedbackPage(@RequestParam(value = "reservationId", required = false) Integer reservationId,
                              HttpSession session, Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        // Lấy danh sách trạm đủ điều kiện trong 15 ngày gần đây
        List<StationResponse> stations = feedbackService.getEligibleStationsForFeedback(driver.getDriverId());
        model.addAttribute("stations", stations);

        // Lấy feedback của driver này
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByDriver(driver.getDriverId());
        model.addAttribute("feedbackList", feedbackList);

        // Form object
        FeedbackRequest feedbackRequest = new FeedbackRequest();
        
        // Pre-select station if reservationId is provided
        if (reservationId != null) {
            try {
                evswap.swp391to4.entity.Reservation reservation = 
                    reservationService.getReservationById(reservationId);
                
                // Verify reservation belongs to this driver
                if (reservation.getDriver().getDriverId().equals(driver.getDriverId())) {
                    // Pre-select the station from reservation
                    feedbackRequest.setStationId(reservation.getStation().getStationId());
                    model.addAttribute("preselectedStationId", reservation.getStation().getStationId());
                }
            } catch (Exception e) {
                // If reservation not found or error, just ignore and continue
            }
        } else if (!stations.isEmpty()) {
            // If no reservationId provided, auto-select the most recent station (first in list)
            StationResponse mostRecentStation = stations.get(0);
            feedbackRequest.setStationId(mostRecentStation.getStationId());
            model.addAttribute("preselectedStationId", mostRecentStation.getStationId());
        }
        
        model.addAttribute("feedback", feedbackRequest);

        return "feedback";
    }

    /**
     * Xử lý submit feedback mới
     */
    @PostMapping
    public String submitFeedback(@Valid @ModelAttribute("feedback") FeedbackRequest feedback,
                                BindingResult bindingResult,
                                HttpSession session,
                                Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            // Hiển thị lỗi validation
            for (FieldError error : bindingResult.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            
            // Load lại danh sách trạm đủ điều kiện
            List<StationResponse> stations = feedbackService.getEligibleStationsForFeedback(driver.getDriverId());
            model.addAttribute("stations", stations);
            List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByDriver(driver.getDriverId());
            model.addAttribute("feedbackList", feedbackList);
            model.addAttribute("feedback", feedback);
            return "feedback";
        }

        try {
            feedbackService.createFeedback(feedback, driver);
            model.addAttribute("success", "Gửi feedback thành công!");
            
            // Create new empty feedback request
            FeedbackRequest newFeedbackRequest = new FeedbackRequest();
            model.addAttribute("feedback", newFeedbackRequest);
            
            // Re-load stations and auto-select most recent if available
            List<StationResponse> stations = feedbackService.getEligibleStationsForFeedback(driver.getDriverId());
            if (!stations.isEmpty()) {
                StationResponse mostRecentStation = stations.get(0);
                newFeedbackRequest.setStationId(mostRecentStation.getStationId());
                model.addAttribute("preselectedStationId", mostRecentStation.getStationId());
            }
            model.addAttribute("stations", stations);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("feedback", feedback);
            
            // Load lại danh sách trạm đủ điều kiện
            List<StationResponse> stations = feedbackService.getEligibleStationsForFeedback(driver.getDriverId());
            model.addAttribute("stations", stations);
        }
        
        // Always load feedback list
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByDriver(driver.getDriverId());
        model.addAttribute("feedbackList", feedbackList);

        return "feedback";
    }

    /**
     * Cập nhật feedback
     */
    @PostMapping("/update/{id}")
    public String updateFeedback(@PathVariable Integer id,
                                @Valid @ModelAttribute("feedback") FeedbackRequest feedback,
                                BindingResult bindingResult,
                                HttpSession session,
                                RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("error", "Dữ liệu không hợp lệ");
            return "redirect:/feedback";
        }

        try {
            feedbackService.updateFeedback(id, feedback, driver.getDriverId());
            redirect.addFlashAttribute("success", "Cập nhật feedback thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/feedback";
    }

    /**
     * Xóa feedback
     */
    @PostMapping("/delete/{id}")
    public String deleteFeedback(@PathVariable Integer id,
                                HttpSession session,
                                RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        try {
            feedbackService.deleteFeedback(id, driver.getDriverId());
            redirect.addFlashAttribute("success", "Xóa feedback thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/feedback";
    }
}