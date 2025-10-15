package evswap.swp391to4.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import evswap.swp391to4.entity.*;
import evswap.swp391to4.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * User Scenario Tests - Test các tình huống sử dụng thực tế
 * Mô phỏng các scenario phức tạp mà người dùng có thể gặp phải
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserScenarioTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private BatteryRepository batteryRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SwapTransactionRepository swapTransactionRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private TicketSupportRepository ticketSupportRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Scenario 1: Tài xế mới sử dụng dịch vụ lần đầu
     * - Đăng ký tài khoản
     * - Xác thực email
     * - Đăng ký xe
     * - Tìm hiểu về dịch vụ
     * - Đặt lịch thử nghiệm
     */
    @Test
    void scenario_NewDriverFirstTimeUsage() throws Exception {
        System.out.println("🚗 Scenario: New Driver First Time Usage");

        // Step 1: Đăng ký tài khoản
        String registrationRequest = """
                {
                    "email": "newbie@example.com",
                    "passwordHash": "password123",
                    "fullName": "Nguyễn Văn Mới",
                    "phone": "0123456789"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newbie@example.com"));

        Driver driver = driverRepository.findByEmail("newbie@example.com").orElse(null);
        assertThat(driver).isNotNull();

        // Step 2: Xác thực email
        mockMvc.perform(post("/api/auth/verify-email")
                        .param("email", "newbie@example.com")
                        .param("otp", driver.getEmailOtp()))
                .andExpect(status().isOk());

        // Step 3: Đăng ký xe
        String vehicleRequest = """
                {
                    "driverId": %d,
                    "vin": "VIN_NEWBIE_001",
                    "plateNumber": "30A-NEWBIE",
                    "model": "VinFast VF8"
                }
                """.formatted(driver.getDriverId());

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest))
                .andExpect(status().isCreated());

        // Step 4: Tìm trạm gần nhất
        Station station = Station.builder()
                .name("Newbie Station")
                .address("123 Newbie Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        mockMvc.perform(get("/api/stations/nearby")
                        .param("latitude", "10.7800")
                        .param("longitude", "106.7200")
                        .param("radius", "5"))
                .andExpect(status().isOk());

        // Step 6: Đặt lịch thử nghiệm
        String reservationRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T10:00:00Z"
                }
                """.formatted(driver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"));

        System.out.println("✅ New driver successfully completed first-time setup");
    }

    /**
     * Scenario 2: Tài xế thường xuyên sử dụng dịch vụ
     * - Đăng nhập nhanh
     * - Xem lịch sử giao dịch
     * - Đặt lịch thường xuyên
     * - Thanh toán tự động
     */
    @Test
    void scenario_RegularDriverFrequentUsage() throws Exception {
        System.out.println("🔄 Scenario: Regular Driver Frequent Usage");

        // Setup: Tạo tài xế thường xuyên với lịch sử
        Driver regularDriver = Driver.builder()
                .email("regular@example.com")
                .passwordHash("encoded_password")
                .fullName("Tài Xế Thường Xuyên")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .build();
        Driver savedDriver = driverRepository.save(regularDriver);

        // Tạo xe
        Vehicle vehicle = Vehicle.builder()
                .driver(savedDriver)
                .vin("VIN_REGULAR_001")
                .plateNumber("30A-REGULAR")
                .model("VinFast VF8")
                .createdAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .build();
        vehicleRepository.save(vehicle);

        // Tạo trạm
        Station station = Station.builder()
                .name("Regular Station")
                .address("123 Regular Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        // Tạo lịch sử giao dịch (3 lần đổi pin trước đó)
        for (int i = 1; i <= 3; i++) {
            Reservation reservation = Reservation.builder()
                    .driver(savedDriver)
                    .station(savedStation)
                    .reservedStart(Instant.now().minus(i * 7, ChronoUnit.DAYS))
                    .status("completed")
                    .createdAt(Instant.now().minus(i * 7 + 1, ChronoUnit.DAYS))
                    .qrNonce("qr_nonce_" + i)
                    .qrExpiresAt(Instant.now().minus(i * 7, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS))
                    .qrStatus("used")
                    .qrToken("qr_token_" + i)
                    .checkedInAt(Instant.now().minus(i * 7, ChronoUnit.DAYS))
                    .build();
            reservationRepository.save(reservation);

            Payment payment = Payment.builder()
                    .driver(savedDriver)
                    .reservation(reservation)
                    .amount(new BigDecimal("25000"))
                    .method("ewallet")
                    .status("succeed")
                    .paidAt(Instant.now().minus(i * 7, ChronoUnit.DAYS))
                    .currency("VND")
                    .providerTxnId("TXN_REGULAR_" + i)
                    .build();
            paymentRepository.save(payment);
        }

        // Step 1: Đăng nhập nhanh
        String loginRequest = """
                {
                    "email": "regular@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk());

        // Step 2: Xem lịch sử giao dịch
        mockMvc.perform(get("/api/drivers/{id}/reservations", savedDriver.getDriverId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        // Step 3: Đặt lịch mới (lần thứ 4)
        String reservationRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T10:00:00Z"
                }
                """.formatted(savedDriver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest))
                .andExpect(status().isCreated());

        // Step 4: Thanh toán tự động (sử dụng phương thức đã lưu)
        Reservation newReservation = reservationRepository.findByDriver(savedDriver).stream()
                .filter(r -> r.getStatus().equals("pending"))
                .findFirst().orElse(null);
        assertThat(newReservation).isNotNull();

        String paymentRequest = """
                {
                    "driverId": %d,
                    "reservationId": %d,
                    "amount": 25000,
                    "method": "ewallet",
                    "currency": "VND"
                }
                """.formatted(savedDriver.getDriverId(), newReservation.getReservationId());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.method").value("ewallet"));

        System.out.println("✅ Regular driver successfully completed frequent usage scenario");
    }

    /**
     * Scenario 3: Tài xế gặp sự cố và cần hỗ trợ
     * - Đặt lịch thành công
     * - Đến trạm nhưng gặp sự cố
     * - Gửi yêu cầu hỗ trợ
     * - Nhận hỗ trợ từ staff
     * - Hoàn thành giao dịch
     */
    @Test
    void scenario_DriverFacingIssuesAndSupport() throws Exception {
        System.out.println("🆘 Scenario: Driver Facing Issues and Support");

        // Setup: Tạo tài xế, trạm và staff
        Driver driver = Driver.builder()
                .email("support@example.com")
                .passwordHash("encoded_password")
                .fullName("Tài Xế Cần Hỗ Trợ")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Support Station")
                .address("123 Support Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        Staff staff = Staff.builder()
                .email("staff@support.com")
                .fullName("Nhân Viên Hỗ Trợ")
                .station(savedStation)
                .passwordHash("encoded_password")
                .isActive(true)
                .build();
        // Note: StaffRepository would be needed

        // Step 1: Đặt lịch thành công
        String reservationRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T10:00:00Z"
                }
                """.formatted(savedDriver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest))
                .andExpect(status().isCreated());

        Reservation reservation = reservationRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(reservation).isNotNull();

        // Step 2: Thanh toán
        String paymentRequest = """
                {
                    "driverId": %d,
                    "reservationId": %d,
                    "amount": 25000,
                    "method": "ewallet",
                    "currency": "VND"
                }
                """.formatted(savedDriver.getDriverId(), reservation.getReservationId());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isCreated());

        // Step 3: Đến trạm và check-in
        reservation.setStatus("confirmed");
        reservation.setCheckedInAt(Instant.now());
        reservationRepository.save(reservation);

        // Step 4: Gặp sự cố - gửi yêu cầu hỗ trợ
        String supportRequest = """
                {
                    "driverId": %d,
                    "category": "station",
                    "comment": "Máy đổi pin không hoạt động, cần hỗ trợ ngay"
                }
                """.formatted(savedDriver.getDriverId());

        mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supportRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("open"));

        // Step 5: Staff xử lý yêu cầu hỗ trợ
        TicketSupport ticket = ticketSupportRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(ticket).isNotNull();

        // Simulate staff response
        ticket.setStatus("in_progress");
        ticket.setNote("Đang kiểm tra máy đổi pin, sẽ khắc phục trong 10 phút");
        ticketSupportRepository.save(ticket);

        // Step 6: Khắc phục sự cố và hoàn thành giao dịch
        ticket.setStatus("resolved");
        ticket.setResolvedAt(Instant.now());
        ticket.setNote("Đã khắc phục sự cố, máy đổi pin hoạt động bình thường");
        ticketSupportRepository.save(ticket);

        // Tạo pin để hoàn thành giao dịch
        Battery batteryOut = Battery.builder()
                .station(savedStation)
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(95)
                .socPercent(100)
                .build();
        Battery savedBatteryOut = batteryRepository.save(batteryOut);

        Battery batteryIn = Battery.builder()
                .station(savedStation)
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(98)
                .socPercent(100)
                .build();
        Battery savedBatteryIn = batteryRepository.save(batteryIn);

        // Hoàn thành đổi pin
        String swapRequest = """
                {
                    "reservationId": %d,
                    "stationId": %d,
                    "batteryOutId": %d,
                    "batteryInId": %d
                }
                """.formatted(reservation.getReservationId(), 
                            savedStation.getStationId(),
                            savedBatteryOut.getBatteryId(),
                            savedBatteryIn.getBatteryId());

        mockMvc.perform(post("/api/swap-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(swapRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("success"));

        // Step 7: Đánh giá dịch vụ hỗ trợ
        String feedbackRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "rating": 4,
                    "comment": "Gặp sự cố nhưng được hỗ trợ nhanh chóng và hiệu quả"
                }
                """.formatted(savedDriver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4));

        System.out.println("✅ Driver successfully resolved issues with support");
    }

    /**
     * Scenario 4: Tài xế hủy lịch và yêu cầu hoàn tiền
     * - Đặt lịch và thanh toán
     * - Hủy lịch vì lý do cá nhân
     * - Yêu cầu hoàn tiền
     * - Nhận hoàn tiền
     */
    @Test
    void scenario_DriverCancellationAndRefund() throws Exception {
        System.out.println("❌ Scenario: Driver Cancellation and Refund");

        // Setup
        Driver driver = Driver.builder()
                .email("cancel@example.com")
                .passwordHash("encoded_password")
                .fullName("Tài Xế Hủy Lịch")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Cancel Station")
                .address("123 Cancel Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        // Step 1: Đặt lịch và thanh toán
        String reservationRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T10:00:00Z"
                }
                """.formatted(savedDriver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest))
                .andExpect(status().isCreated());

        Reservation reservation = reservationRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(reservation).isNotNull();

        String paymentRequest = """
                {
                    "driverId": %d,
                    "reservationId": %d,
                    "amount": 25000,
                    "method": "ewallet",
                    "currency": "VND"
                }
                """.formatted(savedDriver.getDriverId(), reservation.getReservationId());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isCreated());

        Payment payment = paymentRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo("succeed");

        // Step 2: Hủy lịch
        mockMvc.perform(put("/api/reservations/{id}/cancel", reservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("canceled"));

        // Step 3: Yêu cầu hoàn tiền
        String refundRequest = """
                {
                    "paymentId": %d,
                    "reason": "Hủy lịch vì lý do cá nhân"
                }
                """.formatted(payment.getPaymentId());

        mockMvc.perform(post("/api/payments/{id}/refund", payment.getPaymentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refundRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("refunded"));

        // Verify refund
        Payment refundedPayment = paymentRepository.findById(payment.getPaymentId()).orElse(null);
        assertThat(refundedPayment).isNotNull();
        assertThat(refundedPayment.getStatus()).isEqualTo("refunded");

        System.out.println("✅ Driver successfully cancelled and received refund");
    }

    /**
     * Scenario 5: Tài xế sử dụng dịch vụ trong giờ cao điểm
     * - Tìm trạm có sẵn pin
     * - Đặt lịch trong thời gian bận
     * - Chờ đợi và kiên nhẫn
     * - Hoàn thành giao dịch
     */
    @Test
    void scenario_DriverPeakHourUsage() throws Exception {
        System.out.println("⏰ Scenario: Driver Peak Hour Usage");

        // Setup: Tạo nhiều trạm và pin để mô phỏng giờ cao điểm
        Driver driver = Driver.builder()
                .email("peak@example.com")
                .passwordHash("encoded_password")
                .fullName("Tài Xế Giờ Cao Điểm")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // Tạo nhiều trạm
        for (int i = 1; i <= 3; i++) {
            Station station = Station.builder()
                    .name("Peak Station " + i)
                    .address("123 Peak Street " + i + ", District 1, HCMC")
                    .latitude(new BigDecimal("10.7769").add(new BigDecimal(i * 0.01)))
                    .longitude(new BigDecimal("106.7009").add(new BigDecimal(i * 0.01)))
                    .status("active")
                    .build();
            Station savedStation = stationRepository.save(station);

            // Tạo pin cho mỗi trạm (mô phỏng tình trạng khác nhau)
            String[] states = {"full", "charging", "maintenance"};
            for (int j = 0; j < 3; j++) {
                Battery battery = Battery.builder()
                        .station(savedStation)
                        .model("VinFast VF8 Battery")
                        .state(states[j])
                        .sohPercent(95 - j * 2)
                        .socPercent(j == 0 ? 100 : 75)
                        .build();
                batteryRepository.save(battery);
            }
        }

        // Step 1: Tìm trạm có pin sẵn sàng
        mockMvc.perform(get("/api/stations/nearby")
                        .param("latitude", "10.7800")
                        .param("longitude", "106.7200")
                        .param("radius", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 2: Kiểm tra tình trạng pin tại trạm
        Station selectedStation = stationRepository.findAll().get(0);
        mockMvc.perform(get("/api/stations/{id}/batteries", selectedStation.getStationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 3: Đặt lịch (có thể phải chờ)
        String reservationRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T18:00:00Z"
                }
                """.formatted(savedDriver.getDriverId(), selectedStation.getStationId());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"));

        // Step 4: Thanh toán và chờ xác nhận
        Reservation reservation = reservationRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(reservation).isNotNull();

        String paymentRequest = """
                {
                    "driverId": %d,
                    "reservationId": %d,
                    "amount": 25000,
                    "method": "ewallet",
                    "currency": "VND"
                }
                """.formatted(savedDriver.getDriverId(), reservation.getReservationId());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isCreated());

        // Step 5: Chờ và kiểm tra trạng thái lịch đặt
        // Simulate waiting and checking status
        mockMvc.perform(get("/api/reservations/{id}", reservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        System.out.println("✅ Driver successfully handled peak hour usage scenario");
    }
}
