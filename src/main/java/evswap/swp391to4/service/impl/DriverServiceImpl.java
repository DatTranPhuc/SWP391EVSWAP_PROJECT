package evswap.swp391to4.service.impl;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public Driver registerDriver(String email, String rawPassword, String fullName) {
        if (driverRepo.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại!");
        }
        String encoded = passwordEncoder.encode(rawPassword);
        Driver driver = Driver.builder()
                .email(email)
                .passwordHash(encoded)
                .fullName(fullName)
                .build();
        driverRepo.save(driver);

        sendWelcomeEmail(driver.getEmail(), fullName);

        return driver;
    }

    @Override
    public Optional<Driver> login(String email, String rawPassword) {
        Optional<Driver> driverOpt = driverRepo.findByEmail(email);
        if (driverOpt.isEmpty()) {
            return Optional.empty();
        }
        Driver driver = driverOpt.get();
        if (passwordEncoder.matches(rawPassword, driver.getPasswordHash())) {
            return Optional.of(driver);
        }
        return Optional.empty();
    }

    @Override
    public Driver getDriverById(Integer id) {
        return driverRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài xế!"));
    }

    // ====== VERIFY OTP (Email confirmation or password reset)
    @Override
    public boolean verifyOtp(String email, String otp) {
        Optional<Driver> driverOpt = driverRepo.findByEmail(email);
        if (driverOpt.isEmpty()) return false;
        Driver driver = driverOpt.get();
        if (driver.getEmailOtp() != null
                && driver.getOtpExpiry() != null
                && Instant.now().isBefore(driver.getOtpExpiry())
                && driver.getEmailOtp().equals(otp)) {
            // Xác thực thành công, xóa OTP và expiry
            driver.setEmailOtp(null);
            driver.setOtpExpiry(null);
            driverRepo.save(driver);
            return true;
        }
        return false;
    }

    private void sendWelcomeEmail(String email, String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Chào mừng đến với EV SWAP!");
        message.setText("Hi " + name + ",\nChào mừng bạn đã đăng ký tài khoản!.");
        mailSender.send(message);
    }
}
