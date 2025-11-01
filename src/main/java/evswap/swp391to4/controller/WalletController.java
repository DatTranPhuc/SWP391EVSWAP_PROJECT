package evswap.swp391to4.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import evswap.swp391to4.dto.WalletResponse;
import evswap.swp391to4.entity.Driver;
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
                             @RequestParam(value = "method", defaultValue = "wallet") String method,
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

        // Validate minimum amount
        if (amount.compareTo(new BigDecimal("10000")) < 0) {
            redirect.addFlashAttribute("error", "Số tiền nạp tối thiểu là 10,000 VND");
            return "redirect:/wallet";
        }

        try {
            if ("payos".equalsIgnoreCase(method)) {
                // Nạp tiền qua PayOS (thanh toán thật)
                Payment payment = paymentService.createPayOsTopUpRequest(driver, amount);
                // Lưu paymentId vào session để có thể redirect sau khi thanh toán thành công
                session.setAttribute("pendingPaymentId", payment.getPaymentId());
                redirect.addFlashAttribute("payosPayment", true);
                redirect.addFlashAttribute("paymentId", payment.getPaymentId());
                redirect.addFlashAttribute("success", "Đang chuyển đến cổng thanh toán PayOS...");
                return "redirect:/wallet/topup/payos/" + payment.getPaymentId();
            } else {
                redirect.addFlashAttribute("error", "Phương thức này đã bị vô hiệu hóa. Vui lòng chọn PayOS.");
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Lỗi nạp tiền: " + e.getMessage());
        }

        return "redirect:/wallet";
    }

    @GetMapping("/topup/payos/{paymentId}")
    public String payOsTopUpPage(@PathVariable Integer paymentId,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirect) {
        Driver driver = (Driver) session.getAttribute("loggedInDriver");
        if (driver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập");
            return "redirect:/login";
        }

        try {
            Payment payment = paymentService.getPaymentByIdAndDriver(paymentId, driver);
            model.addAttribute("payment", payment);
            model.addAttribute("checkoutUrl", payment.getCheckoutUrl());
            model.addAttribute("driverName", driver.getFullName());
            model.addAttribute("amount", payment.getAmount());
            return "wallet-payos-payment";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không tìm thấy yêu cầu thanh toán");
            return "redirect:/wallet";
        }
    }

    @GetMapping("/topup-success")
    public String topUpSuccess(@org.springframework.web.bind.annotation.RequestParam(name = "orderCode", required = false) Long orderCode,
                               RedirectAttributes redirect) {
        // Trang này cho phép truy cập công khai từ PayOS returnUrl
        if (orderCode != null) {
            try {
                paymentService.reconcileByOrderCode(orderCode);
            } catch (Exception ignored) {
            }
        }
        redirect.addFlashAttribute("success", "Nạp tiền thành công!");
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
