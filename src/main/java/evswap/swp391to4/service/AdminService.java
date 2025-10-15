package evswap.swp391to4.service;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Login admin bằng email và password
     * @param email
     * @param password
     * @return Admin nếu login thành công
     * @throws Exception nếu email không tồn tại hoặc mật khẩu sai
     */
    public Admin login(String email, String password) throws Exception {
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("Admin không tồn tại"));

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new Exception("Mật khẩu không đúng");
        }

        return admin;
    }
}
