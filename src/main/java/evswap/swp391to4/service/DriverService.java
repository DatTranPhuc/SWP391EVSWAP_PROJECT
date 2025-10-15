package evswap.swp391to4.service;

import evswap.swp391to4.dto.RegisterRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Transactional
    public Driver register(RegisterRequest req) {
        driverRepo.findByEmail(req.getEmail())
                .ifPresent(d -> { throw new IllegalStateException("Email đã được đăng ký"); });

        Driver driver = Driver.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .phone(req.getPhone())
                .emailVerified(false)
                .createdAt(Instant.now())
                .build();

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        driver.setEmailOtp(otp);
        driver.setOtpExpiry(Instant.now().plusSeconds(600));

        driverRepo.save(driver);

        // Gửi email
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(driver.getEmail());
            message.setSubject("Mã xác minh email");
            message.setText("Mã OTP của bạn là: " + otp);
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Không gửi được email, in OTP ra console: " + otp);
        }

        System.out.println("OTP đăng ký (test): " + otp);
        return driver;
    }

    @Transactional
    public Driver verifyOtp(String email, String otp) {
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User không tồn tại"));

        if (Boolean.TRUE.equals(driver.getEmailVerified())) {
            throw new IllegalStateException("Email đã được xác minh");
        }

        if (driver.getOtpExpiry() == null || driver.getOtpExpiry().isBefore(Instant.now())) {
            throw new IllegalStateException("OTP hết hạn");
        }

        if (!otp.equals(driver.getEmailOtp())) {
            throw new IllegalArgumentException("OTP không hợp lệ");
        }

        driver.setEmailVerified(true);
        driver.setEmailOtp(null);
        driver.setOtpExpiry(null);
        driverRepo.save(driver);

        return driver;
    }

    @Transactional
    public Driver login(String email, String password) {
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Email không tồn tại"));

        if (!Boolean.TRUE.equals(driver.getEmailVerified())) {
            throw new IllegalStateException("Vui lòng xác minh email trước khi đăng nhập");
        }

        if (!passwordEncoder.matches(password, driver.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu không đúng");
        }

        return driver;
    }

    @Transactional(readOnly = true)
    public Driver getDriverById(Integer driverId) {
        return driverRepo.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Tài khoản tài xế không tồn tại"));
    }
}
