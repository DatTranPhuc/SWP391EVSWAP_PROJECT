package evswap.swp391to4.service;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final DriverRepository driverRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // [DEBUG] Dòng này để kiểm tra xem Spring Security có gọi đến đây không
        System.out.println("--- DEBUG: Bắt đầu tìm kiếm người dùng với email: " + email);

        // 1. Tìm driver trong DB bằng email
        Driver driver = driverRepository.findByEmail(email)
                .orElseThrow(() -> {
                    // [DEBUG] Nếu không tìm thấy, in ra lỗi và ném exception
                    System.out.println("--- DEBUG: KHÔNG TÌM THẤY người dùng với email: " + email);
                    return new UsernameNotFoundException("Không tìm thấy tài khoản với email: " + email);
                });

        // [DEBUG] Nếu tìm thấy, in ra thông tin
        System.out.println("--- DEBUG: ĐÃ TÌM THẤY người dùng: " + driver.getFullName() + ". Chuẩn bị so sánh mật khẩu.");

        // 2. Tạo và trả về một đối tượng UserDetails mà Spring Security có thể hiểu
        return new User(
                driver.getEmail(),
                driver.getPasswordHash(),
                Collections.emptyList() // Tạm thời chưa xét đến vai trò (ROLE)
        );
    }
}