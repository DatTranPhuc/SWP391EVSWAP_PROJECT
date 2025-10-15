package evswap.swp391to4.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Feedback;
import evswap.swp391to4.entity.Notification;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.entity.TicketSupport;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.AdminRepository;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.FeedbackRepository;
import evswap.swp391to4.repository.NotificationRepository;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import evswap.swp391to4.repository.TicketSupportRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final StationRepository stationRepository;
    private final StaffRepository staffRepository;
    private final AdminRepository adminRepository;
    private final BatteryRepository batteryRepository;
    private final FeedbackRepository feedbackRepository;
    private final NotificationRepository notificationRepository;
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final SwapTransactionRepository swapTransactionRepository;
    private final TicketSupportRepository ticketSupportRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Only seed if no data exists
        if (driverRepository.count() == 0) {
            seedData();
        }
    }

    private void seedData() {
        System.out.println("Starting to seed test data...");
        
        // Create multiple drivers for testing
        List<Driver> drivers = createTestDrivers();
        
        // Create multiple stations
        List<Station> stations = createTestStations();
        
        // Create staff for each station
        List<Staff> staffList = createTestStaff(stations);
        
        // Create admin users
        createTestAdmins();
        
        // Create vehicles for drivers
        List<Vehicle> vehicles = createTestVehicles(drivers);
        
        // Create batteries for stations
        List<Battery> batteries = createTestBatteries(stations);
        
        // Create reservations
        List<Reservation> reservations = createTestReservations(drivers, stations);
        
        // Create payments
        List<Payment> payments = createTestPayments(drivers, reservations);
        
        // Create swap transactions
        createTestSwapTransactions(reservations, stations, batteries);
        
        // Create feedback
        createTestFeedback(drivers, stations);
        
        // Create notifications
        createTestNotifications(drivers, reservations, payments);
        
        // Create support tickets
        createTestSupportTickets(drivers, staffList);
        
        System.out.println("Test data seeded successfully!");
        System.out.println("Created: " + drivers.size() + " drivers, " + stations.size() + " stations, " + 
                          vehicles.size() + " vehicles, " + batteries.size() + " batteries, " +
                          reservations.size() + " reservations, " + payments.size() + " payments");
    }
    
    private List<Driver> createTestDrivers() {
        List<Driver> drivers = Arrays.asList(
            Driver.builder()
                .email("demo@example.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Nguyễn Văn Demo")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .build(),
            Driver.builder()
                .email("khangnguyen30092004@gmail.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Nguyễn Khang")
                .phone("0372321672")
                .emailVerified(true)
                .createdAt(Instant.now().minus(25, ChronoUnit.DAYS))
                .build(),
            Driver.builder()
                .email("user1@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Trần Thị Lan")
                .phone("0987654321")
                .emailVerified(true)
                .createdAt(Instant.now().minus(20, ChronoUnit.DAYS))
                .build(),
            Driver.builder()
                .email("user2@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Lê Văn Minh")
                .phone("0912345678")
                .emailVerified(false)
                .createdAt(Instant.now().minus(15, ChronoUnit.DAYS))
                .build(),
            Driver.builder()
                .email("user3@test.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .fullName("Phạm Thị Hoa")
                .phone("0923456789")
                .emailVerified(true)
                .createdAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .build()
        );
        
        return driverRepository.saveAll(drivers);
    }
    
    private List<Station> createTestStations() {
        List<Station> stations = Arrays.asList(
            Station.builder()
                .name("Trạm đổi pin Quận 1")
                .address("123 Nguyễn Huệ, Quận 1, TP.HCM")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build(),
            Station.builder()
                .name("Trạm đổi pin Quận 2")
                .address("456 Thủ Thiêm, Quận 2, TP.HCM")
                .latitude(new BigDecimal("10.7872"))
                .longitude(new BigDecimal("106.7491"))
                .status("active")
                .build(),
            Station.builder()
                .name("Trạm đổi pin Quận 3")
                .address("789 Võ Văn Tần, Quận 3, TP.HCM")
                .latitude(new BigDecimal("10.7829"))
                .longitude(new BigDecimal("106.6904"))
                .status("active")
                .build(),
            Station.builder()
                .name("Trạm đổi pin Quận 7")
                .address("321 Nguyễn Thị Thập, Quận 7, TP.HCM")
                .latitude(new BigDecimal("10.7329"))
                .longitude(new BigDecimal("106.7229"))
                .status("maintenance")
                .build(),
            Station.builder()
                .name("Trạm đổi pin Quận 10")
                .address("654 Lý Thái Tổ, Quận 10, TP.HCM")
                .latitude(new BigDecimal("10.7678"))
                .longitude(new BigDecimal("106.6668"))
                .status("active")
                .build()
        );
        
        return stationRepository.saveAll(stations);
    }
    
    private List<Staff> createTestStaff(List<Station> stations) {
        List<Staff> staffList = Arrays.asList(
            Staff.builder()
                .email("staff1@example.com")
                .fullName("Nguyễn Văn A")
                .station(stations.get(0))
                .passwordHash(passwordEncoder.encode("staff123"))
                .isActive(true)
                .build(),
            Staff.builder()
                .email("staff2@example.com")
                .fullName("Trần Thị B")
                .station(stations.get(1))
                .passwordHash(passwordEncoder.encode("staff123"))
                .isActive(true)
                .build(),
            Staff.builder()
                .email("staff3@example.com")
                .fullName("Lê Văn C")
                .station(stations.get(2))
                .passwordHash(passwordEncoder.encode("staff123"))
                .isActive(true)
                .build(),
            Staff.builder()
                .email("staff4@example.com")
                .fullName("Phạm Thị D")
                .station(stations.get(3))
                .passwordHash(passwordEncoder.encode("staff123"))
                .isActive(false)
                .build(),
            Staff.builder()
                .email("staff5@example.com")
                .fullName("Hoàng Văn E")
                .station(stations.get(4))
                .passwordHash(passwordEncoder.encode("staff123"))
                .isActive(true)
                .build()
        );
        
        return staffRepository.saveAll(staffList);
    }
    
    private void createTestAdmins() {
        List<Admin> admins = Arrays.asList(
            Admin.builder()
                .email("admin@example.com")
                .fullName("Admin User")
                .passwordHash(passwordEncoder.encode("admin123"))
                .createdAt(Instant.now().minus(60, ChronoUnit.DAYS))
                .build(),
            Admin.builder()
                .email("superadmin@example.com")
                .fullName("Super Admin")
                .passwordHash(passwordEncoder.encode("superadmin123"))
                .createdAt(Instant.now().minus(45, ChronoUnit.DAYS))
                .build()
        );
        
        adminRepository.saveAll(admins);
    }
    
    private List<Vehicle> createTestVehicles(List<Driver> drivers) {
        List<Vehicle> vehicles = Arrays.asList(
            Vehicle.builder()
                .driver(drivers.get(0))
                .vin("VIN123456789")
                .plateNumber("30A-12345")
                .model("VinFast VF8")
                .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .build(),
            Vehicle.builder()
                .driver(drivers.get(1))
                .vin("VIN987654321")
                .plateNumber("51A-67890")
                .model("VinFast VF9")
                .createdAt(Instant.now().minus(25, ChronoUnit.DAYS))
                .build(),
            Vehicle.builder()
                .driver(drivers.get(2))
                .vin("VIN456789123")
                .plateNumber("43A-11111")
                .model("VinFast VF6")
                .createdAt(Instant.now().minus(20, ChronoUnit.DAYS))
                .build(),
            Vehicle.builder()
                .driver(drivers.get(3))
                .vin("VIN789123456")
                .plateNumber("29A-22222")
                .model("VinFast VF8")
                .createdAt(Instant.now().minus(15, ChronoUnit.DAYS))
                .build(),
            Vehicle.builder()
                .driver(drivers.get(4))
                .vin("VIN321654987")
                .plateNumber("61A-33333")
                .model("VinFast VF9")
                .createdAt(Instant.now().minus(10, ChronoUnit.DAYS))
                .build()
        );
        
        return vehicleRepository.saveAll(vehicles);
    }
    
    private List<Battery> createTestBatteries(List<Station> stations) {
        List<Battery> batteries = Arrays.asList(
            // Station 1 batteries
            Battery.builder()
                .station(stations.get(0))
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(95)
                .socPercent(100)
                .build(),
            Battery.builder()
                .station(stations.get(0))
                .model("VinFast VF9 Battery")
                .state("charging")
                .sohPercent(90)
                .socPercent(75)
                .build(),
            Battery.builder()
                .station(stations.get(0))
                .model("VinFast VF6 Battery")
                .state("full")
                .sohPercent(98)
                .socPercent(100)
                .build(),
            
            // Station 2 batteries
            Battery.builder()
                .station(stations.get(1))
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(92)
                .socPercent(100)
                .build(),
            Battery.builder()
                .station(stations.get(1))
                .model("VinFast VF9 Battery")
                .state("maintenance")
                .sohPercent(85)
                .socPercent(50)
                .build(),
            
            // Station 3 batteries
            Battery.builder()
                .station(stations.get(2))
                .model("VinFast VF6 Battery")
                .state("full")
                .sohPercent(96)
                .socPercent(100)
                .build(),
            Battery.builder()
                .station(stations.get(2))
                .model("VinFast VF8 Battery")
                .state("charging")
                .sohPercent(88)
                .socPercent(80)
                .build(),
            
            // Station 4 batteries (maintenance station)
            Battery.builder()
                .station(stations.get(3))
                .model("VinFast VF9 Battery")
                .state("retired")
                .sohPercent(70)
                .socPercent(0)
                .build(),
            
            // Station 5 batteries
            Battery.builder()
                .station(stations.get(4))
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(94)
                .socPercent(100)
                .build(),
            Battery.builder()
                .station(stations.get(4))
                .model("VinFast VF6 Battery")
                .state("full")
                .sohPercent(97)
                .socPercent(100)
                .build()
        );
        
        return batteryRepository.saveAll(batteries);
    }
    
    
    private List<Reservation> createTestReservations(List<Driver> drivers, List<Station> stations) {
        List<Reservation> reservations = Arrays.asList(
            Reservation.builder()
                .driver(drivers.get(0))
                .station(stations.get(0))
                .reservedStart(Instant.now().plus(2, ChronoUnit.HOURS))
                .status("confirmed")
                .createdAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .qrNonce("qr_nonce_1")
                .qrExpiresAt(Instant.now().plus(3, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("qr_token_1")
                .build(),
            Reservation.builder()
                .driver(drivers.get(1))
                .station(stations.get(1))
                .reservedStart(Instant.now().plus(4, ChronoUnit.HOURS))
                .status("pending")
                .createdAt(Instant.now().minus(30, ChronoUnit.MINUTES))
                .qrNonce("qr_nonce_2")
                .qrExpiresAt(Instant.now().plus(5, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("qr_token_2")
                .build(),
            Reservation.builder()
                .driver(drivers.get(2))
                .station(stations.get(2))
                .reservedStart(Instant.now().minus(2, ChronoUnit.HOURS))
                .status("completed")
                .createdAt(Instant.now().minus(3, ChronoUnit.HOURS))
                .qrNonce("qr_nonce_3")
                .qrExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .qrStatus("used")
                .qrToken("qr_token_3")
                .checkedInAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .build(),
            Reservation.builder()
                .driver(drivers.get(3))
                .station(stations.get(0))
                .reservedStart(Instant.now().minus(1, ChronoUnit.DAYS))
                .status("canceled")
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS).minus(1, ChronoUnit.HOURS))
                .qrNonce("qr_nonce_4")
                .qrExpiresAt(Instant.now().minus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS))
                .qrStatus("revoked")
                .qrToken("qr_token_4")
                .build(),
            Reservation.builder()
                .driver(drivers.get(4))
                .station(stations.get(4))
                .reservedStart(Instant.now().plus(6, ChronoUnit.HOURS))
                .status("confirmed")
                .createdAt(Instant.now().minus(15, ChronoUnit.MINUTES))
                .qrNonce("qr_nonce_5")
                .qrExpiresAt(Instant.now().plus(7, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("qr_token_5")
                .build()
        );
        
        return reservationRepository.saveAll(reservations);
    }
    
    private List<Payment> createTestPayments(List<Driver> drivers, List<Reservation> reservations) {
        List<Payment> payments = Arrays.asList(
            Payment.builder()
                .driver(drivers.get(0))
                .reservation(reservations.get(0))
                .amount(new BigDecimal("25000"))
                .method("ewallet")
                .status("succeed")
                .paidAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .currency("VND")
                .providerTxnId("TXN_001")
                .build(),
            Payment.builder()
                .driver(drivers.get(1))
                .reservation(reservations.get(1))
                .amount(new BigDecimal("25000"))
                .method("card")
                .status("pending")
                .currency("VND")
                .providerTxnId("TXN_002")
                .build(),
            Payment.builder()
                .driver(drivers.get(2))
                .reservation(reservations.get(2))
                .amount(new BigDecimal("25000"))
                .method("cash")
                .status("succeed")
                .paidAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .currency("VND")
                .providerTxnId("TXN_003")
                .build(),
            Payment.builder()
                .driver(drivers.get(3))
                .reservation(reservations.get(3))
                .amount(new BigDecimal("25000"))
                .method("ewallet")
                .status("refunded")
                .paidAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .currency("VND")
                .providerTxnId("TXN_004")
                .build(),
            Payment.builder()
                .driver(drivers.get(4))
                .reservation(reservations.get(4))
                .amount(new BigDecimal("25000"))
                .method("card")
                .status("succeed")
                .paidAt(Instant.now().minus(15, ChronoUnit.MINUTES))
                .currency("VND")
                .providerTxnId("TXN_005")
                .build()
        );
        
        return paymentRepository.saveAll(payments);
    }
    
    private void createTestSwapTransactions(List<Reservation> reservations, 
                                          List<Station> stations, 
                                          List<Battery> batteries) {
        List<SwapTransaction> swapTransactions = Arrays.asList(
            SwapTransaction.builder()
                .reservation(reservations.get(2)) // completed reservation
                .station(stations.get(2))
                .batteryOut(batteries.get(5)) // VF6 battery from station 3
                .batteryIn(batteries.get(6)) // VF8 battery from station 3
                .swappedAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .result("success")
                .build()
        );
        
        swapTransactionRepository.saveAll(swapTransactions);
    }
    
    private void createTestFeedback(List<Driver> drivers, List<Station> stations) {
        List<Feedback> feedbacks = Arrays.asList(
            Feedback.builder()
                .driver(drivers.get(0))
                .station(stations.get(0))
                .rating(5)
                .comment("Dịch vụ rất tốt, nhân viên thân thiện")
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build(),
            Feedback.builder()
                .driver(drivers.get(1))
                .station(stations.get(1))
                .rating(4)
                .comment("Trạm sạch sẽ, thời gian chờ hợp lý")
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .build(),
            Feedback.builder()
                .driver(drivers.get(2))
                .station(stations.get(2))
                .rating(5)
                .comment("Rất hài lòng với dịch vụ")
                .createdAt(Instant.now().minus(3, ChronoUnit.DAYS))
                .build(),
            Feedback.builder()
                .driver(drivers.get(3))
                .station(stations.get(0))
                .rating(3)
                .comment("Cần cải thiện thời gian chờ")
                .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .build(),
            Feedback.builder()
                .driver(drivers.get(4))
                .station(stations.get(4))
                .rating(4)
                .comment("Dịch vụ ổn, giá cả hợp lý")
                .createdAt(Instant.now().minus(7, ChronoUnit.DAYS))
                .build()
        );
        
        feedbackRepository.saveAll(feedbacks);
    }
    
    private void createTestNotifications(List<Driver> drivers, 
                                       List<Reservation> reservations, 
                                       List<Payment> payments) {
        List<Notification> notifications = Arrays.asList(
            Notification.builder()
                .driver(drivers.get(0))
                .type("reservation")
                .title("Đặt lịch thành công")
                .isRead(false)
                .sentAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .reservation(reservations.get(0))
                .build(),
            Notification.builder()
                .driver(drivers.get(1))
                .type("payment")
                .title("Thanh toán thành công")
                .isRead(true)
                .sentAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .payment(payments.get(1))
                .build(),
            Notification.builder()
                .driver(drivers.get(2))
                .type("swap")
                .title("Đổi pin thành công")
                .isRead(false)
                .sentAt(Instant.now().minus(3, ChronoUnit.HOURS))
                .reservation(reservations.get(2))
                .build(),
            Notification.builder()
                .driver(drivers.get(3))
                .type("reservation")
                .title("Hủy đặt lịch thành công")
                .isRead(true)
                .sentAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .reservation(reservations.get(3))
                .build(),
            Notification.builder()
                .driver(drivers.get(4))
                .type("reminder")
                .title("Nhắc nhở: Lịch đổi pin sắp tới")
                .isRead(false)
                .sentAt(Instant.now().minus(30, ChronoUnit.MINUTES))
                .reservation(reservations.get(4))
                .build()
        );
        
        notificationRepository.saveAll(notifications);
    }
    
    private void createTestSupportTickets(List<Driver> drivers, List<Staff> staffList) {
        List<TicketSupport> tickets = Arrays.asList(
            TicketSupport.builder()
                .driver(drivers.get(0))
                .staff(staffList.get(0))
                .category("station")
                .comment("Trạm không hoạt động, cần kiểm tra")
                .status("resolved")
                .createdAt(Instant.now().minus(3, ChronoUnit.DAYS))
                .resolvedAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .note("Đã khắc phục sự cố, trạm hoạt động bình thường")
                .build(),
            TicketSupport.builder()
                .driver(drivers.get(1))
                .staff(staffList.get(1))
                .category("battery")
                .comment("Pin không sạc được, cần thay thế")
                .status("in_progress")
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .note("Đang xử lý, sẽ thay pin mới")
                .build(),
            TicketSupport.builder()
                .driver(drivers.get(2))
                .staff(null)
                .category("payment")
                .comment("Thanh toán bị lỗi, tiền đã trừ nhưng chưa nhận được dịch vụ")
                .status("open")
                .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .build(),
            TicketSupport.builder()
                .driver(drivers.get(3))
                .staff(staffList.get(2))
                .category("station")
                .comment("Máy đổi pin bị kẹt")
                .status("resolved")
                .createdAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .resolvedAt(Instant.now().minus(4, ChronoUnit.DAYS))
                .note("Đã sửa chữa máy đổi pin")
                .build(),
            TicketSupport.builder()
                .driver(drivers.get(4))
                .staff(null)
                .category("battery")
                .comment("Pin mới có vấn đề về hiệu suất")
                .status("open")
                .createdAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build()
        );
        
        ticketSupportRepository.saveAll(tickets);
    }
}
