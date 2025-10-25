package evswap.swp391to4.controller;

import evswap.swp391to4.dto.ReservationScheduleForm;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final StationService stationService;
    private final ReservationService reservationService;

    @GetMapping("/search")
    public String showSearchPage(@RequestParam(value = "stationId", required = false) Integer stationId,
                                 @RequestParam(value = "q", required = false) String query,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirect) {
        Driver driver = requireDriver(session, redirect, "Vui lòng đăng nhập để đặt lịch đổi pin");
        if (driver == null) {
            return "redirect:/login";
        }

        List<StationResponse> stations = (query == null || query.isBlank())
                ? stationService.getAllStations()
                : stationService.searchByName(query);
        populateStepModel(model, driver,
                "Chọn trạm đổi pin",
                "Xin chào " + driver.getFullName() + "! Chọn trạm phù hợp để bắt đầu quy trình đổi pin.",
                "search");
        model.addAttribute("stations", stations);
        model.addAttribute("searchQuery", query);
        model.addAttribute("highlightStationId", stationId);
        model.addAttribute("progressLinks", Collections.emptyMap());
        model.addAttribute("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));
        return "reservation-search";
    }

    @GetMapping("/schedule")
    public String showSchedulePage(@RequestParam(value = "stationId") Integer stationId,
                                   @RequestParam(value = "q", required = false) String query,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirect) {
        Driver driver = requireDriver(session, redirect, "Vui lòng đăng nhập để đặt lịch đổi pin");
        if (driver == null) {
            return "redirect:/login";
        }

        if (stationId == null) {
            redirect.addFlashAttribute("reservationError", "Hãy chọn trạm trước khi đặt lịch");
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
            return "redirect:/reservations/search";
        }

        StationResponse selected;
        try {
            selected = stationService.findById(stationId);
            model.addAttribute("selectedStation", selected);
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy trạm đã chọn");
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
            return "redirect:/reservations/search";
        }

        populateStepModel(model, driver,
                "Đặt lịch đổi pin",
                "Bạn đang đặt lịch tại " + selected.getName() + ". Vui lòng chọn thời gian phù hợp.",
                "schedule");
        model.addAttribute("searchQuery", query);
        model.addAttribute("progressLinks", buildScheduleProgressLinks(stationId, query));
        if (!model.containsAttribute("reservationForm")) {
            ReservationScheduleForm form = new ReservationScheduleForm();
            form.setStationId(stationId);
            model.addAttribute("reservationForm", form);
        }

        return "reservation-schedule";
    }

    @PostMapping("/schedule")
    public String submitReservation(@ModelAttribute("reservationForm") ReservationScheduleForm form,
                                    @RequestParam(value = "q", required = false) String query,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
        Driver driver = requireDriver(session, redirect, "Vui lòng đăng nhập để đặt lịch đổi pin");
        if (driver == null) {
            return "redirect:/login";
        }

        if (form.getStationId() == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn trạm đổi pin");
            redirect.addFlashAttribute("reservationForm", form);
            return redirectToSchedule(query, form.getStationId(), redirect);
        }

        LocalDate date = form.getDate();
        LocalTime time = form.getTime();
        if (date == null || time == null) {
            redirect.addFlashAttribute("reservationError", "Vui lòng chọn ngày và giờ đặt lịch");
            redirect.addFlashAttribute("reservationForm", form);
            return redirectToSchedule(query, form.getStationId(), redirect);
        }

        LocalDateTime localDateTime = LocalDateTime.of(date, time);
        Instant reservedStart = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        if (reservedStart.isBefore(Instant.now())) {
            redirect.addFlashAttribute("reservationError", "Thời gian đặt lịch phải ở tương lai");
            redirect.addFlashAttribute("reservationForm", form);
            return redirectToSchedule(query, form.getStationId(), redirect);
        }

        try {
            Integer reservationId = reservationService
                    .createReservation(driver.getDriverId(), form.getStationId(), reservedStart)
                    .getReservationId();
            redirect.addFlashAttribute("reservationSuccess", "Đặt lịch đổi pin thành công! Hãy chuẩn bị cho bước thanh toán.");
            redirect.addAttribute("reservationId", reservationId);
            return "redirect:/reservations/payment";
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", e.getMessage());
            redirect.addFlashAttribute("reservationForm", form);
            return redirectToSchedule(query, form.getStationId(), redirect);
        }
    }

    @GetMapping("/payment")
    public String showPaymentPage(@RequestParam("reservationId") Integer reservationId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes redirect) {
        Driver driver = requireDriver(session, redirect, "Vui lòng đăng nhập để tiếp tục quy trình thanh toán");
        if (driver == null) {
            return "redirect:/login";
        }

        ReservationService.ReservationDetail detail = reservationService
                .getReservationDetail(reservationId, driver.getDriverId())
                .orElse(null);
        if (detail == null) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy lịch đổi pin");
            return "redirect:/reservations/search";
        }

        populateStepModel(model, driver,
                "Thanh toán đặt lịch",
                "Hãy kiểm tra thông tin lịch hẹn và hoàn tất thanh toán để xác nhận việc đổi pin.",
                "payment");
        model.addAttribute("reservationDetail", detail);
        model.addAttribute("progressLinks", buildPaymentProgressLinks(detail));

        return "reservation-payment";
    }

    @GetMapping("/swap")
    public String showSwapPage(@RequestParam("reservationId") Integer reservationId,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirect) {
        Driver driver = requireDriver(session, redirect, "Vui lòng đăng nhập để theo dõi quá trình đổi pin");
        if (driver == null) {
            return "redirect:/login";
        }

        ReservationService.ReservationDetail detail = reservationService
                .getReservationDetail(reservationId, driver.getDriverId())
                .orElse(null);
        if (detail == null) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy lịch đổi pin");
            return "redirect:/reservations/search";
        }

        populateStepModel(model, driver,
                "Chuẩn bị đổi pin",
                "Đến trạm đúng giờ và đưa mã đặt lịch cho nhân viên để được hỗ trợ nhanh chóng.",
                "swap");
        model.addAttribute("reservationDetail", detail);
        model.addAttribute("progressLinks", buildSwapProgressLinks(detail));

        return "reservation-swap";
    }

    @GetMapping("/done")
    public String showCompletionPage(@RequestParam("reservationId") Integer reservationId,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirect) {
        Driver driver = requireDriver(session, redirect, "Vui lòng đăng nhập để xem trạng thái hoàn tất");
        if (driver == null) {
            return "redirect:/login";
        }

        ReservationService.ReservationDetail detail = reservationService
                .getReservationDetail(reservationId, driver.getDriverId())
                .orElse(null);
        if (detail == null) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy lịch đổi pin");
            return "redirect:/reservations/search";
        }

        populateStepModel(model, driver,
                "Hoàn tất quy trình",
                "Chúc mừng! Quy trình đổi pin đã hoàn tất. Hãy đánh giá trải nghiệm và đặt lịch cho lần tiếp theo.",
                "done");
        model.addAttribute("reservationDetail", detail);
        model.addAttribute("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));
        model.addAttribute("progressLinks", Collections.emptyMap());

        return "reservation-complete";
    }

    private String redirectToSchedule(String query, Integer stationId, RedirectAttributes redirect) {
        if (stationId != null) {
            redirect.addAttribute("stationId", stationId);
        }
        if (query != null && !query.isBlank()) {
            redirect.addAttribute("q", query);
        }
        return "redirect:/reservations/schedule";
    }

    private Driver requireDriver(HttpSession session, RedirectAttributes redirect, String message) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", message);
        }
        return driver;
    }

    private void populateStepModel(Model model, Driver driver, String pageTitle, String pageHint, String currentStep) {
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("pageSubtitle", "Quy trình đổi pin");
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("pageHint", pageHint);
        model.addAttribute("currentStep", currentStep);
    }

    private Map<String, String> buildScheduleProgressLinks(Integer stationId, String query) {
        if (stationId == null) {
            return Collections.emptyMap();
        }
        Map<String, String> links = new LinkedHashMap<>();
        links.put("search", buildSearchUrl(stationId, query));
        return links;
    }

    private Map<String, String> buildPaymentProgressLinks(ReservationService.ReservationDetail detail) {
        if (detail == null) {
            return Collections.emptyMap();
        }
        Map<String, String> links = new LinkedHashMap<>();
        if (detail.stationId() != null) {
            links.put("search", buildSearchUrl(detail.stationId(), null));
            links.put("schedule", buildScheduleUrl(detail.stationId(), null));
        }
        return links;
    }

    private Map<String, String> buildSwapProgressLinks(ReservationService.ReservationDetail detail) {
        if (detail == null) {
            return Collections.emptyMap();
        }
        Map<String, String> links = new LinkedHashMap<>();
        links.put("payment", "/reservations/payment?reservationId=" + detail.reservationId());
        if (detail.stationId() != null) {
            links.put("search", buildSearchUrl(detail.stationId(), null));
            links.put("schedule", buildScheduleUrl(detail.stationId(), null));
        }
        return links;
    }

    private String buildSearchUrl(Integer stationId, String query) {
        StringBuilder builder = new StringBuilder("/reservations/search");
        boolean hasParam = false;
        if (stationId != null) {
            builder.append(hasParam ? '&' : '?').append("stationId=").append(stationId);
            hasParam = true;
        }
        if (query != null && !query.isBlank()) {
            builder.append(hasParam ? '&' : '?').append("q=").append(encode(query));
        }
        return builder.toString();
    }

    private String buildScheduleUrl(Integer stationId, String query) {
        StringBuilder builder = new StringBuilder("/reservations/schedule");
        boolean hasParam = false;
        if (stationId != null) {
            builder.append(hasParam ? '&' : '?').append("stationId=").append(stationId);
            hasParam = true;
        }
        if (query != null && !query.isBlank()) {
            builder.append(hasParam ? '&' : '?').append("q=").append(encode(query));
        }
        return builder.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }
}
