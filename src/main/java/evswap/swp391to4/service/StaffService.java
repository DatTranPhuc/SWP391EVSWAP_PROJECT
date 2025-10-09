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

    /**
     * 🧑‍💼 Tạo nhân viên mới
     */
    @Transactional
    public StaffResponse createStaff(StaffCreateRequest req) {
        // Check email trùng
        if (staffRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email đã tồn tại");
        }

        // Kiểm tra station nếu có
        Station station = null;
        if (req.getStationId() != null) {
            station = stationRepo.findById(req.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

            if (!"active".equalsIgnoreCase(station.getStatus())) {
                throw new IllegalArgumentException("Station chưa ở trạng thái ACTIVE");
            }
        }

        // Mật khẩu: sinh tự động nếu trống
        String rawPassword = (req.getPassword() == null || req.getPassword().isBlank())
                ? java.util.UUID.randomUUID().toString().substring(0, 8)
                : req.getPassword();

        // Tạo staff entity
        Staff staff = Staff.builder()
                .email(req.getEmail().trim().toLowerCase())
                .fullName(req.getFullName().trim())
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

    /**
     * 📋 Lấy danh sách tất cả nhân viên
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        return staffRepo.findAll().stream()
                .map(staff -> StaffResponse.builder()
                        .staffId(staff.getStaffId())
                        .email(staff.getEmail())
                        .fullName(staff.getFullName())
                        .isActive(staff.getIsActive())
                        .stationId(staff.getStation() != null ? staff.getStation().getStationId() : null)
                        .build())
                .toList();
    }
}
