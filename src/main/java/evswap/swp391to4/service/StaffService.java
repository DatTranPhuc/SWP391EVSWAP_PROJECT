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

import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final StationRepository stationRepo;
    private final PasswordEncoder passwordEncoder;

    // ------------------ CREATE ------------------
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

        return toResponse(saved);
    }

    // ------------------ FIND BY ID ------------------
    public StaffResponse getStaffById(Integer id) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id));
        return toResponse(staff);
    }

    // ------------------ GET ALL ------------------
    public List<StaffResponse> getAllStaff() {
        return staffRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    // ------------------ HELPER ------------------
    private StaffResponse toResponse(Staff staff) {
        return StaffResponse.builder()
                .staffId(staff.getStaffId())
                .email(staff.getEmail())
                .fullName(staff.getFullName())
                .isActive(staff.getIsActive())
                .stationId(staff.getStation() != null ? staff.getStation().getStationId() : null)
                .build();
    }
}
