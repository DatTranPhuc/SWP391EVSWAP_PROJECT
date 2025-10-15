package evswap.swp391to4.config;

import evswap.swp391to4.entity.Admin;
import evswap.swp391to4.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
public class AdminBootstrap implements CommandLineRunner {

    private final AdminRepository adminRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@gmail.com";
        String adminPassword = "Admin123"; // mặc định, đổi sau khi  lần đầu

        if (adminRepo.findByEmail(adminEmail).isEmpty()) {
            Admin admin = Admin.builder()
                    .email(adminEmail)
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .fullName("System Admin")
                    .createdAt(Instant.now())
                    .build();
            adminRepo.save(admin);
            System.out.println("Admin account created: " + adminEmail + " / " + adminPassword);
        } else {
            System.out.println("Admin already exists.");
        }
    }
}
