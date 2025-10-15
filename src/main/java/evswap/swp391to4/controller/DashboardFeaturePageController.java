package evswap.swp391to4.controller;

import evswap.swp391to4.dto.PaymentResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.service.FeedbackService;
import evswap.swp391to4.service.NotificationService;
import evswap.swp391to4.service.PaymentService;
import evswap.swp391to4.service.ReservationService;
import evswap.swp391to4.service.SwapTransactionService;
import evswap.swp391to4.service.TicketSupportService;
import evswap.swp391to4.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardFeaturePageController {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault());

    private final VehicleService vehicleService;
    private final ReservationService reservationService;
    private final SwapTransactionService swapTransactionService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final TicketSupportService ticketSupportService;
    private final FeedbackService feedbackService;

    @GetMapping("/vehicles")
    public String vehicles(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Driver driver = guardDriver(session, redirectAttributes);
        if (driver == null) {
            return "redirect:/login";
        }

        populateDriver(model, driver, "vehicles");

        List<VehicleSummary> vehicles = vehicleService.getVehiclesForDriver(driver.getDriverId()).stream()
                .sorted(Comparator.comparing(Vehicle::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(vehicle -> new VehicleSummary(
                        vehicle.getVehicleId(),
                        Optional.ofNullable(vehicle.getModel()).filter(s -> !s.isBlank()).orElse("Chưa cập nhật"),
                        Optional.ofNullable(vehicle.getPlateNumber()).filter(s -> !s.isBlank()).orElse("Chưa cập nhật"),
                        Optional.ofNullable(vehicle.getVin()).filter(s -> !s.isBlank()).orElse("Chưa cập nhật"),
                        formatDate(vehicle.getCreatedAt())))
                .collect(Collectors.toList());

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("vehicleCount", vehicles.size());
        return "dashboard-vehicles";
    }

    @GetMapping("/reservations")
    public String reservations(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Driver driver = guardDriver(session, redirectAttributes);
        if (driver == null) {
            return "redirect:/login";
        }

        populateDriver(model, driver, "reservations");

        List<ReservationSummary> reservations = reservationService.getReservationsForDriver(driver.getDriverId())
                .stream()
                .map(reservation -> new ReservationSummary(
                        reservation.getReservationId(),
                        Optional.ofNullable(reservation.getStationName()).orElse("Đang cập nhật"),
                        formatDateTime(reservation.getReservedStart()),
                        prettifyStatus(reservation.getStatus()),
                        prettifyStatus(reservation.getQrStatus()),
                        formatDateTime(reservation.getQrExpiresAt())))
                .collect(Collectors.toList());

        List<SwapSummary> swaps = swapTransactionService.listByDriver(driver.getDriverId()).stream()
                .map(swap -> new SwapSummary(
                        swap.getSwapId(),
                        swap.getReservationId(),
                        Optional.ofNullable(swap.getStationName()).orElse("Đang cập nhật"),
                        formatDateTime(swap.getSwappedAt()),
                        prettifyStatus(swap.getResult())))
                .collect(Collectors.toList());

        model.addAttribute("reservations", reservations);
        model.addAttribute("swaps", swaps);
        return "dashboard-reservations";
    }

    @GetMapping("/payments")
    public String payments(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Driver driver = guardDriver(session, redirectAttributes);
        if (driver == null) {
            return "redirect:/login";
        }

        populateDriver(model, driver, "payments");

        List<PaymentResponse> driverPayments = paymentService.getPaymentsForDriver(driver.getDriverId());

        List<PaymentSummary> payments = driverPayments.stream()
                .map(payment -> new PaymentSummary(
                        payment.getPaymentId(),
                        payment.getReservationId(),
                        formatCurrency(payment.getAmount()),
                        Optional.ofNullable(payment.getMethod()).orElse("Không xác định"),
                        prettifyStatus(payment.getStatus()),
                        formatDateTime(payment.getPaidAt()),
                        Optional.ofNullable(payment.getProviderTxnId()).orElse("-")
                ))
                .collect(Collectors.toList());

        BigDecimal totalAmount = driverPayments.stream()
                .map(PaymentResponse::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("payments", payments);
        model.addAttribute("paymentTotal", formatCurrency(totalAmount));
        model.addAttribute("paymentCount", payments.size());
        return "dashboard-payments";
    }

    @GetMapping("/notifications")
    public String notifications(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Driver driver = guardDriver(session, redirectAttributes);
        if (driver == null) {
            return "redirect:/login";
        }

        populateDriver(model, driver, "notifications");

        List<NotificationSummary> notifications = notificationService.getNotificationsForDriver(driver.getDriverId()).stream()
                .map(notification -> new NotificationSummary(
                        notification.getNotificationId(),
                        Optional.ofNullable(notification.getTitle()).orElse("Thông báo"),
                        Optional.ofNullable(notification.getType()).orElse("Hệ thống"),
                        formatDateTime(notification.getSentAt()),
                        Boolean.TRUE.equals(notification.getRead()),
                        notification.getReservationId(),
                        notification.getPaymentId()))
                .collect(Collectors.toList());

        long unread = notificationService.countUnread(driver.getDriverId());
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unread);
        return "dashboard-notifications";
    }

    @GetMapping("/support")
    public String support(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Driver driver = guardDriver(session, redirectAttributes);
        if (driver == null) {
            return "redirect:/login";
        }

        populateDriver(model, driver, "support");

        List<TicketSummary> tickets = ticketSupportService.getTicketsForDriver(driver.getDriverId()).stream()
                .map(ticket -> new TicketSummary(
                        ticket.getTicketId(),
                        Optional.ofNullable(ticket.getCategory()).orElse("Khác"),
                        prettifyStatus(ticket.getStatus()),
                        formatDateTime(ticket.getCreatedAt()),
                        formatDateTime(ticket.getResolvedAt()),
                        Optional.ofNullable(ticket.getNote()).orElse("Chưa có ghi chú")))
                .collect(Collectors.toList());

        List<FeedbackSummary> feedback = feedbackService.getFeedbackForDriver(driver.getDriverId()).stream()
                .map(item -> new FeedbackSummary(
                        item.getFeedbackId(),
                        Optional.ofNullable(item.getStationName()).orElse("Trạm EV SWAP"),
                        item.getRating() != null ? item.getRating() : 0,
                        Optional.ofNullable(item.getComment()).orElse("Không có nhận xét"),
                        formatDateTime(item.getCreatedAt())))
                .collect(Collectors.toList());

        model.addAttribute("tickets", tickets);
        model.addAttribute("feedback", feedback);
        model.addAttribute("driverId", driver.getDriverId());
        return "dashboard-support";
    }

    private Driver guardDriver(HttpSession session, RedirectAttributes redirectAttributes) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirectAttributes.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để tiếp tục");
        }
        return driver;
    }

    private void populateDriver(Model model, Driver driver, String activePage) {
        model.addAttribute("driverName", Optional.ofNullable(driver.getFullName()).orElse("Tài xế EV"));
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));
        model.addAttribute("activePage", activePage);
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "E";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }

    private String formatDateTime(Instant instant) {
        if (instant == null) {
            return "Chưa cập nhật";
        }
        return DATE_TIME_FORMATTER.format(instant);
    }

    private String formatDate(Instant instant) {
        if (instant == null) {
            return "Chưa cập nhật";
        }
        return DATE_FORMATTER.format(instant);
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) {
            return "0 ₫";
        }
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return formatter.format(amount);
    }

    private String prettifyStatus(String status) {
        if (status == null || status.isBlank()) {
            return "Đang cập nhật";
        }
        String normalized = status.replace('_', ' ').toLowerCase(Locale.forLanguageTag("vi"));
        String[] words = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isBlank()) {
                continue;
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(' ');
        }
        return builder.toString().trim();
    }

    private record VehicleSummary(Integer id, String name, String plate, String vin, String addedOn) {
    }

    private record ReservationSummary(Integer id, String stationName, String startTime,
                                      String status, String qrStatus, String qrExpiresAt) {
    }

    private record SwapSummary(Integer id, Integer reservationId, String stationName,
                               String swappedAt, String result) {
    }

    private record PaymentSummary(Integer id, Integer reservationId, String amount,
                                  String method, String status, String paidAt, String providerTxnId) {
    }

    private record NotificationSummary(Integer id, String title, String type,
                                       String sentAt, boolean read, Integer reservationId, Integer paymentId) {
    }

    private record TicketSummary(Integer id, String category, String status,
                                 String createdAt, String resolvedAt, String note) {
    }

    private record FeedbackSummary(Integer id, String stationName, int rating,
                                   String comment, String createdAt) {
    }
}
