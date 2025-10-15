package evswap.swp391to4.service;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private DriverService driverService;

    private Driver testDriver;

    @BeforeEach
    void setUp() {
        testDriver = Driver.builder()
                .email("test@example.com")
                .fullName("Test User")
                .phone("0123456789")
                .emailVerified(false)
                .createdAt(Instant.now())
                .build();
    }

    @Test
    void shouldRegisterDriverSuccessfully() {
        // Given
        testDriver.setPasswordHash("plain_password");
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain_password")).thenReturn("encoded_password");
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

        // When
        Driver result = driverService.register(testDriver);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getEmailVerified()).isFalse();
        assertThat(result.getEmailOtp()).isNotNull();
        assertThat(result.getOtpExpiry()).isNotNull();
        verify(driverRepository).save(testDriver);
        verify(passwordEncoder).encode("plain_password");
        verify(mailSender).send(any());
    }

    @Test
    void shouldThrowExceptionWhenRegisteringDuplicateEmail() {
        // Given
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));

        // When & Then
        assertThatThrownBy(() -> driverService.register(testDriver))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email đã được đăng ký");
    }

    @Test
    void shouldVerifyOtpSuccessfully() {
        // Given
        testDriver.setEmailOtp("123456");
        testDriver.setOtpExpiry(Instant.now().plusSeconds(300));
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

        // When
        Driver result = driverService.verifyOtp("test@example.com", "123456");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmailVerified()).isTrue();
        assertThat(result.getEmailOtp()).isNull();
        assertThat(result.getOtpExpiry()).isNull();
        verify(driverRepository).save(testDriver);
    }

    @Test
    void shouldThrowExceptionWhenVerifyingNonExistentDriver() {
        // Given
        when(driverRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> driverService.verifyOtp("notfound@example.com", "123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User không tồn tại");
    }

    @Test
    void shouldThrowExceptionWhenVerifyingAlreadyVerifiedEmail() {
        // Given
        testDriver.setEmailVerified(true);
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));

        // When & Then
        assertThatThrownBy(() -> driverService.verifyOtp("test@example.com", "123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email đã được xác minh");
    }

    @Test
    void shouldThrowExceptionWhenVerifyingExpiredOtp() {
        // Given
        testDriver.setEmailOtp("123456");
        testDriver.setOtpExpiry(Instant.now().minusSeconds(300)); // Expired
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));

        // When & Then
        assertThatThrownBy(() -> driverService.verifyOtp("test@example.com", "123456"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OTP hết hạn");
    }

    @Test
    void shouldThrowExceptionWhenVerifyingWrongOtp() {
        // Given
        testDriver.setEmailOtp("123456");
        testDriver.setOtpExpiry(Instant.now().plusSeconds(300));
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));

        // When & Then
        assertThatThrownBy(() -> driverService.verifyOtp("test@example.com", "654321"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("OTP không hợp lệ");
    }

    @Test
    void shouldLoginSuccessfully() {
        // Given
        testDriver.setEmailVerified(true);
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));
        when(passwordEncoder.matches("password123", testDriver.getPasswordHash())).thenReturn(true);

        // When
        Driver result = driverService.login("test@example.com", "password123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(passwordEncoder).matches("password123", testDriver.getPasswordHash());
    }

    @Test
    void shouldThrowExceptionWhenLoginWithNonExistentEmail() {
        // Given
        when(driverRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> driverService.login("notfound@example.com", "password123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email không tồn tại");
    }

    @Test
    void shouldThrowExceptionWhenLoginWithUnverifiedEmail() {
        // Given
        testDriver.setEmailVerified(false);
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));

        // When & Then
        assertThatThrownBy(() -> driverService.login("test@example.com", "password123"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Vui lòng xác minh email trước khi đăng nhập");
    }

    @Test
    void shouldThrowExceptionWhenLoginWithWrongPassword() {
        // Given
        testDriver.setEmailVerified(true);
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));
        when(passwordEncoder.matches("wrongpassword", testDriver.getPasswordHash())).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> driverService.login("test@example.com", "wrongpassword"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Mật khẩu không đúng");
    }

    @Test
    void shouldGetDriverById() {
        // Given
        testDriver.setDriverId(1);
        when(driverRepository.findById(1)).thenReturn(Optional.of(testDriver));

        // When
        Driver result = driverService.getDriverById(1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDriverId()).isEqualTo(1);
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentDriverById() {
        // Given
        when(driverRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> driverService.getDriverById(999))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Tài khoản tài xế không tồn tại");
    }

    @Test
    void shouldGetDriverByEmail() {
        // Given
        when(driverRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testDriver));

        // When
        Driver result = driverService.getDriverByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void shouldThrowExceptionWhenGettingNonExistentDriverByEmail() {
        // Given
        when(driverRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> driverService.getDriverByEmail("notfound@example.com"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Email không tồn tại");
    }

    @Test
    void shouldUpdateDriver() {
        // Given
        testDriver.setDriverId(1);
        testDriver.setFullName("Updated Name");
        when(driverRepository.save(any(Driver.class))).thenReturn(testDriver);

        // When
        Driver result = driverService.updateDriver(testDriver);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo("Updated Name");
        verify(driverRepository).save(testDriver);
    }
}
