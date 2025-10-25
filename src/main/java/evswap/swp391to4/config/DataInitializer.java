package evswap.swp391to4.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

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

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "data.initializer.enabled", havingValue = "true", matchIfMissing = false)
public class DataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepo;
    private final DriverRepository driverRepo;
    private final StationRepository stationRepo;
    private final StaffRepository staffRepo;
    private final VehicleRepository vehicleRepo;
    private final BatteryRepository batteryRepo;
    private final ReservationRepository reservationRepo;
    private final PaymentRepository paymentRepo;
    private final SwapTransactionRepository swapTransactionRepo;
    private final FeedbackRepository feedbackRepo;
    private final TicketSupportRepository ticketSupportRepo;
    private final NotificationRepository notificationRepo;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Starting data initialization...");

        // Check if we already have comprehensive data (more than just the basic admin)
        if (driverRepo.count() > 0 || stationRepo.count() > 0) {
            System.out.println("📊 Data already exists, skipping initialization.");
            return;
        }

        // Insert data in proper order respecting FK constraints
        initializeAdmins();
        initializeDrivers();
        initializeStations();
        initializeStaff();
        initializeVehicles();
        initializeBatteries();
        // initializeVehicleBatteryCompatibility(); // Skipped due to access restrictions
        initializeReservations();
        initializePayments();
        initializeSwapTransactions();
        initializeFeedback();
        initializeTicketSupport();
        initializeNotifications();

        System.out.println("✅ Data initialization completed successfully!");
    }

    private void initializeAdmins() {
        System.out.println("👤 Initializing admins...");
        
        List<Admin> admins = new ArrayList<>();
        
        // Check if basic admin already exists (from AdminBootstrap)
        if (adminRepo.findByEmail("admin@gmail.com").isEmpty()) {
            admins.add(Admin.builder()
                .email("admin@gmail.com")
                .passwordHash(passwordEncoder.encode("Admin123"))
                .fullName("System Administrator")
                .createdAt(Instant.now())
                .build());
        }
        
        // Add additional admin
        admins.add(Admin.builder()
            .email("superadmin@evswap.com")
            .passwordHash(passwordEncoder.encode("SuperAdmin123"))
            .fullName("Super Administrator")
            .createdAt(Instant.now())
            .build());
        
        if (!admins.isEmpty()) {
            adminRepo.saveAll(admins);
            System.out.println("✅ Created " + admins.size() + " admin accounts");
        } else {
            System.out.println("✅ Admin accounts already exist");
        }
    }

    private void initializeDrivers() {
        System.out.println("🚗 Initializing drivers...");
        
        List<Driver> drivers = new ArrayList<>();
        String[] names = {"Nguyễn Văn An", "Trần Thị Bình", "Lê Văn Cường", "Phạm Thị Dung", "Hoàng Văn Em",
                         "Vũ Thị Phương", "Đặng Văn Giang", "Bùi Thị Hoa", "Phan Văn Ích", "Ngô Thị Kim",
                         "Dương Văn Long", "Lý Thị Mai", "Võ Văn Nam", "Đinh Thị Oanh", "Tôn Văn Phúc"};
        
        for (int i = 0; i < 30; i++) {
            String name = names[i % names.length] + " " + (i + 1);
            String email = "driver" + (i + 1) + "@gmail.com";
            
            Driver driver = Driver.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode("Driver123"))
                .fullName(name)
                .phone("09" + String.format("%08d", random.nextInt(100000000)))
                .emailVerified(random.nextBoolean())
                .createdAt(Instant.now().minusSeconds(random.nextInt(86400 * 30))) // Random date within last 30 days
                .build();
            
            // Add OTP data for some drivers
            if (random.nextBoolean()) {
                driver.setEmailOtp(String.format("%06d", random.nextInt(1000000)));
                driver.setOtpExpiry(Instant.now().plusSeconds(300)); // 5 minutes from now
            }
            
            drivers.add(driver);
        }
        
        driverRepo.saveAll(drivers);
        System.out.println("✅ Created " + drivers.size() + " driver accounts");
    }

    private void initializeStations() {
        System.out.println("🏢 Initializing stations...");
        
        List<Station> stations = new ArrayList<>();
        String[] stationNames = {
            "Trạm Sạc Quận 1", "Trạm Sạc Quận 2", "Trạm Sạc Quận 3", "Trạm Sạc Quận 4", "Trạm Sạc Quận 5",
            "Trạm Sạc Quận 6", "Trạm Sạc Quận 7", "Trạm Sạc Quận 8", "Trạm Sạc Quận 9", "Trạm Sạc Quận 10",
            "Trạm Sạc Thủ Đức", "Trạm Sạc Bình Thạnh", "Trạm Sạc Tân Bình", "Trạm Sạc Phú Nhuận", "Trạm Sạc Gò Vấp",
            "Trạm Sạc Bình Tân", "Trạm Sạc Hóc Môn", "Trạm Sạc Củ Chi", "Trạm Sạc Nhà Bè", "Trạm Sạc Cần Giờ"
        };
        
        String[] addresses = {
            "123 Nguyễn Huệ, Q1, TP.HCM", "456 Lê Văn Việt, Q2, TP.HCM", "789 Nguyễn Thị Minh Khai, Q3, TP.HCM",
            "321 Võ Văn Tần, Q3, TP.HCM", "654 Cách Mạng Tháng 8, Q10, TP.HCM", "987 Lý Thái Tổ, Q10, TP.HCM",
            "147 Nguyễn Văn Cừ, Q5, TP.HCM", "258 Trần Hưng Đạo, Q5, TP.HCM", "369 Điện Biên Phủ, Q.Bình Thạnh, TP.HCM",
            "741 Xô Viết Nghệ Tĩnh, Q.Bình Thạnh, TP.HCM", "852 Nguyễn Oanh, Q.Gò Vấp, TP.HCM", "963 Quang Trung, Q.Gò Vấp, TP.HCM",
            "159 Lê Đức Thọ, Q.Gò Vấp, TP.HCM", "357 Tân Sơn Nhì, Q.Tân Phú, TP.HCM", "468 Lê Văn Khương, Q.12, TP.HCM",
            "579 Nguyễn Ảnh Thủ, Q.12, TP.HCM", "680 Nguyễn Thị Định, Q.2, TP.HCM", "791 Nguyễn Duy Trinh, Q.2, TP.HCM",
            "802 Nguyễn Thị Thập, Q.7, TP.HCM", "913 Huỳnh Tấn Phát, Q.7, TP.HCM"
        };
        
        for (int i = 0; i < 20; i++) {
            Station station = Station.builder()
                .name(stationNames[i])
                .address(addresses[i])
                .latitude(BigDecimal.valueOf(10.7 + random.nextDouble() * 0.3)) // HCMC latitude range
                .longitude(BigDecimal.valueOf(106.6 + random.nextDouble() * 0.3)) // HCMC longitude range
                .status(random.nextDouble() < 0.9 ? "active" : "closed") // 90% active
                .build();
            
            stations.add(station);
        }
        
        stationRepo.saveAll(stations);
        System.out.println("✅ Created " + stations.size() + " stations");
    }

    private void initializeStaff() {
        System.out.println("👨‍💼 Initializing staff...");
        
        List<Staff> staffList = new ArrayList<>();
        List<Station> stations = stationRepo.findAll();
        String[] staffNames = {
            "Nguyễn Văn A", "Trần Thị B", "Lê Văn C", "Phạm Thị D", "Hoàng Văn E",
            "Vũ Thị F", "Đặng Văn G", "Bùi Thị H", "Phan Văn I", "Ngô Thị J",
            "Dương Văn K", "Lý Thị L", "Võ Văn M", "Đinh Thị N", "Tôn Văn O",
            "Bùi Văn P", "Lê Thị Q", "Phan Văn R", "Nguyễn Thị S", "Trần Văn T",
            "Hoàng Thị U", "Vũ Văn V", "Đặng Thị W", "Bùi Văn X", "Lê Thị Y"
        };
        
        for (int i = 0; i < 25; i++) {
            Staff staff = Staff.builder()
                .station(stations.get(random.nextInt(stations.size())))
                .fullName(staffNames[i])
                .email("staff" + (i + 1) + "@evswap.com")
                .passwordHash(passwordEncoder.encode("Staff123"))
                .isActive(random.nextDouble() < 0.85) // 85% active
                .build();
            
            staffList.add(staff);
        }
        
        staffRepo.saveAll(staffList);
        System.out.println("✅ Created " + staffList.size() + " staff members");
    }

    private void initializeVehicles() {
        System.out.println("🚙 Initializing vehicles...");
        
        List<Vehicle> vehicles = new ArrayList<>();
        List<Driver> drivers = driverRepo.findAll();
        String[] models = {"VinFast VF8", "VinFast VF9", "Tesla Model 3", "Tesla Model Y", "BMW i3", "BMW iX",
                          "Audi e-tron", "Mercedes EQC", "Nissan Leaf", "Hyundai Kona Electric", "Kia EV6", "Ford Mustang Mach-E"};
        
        for (int i = 0; i < 40; i++) {
            Vehicle vehicle = Vehicle.builder()
                .driver(drivers.get(random.nextInt(drivers.size())))
                .vin("VIN" + String.format("%015d", random.nextInt(1000000000)))
                .plateNumber("51" + String.format("%02d", random.nextInt(100)) + "-" + 
                           String.format("%04d", random.nextInt(10000)))
                .model(models[random.nextInt(models.length)])
                .createdAt(Instant.now().minusSeconds(random.nextInt(86400 * 60))) // Random date within last 60 days
                .build();
            
            vehicles.add(vehicle);
        }
        
        vehicleRepo.saveAll(vehicles);
        System.out.println("✅ Created " + vehicles.size() + " vehicles");
    }

    private void initializeBatteries() {
        System.out.println("🔋 Initializing batteries...");
        
        List<Battery> batteries = new ArrayList<>();
        List<Station> stations = stationRepo.findAll();
        String[] models = {"CATL 100kWh", "BYD Blade 120kWh", "LG Chem 90kWh", "Panasonic 85kWh", "Samsung SDI 95kWh"};
        String[] states = {"full", "charging", "maintenance", "retired"};
        
        for (int i = 0; i < 50; i++) {
            Battery battery = Battery.builder()
                .station(stations.get(random.nextInt(stations.size())))
                .model(models[random.nextInt(models.length)])
                .state(states[random.nextInt(states.length)])
                .sohPercent(70 + random.nextInt(31)) // 70-100%
                .socPercent(random.nextInt(101)) // 0-100%
                .build();
            
            batteries.add(battery);
        }
        
        batteryRepo.saveAll(batteries);
        System.out.println("✅ Created " + batteries.size() + " batteries");
    }


    private void initializeReservations() {
        System.out.println("📅 Initializing reservations...");
        
        List<Reservation> reservations = new ArrayList<>();
        List<Driver> drivers = driverRepo.findAll();
        List<Station> stations = stationRepo.findAll();
        String[] statuses = {"pending", "confirmed", "canceled", "no_show", "completed"};
        
        for (int i = 0; i < 30; i++) {
            Instant reservedStart = Instant.now().plusSeconds(random.nextInt(86400 * 7)); // Within next 7 days
            String status = statuses[random.nextInt(statuses.length)];
            
            Reservation reservation = Reservation.builder()
                .driver(drivers.get(random.nextInt(drivers.size())))
                .station(stations.get(random.nextInt(stations.size())))
                .reservedStart(reservedStart)
                .status(status)
                .createdAt(Instant.now().minusSeconds(random.nextInt(86400 * 3))) // Within last 3 days
                .qrNonce("QR" + String.format("%012d", random.nextInt(1000000000)))
                .qrExpiresAt(reservedStart.plusSeconds(3600)) // 1 hour after reservation
                .qrStatus(random.nextBoolean() ? "active" : "expired")
                .qrToken("TOKEN" + String.format("%016d", random.nextInt(1000000000)))
                .build();
            
            if ("completed".equals(status)) {
                reservation.setCheckedInAt(reservedStart.plusSeconds(random.nextInt(3600))); // Checked in within 1 hour
            }
            
            reservations.add(reservation);
        }
        
        reservationRepo.saveAll(reservations);
        System.out.println("✅ Created " + reservations.size() + " reservations");
    }

    private void initializePayments() {
        System.out.println("💳 Initializing payments...");
        
        List<Payment> payments = new ArrayList<>();
        List<Driver> drivers = driverRepo.findAll();
        List<Reservation> reservations = reservationRepo.findAll();
        String[] methods = {"cash", "card", "ewallet"};
        String[] statuses = {"pending", "succeed", "failed", "refunded"};
        
        for (int i = 0; i < 25; i++) {
            Payment payment = Payment.builder()
                .driver(drivers.get(random.nextInt(drivers.size())))
                .reservation(reservations.get(random.nextInt(reservations.size())))
                .amount(BigDecimal.valueOf(50000 + random.nextInt(200000))) // 50k-250k VND
                .method(methods[random.nextInt(methods.length)])
                .status(statuses[random.nextInt(statuses.length)])
                .paidAt(Instant.now().minusSeconds(random.nextInt(86400 * 5))) // Within last 5 days
                .currency("VND")
                .providerTxnId("TXN" + String.format("%012d", random.nextInt(1000000000)))
                .build();
            
            payments.add(payment);
        }
        
        paymentRepo.saveAll(payments);
        System.out.println("✅ Created " + payments.size() + " payments");
    }

    private void initializeSwapTransactions() {
        System.out.println("🔄 Initializing swap transactions...");
        
        List<SwapTransaction> swapTransactions = new ArrayList<>();
        List<Reservation> reservations = reservationRepo.findAll();
        List<Station> stations = stationRepo.findAll();
        List<Battery> batteries = batteryRepo.findAll();
        String[] results = {"success", "failed", "aborted"};
        
        // Ensure we don't create more swap transactions than reservations
        int maxSwapTransactions = Math.min(15, reservations.size());
        
        for (int i = 0; i < maxSwapTransactions; i++) {
            SwapTransaction swapTransaction = SwapTransaction.builder()
                .reservation(reservations.get(i)) // Use unique reservation for each swap transaction
                .station(stations.get(random.nextInt(stations.size())))
                .batteryOut(batteries.get(random.nextInt(batteries.size())))
                .batteryIn(batteries.get(random.nextInt(batteries.size())))
                .swappedAt(Instant.now().minusSeconds(random.nextInt(86400 * 3))) // Within last 3 days
                .result(results[random.nextInt(results.length)])
                .build();
            
            swapTransactions.add(swapTransaction);
        }
        
        swapTransactionRepo.saveAll(swapTransactions);
        System.out.println("✅ Created " + swapTransactions.size() + " swap transactions");
    }

    private void initializeFeedback() {
        System.out.println("⭐ Initializing feedback...");
        
        List<Feedback> feedbackList = new ArrayList<>();
        List<Driver> drivers = driverRepo.findAll();
        List<Station> stations = stationRepo.findAll();
        String[] comments = {
            "Dịch vụ tốt, nhân viên thân thiện", "Trạm sạc sạch sẽ, hiện đại", "Thời gian chờ ngắn",
            "Giá cả hợp lý", "Vị trí thuận tiện", "Cần cải thiện thêm", "Rất hài lòng với dịch vụ",
            "Nhân viên chuyên nghiệp", "Thiết bị hoạt động tốt", "Không gian thoải mái"
        };
        
        for (int i = 0; i < 35; i++) {
            Feedback feedback = Feedback.builder()
                .driver(drivers.get(random.nextInt(drivers.size())))
                .station(stations.get(random.nextInt(stations.size())))
                .rating(1 + random.nextInt(5)) // 1-5 stars
                .comment(comments[random.nextInt(comments.length)])
                .createdAt(Instant.now().minusSeconds(random.nextInt(86400 * 10))) // Within last 10 days
                .build();
            
            feedbackList.add(feedback);
        }
        
        feedbackRepo.saveAll(feedbackList);
        System.out.println("✅ Created " + feedbackList.size() + " feedback entries");
    }

    private void initializeTicketSupport() {
        System.out.println("🎫 Initializing support tickets...");
        
        List<TicketSupport> tickets = new ArrayList<>();
        List<Driver> drivers = driverRepo.findAll();
        List<Staff> staffList = staffRepo.findAll();
        String[] categories = {"station", "battery", "payment"};
        String[] statuses = {"open", "in_progress", "resolved", "closed"};
        String[] comments = {
            "Trạm sạc không hoạt động", "Pin bị lỗi", "Thanh toán thất bại", "Cần hỗ trợ kỹ thuật",
            "Yêu cầu hoàn tiền", "Vấn đề với QR code", "Thiết bị bị hỏng", "Cần tư vấn"
        };
        
        for (int i = 0; i < 20; i++) {
            TicketSupport ticket = TicketSupport.builder()
                .driver(drivers.get(random.nextInt(drivers.size())))
                .staff(staffList.get(random.nextInt(staffList.size())))
                .category(categories[random.nextInt(categories.length)])
                .comment(comments[random.nextInt(comments.length)])
                .status(statuses[random.nextInt(statuses.length)])
                .createdAt(Instant.now().minusSeconds(random.nextInt(86400 * 7))) // Within last 7 days
                .note("Ticket #" + (i + 1) + " - " + (random.nextBoolean() ? "Resolved" : "In progress"))
                .commentHistory("Initial comment: " + comments[random.nextInt(comments.length)])
                .attachments("attachment_" + (i + 1) + ".pdf")
                .build();
            
            if ("resolved".equals(ticket.getStatus()) || "closed".equals(ticket.getStatus())) {
                ticket.setResolvedAt(Instant.now().minusSeconds(random.nextInt(86400 * 3)));
            }
            
            tickets.add(ticket);
        }
        
        ticketSupportRepo.saveAll(tickets);
        System.out.println("✅ Created " + tickets.size() + " support tickets");
    }

    private void initializeNotifications() {
        System.out.println("🔔 Initializing notifications...");
        
        List<Notification> notifications = new ArrayList<>();
        List<Driver> drivers = driverRepo.findAll();
        List<Reservation> reservations = reservationRepo.findAll();
        List<Payment> payments = paymentRepo.findAll();
        String[] types = {"reservation", "payment", "system", "promotion"};
        String[] titles = {
            "Đặt lịch thành công", "Thanh toán hoàn tất", "Thông báo hệ thống", "Ưu đãi đặc biệt",
            "Nhắc nhở đặt lịch", "Cập nhật trạng thái", "Khuyến mãi mới", "Bảo trì hệ thống"
        };
        
        for (int i = 0; i < 40; i++) {
            Notification notification = Notification.builder()
                .driver(drivers.get(random.nextInt(drivers.size())))
                .type(types[random.nextInt(types.length)])
                .title(titles[random.nextInt(titles.length)])
                .isRead(random.nextBoolean())
                .sentAt(Instant.now().minusSeconds(random.nextInt(86400 * 5))) // Within last 5 days
                .build();
            
            // Randomly assign reservation or payment
            if (random.nextBoolean() && !reservations.isEmpty()) {
                notification.setReservation(reservations.get(random.nextInt(reservations.size())));
            }
            if (random.nextBoolean() && !payments.isEmpty()) {
                notification.setPayment(payments.get(random.nextInt(payments.size())));
            }
            
            notifications.add(notification);
        }
        
        notificationRepo.saveAll(notifications);
        System.out.println("✅ Created " + notifications.size() + " notifications");
    }
}
