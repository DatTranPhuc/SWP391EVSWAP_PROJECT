package evswap.swp391to4.util;

import evswap.swp391to4.entity.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for building test data objects
 */
public class TestDataBuilder {

    public static Driver.DriverBuilder driverBuilder() {
        return Driver.builder()
                .email("test@example.com")
                .passwordHash("encoded_password")
                .fullName("Test User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now());
    }

    public static Driver.DriverBuilder driverBuilder(String email) {
        return driverBuilder().email(email);
    }

    public static Driver.DriverBuilder driverBuilder(String email, String fullName) {
        return driverBuilder(email).fullName(fullName);
    }

    public static Station.StationBuilder stationBuilder() {
        return Station.builder()
                .name("Test Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active");
    }

    public static Station.StationBuilder stationBuilder(String name) {
        return stationBuilder().name(name);
    }

    public static Vehicle.VehicleBuilder vehicleBuilder() {
        return Vehicle.builder()
                .vin("VIN123456789")
                .plateNumber("30A-12345")
                .model("VinFast VF8")
                .createdAt(Instant.now());
    }

    public static Vehicle.VehicleBuilder vehicleBuilder(String vin) {
        return vehicleBuilder().vin(vin);
    }

    public static Vehicle.VehicleBuilder vehicleBuilder(Driver driver) {
        return vehicleBuilder().driver(driver);
    }

    public static Battery.BatteryBuilder batteryBuilder() {
        return Battery.builder()
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(95)
                .socPercent(100);
    }

    public static Battery.BatteryBuilder batteryBuilder(Station station) {
        return batteryBuilder().station(station);
    }

    public static Battery.BatteryBuilder batteryBuilder(String state) {
        return batteryBuilder().state(state);
    }

    public static Staff.StaffBuilder staffBuilder() {
        return Staff.builder()
                .email("staff@example.com")
                .fullName("Test Staff")
                .passwordHash("encoded_password")
                .isActive(true);
    }

    public static Staff.StaffBuilder staffBuilder(Station station) {
        return staffBuilder().station(station);
    }

    public static Admin.AdminBuilder adminBuilder() {
        return Admin.builder()
                .email("admin@example.com")
                .fullName("Test Admin")
                .passwordHash("encoded_password")
                .createdAt(Instant.now());
    }

    public static Reservation.ReservationBuilder reservationBuilder() {
        return Reservation.builder()
                .reservedStart(Instant.now().plus(2, ChronoUnit.HOURS))
                .status("pending")
                .createdAt(Instant.now())
                .qrNonce("test_qr_nonce")
                .qrExpiresAt(Instant.now().plus(3, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("test_qr_token");
    }

    public static Reservation.ReservationBuilder reservationBuilder(Driver driver, Station station) {
        return reservationBuilder().driver(driver).station(station);
    }

    public static Payment.PaymentBuilder paymentBuilder() {
        return Payment.builder()
                .amount(new BigDecimal("25000"))
                .method("ewallet")
                .status("succeed")
                .paidAt(Instant.now())
                .currency("VND")
                .providerTxnId("TXN_TEST_001");
    }

    public static Payment.PaymentBuilder paymentBuilder(Driver driver) {
        return paymentBuilder().driver(driver);
    }

    public static Payment.PaymentBuilder paymentBuilder(Driver driver, Reservation reservation) {
        return paymentBuilder(driver).reservation(reservation);
    }

    public static SwapTransaction.SwapTransactionBuilder swapTransactionBuilder() {
        return SwapTransaction.builder()
                .swappedAt(Instant.now())
                .result("success");
    }

    public static SwapTransaction.SwapTransactionBuilder swapTransactionBuilder(Reservation reservation, Station station) {
        return swapTransactionBuilder().reservation(reservation).station(station);
    }

    public static Feedback.FeedbackBuilder feedbackBuilder() {
        return Feedback.builder()
                .rating(5)
                .comment("Great service!")
                .createdAt(Instant.now());
    }

    public static Feedback.FeedbackBuilder feedbackBuilder(Driver driver, Station station) {
        return feedbackBuilder().driver(driver).station(station);
    }

    public static Notification.NotificationBuilder notificationBuilder() {
        return Notification.builder()
                .type("reservation")
                .title("Test Notification")
                .isRead(false)
                .sentAt(Instant.now());
    }

    public static Notification.NotificationBuilder notificationBuilder(Driver driver) {
        return notificationBuilder().driver(driver);
    }

    public static TicketSupport.TicketSupportBuilder ticketSupportBuilder() {
        return TicketSupport.builder()
                .category("station")
                .comment("Test support ticket")
                .status("open")
                .createdAt(Instant.now());
    }

    public static TicketSupport.TicketSupportBuilder ticketSupportBuilder(Driver driver) {
        return ticketSupportBuilder().driver(driver);
    }


    // Helper methods for creating complete test objects
    public static Driver createTestDriver() {
        return driverBuilder().build();
    }

    public static Driver createTestDriver(String email) {
        return driverBuilder(email).build();
    }

    public static Station createTestStation() {
        return stationBuilder().build();
    }

    public static Station createTestStation(String name) {
        return stationBuilder(name).build();
    }

    public static Vehicle createTestVehicle(Driver driver) {
        return vehicleBuilder(driver).build();
    }

    public static Battery createTestBattery(Station station) {
        return batteryBuilder(station).build();
    }

    public static Staff createTestStaff(Station station) {
        return staffBuilder(station).build();
    }

    public static Admin createTestAdmin() {
        return adminBuilder().build();
    }

    public static Reservation createTestReservation(Driver driver, Station station) {
        return reservationBuilder(driver, station).build();
    }

    public static Payment createTestPayment(Driver driver, Reservation reservation) {
        return paymentBuilder(driver, reservation).build();
    }

    public static SwapTransaction createTestSwapTransaction(Reservation reservation, Station station) {
        return swapTransactionBuilder(reservation, station).build();
    }

    public static Feedback createTestFeedback(Driver driver, Station station) {
        return feedbackBuilder(driver, station).build();
    }

    public static Notification createTestNotification(Driver driver) {
        return notificationBuilder(driver).build();
    }

    public static TicketSupport createTestTicketSupport(Driver driver) {
        return ticketSupportBuilder(driver).build();
    }

}
