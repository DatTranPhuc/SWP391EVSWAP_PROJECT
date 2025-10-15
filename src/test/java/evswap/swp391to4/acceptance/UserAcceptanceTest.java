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
 * User Acceptance Tests - Test các chức năng từ góc độ người dùng
 * Mô phỏng các hành vi thực tế của người dùng trong hệ thống EV Swap
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class UserAcceptanceTest {

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
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Test Case 1: Người dùng đăng ký tài khoản mới
     * Scenario: Tài xế mới muốn sử dụng dịch vụ đổi pin
     */
    @Test
    void userStory_RegisterNewDriver() throws Exception {
        // Given - Người dùng có thông tin đăng ký
        String registrationRequest = """
                {
                    "email": "newuser@example.com",
                    "passwordHash": "password123",
                    "fullName": "Nguyễn Văn A",
                    "phone": "0123456789"
                }
                """;

        // When - Người dùng gửi yêu cầu đăng ký
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("Nguyễn Văn A"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        // Then - Tài khoản được tạo và OTP được gửi
        Driver savedDriver = driverRepository.findByEmail("newuser@example.com").orElse(null);
        assertThat(savedDriver).isNotNull();
        assertThat(savedDriver.getEmailOtp()).isNotNull();
        assertThat(savedDriver.getOtpExpiry()).isNotNull();
    }

    /**
     * Test Case 2: Người dùng xác thực email
     * Scenario: Tài xế nhận được OTP và muốn xác thực email
     */
    @Test
    void userStory_VerifyEmail() throws Exception {
        // Given - Người dùng đã đăng ký và nhận OTP
        Driver driver = Driver.builder()
                .email("verify@example.com")
                .passwordHash("encoded_password")
                .fullName("Test User")
                .phone("0123456789")
                .emailVerified(false)
                .emailOtp("123456")
                .otpExpiry(Instant.now().plusSeconds(600))
                .createdAt(Instant.now())
                .build();
        driverRepository.save(driver);

        // When - Người dùng nhập OTP để xác thực
        mockMvc.perform(post("/api/auth/verify-email")
                        .param("email", "verify@example.com")
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully"));

        // Then - Email được xác thực thành công
        Driver verifiedDriver = driverRepository.findByEmail("verify@example.com").orElse(null);
        assertThat(verifiedDriver).isNotNull();
        assertThat(verifiedDriver.getEmailVerified()).isTrue();
        assertThat(verifiedDriver.getEmailOtp()).isNull();
    }

    /**
     * Test Case 3: Người dùng đăng nhập
     * Scenario: Tài xế đã xác thực email muốn đăng nhập
     */
    @Test
    void userStory_Login() throws Exception {
        // Given - Người dùng đã đăng ký và xác thực email
        Driver driver = Driver.builder()
                .email("login@example.com")
                .passwordHash("$2a$10$encoded_password_hash")
                .fullName("Login User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        driverRepository.save(driver);

        // When - Người dùng đăng nhập
        String loginRequest = """
                {
                    "email": "login@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.fullName").value("Login User"));
    }

    /**
     * Test Case 4: Người dùng đăng ký xe
     * Scenario: Tài xế muốn thêm xe của mình vào hệ thống
     */
    @Test
    void userStory_RegisterVehicle() throws Exception {
        // Given - Người dùng đã đăng nhập
        Driver driver = Driver.builder()
                .email("vehicle@example.com")
                .passwordHash("encoded_password")
                .fullName("Vehicle Owner")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // When - Người dùng đăng ký xe
        String vehicleRequest = """
                {
                    "driverId": %d,
                    "vin": "VIN123456789",
                    "plateNumber": "30A-12345",
                    "model": "VinFast VF8"
                }
                """.formatted(savedDriver.getDriverId());

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin").value("VIN123456789"))
                .andExpect(jsonPath("$.plateNumber").value("30A-12345"))
                .andExpect(jsonPath("$.model").value("VinFast VF8"));

        // Then - Xe được đăng ký thành công
        Vehicle savedVehicle = vehicleRepository.findByVin("VIN123456789").orElse(null);
        assertThat(savedVehicle).isNotNull();
        assertThat(savedVehicle.getDriver().getDriverId()).isEqualTo(savedDriver.getDriverId());
    }

    /**
     * Test Case 5: Người dùng tìm trạm đổi pin gần nhất
     * Scenario: Tài xế muốn tìm trạm đổi pin gần vị trí hiện tại
     */
    @Test
    void userStory_FindNearbyStations() throws Exception {
        // Given - Có các trạm đổi pin trong hệ thống
        Station station1 = Station.builder()
                .name("Trạm Quận 1")
                .address("123 Nguyễn Huệ, Quận 1, TP.HCM")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        stationRepository.save(station1);

        Station station2 = Station.builder()
                .name("Trạm Quận 2")
                .address("456 Thủ Thiêm, Quận 2, TP.HCM")
                .latitude(new BigDecimal("10.7872"))
                .longitude(new BigDecimal("106.7491"))
                .status("active")
                .build();
        stationRepository.save(station2);

        // When - Người dùng tìm trạm gần vị trí
        mockMvc.perform(get("/api/stations/nearby")
                        .param("latitude", "10.7800")
                        .param("longitude", "106.7200")
                        .param("radius", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].address").exists())
                .andExpect(jsonPath("$[0].status").value("active"));
    }

    /**
     * Test Case 6: Người dùng đặt lịch đổi pin
     * Scenario: Tài xế muốn đặt lịch đổi pin tại trạm cụ thể
     */
    @Test
    void userStory_BookBatterySwap() throws Exception {
        // Given - Người dùng và trạm đã tồn tại
        Driver driver = Driver.builder()
                .email("booking@example.com")
                .passwordHash("encoded_password")
                .fullName("Booking User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Booking Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        // When - Người dùng đặt lịch
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
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driver.driverId").value(savedDriver.getDriverId()))
                .andExpect(jsonPath("$.station.stationId").value(savedStation.getStationId()))
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.qrToken").exists());

        // Then - Lịch đặt được tạo và QR code được sinh
        Reservation savedReservation = reservationRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(savedReservation).isNotNull();
        assertThat(savedReservation.getQrToken()).isNotNull();
        assertThat(savedReservation.getQrExpiresAt()).isNotNull();
    }

    /**
     * Test Case 7: Người dùng thanh toán
     * Scenario: Tài xế muốn thanh toán cho dịch vụ đổi pin
     */
    @Test
    void userStory_MakePayment() throws Exception {
        // Given - Người dùng có lịch đặt và muốn thanh toán
        Driver driver = Driver.builder()
                .email("payment@example.com")
                .passwordHash("encoded_password")
                .fullName("Payment User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Payment Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        Reservation reservation = Reservation.builder()
                .driver(savedDriver)
                .station(savedStation)
                .reservedStart(Instant.now().plus(2, ChronoUnit.HOURS))
                .status("confirmed")
                .createdAt(Instant.now())
                .qrNonce("test_qr_nonce")
                .qrExpiresAt(Instant.now().plus(3, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("test_qr_token")
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        // When - Người dùng thanh toán
        String paymentRequest = """
                {
                    "driverId": %d,
                    "reservationId": %d,
                    "amount": 25000,
                    "method": "ewallet",
                    "currency": "VND"
                }
                """.formatted(savedDriver.getDriverId(), savedReservation.getReservationId());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(25000))
                .andExpect(jsonPath("$.method").value("ewallet"))
                .andExpect(jsonPath("$.status").value("succeed"));

        // Then - Thanh toán được xử lý thành công
        Payment savedPayment = paymentRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(savedPayment).isNotNull();
        assertThat(savedPayment.getAmount()).isEqualTo(new BigDecimal("25000"));
        assertThat(savedPayment.getStatus()).isEqualTo("succeed");
    }

    /**
     * Test Case 8: Người dùng thực hiện đổi pin
     * Scenario: Tài xế đến trạm và thực hiện đổi pin
     */
    @Test
    void userStory_PerformBatterySwap() throws Exception {
        // Given - Người dùng có lịch đặt và đã thanh toán
        Driver driver = Driver.builder()
                .email("swap@example.com")
                .passwordHash("encoded_password")
                .fullName("Swap User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Swap Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

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

        Reservation reservation = Reservation.builder()
                .driver(savedDriver)
                .station(savedStation)
                .reservedStart(Instant.now().minus(1, ChronoUnit.HOURS))
                .status("confirmed")
                .createdAt(Instant.now().minus(2, ChronoUnit.HOURS))
                .qrNonce("test_qr_nonce")
                .qrExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("test_qr_token")
                .checkedInAt(Instant.now().minus(30, ChronoUnit.MINUTES))
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        // When - Người dùng thực hiện đổi pin
        String swapRequest = """
                {
                    "reservationId": %d,
                    "stationId": %d,
                    "batteryOutId": %d,
                    "batteryInId": %d
                }
                """.formatted(savedReservation.getReservationId(), 
                            savedStation.getStationId(),
                            savedBatteryOut.getBatteryId(),
                            savedBatteryIn.getBatteryId());

        mockMvc.perform(post("/api/swap-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(swapRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.swappedAt").exists());

        // Then - Giao dịch đổi pin được ghi nhận
        SwapTransaction savedSwap = swapTransactionRepository.findByReservation(savedReservation).orElse(null);
        assertThat(savedSwap).isNotNull();
        assertThat(savedSwap.getResult()).isEqualTo("success");
        assertThat(savedSwap.getBatteryOut().getBatteryId()).isEqualTo(savedBatteryOut.getBatteryId());
        assertThat(savedSwap.getBatteryIn().getBatteryId()).isEqualTo(savedBatteryIn.getBatteryId());
    }

    /**
     * Test Case 9: Người dùng đánh giá dịch vụ
     * Scenario: Tài xế muốn đánh giá trạm sau khi sử dụng dịch vụ
     */
    @Test
    void userStory_SubmitFeedback() throws Exception {
        // Given - Người dùng đã sử dụng dịch vụ
        Driver driver = Driver.builder()
                .email("feedback@example.com")
                .passwordHash("encoded_password")
                .fullName("Feedback User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Feedback Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        // When - Người dùng gửi đánh giá
        String feedbackRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "rating": 5,
                    "comment": "Dịch vụ rất tốt, nhân viên thân thiện"
                }
                """.formatted(savedDriver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Dịch vụ rất tốt, nhân viên thân thiện"));

        // Then - Đánh giá được lưu thành công
        // Note: Cần implement FeedbackRepository.findByDriverAndStation() method
    }

    /**
     * Test Case 10: Người dùng hủy lịch đặt
     * Scenario: Tài xế muốn hủy lịch đặt vì lý do cá nhân
     */
    @Test
    void userStory_CancelReservation() throws Exception {
        // Given - Người dùng có lịch đặt
        Driver driver = Driver.builder()
                .email("cancel@example.com")
                .passwordHash("encoded_password")
                .fullName("Cancel User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Cancel Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        Reservation reservation = Reservation.builder()
                .driver(savedDriver)
                .station(savedStation)
                .reservedStart(Instant.now().plus(2, ChronoUnit.HOURS))
                .status("confirmed")
                .createdAt(Instant.now())
                .qrNonce("test_qr_nonce")
                .qrExpiresAt(Instant.now().plus(3, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("test_qr_token")
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        // When - Người dùng hủy lịch đặt
        mockMvc.perform(put("/api/reservations/{id}/cancel", savedReservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("canceled"))
                .andExpect(jsonPath("$.qrStatus").value("revoked"));

        // Then - Lịch đặt được hủy thành công
        Reservation canceledReservation = reservationRepository.findById(savedReservation.getReservationId()).orElse(null);
        assertThat(canceledReservation).isNotNull();
        assertThat(canceledReservation.getStatus()).isEqualTo("canceled");
        assertThat(canceledReservation.getQrStatus()).isEqualTo("revoked");
    }

    /**
     * Test Case 11: Người dùng xem lịch sử giao dịch
     * Scenario: Tài xế muốn xem lịch sử các lần đổi pin
     */
    @Test
    void userStory_ViewTransactionHistory() throws Exception {
        // Given - Người dùng có lịch sử giao dịch
        Driver driver = Driver.builder()
                .email("history@example.com")
                .passwordHash("encoded_password")
                .fullName("History User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // When - Người dùng xem lịch sử
        mockMvc.perform(get("/api/drivers/{id}/transactions", savedDriver.getDriverId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Then - Lịch sử được trả về
        // Note: Cần implement endpoint này trong controller
    }

    /**
     * Test Case 12: Người dùng gửi yêu cầu hỗ trợ
     * Scenario: Tài xế gặp vấn đề và cần hỗ trợ
     */
    @Test
    void userStory_SubmitSupportTicket() throws Exception {
        // Given - Người dùng gặp vấn đề
        Driver driver = Driver.builder()
                .email("support@example.com")
                .passwordHash("encoded_password")
                .fullName("Support User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // When - Người dùng gửi yêu cầu hỗ trợ
        String supportRequest = """
                {
                    "driverId": %d,
                    "category": "station",
                    "comment": "Trạm không hoạt động, cần kiểm tra"
                }
                """.formatted(savedDriver.getDriverId());

        mockMvc.perform(post("/api/support-tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(supportRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.category").value("station"))
                .andExpect(jsonPath("$.comment").value("Trạm không hoạt động, cần kiểm tra"))
                .andExpect(jsonPath("$.status").value("open"));

        // Then - Yêu cầu hỗ trợ được tạo
        // Note: Cần implement TicketSupportRepository.findByDriver() method
    }
}
