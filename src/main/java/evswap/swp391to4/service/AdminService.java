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

    public Admin login(String email, String password) {
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Email không tồn tại"));

        if (!passwordEncoder.matches(password, admin.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu không đúng");
        }

        return admin;
    }
}
