package evswap.swp391to4.service;

import java.time.Instant;
import java.util.Random;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class    DriverService {

    private final DriverRepository driverRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    // Register + gửi OTP
    public void register(Driver driver) {
        if (driverRepo.findByEmail(driver.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã được đăng ký");
        }
        if (driver.getPhone() != null && !driver.getPhone().isBlank() && driverRepo.existsByPhone(driver.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được sử dụng");
        }

        driver.setPasswordHash(passwordEncoder.encode(driver.getPasswordHash()));
        driver.setEmailVerified(false);
        driver.setCreatedAt(Instant.now());

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        driver.setEmailOtp(otp);
        driver.setOtpExpiry(Instant.now().plusSeconds(600));

        driverRepo.save(driver);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(driver.getEmail());
        message.setSubject("Email Verification Code");
        message.setText("Your verification code is: " + otp);
        mailSender.send(message);
    }

    // Verify OTP
    public void verifyOtp(String email, String otp) {
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (driver.getOtpExpiry() == null || driver.getOtpExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("OTP expired");
        }

        if (!otp.equals(driver.getEmailOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        driver.setEmailVerified(true);
        driver.setEmailOtp(null);
        driver.setOtpExpiry(null);
        driverRepo.save(driver);
    }

    // Login
    public void login(String email, String password) {
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!driver.getEmailVerified()) {
            throw new RuntimeException("Please verify your email first!");
        }

        if (!passwordEncoder.matches(password, driver.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
