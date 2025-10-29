package evswap.swp391to4.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.dto.WalletResponse;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.service.PaymentService;
import evswap.swp391to4.service.WalletService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @GetMapping
    public String walletPage(HttpSession session, Model model, RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để quản lý ví");
            return "redirect:/login";
        }

        BigDecimal balance = walletService.getBalance(driver.getDriverId());
        List<Payment> transactions = paymentRepository.findByDriverOrderByPaidAtDesc(driver);

        model.addAttribute("balance", balance);
        model.addAttribute("transactions", transactions);
        model.addAttribute("driverName", driver.getFullName());
        model.addAttribute("driverInitial", extractInitial(driver.getFullName()));

        return "wallet";
    }

    @PostMapping("/topup")
    public String topUpWallet(@RequestParam("amount") BigDecimal amount,
                             HttpSession session,
                             RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để nạp tiền");
            return "redirect:/login";
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirect.addFlashAttribute("error", "Số tiền nạp phải lớn hơn 0");
            return "redirect:/wallet";
        }

        try {
            paymentService.simulateTopUp(driver, amount);
            redirect.addFlashAttribute("success", "Nạp tiền thành công! Số tiền: " + 
                String.format("%,.0f", amount) + " VND");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi nạp tiền: " + e.getMessage());
        }

        return "redirect:/wallet";
    }

    private String extractInitial(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "U";
        }
        return fullName.trim().substring(0, 1).toUpperCase();
    }

    // JSON API moved here for consistency with Wallet feature
    @GetMapping("/api/balance")
    public ResponseEntity<WalletResponse> apiBalance(HttpSession session) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            return ResponseEntity.status(401).build();
        }
        try {
            BigDecimal balance = walletService.getBalance(driver.getDriverId());
            WalletResponse response = WalletResponse.builder()
                    .balance(balance)
                    .currency("VND")
                    .recentTransactions(java.util.List.of())
                    .build();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
