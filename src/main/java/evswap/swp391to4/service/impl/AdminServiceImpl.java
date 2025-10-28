package evswap.swp391to4.service.impl;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.repository.AdminRepository;
import evswap.swp391to4.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Admin login(String email, String password) {
        Admin admin = adminRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản hoặc mật khẩu không đúng"));
        if (passwordEncoder.matches(password, admin.getPasswordHash())) {
            return admin;
        } else {
            throw new IllegalArgumentException("Tài khoản hoặc mật khẩu không đúng");
        }
    }
}
