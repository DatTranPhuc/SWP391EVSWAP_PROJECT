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
 * API Workflow Tests - Test các workflow API chính
 * Kiểm tra các luồng xử lý API từ đầu đến cuối
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class ApiWorkflowTest {

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
     * Workflow 1: Authentication Flow
     * Register -> Verify Email -> Login -> Get Profile
     */
    @Test
    void workflow_AuthenticationFlow() throws Exception {
        System.out.println("🔐 Testing Authentication Workflow");

        // Step 1: Register
        String registrationRequest = """
                {
                    "email": "auth@example.com",
                    "passwordHash": "password123",
                    "fullName": "Auth User",
                    "phone": "0123456789"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("auth@example.com"))
                .andExpect(jsonPath("$.emailVerified").value(false));

        Driver driver = driverRepository.findByEmail("auth@example.com").orElse(null);
        assertThat(driver).isNotNull();
        assertThat(driver.getEmailOtp()).isNotNull();

        // Step 2: Verify Email
        mockMvc.perform(post("/api/auth/verify-email")
                        .param("email", "auth@example.com")
                        .param("otp", driver.getEmailOtp()))
                .andExpect(status().isOk());

        Driver verifiedDriver = driverRepository.findByEmail("auth@example.com").orElse(null);
        assertThat(verifiedDriver.getEmailVerified()).isTrue();

        // Step 3: Login
        String loginRequest = """
                {
                    "email": "auth@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("auth@example.com"))
                .andExpect(jsonPath("$.fullName").value("Auth User"));

        // Step 4: Get Profile
        mockMvc.perform(get("/api/drivers/{id}", verifiedDriver.getDriverId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("auth@example.com"))
                .andExpect(jsonPath("$.fullName").value("Auth User"))
                .andExpect(jsonPath("$.emailVerified").value(true));

        System.out.println("✅ Authentication workflow completed successfully");
    }

    /**
     * Workflow 2: Vehicle Management Flow
     * Register Vehicle -> Update Vehicle -> Get Vehicle List -> Delete Vehicle
     */
    @Test
    void workflow_VehicleManagementFlow() throws Exception {
        System.out.println("🚗 Testing Vehicle Management Workflow");

        // Setup: Create driver
        Driver driver = Driver.builder()
                .email("vehicle@example.com")
                .passwordHash("encoded_password")
                .fullName("Vehicle Manager")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // Step 1: Register Vehicle
        String vehicleRequest = """
                {
                    "driverId": %d,
                    "vin": "VIN_WORKFLOW_001",
                    "plateNumber": "30A-WORKFLOW",
                    "model": "VinFast VF8"
                }
                """.formatted(savedDriver.getDriverId());

        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin").value("VIN_WORKFLOW_001"))
                .andExpect(jsonPath("$.plateNumber").value("30A-WORKFLOW"))
                .andExpect(jsonPath("$.model").value("VinFast VF8"));

        Vehicle vehicle = vehicleRepository.findByVin("VIN_WORKFLOW_001").orElse(null);
        assertThat(vehicle).isNotNull();

        // Step 2: Update Vehicle
        String updateRequest = """
                {
                    "vehicleId": %d,
                    "driverId": %d,
                    "vin": "VIN_WORKFLOW_001",
                    "plateNumber": "30A-UPDATED",
                    "model": "VinFast VF8"
                }
                """.formatted(vehicle.getVehicleId(), savedDriver.getDriverId());

        mockMvc.perform(put("/api/vehicles/{id}", vehicle.getVehicleId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plateNumber").value("30A-UPDATED"));

        // Step 3: Get Vehicle List
        mockMvc.perform(get("/api/drivers/{id}/vehicles", savedDriver.getDriverId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].vin").value("VIN_WORKFLOW_001"));

        // Step 4: Get Single Vehicle
        mockMvc.perform(get("/api/vehicles/{id}", vehicle.getVehicleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.vin").value("VIN_WORKFLOW_001"))
                .andExpect(jsonPath("$.plateNumber").value("30A-UPDATED"));

        // Step 5: Delete Vehicle
        mockMvc.perform(delete("/api/vehicles/{id}", vehicle.getVehicleId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        Vehicle deletedVehicle = vehicleRepository.findByVin("VIN_WORKFLOW_001").orElse(null);
        assertThat(deletedVehicle).isNull();

        System.out.println("✅ Vehicle management workflow completed successfully");
    }

    /**
     * Workflow 3: Station Discovery Flow
     * Get All Stations -> Get Station Details -> Get Nearby Stations -> Get Station Batteries
     */
    @Test
    void workflow_StationDiscoveryFlow() throws Exception {
        System.out.println("📍 Testing Station Discovery Workflow");

        // Setup: Create stations and batteries
        Station station1 = Station.builder()
                .name("Discovery Station 1")
                .address("123 Discovery Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation1 = stationRepository.save(station1);

        Station station2 = Station.builder()
                .name("Discovery Station 2")
                .address("456 Discovery Avenue, District 2, HCMC")
                .latitude(new BigDecimal("10.7872"))
                .longitude(new BigDecimal("106.7491"))
                .status("active")
                .build();
        Station savedStation2 = stationRepository.save(station2);

        // Add batteries to stations
        for (int i = 1; i <= 3; i++) {
            Battery battery = Battery.builder()
                    .station(savedStation1)
                    .model("VinFast VF8 Battery")
                    .state(i == 1 ? "full" : "charging")
                    .sohPercent(95)
                    .socPercent(i == 1 ? 100 : 75)
                    .build();
            batteryRepository.save(battery);
        }

        // Step 1: Get All Stations
        mockMvc.perform(get("/api/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        // Step 2: Get Station Details
        mockMvc.perform(get("/api/stations/{id}", savedStation1.getStationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Discovery Station 1"))
                .andExpect(jsonPath("$.address").value("123 Discovery Street, District 1, HCMC"))
                .andExpect(jsonPath("$.status").value("active"));

        // Step 3: Get Nearby Stations
        mockMvc.perform(get("/api/stations/nearby")
                        .param("latitude", "10.7800")
                        .param("longitude", "106.7200")
                        .param("radius", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].latitude").exists())
                .andExpect(jsonPath("$[0].longitude").exists());

        // Step 4: Get Station Batteries
        mockMvc.perform(get("/api/stations/{id}/batteries", savedStation1.getStationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].model").value("VinFast VF8 Battery"));

        // Step 5: Get Available Batteries
        mockMvc.perform(get("/api/stations/{id}/batteries/available", savedStation1.getStationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].state").value("full"));

        System.out.println("✅ Station discovery workflow completed successfully");
    }

    /**
     * Workflow 4: Reservation Management Flow
     * Create Reservation -> Update Reservation -> Check-in -> Complete -> Cancel
     */
    @Test
    void workflow_ReservationManagementFlow() throws Exception {
        System.out.println("📅 Testing Reservation Management Workflow");

        // Setup
        Driver driver = Driver.builder()
                .email("reservation@example.com")
                .passwordHash("encoded_password")
                .fullName("Reservation Manager")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Station station = Station.builder()
                .name("Reservation Station")
                .address("123 Reservation Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        // Step 1: Create Reservation
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
                .andExpect(jsonPath("$.status").value("pending"))
                .andExpect(jsonPath("$.qrToken").exists())
                .andExpect(jsonPath("$.qrExpiresAt").exists());

        Reservation reservation = reservationRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(reservation).isNotNull();

        // Step 2: Update Reservation (change time)
        String updateRequest = """
                {
                    "reservationId": %d,
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T11:00:00Z"
                }
                """.formatted(reservation.getReservationId(), 
                            savedDriver.getDriverId(), 
                            savedStation.getStationId());

        mockMvc.perform(put("/api/reservations/{id}", reservation.getReservationId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reservedStart").value("2024-12-31T11:00:00Z"));

        // Step 3: Confirm Reservation
        mockMvc.perform(put("/api/reservations/{id}/confirm", reservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("confirmed"));

        // Step 4: Check-in
        mockMvc.perform(put("/api/reservations/{id}/checkin", reservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checkedInAt").exists());

        // Step 5: Get Reservation Status
        mockMvc.perform(get("/api/reservations/{id}", reservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("confirmed"))
                .andExpect(jsonPath("$.checkedInAt").exists());

        // Step 6: Cancel Reservation
        mockMvc.perform(put("/api/reservations/{id}/cancel", reservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("canceled"))
                .andExpect(jsonPath("$.qrStatus").value("revoked"));

        System.out.println("✅ Reservation management workflow completed successfully");
    }

    /**
     * Workflow 5: Payment Processing Flow
     * Create Payment -> Process Payment -> Verify Payment -> Refund Payment
     */
    @Test
    void workflow_PaymentProcessingFlow() throws Exception {
        System.out.println("💳 Testing Payment Processing Workflow");

        // Setup
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
                .address("123 Payment Street, District 1, HCMC")
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

        // Step 1: Create Payment
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
                .andExpect(jsonPath("$.status").value("succeed"))
                .andExpect(jsonPath("$.providerTxnId").exists());

        Payment payment = paymentRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(payment).isNotNull();

        // Step 2: Get Payment Details
        mockMvc.perform(get("/api/payments/{id}", payment.getPaymentId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(25000))
                .andExpect(jsonPath("$.status").value("succeed"))
                .andExpect(jsonPath("$.paidAt").exists());

        // Step 3: Get Payment History
        mockMvc.perform(get("/api/drivers/{id}/payments", savedDriver.getDriverId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].amount").value(25000));

        // Step 4: Refund Payment
        String refundRequest = """
                {
                    "reason": "Customer requested refund"
                }
                """;

        mockMvc.perform(post("/api/payments/{id}/refund", payment.getPaymentId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(refundRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("refunded"));

        // Verify refund
        Payment refundedPayment = paymentRepository.findById(payment.getPaymentId()).orElse(null);
        assertThat(refundedPayment.getStatus()).isEqualTo("refunded");

        System.out.println("✅ Payment processing workflow completed successfully");
    }

    /**
     * Workflow 6: Battery Swap Flow
     * Check Battery Compatibility -> Perform Swap -> Record Transaction -> Update Battery Status
     */
    @Test
    void workflow_BatterySwapFlow() throws Exception {
        System.out.println("🔋 Testing Battery Swap Workflow");

        // Setup
        Driver driver = Driver.builder()
                .email("swap@example.com")
                .passwordHash("encoded_password")
                .fullName("Swap User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        Vehicle vehicle = Vehicle.builder()
                .driver(savedDriver)
                .vin("VIN_SWAP_001")
                .plateNumber("30A-SWAP")
                .model("VinFast VF8")
                .createdAt(Instant.now())
                .build();
        Vehicle savedVehicle = vehicleRepository.save(vehicle);

        Station station = Station.builder()
                .name("Swap Station")
                .address("123 Swap Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

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

        // Create batteries
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

        // Step 1: Check Battery Compatibility
        mockMvc.perform(get("/api/vehicles/{vehicleId}/compatible-batteries", savedVehicle.getVehicleId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // Step 2: Perform Battery Swap
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

        // Step 3: Get Swap Transaction Details
        SwapTransaction swapTransaction = swapTransactionRepository.findByReservation(savedReservation).orElse(null);
        assertThat(swapTransaction).isNotNull();

        mockMvc.perform(get("/api/swap-transactions/{id}", swapTransaction.getSwapId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("success"))
                .andExpect(jsonPath("$.batteryOut.batteryId").value(savedBatteryOut.getBatteryId()))
                .andExpect(jsonPath("$.batteryIn.batteryId").value(savedBatteryIn.getBatteryId()));

        // Step 4: Update Reservation Status
        mockMvc.perform(put("/api/reservations/{id}/complete", savedReservation.getReservationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"));

        // Step 5: Get Swap History
        mockMvc.perform(get("/api/drivers/{id}/swap-history", savedDriver.getDriverId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].result").value("success"));

        System.out.println("✅ Battery swap workflow completed successfully");
    }

    /**
     * Workflow 7: Feedback and Rating Flow
     * Submit Feedback -> Get Feedback -> Update Feedback -> Get Station Rating
     */
    @Test
    void workflow_FeedbackAndRatingFlow() throws Exception {
        System.out.println("⭐ Testing Feedback and Rating Workflow");

        // Setup
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
                .address("123 Feedback Street, District 1, HCMC")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        // Step 1: Submit Feedback
        String feedbackRequest = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "rating": 5,
                    "comment": "Excellent service! Very fast and efficient."
                }
                """.formatted(savedDriver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(feedbackRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent service! Very fast and efficient."));

        Feedback feedback = feedbackRepository.findByDriver(savedDriver).stream()
                .findFirst().orElse(null);
        assertThat(feedback).isNotNull();

        // Step 2: Get Feedback Details
        mockMvc.perform(get("/api/feedback/{id}", feedback.getFeedbackId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.comment").value("Excellent service! Very fast and efficient."));

        // Step 3: Update Feedback
        String updateRequest = """
                {
                    "feedbackId": %d,
                    "driverId": %d,
                    "stationId": %d,
                    "rating": 4,
                    "comment": "Good service, but could be faster."
                }
                """.formatted(feedback.getFeedbackId(), 
                            savedDriver.getDriverId(), 
                            savedStation.getStationId());

        mockMvc.perform(put("/api/feedback/{id}", feedback.getFeedbackId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4))
                .andExpect(jsonPath("$.comment").value("Good service, but could be faster."));

        // Step 4: Get Station Rating Summary
        mockMvc.perform(get("/api/stations/{id}/rating", savedStation.getStationId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").exists())
                .andExpect(jsonPath("$.totalReviews").exists());

        // Step 5: Get Driver's Feedback History
        mockMvc.perform(get("/api/drivers/{id}/feedback", savedDriver.getDriverId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].rating").value(4));

        System.out.println("✅ Feedback and rating workflow completed successfully");
    }
}
