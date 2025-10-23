package evswap.swp391to4.controller;

import evswap.swp391to4.dto.DepositRequest;
import evswap.swp391to4.dto.QrCodeResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.AccountService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account") // Đường dẫn chung cho trang tài khoản
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    /**
     * Hiển thị trang tài khoản (GET /account).
     * Bao gồm thông tin số dư, lịch sử nạp tiền, và form nạp tiền.
     */
    @GetMapping
    public String showAccountPage(HttpSession session, Model model, RedirectAttributes redirect) {
        Driver loggedInDriver = (Driver) session.getAttribute("loggedInDriver");
        if (loggedInDriver == null) {
            log.warn("User not logged in trying to access /account. Redirecting to login.");
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem tài khoản.");
            return "redirect:/login";
        }
        log.info("Displaying account page for driver ID: {}", loggedInDriver.getDriverId());

        // Thêm form nạp tiền trống nếu chưa có (cần cho Thymeleaf binding)
        if (!model.containsAttribute("depositForm")) {
            model.addAttribute("depositForm", new DepositRequest());
        }

        // Lấy dữ liệu tài khoản và lịch sử
        try {
            model.addAttribute("accountDetails", accountService.getAccountDetails(loggedInDriver.getDriverId()));
            model.addAttribute("paymentHistory", accountService.getPaymentHistory(loggedInDriver.getDriverId()));
        } catch (RuntimeException e) {
            log.error("Error loading account data for driver ID: {}", loggedInDriver.getDriverId(), e);
            model.addAttribute("accountLoadError", "Không thể tải dữ liệu tài khoản. Vui lòng thử lại sau.");
        }

        // Dữ liệu QR hoặc lỗi QR từ redirect sẽ tự động được thêm vào model
        return "account-page";
    }

    /**
     * Xử lý yêu cầu tạo mã QR để nạp tiền từ form (POST /account/deposit/qr).
     */
    @PostMapping("/deposit/qr")
    public String initiateQrDeposit(@Valid @ModelAttribute("depositForm") DepositRequest depositRequest,
                                    BindingResult bindingResult,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        Driver loggedInDriver = (Driver) session.getAttribute("loggedInDriver");
        if (loggedInDriver == null) {
            log.warn("User not logged in trying to initiate QR deposit via POST.");
            return "redirect:/login";
        }

        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors on deposit form for driver ID {}: {}", loggedInDriver.getDriverId(), bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.depositForm", bindingResult);
            redirectAttributes.addFlashAttribute("depositForm", depositRequest); // Gửi lại dữ liệu form cũ
            redirectAttributes.addFlashAttribute("qrError", "Số tiền không hợp lệ. Vui lòng kiểm tra lại.");
            return "redirect:/account";
        }

        // Gọi service tạo QR/URL
        log.info("Initiating QR deposit request for driver ID: {} with amount: {}", loggedInDriver.getDriverId(), depositRequest.getAmount());
        try {
            QrCodeResponse qrCodeResponse = accountService.initiateQrPayment(loggedInDriver.getDriverId(), depositRequest.getAmount());
            // Gửi dữ liệu QR về trang account qua redirect
            redirectAttributes.addFlashAttribute("qrCodeData", qrCodeResponse);
            log.info("Successfully initiated QR deposit for driver ID: {}", loggedInDriver.getDriverId());
        } catch (Exception e) {
            log.error("Error initiating QR payment for driver ID: {}", loggedInDriver.getDriverId(), e);
            // Gửi thông báo lỗi về trang account qua redirect
            redirectAttributes.addFlashAttribute("qrError", "Lỗi tạo mã QR: " + e.getMessage());
        }

        // Luôn redirect về trang tài khoản
        return "redirect:/account";
    }
}