package evswap.swp391to4.config;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final DriverRepository driverRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Lấy email của người dùng vừa đăng nhập thành công
        String email = authentication.getName();

        // Dùng email để tìm đối tượng Driver đầy đủ trong database
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy Driver cho email đã đăng nhập."));

        // Lấy session hiện tại
        HttpSession session = request.getSession();

        // Đặt đối tượng Driver vào session với key là "loggedInDriver"
        session.setAttribute("loggedInDriver", driver);

        // Chuyển hướng người dùng đến trang dashboard
        response.sendRedirect("/dashboard");
    }
}