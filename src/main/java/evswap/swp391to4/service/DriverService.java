package evswap.swp391to4.service;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepo;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Transactional
    public Driver register(Driver driver) {
        // 1. Check if email already exists
        if (driverRepo.findByEmail(driver.getEmail()).isPresent()) {
            throw new IllegalStateException("Email đã được đăng ký");
        }

        // 2. Encode password
        driver.setPasswordHash(passwordEncoder.encode(driver.getPasswordHash()));

        // 3. Set initial status
        driver.setEmailVerified(false);
        driver.setCreatedAt(Instant.now());

        // 4. Generate OTP and expiry time (10 minutes)
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        driver.setEmailOtp(otp);
        driver.setOtpExpiry(Instant.now().plus(10, ChronoUnit.MINUTES));

        // 5. Save driver information (including OTP)
        Driver savedDriver = driverRepo.save(driver);

        // 6. Send HTML Verification Email
        sendVerificationEmail(savedDriver, otp); // Call helper method to send email

        // 7. Return saved driver
        return savedDriver;
    }

    /**
     * Helper method to construct and send the HTML verification email.
     * Uses System.out/err for output.
     */
    private void sendVerificationEmail(Driver driver, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "utf-8"); // Enable HTML, set encoding

            helper.setTo(driver.getEmail());
            helper.setSubject("EV SWAP - Xác thực địa chỉ email của bạn");

            // --- Construct HTML content with Inline CSS (Keep as is) ---
            String htmlContent = String.format("""
                <div style="font-family: 'Montserrat', Arial, sans-serif; max-width: 600px; margin: 20px auto; padding: 25px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #ffffff; box-shadow: 0 4px 15px rgba(0,0,0,0.07);">
                    <div style="text-align: center; margin-bottom: 25px;">
                        <span style="font-size: 28px; margin-right: 8px;">🚗</span>
                        <span style="font-size: 24px; font-weight: 700; color: #333;">EV SWAP</span>
                    </div>
                    <h2 style="color: #007bff; text-align: center; font-weight: 600; margin-bottom: 15px;">Chào mừng %s!</h2>
                    <p style="font-size: 16px; color: #555; line-height: 1.6;">Để hoàn tất đăng ký tài khoản EV SWAP và bắt đầu trải nghiệm dịch vụ đổi pin thông minh, vui lòng sử dụng mã xác thực (OTP) dưới đây:</p>
                    <div style="background-color: #f0f7ff; padding: 20px; border-radius: 8px; text-align: center; margin: 25px 0;">
                        <p style="margin: 0; font-size: 14px; color: #555;">Mã OTP của bạn là:</p>
                        <strong style="font-size: 32px; color: #007bff; letter-spacing: 3px; display: block; margin-top: 10px;">%s</strong>
                    </div>
                    <p style="font-size: 14px; color: #777; text-align: center;">Mã này sẽ hết hạn sau <strong style="color: #333;">10 phút</strong>.</p>
                    <p style="font-size: 14px; color: #777; margin-top: 20px;">Nếu bạn không thực hiện đăng ký này, vui lòng bỏ qua email. Vì lý do bảo mật, tuyệt đối không chia sẻ mã OTP này với bất kỳ ai.</p>
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 25px 0;">
                    <p style="font-size: 13px; color: #999; text-align: center;">Trân trọng,<br>Đội ngũ hỗ trợ EV SWAP</p>
                </div>
                """,
                    driver.getFullName(), // Insert driver's name
                    otp                 // Insert the OTP
            );
            // --- End of HTML content ---

            helper.setText(htmlContent, true); // true indicates HTML content

            mailSender.send(mimeMessage);
            // Replace log.info with System.out.println
            System.out.println("Đã gửi email xác thực HTML thành công tới " + driver.getEmail());

        } catch (MessagingException e) {
            // Replace log.error with System.err.println
            System.err.println("Lỗi nghiêm trọng khi gửi email xác thực HTML cho " + driver.getEmail() + ": " + e.getMessage());
            // Optionally print stack trace for detailed debugging
            // e.printStackTrace();
        } catch (Exception e) {
            // Replace log.error with System.err.println
            System.err.println("Lỗi không xác định khi gửi email xác thực cho " + driver.getEmail() + ": " + e.getMessage());
            // e.printStackTrace();
        }
    }

    @Transactional
    public Driver verifyOtp(String email, String otp) {
        Driver driver = driverRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User không tồn tại"));

        if (driver.getEmailVerified()) {
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

        if (!driver.getEmailVerified()) {
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
