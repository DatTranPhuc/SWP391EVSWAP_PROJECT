package evswap.swp391to4.controller;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.DriverService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final DriverService driverService;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        Model model) {
        try {
            driverService.login(email, password);
            model.addAttribute("loginSuccess", "Login successful!");
        } catch (Exception e) {
            model.addAttribute("loginError", e.getMessage());
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String fullName,
                           @RequestParam String phone,
                           RedirectAttributes redirect) {
        Driver driver = Driver.builder()
                .email(email)
                .passwordHash(password)
                .fullName(fullName)
                .phone(phone)
                .build();
        driverService.register(driver);
        redirect.addFlashAttribute("email", email);
        return "redirect:/verify";
    }

    @GetMapping("/verify")
    public String verifyPage() {
        return "verify";
    }

    @PostMapping("/verify-otp")
    public String verifyOtp(@RequestParam String email,
                            @RequestParam String otp,
                            RedirectAttributes redirect) {
        driverService.verifyOtp(email, otp);
        redirect.addFlashAttribute("verifySuccess", "Email verified! Please login.");
        return "redirect:/login";
    }
}
