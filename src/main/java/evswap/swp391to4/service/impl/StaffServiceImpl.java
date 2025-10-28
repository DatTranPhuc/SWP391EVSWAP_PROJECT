package evswap.swp391to4.service.impl;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StaffUpdateRequest;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.service.StaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepo;
    private final StationRepository stationRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff(String searchName) {
        List<Staff> staffList;
        if (searchName == null || searchName.isBlank()) {
            staffList = staffRepo.findAll();
        } else {
            staffList = staffRepo.findByFullNameContainingIgnoreCase(searchName);
        }
        List<StaffResponse> responseList = new ArrayList<>();
        for (Staff staff : staffList) {
            responseList.add(mapToStaffResponse(staff));
        }
        return responseList;
    }

    @Override
    @Transactional
    public StaffResponse createStaff(StaffCreateRequest req) {
        if (staffRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email đã tồn tại");
        }
        Station station = stationRepo.findById(req.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));
        if (!"active".equalsIgnoreCase(station.getStatus())) {
            throw new IllegalArgumentException("Trạm này đang không hoạt động (không active)");
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
        return mapToStaffResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffUpdateRequest getStaffDetails(Integer id) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));
        return StaffUpdateRequest.builder()
                .staffId(staff.getStaffId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .isActive(staff.getIsActive())
                .stationId(staff.getStation() != null ? staff.getStation().getStationId() : null)
                .build();
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(Integer id, StaffUpdateRequest req) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));
        if (!staff.getEmail().equals(req.getEmail()) && staffRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email này đã được sử dụng bởi tài khoản khác");
        }
        Station station = null;
        if (req.getStationId() != null) {
            station = stationRepo.findById(req.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));
        }
        staff.setFullName(req.getFullName());
        staff.setEmail(req.getEmail());
        staff.setIsActive(req.getIsActive());
        staff.setStation(station);
        Staff updated = staffRepo.save(staff);
        return mapToStaffResponse(updated);
    }

    @Override
    @Transactional
    public void deleteStaff(Integer id) {
        if (!staffRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên để xóa");
        }
        staffRepo.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Staff> login(String email, String password) {
        return staffRepo.findByEmail(email)
                .filter(staff -> passwordEncoder.matches(password, staff.getPasswordHash()))
                .filter(Staff::getIsActive); // Chỉ nhận nếu active và đúng mật khẩu, không trả về lỗi
    }


    // Helper private method
    private StaffResponse mapToStaffResponse(Staff staff) {
        return StaffResponse.builder()
                .staffId(staff.getStaffId())
                .email(staff.getEmail())
                .fullName(staff.getFullName())
                .isActive(staff.getIsActive())
                .stationId(staff.getStation() != null ? staff.getStation().getStationId() : null)
                .build();
    }
}
