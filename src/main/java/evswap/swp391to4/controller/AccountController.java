package evswap.swp391to4.controller;

import evswap.swp391to4.dto.DepositRequest;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.service.AccountService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Hiển thị trang thông tin tài khoản và lịch sử nạp tiền.
     * URL: GET /account
     */
    @GetMapping
    public String showAccountPage(HttpSession session, Model model, RedirectAttributes redirect) {
        // 1. Kiểm tra người dùng đã đăng nhập chưa
        Driver loggedInDriver = (Driver) session.getAttribute("loggedInDriver");
        if (loggedInDriver == null) {
            redirect.addFlashAttribute("loginRequired", "Vui lòng đăng nhập để xem tài khoản.");
            return "redirect:/login";
        }

        // 2. Thêm một đối tượng DepositRequest trống vào model để form có thể liên kết
        if (!model.containsAttribute("depositForm")) {
            model.addAttribute("depositForm", new DepositRequest());
        }

        // 3. Gọi service để lấy dữ liệu
        model.addAttribute("accountDetails", accountService.getAccountDetails(loggedInDriver.getDriverId()));
        model.addAttribute("paymentHistory", accountService.getPaymentHistory(loggedInDriver.getDriverId()));

        // 4. Trả về trang HTML
        return "account-page";
    }

    /**
     * Xử lý yêu cầu nạp tiền từ form.
     * URL: POST /account/deposit
     */
    @PostMapping("/deposit")
    public String handleDeposit(@Valid @ModelAttribute("depositForm") DepositRequest depositRequest,
                                BindingResult bindingResult, // Nơi chứa kết quả validation
                                HttpSession session,
                                RedirectAttributes redirect) {
        // 1. Kiểm tra đăng nhập
        Driver loggedInDriver = (Driver) session.getAttribute("loggedInDriver");
        if (loggedInDriver == null) {
            return "redirect:/login";
        }

        // 2. Kiểm tra nếu có lỗi validation (ví dụ: số tiền âm, bỏ trống)
        if (bindingResult.hasErrors()) {
            // Gửi lại các lỗi và dữ liệu đã nhập về trang tài khoản
            redirect.addFlashAttribute("org.springframework.validation.BindingResult.depositForm", bindingResult);
            redirect.addFlashAttribute("depositForm", depositRequest);
            redirect.addFlashAttribute("accountError", "Dữ liệu không hợp lệ. Vui lòng kiểm tra lại.");
            return "redirect:/account";
        }

        // 3. Nếu không có lỗi, tiến hành nạp tiền
        try {
            accountService.addFunds(loggedInDriver.getDriverId(), depositRequest.getAmount());
            redirect.addFlashAttribute("accountSuccess", "Nạp tiền thành công!");
        } catch (Exception e) {
            redirect.addFlashAttribute("accountError", "Nạp tiền thất bại: " + e.getMessage());
        }

        return "redirect:/account";
    }
}