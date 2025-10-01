package evswap.swp391to4.controller;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;

    @GetMapping("/login")
    public ResponseEntity<Void> loginPage(@RequestParam(name = "email", required = false) String email) {
        String qs = (email != null && !email.isBlank()) ? ("?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)) : "";
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://127.0.0.1:5173/login.html" + qs));
        return ResponseEntity.status(302).headers(headers).build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        String qs = (email != null && !email.isBlank()) ? ("?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)) : "";
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://127.0.0.1:5173/login.html" + qs));
        return ResponseEntity.status(302).headers(headers).build();
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(name = "email", required = false) String email) {
        String qs = (email != null && !email.isBlank()) ? ("?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)) : "";
        return "redirect:http://127.0.0.1:5173/register.html" + qs;
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String phone,
                           RedirectAttributes redirect) {
        try {
            Driver driver = Driver.builder()
                    .email(email)
                    .passwordHash(password)
                    .fullName(fullName)
                    .phone(phone)
                    .build();
            driverService.register(driver);
            redirect.addFlashAttribute("email", email);
            return "redirect:/verify";
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Đăng ký thất bại";
            String lower = msg.toLowerCase();
            if (lower.contains("email")) msg = "Email đã được đăng ký";
            else if (lower.contains("phone") || lower.contains("điện thoại")) msg = "Số điện thoại đã được sử dụng";
            else msg = "Đăng ký thất bại. Vui lòng kiểm tra thông tin và thử lại";
            redirect.addFlashAttribute("registerError", msg);
            return "redirect:/register";
        }
    }

    @GetMapping("/verify")
    public String verifyPage(@RequestParam(name = "email", required = false) String email,
                             @RequestParam(name = "error", required = false) String error,
                             Model model) {
        if (email != null && !email.isBlank()) {
            model.addAttribute("email", email);
        }
        if (error != null && !error.isBlank()) {
            model.addAttribute("verifyError", error);
        }
        return "verify";
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Void> verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirect) {
        try {
            driverService.verifyOtp(email, otp);
            String qs = "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("http://127.0.0.1:5173/login.html" + qs));
            return ResponseEntity.status(302).headers(headers).build();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Xác minh thất bại";
            String qs = "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8) + "&error=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("/verify" + qs));
            return ResponseEntity.status(302).headers(headers).build();
        }
    }
}
