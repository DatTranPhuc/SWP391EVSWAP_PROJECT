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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final StationRepository stationRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * 1. CREATE: Tạo một nhân viên mới
     */
    @Transactional
    public StaffResponse createStaff(StaffCreateRequest req) {
        if (staffRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email '" + req.getEmail() + "' đã tồn tại.");
        }

        Station station = stationRepo.findById(req.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Trạm với ID " + req.getStationId() + " không tồn tại."));

        Staff staff = Staff.builder()
                .email(req.getEmail())
                .fullName(req.getFullName())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .isActive(true) // Mặc định là active khi tạo mới
                .station(station)
                .build();

        Staff savedStaff = staffRepo.save(staff);
        return toResponse(savedStaff);
    }

    /**
     * 2. READ: Lấy danh sách tất cả nhân viên
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff() {
        return staffRepo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. READ BY ID: Lấy thông tin một nhân viên theo ID
     */
    @Transactional(readOnly = true)
    public StaffResponse getStaffById(Integer id) {
        return staffRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id));
    }

    /**
     * 4. UPDATE: Cập nhật thông tin của một nhân viên
     */
    @Transactional
    public StaffResponse updateStaff(Integer id, StaffCreateRequest req) {
        Staff staffToUpdate = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id));

        // Cập nhật họ tên
        staffToUpdate.setFullName(req.getFullName());

        // Cập nhật trạm làm việc
        if (req.getStationId() != null) {
            Station station = stationRepo.findById(req.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Trạm với ID " + req.getStationId() + " không tồn tại."));
            staffToUpdate.setStation(station);
        }

        // Chỉ cập nhật mật khẩu nếu người dùng cung cấp mật khẩu mới (không rỗng)
        if (StringUtils.hasText(req.getPassword())) {
            staffToUpdate.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }

        // Giả sử có một trường để cập nhật trạng thái active/inactive
        // staffToUpdate.setIsActive(req.getIsActive());

        Staff updatedStaff = staffRepo.save(staffToUpdate);
        return toResponse(updatedStaff);
    }

    /**
     * 5. DELETE: Xóa một nhân viên
     */
    @Transactional
    public void deleteStaff(Integer id) {
        if (!staffRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên với ID: " + id);
        }
        staffRepo.deleteById(id);
    }

    /**
     * HELPER: Chuyển đổi từ Entity sang DTO Response
     */
    private StaffResponse toResponse(Staff staff) {
        return StaffResponse.builder()
                .staffId(staff.getStaffId())
                .email(staff.getEmail())
                .fullName(staff.getFullName())
                .isActive(staff.getIsActive())
                .stationId(staff.getStation() != null ? staff.getStation().getStationId() : null)
                .stationName(staff.getStation() != null ? staff.getStation().getName() : "Chưa được phân công")
                .status(Boolean.TRUE.equals(staff.getIsActive()) ? "ACTIVE" : "INACTIVE")
                .build();
    }
}