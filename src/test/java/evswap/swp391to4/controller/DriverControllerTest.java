package evswap.swp391to4.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DriverService driverService;

    @Autowired
    private ObjectMapper objectMapper;

    private Driver testDriver;

    @BeforeEach
    void setUp() {
        testDriver = Driver.builder()
                .driverId(1)
                .email("test@example.com")
                .fullName("Test User")
                .phone("0123456789")
                .emailVerified(true)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldGetAllDrivers() throws Exception {
        // Given
        when(driverService.findAll()).thenReturn(Arrays.asList(testDriver));

        // When & Then
        mockMvc.perform(get("/api/drivers"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].fullName").value("Test User"));
    }

    @Test
    void shouldGetDriverById() throws Exception {
        // Given
        when(driverService.findById(1)).thenReturn(Optional.of(testDriver));

        // When & Then
        mockMvc.perform(get("/api/drivers/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"));
    }

    @Test
    void shouldReturn404WhenDriverNotFound() throws Exception {
        // Given
        when(driverService.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/drivers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldCreateDriver() throws Exception {
        // Given
        Driver newDriver = Driver.builder()
                .email("new@example.com")
                .fullName("New User")
                .phone("0987654321")
                .build();

        when(driverService.createDriver(any(Driver.class))).thenReturn(testDriver);

        // When & Then
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newDriver)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldUpdateDriver() throws Exception {
        // Given
        Driver updatedDriver = Driver.builder()
                .driverId(1)
                .email("test@example.com")
                .fullName("Updated User")
                .phone("0123456789")
                .build();

        when(driverService.updateDriver(anyInt(), any(Driver.class))).thenReturn(updatedDriver);

        // When & Then
        mockMvc.perform(put("/api/drivers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDriver)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fullName").value("Updated User"));
    }

    @Test
    void shouldDeleteDriver() throws Exception {
        // Given
        when(driverService.findById(1)).thenReturn(Optional.of(testDriver));

        // When & Then
        mockMvc.perform(delete("/api/drivers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenDeletingNonExistentDriver() throws Exception {
        // Given
        when(driverService.findById(999)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/drivers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetDriverByEmail() throws Exception {
        // Given
        when(driverService.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));

        // When & Then
        mockMvc.perform(get("/api/drivers/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void shouldReturn404WhenDriverEmailNotFound() throws Exception {
        // Given
        when(driverService.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/drivers/email/notfound@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldVerifyEmail() throws Exception {
        // Given
        when(driverService.verifyEmail("test@example.com", "123456")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/drivers/verify-email")
                        .param("email", "test@example.com")
                        .param("otp", "123456"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email verified successfully"));
    }

    @Test
    void shouldFailEmailVerification() throws Exception {
        // Given
        when(driverService.verifyEmail("test@example.com", "654321")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/drivers/verify-email")
                        .param("email", "test@example.com")
                        .param("otp", "654321"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid or expired OTP"));
    }

    @Test
    void shouldValidateDriverData() throws Exception {
        // Given - Invalid driver data (missing email)
        Driver invalidDriver = Driver.builder()
                .fullName("Test User")
                .phone("0123456789")
                .build();

        // When & Then
        mockMvc.perform(post("/api/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDriver)))
                .andExpect(status().isBadRequest());
    }
}
