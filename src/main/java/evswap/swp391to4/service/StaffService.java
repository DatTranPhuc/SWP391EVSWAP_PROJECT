package evswap.swp391to4.service;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final StationRepository stationRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public StaffResponse createStaff(StaffCreateRequest req) {
        if (staffRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email đã tồn tại");
        }

        Station station = null;
        if (req.getStationId() != null) {
            station = stationRepo.findById(req.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

            if (!"active".equalsIgnoreCase(station.getStatus())) {
                throw new IllegalArgumentException("Station không active");
            }
        }

        String rawPassword = req.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        }

        Staff staff = Staff.builder()
                .email(req.getEmail())
                .fullName(req.getFullName())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .station(station)
                .build();

        Staff saved = staffRepo.save(staff);

        return StaffResponse.builder()
                .staffId(saved.getStaffId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .isActive(saved.getIsActive())
                .stationId(saved.getStation() != null ? saved.getStation().getStationId() : null)
                .build();
    }
}
