package evswap.swp391to4.controller;

import evswap.swp391to4.dto.SessionDriver;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.StationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final StationService stationService;

    @GetMapping("/new")
    public String showReservationForm(@RequestParam(value = "stationId", required = false) Integer stationId,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirect) {
        SessionDriver driver = (SessionDriver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        if (!model.containsAttribute("reservationForm")) {
            ReservationForm form = new ReservationForm();
            form.setStationId(stationId);
            LocalDateTime defaultSlot = LocalDateTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0);
            form.setDate(defaultSlot.toLocalDate().toString());
            form.setTime(defaultSlot.toLocalTime().toString());
            model.addAttribute("reservationForm", form);
        }

        model.addAttribute("driverName", driver.fullName());
        model.addAttribute("driverInitial", extractInitial(driver.fullName()));
        model.addAttribute("stations", stationService.getAllStations());
        model.addAttribute("reservations", mapReservations(reservationService.getReservationsForDriver(driver.driverId())));

        return "reservation-schedule";
    }

    @PostMapping
    public String createReservation(@ModelAttribute("reservationForm") ReservationForm form,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    RedirectAttributes redirect) {
        SessionDriver driver = (SessionDriver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để đặt lịch đổi pin");
            return "redirect:/login";
        }

        if (form.getStationId() == null) {
            bindingResult.rejectValue("stationId", "station.required", "Vui lòng chọn trạm đổi pin");
        }

        Instant reservedStart = null;
        if (form.getDate() == null || form.getTime() == null) {
            bindingResult.rejectValue("date", "datetime.required", "Vui lòng chọn ngày và giờ đổi pin");
        } else {
            try {
                LocalDate date = LocalDate.parse(form.getDate(), DateTimeFormatter.ISO_DATE);
                LocalTime time = LocalTime.parse(form.getTime(), DateTimeFormatter.ISO_TIME);
                LocalDateTime dateTime = LocalDateTime.of(date, time);
                reservedStart = dateTime.atZone(ZoneId.systemDefault()).toInstant();
                if (reservedStart.isBefore(Instant.now())) {
                    bindingResult.rejectValue("date", "datetime.past", "Thời gian đặt lịch phải ở tương lai");
                }
            } catch (DateTimeParseException e) {
                bindingResult.rejectValue("date", "datetime.invalid", "Định dạng ngày giờ không hợp lệ");
            }
        }

        if (bindingResult.hasErrors()) {
            redirect.addFlashAttribute("reservationError", "Không thể đặt lịch. Vui lòng kiểm tra lại thông tin");
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.reservationForm", bindingResult);
            redirect.addFlashAttribute("reservationForm", form);
            if (form.getStationId() != null) {
                redirect.addAttribute("stationId", form.getStationId());
            }
            return "redirect:/reservations/new";
        }

        reservationService.createReservation(driver.driverId(), form.getStationId(), reservedStart);
        redirect.addFlashAttribute("reservationSuccess", "Đặt lịch đổi pin thành công!");
        redirect.addAttribute("stationId", form.getStationId());
        return "redirect:/reservations/new";
    }

    private List<ReservationView> mapReservations(List<Reservation> reservations) {
        return reservations.stream()
                .map(reservation -> new ReservationView(
                        reservation.getReservationId(),
                        reservation.getStation().getName(),
                        reservation.getStation().getAddress(),
                        reservation.getReservedStart(),
                        reservation.getStatus()
                ))
                .toList();
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }

    @Data
    public static class ReservationForm {
        @NotNull
        private Integer stationId;

        @NotNull
        private String date;

        @NotNull
        private String time;
    }

    public record ReservationView(Integer reservationId,
                                  String stationName,
                                  String stationAddress,
                                  Instant reservedStart,
                                  String status) {
    }
}
