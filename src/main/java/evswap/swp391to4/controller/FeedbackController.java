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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.FeedbackRequest;
import evswap.swp391to4.dto.FeedbackResponse;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.FeedbackService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final StationService stationService;

    /**
     * Hiển thị trang feedback (form + danh sách feedback của driver)
     */
    @GetMapping
    public String feedbackPage(HttpSession session, Model model) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return "redirect:/login";
        }

        // Lấy danh sách trạm cho dropdown
        List<StationResponse> stations = stationService.getAllStations();
        model.addAttribute("stations", stations);

        // Lấy feedback của driver này
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByDriver(driver.getDriverId());
        model.addAttribute("feedbackList", feedbackList);

        // Form object
        model.addAttribute("feedback", new FeedbackRequest());

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
            
            // Load lại dữ liệu cần thiết
            List<StationResponse> stations = stationService.getAllStations();
            model.addAttribute("stations", stations);
            List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByDriver(driver.getDriverId());
            model.addAttribute("feedbackList", feedbackList);
            model.addAttribute("feedback", feedback);
            return "feedback";
        }

        try {
            feedbackService.createFeedback(feedback, driver);
            model.addAttribute("success", "Gửi feedback thành công!");
            model.addAttribute("feedback", new FeedbackRequest());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("feedback", feedback);
        }

        // Load lại dữ liệu
        List<StationResponse> stations = stationService.getAllStations();
        model.addAttribute("stations", stations);
        List<FeedbackResponse> feedbackList = feedbackService.getFeedbackByDriver(driver.getDriverId());
        model.addAttribute("feedbackList", feedbackList);

        return "feedback";
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