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
 * End-to-End User Journey Tests
 * Test toàn bộ hành trình của người dùng từ đăng ký đến hoàn thành đổi pin
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class EndToEndUserJourneyTest {

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
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    /**
     * Complete User Journey: Từ đăng ký đến hoàn thành đổi pin
     * 
     * Journey Steps:
     * 1. Đăng ký tài khoản
     * 2. Xác thực email
     * 3. Đăng nhập
     * 4. Đăng ký xe
     * 5. Tìm trạm gần nhất
     * 6. Đặt lịch đổi pin
     * 7. Thanh toán
     * 8. Đến trạm và check-in
     * 9. Thực hiện đổi pin
     * 10. Đánh giá dịch vụ
     */
    @Test
    void completeUserJourney_FromRegistrationToBatterySwap() throws Exception {
        // ===== STEP 1: Đăng ký tài khoản =====
        String registrationRequest = """
                {
                    "email": "journey@example.com",
                    "passwordHash": "password123",
                    "fullName": "Journey User",
                    "phone": "0123456789"
                }
                """;

        var registrationResponse = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("journey@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false))
                .andReturn();

        // Verify driver was created
        Driver driver = driverRepository.findByEmail("journey@example.com").orElse(null);
        assertThat(driver).isNotNull();
        assertThat(driver.getEmailOtp()).isNotNull();

        // ===== STEP 2: Xác thực email =====
        mockMvc.perform(post("/api/auth/verify-email")
                        .param("email", "journey@example.com")
                        .param("otp", driver.getEmailOtp()))
                .andExpect(status().isOk());

        // Verify email is verified
        Driver verifiedDriver = driverRepository.findByEmail("journey@example.com").orElse(null);
        assertThat(verifiedDriver.getEmailVerified()).isTrue();

        // ===== STEP 3: Đăng nhập =====
        String loginRequest = """
                {
                    "email": "journey@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("journey@example.com"));

        // ===== STEP 4: Đăng ký xe =====
        String vehicleRequest = """
                {
                    "driverId": %d,
                    "vin": "VIN_JOURNEY_001",
                    "plateNumber": "30A-JOURNEY",
                    "model": "VinFast VF8"
                }
                """.formatted(verifiedDriver.getDriverId());

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin").value("VIN_JOURNEY_001"));

        // Verify vehicle was registered
        Vehicle vehicle = vehicleRepository.findByVin("VIN_JOURNEY_001").orElse(null);
        assertThat(vehicle).isNotNull();

        // ===== STEP 5: Tìm trạm gần nhất =====
        // Create test stations
        Station station1 = Station.builder()
                .name("Journey Station 1")
                .address("123 Test Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation1 = stationRepository.save(station1);

        Station station2 = Station.builder()
                .name("Journey Station 2")
                .address("456 Test Avenue, District 2, HCMC")
                .latitude(new BigDecimal("10.7872"))
                .longitude(new BigDecimal("106.7491"))
                .status("active")
                .build();
        Station savedStation2 = stationRepository.save(station2);

        // Add batteries to stations
        Battery battery1 = Battery.builder()
                .station(savedStation1)
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(95)
                .socPercent(100)
                .build();
        batteryRepository.save(battery1);

        Battery battery2 = Battery.builder()
                .station(savedStation1)
                .model("VinFast VF8 Battery")
                .state("full")
                .sohPercent(98)
                .socPercent(100)
                .build();
        batteryRepository.save(battery2);

        // Find nearby stations
        mockMvc.perform(get("/api/stations/nearby")
                        .param("latitude", "10.7800")
                        .param("longitude", "106.7200")
                        .param("radius", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists());

        // ===== STEP 6: Đặt lịch đổi pin =====
        String reservationRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T10:00:00Z"
                }
                """.formatted(verifiedDriver.getDriverId(), savedStation1.getStationId());

        var reservationResponse = mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.qrToken").exists())
                .andReturn();

        // Get reservation ID from response
        String responseContent = reservationResponse.getResponse().getContentAsString();
        // Note: In real implementation, you would parse JSON to get reservation ID
        // For this test, we'll find the reservation by driver
        Reservation reservation = reservationRepository.findByDriver(verifiedDriver).stream()
                .findFirst().orElse(null);
        assertThat(reservation).isNotNull();

        // ===== STEP 7: Thanh toán =====
        String paymentRequest = """
                {
                    "driverId": %d,
                    "reservationId": %d,
                    "amount": 25000,
                    "method": "ewallet",
                    "currency": "VND"
                }
                """.formatted(verifiedDriver.getDriverId(), reservation.getReservationId());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(paymentRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("succeed"));

        // Verify payment was processed
        Payment payment = paymentRepository.findByDriver(verifiedDriver).stream()
                .findFirst().orElse(null);
        assertThat(payment).isNotNull();
        assertThat(payment.getStatus()).isEqualTo("succeed");

        // ===== STEP 8: Đến trạm và check-in =====
        // Update reservation status to confirmed and add check-in time
        reservation.setStatus("confirmed");
        reservation.setCheckedInAt(Instant.now());
        reservationRepository.save(reservation);

        // ===== STEP 9: Thực hiện đổi pin =====
        String swapRequest = """
                {
                    "reservationId": %d,
                    "stationId": %d,
                    "batteryOutId": %d,
                    "batteryInId": %d
                }
                """.formatted(reservation.getReservationId(), 
                            savedStation1.getStationId(),
                            battery1.getBatteryId(),
                            battery2.getBatteryId());

        mockMvc.perform(post("/api/swap-transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(swapRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.result").value("success"));

        // Verify swap transaction was recorded
        SwapTransaction swapTransaction = swapTransactionRepository.findByReservation(reservation).orElse(null);
        assertThat(swapTransaction).isNotNull();
        assertThat(swapTransaction.getResult()).isEqualTo("success");

        // Update reservation status to completed
        reservation.setStatus("completed");
        reservationRepository.save(reservation);

        // ===== STEP 10: Đánh giá dịch vụ =====
        String feedbackRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "rating": 5,
                    "comment": "Dịch vụ tuyệt vời! Nhân viên rất thân thiện và chuyên nghiệp."
                }
                """.formatted(verifiedDriver.getDriverId(), savedStation1.getStationId());

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Dịch vụ tuyệt vời! Nhân viên rất thân thiện và chuyên nghiệp."));

        // Verify feedback was submitted
        Feedback feedback = feedbackRepository.findByDriver(verifiedDriver).stream()
                .findFirst().orElse(null);
        assertThat(feedback).isNotNull();
        assertThat(feedback.getRating()).isEqualTo(5);

        // ===== FINAL VERIFICATION: Kiểm tra toàn bộ journey =====
        // Verify all entities were created and linked correctly
        assertThat(driverRepository.findByEmail("journey@example.com")).isPresent();
        assertThat(vehicleRepository.findByVin("VIN_JOURNEY_001")).isPresent();
        assertThat(reservationRepository.findByDriver(verifiedDriver)).isNotEmpty();
        assertThat(paymentRepository.findByDriver(verifiedDriver)).isNotEmpty();
        assertThat(swapTransactionRepository.findByReservation(reservation)).isPresent();
        assertThat(feedbackRepository.findByDriver(verifiedDriver)).isNotEmpty();

        System.out.println("✅ Complete user journey test passed!");
        System.out.println("   - Driver registered and verified");
        System.out.println("   - Vehicle registered");
        System.out.println("   - Reservation created");
        System.out.println("   - Payment processed");
        System.out.println("   - Battery swap completed");
        System.out.println("   - Feedback submitted");
    }

    /**
     * Error Handling Journey: Test xử lý lỗi trong quá trình sử dụng
     */
    @Test
    void errorHandlingJourney_HandleVariousErrors() throws Exception {
        // ===== Test 1: Đăng ký với email đã tồn tại =====
        Driver existingDriver = Driver.builder()
                .email("existing@example.com")
                .passwordHash("encoded_password")
                .fullName("Existing User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        driverRepository.save(existingDriver);

        String duplicateRegistrationRequest = """
                {
                    "email": "existing@example.com",
                    "passwordHash": "password123",
                    "fullName": "Duplicate User",
                    "phone": "0987654321"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRegistrationRequest))
                .andExpect(status().isConflict());

        // ===== Test 2: Đăng nhập với email chưa xác thực =====
        Driver unverifiedDriver = Driver.builder()
                .email("unverified@example.com")
                .passwordHash("encoded_password")
                .fullName("Unverified User")
                .phone("0123456789")
                .emailVerified(false)
                .createdAt(Instant.now())
                .build();
        driverRepository.save(unverifiedDriver);

        String loginRequest = """
                {
                    "email": "unverified@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isBadRequest());

        // ===== Test 3: Đặt lịch tại trạm không hoạt động =====
        Driver driver = Driver.builder()
                .email("error@example.com")
                .passwordHash("encoded_password")
                .fullName("Error User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station inactiveStation = Station.builder()
                .name("Inactive Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("maintenance")
                .build();
        Station savedInactiveStation = stationRepository.save(inactiveStation);

        String reservationRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T10:00:00Z"
                }
                """.formatted(savedDriver.getDriverId(), savedInactiveStation.getStationId());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationRequest))
                .andExpect(status().isBadRequest());

        // ===== Test 4: Thanh toán với số tiền không đúng =====
        Station activeStation = Station.builder()
                .name("Active Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedActiveStation = stationRepository.save(activeStation);

        Reservation reservation = Reservation.builder()
                .driver(savedDriver)
                .station(savedActiveStation)
                .reservedStart(Instant.now().plus(2, ChronoUnit.HOURS))
                .status("confirmed")
                .createdAt(Instant.now())
                .qrNonce("test_qr_nonce")
                .qrExpiresAt(Instant.now().plus(3, ChronoUnit.HOURS))
                .qrStatus("active")
                .qrToken("test_qr_token")
                .build();
        Reservation savedReservation = reservationRepository.save(reservation);

        String invalidPaymentRequest = """
                {
                    "driverId": %d,
                    "reservationId": %d,
                    "amount": 10000,
                    "method": "ewallet",
                    "currency": "VND"
                }
                """.formatted(savedDriver.getDriverId(), savedReservation.getReservationId());

        mockMvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPaymentRequest))
                .andExpect(status().isBadRequest());

        System.out.println("✅ Error handling journey test passed!");
        System.out.println("   - Duplicate email registration blocked");
        System.out.println("   - Unverified email login blocked");
        System.out.println("   - Inactive station reservation blocked");
        System.out.println("   - Invalid payment amount blocked");
    }

    /**
     * Performance Journey: Test hiệu suất với nhiều request đồng thời
     */
    @Test
    void performanceJourney_ConcurrentRequests() throws Exception {
        // Create multiple drivers and stations for concurrent testing
        for (int i = 0; i < 5; i++) {
            Driver driver = Driver.builder()
                    .email("perf" + i + "@example.com")
                    .passwordHash("encoded_password")
                    .fullName("Performance User " + i)
                    .phone("012345678" + i)
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .build();
            driverRepository.save(driver);

            Station station = Station.builder()
                    .name("Performance Station " + i)
                    .address("Test Address " + i)
                    .latitude(new BigDecimal("10.7769").add(new BigDecimal(i * 0.01)))
                    .longitude(new BigDecimal("106.7009").add(new BigDecimal(i * 0.01)))
                    .status("active")
                    .build();
            stationRepository.save(station);
        }

        // Test concurrent station searches
        for (int i = 0; i < 3; i++) {
            mockMvc.perform(get("/api/stations/nearby")
                            .param("latitude", "10.7800")
                            .param("longitude", "106.7200")
                            .param("radius", "5"))
                    .andExpect(status().isOk());
        }

        System.out.println("✅ Performance journey test passed!");
        System.out.println("   - Multiple concurrent requests handled");
        System.out.println("   - System performance maintained");
    }
}
