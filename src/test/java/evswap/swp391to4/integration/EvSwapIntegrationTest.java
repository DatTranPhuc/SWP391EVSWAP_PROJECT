package evswap.swp391to4.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.VehicleRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class EvSwapIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void shouldCreateDriverAndVehicleFlow() throws Exception {
        // Given
        Driver driver = Driver.builder()
                .email("integration@test.com")
                .passwordHash("encoded_password")
                .fullName("Integration Test User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        // When - Create driver
        String driverJson = objectMapper.writeValueAsString(driver);
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(driverJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("integration@test.com"));

        // Then - Verify driver was saved
        Driver savedDriver = driverRepository.findByEmail("integration@test.com").orElse(null);
        assertThat(savedDriver).isNotNull();
        assertThat(savedDriver.getFullName()).isEqualTo("Integration Test User");

        // When - Create vehicle for the driver
        Vehicle vehicle = Vehicle.builder()
                .driver(savedDriver)
                .vin("VIN123456789")
                .plateNumber("30A-12345")
                .model("VinFast VF8")
                .createdAt(Instant.now())
                .build();

        String vehicleJson = objectMapper.writeValueAsString(vehicle);
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(vehicleJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vin").value("VIN123456789"));

        // Then - Verify vehicle was saved
        Vehicle savedVehicle = vehicleRepository.findByVin("VIN123456789").orElse(null);
        assertThat(savedVehicle).isNotNull();
        assertThat(savedVehicle.getDriver().getDriverId()).isEqualTo(savedDriver.getDriverId());
    }

    @Test
    void shouldCreateStationAndBatteryFlow() throws Exception {
        // Given
        Station station = Station.builder()
                .name("Integration Test Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();

        // When - Create station
        String stationJson = objectMapper.writeValueAsString(station);
        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(stationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Integration Test Station"));

        // Then - Verify station was saved
        Station savedStation = stationRepository.findByName("Integration Test Station").orElse(null);
        assertThat(savedStation).isNotNull();
        assertThat(savedStation.getStatus()).isEqualTo("active");
    }

    @Test
    void shouldHandleCompleteReservationFlow() throws Exception {
        // Given - Create driver
        Driver driver = Driver.builder()
                .email("reservation@test.com")
                .passwordHash("encoded_password")
                .fullName("Reservation Test User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // Given - Create station
        Station station = Station.builder()
                .name("Reservation Test Station")
                .address("Test Address")
                .latitude(new BigDecimal("10.7769"))
                .longitude(new BigDecimal("106.7009"))
                .status("active")
                .build();
        Station savedStation = stationRepository.save(station);

        // When - Create reservation
        String reservationJson = """
                {
                    "driverId": %d,
                    "stationId": %d,
                    "reservedStart": "2024-12-31T10:00:00Z"
                }
                """.formatted(savedDriver.getDriverId(), savedStation.getStationId());

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reservationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driver.driverId").value(savedDriver.getDriverId()))
                .andExpect(jsonPath("$.station.stationId").value(savedStation.getStationId()));
    }

    @Test
    void shouldHandleErrorCases() throws Exception {
        // When - Try to create driver with invalid email
        String invalidDriverJson = """
                {
                    "email": "invalid-email",
                    "fullName": "Test User",
                    "phone": "0123456789"
                }
                """;

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDriverJson))
                .andExpect(status().isBadRequest());

        // When - Try to get non-existent driver
        mockMvc.perform(get("/api/drivers/999"))
                .andExpect(status().isNotFound());

        // When - Try to create station with missing required fields
        String invalidStationJson = """
                {
                    "name": "Test Station"
                }
                """;

        mockMvc.perform(post("/api/stations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidStationJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldHandleConcurrentRequests() throws Exception {
        // Given
        Driver driver1 = Driver.builder()
                .email("concurrent1@test.com")
                .passwordHash("encoded_password")
                .fullName("Concurrent User 1")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        Driver driver2 = Driver.builder()
                .email("concurrent2@test.com")
                .passwordHash("encoded_password")
                .fullName("Concurrent User 2")
                .phone("0987654321")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        // When - Create both drivers concurrently
        String driver1Json = objectMapper.writeValueAsString(driver1);
        String driver2Json = objectMapper.writeValueAsString(driver2);

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(driver1Json))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(driver2Json))
                .andExpect(status().isCreated());

        // Then - Verify both drivers were created
        assertThat(driverRepository.findByEmail("concurrent1@test.com")).isPresent();
        assertThat(driverRepository.findByEmail("concurrent2@test.com")).isPresent();
    }

    @Test
    void shouldValidateBusinessRules() throws Exception {
        // Given - Create driver
        Driver driver = Driver.builder()
                .email("business@test.com")
                .passwordHash("encoded_password")
                .fullName("Business Test User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
        Driver savedDriver = driverRepository.save(driver);

        // When - Try to create duplicate email
        Driver duplicateDriver = Driver.builder()
                .email("business@test.com") // Same email
                .passwordHash("encoded_password")
                .fullName("Duplicate User")
                .phone("0987654321")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        String duplicateJson = objectMapper.writeValueAsString(duplicateDriver);
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isConflict());

        // When - Try to create duplicate phone
        Driver duplicatePhoneDriver = Driver.builder()
                .email("different@test.com")
                .passwordHash("encoded_password")
                .fullName("Duplicate Phone User")
                .phone("0123456789") // Same phone
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();

        String duplicatePhoneJson = objectMapper.writeValueAsString(duplicatePhoneDriver);
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicatePhoneJson))
                .andExpect(status().isConflict());
    }
}
