package evswap.swp391to4.service;

import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;

    // Admin tạo Staff
    public Staff createStaff(Station station, String fullName, String email, String password) {
        if (staffRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Staff staff = Staff.builder()
                .station(station)
                .fullName(fullName)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .isActive(true)
                .build();

        return staffRepo.save(staff);
    }
}

