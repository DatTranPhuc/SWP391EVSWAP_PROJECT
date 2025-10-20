package evswap.swp391to4.service;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Hàm này xử lý logic đăng nhập cho Admin
     */
    public Admin login(String email, String password) {
        // 1. Tìm admin bằng email trong DB
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản hoặc mật khẩu không đúng"));

        // 2. Nếu tìm thấy, so sánh mật khẩu đã mã hóa
        if (passwordEncoder.matches(password, admin.getPasswordHash())) {
            // 3. Nếu đúng, trả về admin
            return admin;
        } else {
            // 4. Nếu sai, ném lỗi
            throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không đúng");
        }
    }
}