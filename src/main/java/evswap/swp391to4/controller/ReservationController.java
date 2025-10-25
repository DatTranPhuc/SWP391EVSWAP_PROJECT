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

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

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
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        List<StationResponse> stations = (query == null || query.isBlank())
                ? stationService.getAllStations()
                : stationService.searchByName(query);
        model.addAttribute("stations", stations);
        model.addAttribute("searchQuery", query);
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("highlightStationId", stationId);
        model.addAttribute("currentStep", "search");
        model.addAttribute("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));

        return "reservation-search";
    }

    @GetMapping("/schedule")
    public String showSchedulePage(@RequestParam(value = "stationId") Integer stationId,
                                   @RequestParam(value = "q", required = false) String query,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        if (stationId == null) {
            redirect.addFlashAttribute("reservationError", "Hãy chọn trạm trước khi đặt lịch");
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
            return "redirect:/reservations/search";
        }

        try {
            StationResponse selected = stationService.findById(stationId);
            model.addAttribute("selectedStation", selected);
        } catch (Exception e) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy trạm đã chọn");
            if (query != null && !query.isBlank()) {
                redirect.addAttribute("q", query);
            }
            return "redirect:/reservations/search";
        }

        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("searchQuery", query);
        model.addAttribute("currentStep", "schedule");

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
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
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
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để tiếp tục quy trình thanh toán");
            return "redirect:/login";
        }

        ReservationService.ReservationDetail detail = reservationService
                .getReservationDetail(reservationId, driver.getDriverId())
                .orElse(null);
        if (detail == null) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy lịch đổi pin");
            return "redirect:/reservations/search";
        }

        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("reservationDetail", detail);
        model.addAttribute("currentStep", "payment");

        return "reservation-payment";
    }

    @GetMapping("/swap")
    public String showSwapPage(@RequestParam("reservationId") Integer reservationId,
                               HttpSession session,
                               Model model,
                               RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để theo dõi quá trình đổi pin");
            return "redirect:/login";
        }

        ReservationService.ReservationDetail detail = reservationService
                .getReservationDetail(reservationId, driver.getDriverId())
                .orElse(null);
        if (detail == null) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy lịch đổi pin");
            return "redirect:/reservations/search";
        }

        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("reservationDetail", detail);
        model.addAttribute("currentStep", "swap");

        return "reservation-swap";
    }

    @GetMapping("/done")
    public String showCompletionPage(@RequestParam("reservationId") Integer reservationId,
                                     HttpSession session,
                                     Model model,
                                     RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem trạng thái hoàn tất");
            return "redirect:/login";
        }

        ReservationService.ReservationDetail detail = reservationService
                .getReservationDetail(reservationId, driver.getDriverId())
                .orElse(null);
        if (detail == null) {
            redirect.addFlashAttribute("reservationError", "Không tìm thấy lịch đổi pin");
            return "redirect:/reservations/search";
        }

        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("reservationDetail", detail);
        model.addAttribute("upcomingReservations", reservationService.getUpcomingReservations(driver.getDriverId()));
        model.addAttribute("currentStep", "done");

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

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }
}
