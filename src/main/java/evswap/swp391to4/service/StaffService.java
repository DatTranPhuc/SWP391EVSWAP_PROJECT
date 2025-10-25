package evswap.swp391to4.service;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StaffUpdateRequest;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // Import cho 'new ArrayList<>()'
import java.util.List;
// import java.util.stream.Collectors; // Không cần nữa vì dùng 'for'

/**
 * Lớp Service (Bộ não 🧠)
 * Phiên bản này DÙNG HÀM HELPER và VÒNG LẶP FOR
 */
@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final StationRepository stationRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * CHỨC NĂNG 1: Lấy danh sách nhân viên (có tìm kiếm)
     * (Sử dụng vòng lặp 'for' cho dễ hiểu)
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff(String searchName) {
        List<Staff> staffList;
        if (searchName == null || searchName.isBlank()) {
            staffList = staffRepo.findAll();
        } else {
            staffList = staffRepo.findByFullNameContainingIgnoreCase(searchName);
        }

        // === SỬA LẠI (Dùng vòng lặp 'for') ===
        List<StaffResponse> responseList = new ArrayList<>();
        for (Staff staff : staffList) {
            // Gọi helper cho từng 'staff' và thêm vào list mới
            responseList.add(mapToStaffResponse(staff));
        }
        return responseList;
    }

    /**
     * CHỨC NĂNG 2: Tạo nhân viên mới
     * (ĐÃ CẬP NHẬT: Bắt buộc phải có Station ID)
     */
    @Transactional
    public StaffResponse createStaff(StaffCreateRequest req) {
        if (staffRepo.existsByEmail(req.getEmail())) {
            throw new IllegalStateException("Email đã tồn tại");
        }

        // === LOGIC CŨ (Bị xóa) ===
        // Station station = null;
        // if (req.getStationId() != null) { ... }

        // === LOGIC MỚI ===
        // Vì stationId là bắt buộc, ta tìm luôn
        Station station = stationRepo.findById(req.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

        // Kiểm tra trạm có active không
        if (!"active".equalsIgnoreCase(station.getStatus())) {
            throw new IllegalArgumentException("Trạm này đang không hoạt động (không active)");
        }

        // (Phần xử lý password giữ nguyên)
        String rawPassword = req.getPassword();
        if (rawPassword == null || rawPassword.isBlank()) {
            rawPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        }

        // (Phần build và save giữ nguyên)
        Staff staff = Staff.builder()
                .email(req.getEmail())
                .fullName(req.getFullName())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .isActive(true)
                .station(station) // <-- station giờ sẽ không bao giờ null
                .build();

        Staff saved = staffRepo.save(staff);

        return mapToStaffResponse(saved);
    }

    /**
     * CHỨC NĂNG 3: Lấy thông tin chi tiết 1 staff để sửa
     */
    @Transactional(readOnly = true)
    public StaffUpdateRequest getStaffDetails(Integer id) {
        Staff staff = staffRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên"));

        // (Hàm này trả về DTO khác (StaffUpdateRequest) nên không dùng helper)
        return StaffUpdateRequest.builder()
                .staffId(staff.getStaffId())
                .fullName(staff.getFullName())
                .email(staff.getEmail())
                .isActive(staff.getIsActive())
                .stationId(staff.getStation() != null ? staff.getStation().getStationId() : null)
                .build();
    }

    /**
     * CHỨC NĂNG 4: Cập nhật thông tin staff
     */
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

        // Gọi helper
        return mapToStaffResponse(updated);
    }

    /**
     * CHỨC NĂNG 5: Xóa nhân viên
     */
    @Transactional
    public void deleteStaff(Integer id) {
        if (!staffRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên để xóa");
        }
        staffRepo.deleteById(id);
    }

    /**
     * CHỨC NĂNG 6: Đăng nhập cho Staff
     */
    @Transactional(readOnly = true)
    public Staff login(String email, String password) {
        // 1. Tìm staff bằng email
        Staff staff = staffRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc mật khẩu không đúng"));

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(password, staff.getPasswordHash())) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }

        // 3. Kiểm tra tài khoản có bị khóa không
        if (!staff.getIsActive()) {
            throw new IllegalStateException("Tài khoản này đã bị quản trị viên vô hiệu hóa");
        }

        // 4. SỬA LỖI 500:
        // Chủ động "đánh thức" Station TRƯỚC KHI transaction kết thúc
        staff.getStation().getName(); // <-- THÊM DÒNG NÀY

        // 5. Đăng nhập thành công, trả về Entity
        return staff;
    }

    /**
     * HÀM HELPER (PRIVATE) 📦
     * "Đóng gói" Staff (Entity) thành StaffResponse (DTO).
     */
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