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

import java.util.ArrayList;
import java.util.List;

/**
 * Lớp Service (Bộ não 🧠)
 * Phiên bản này KHÔNG DÙNG HÀM HELPER (theo yêu cầu)
 * Dẫn đến việc code bị lặp lại ở 2 chỗ.
 */
@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final StationRepository stationRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * CHỨC NĂNG 1: Lấy danh sách nhân viên (có tìm kiếm)
     */
    @Transactional(readOnly = true)
    public List<StaffResponse> getAllStaff(String searchName) {

        // 1. Lấy "Hàng thô" (List<Staff>) từ Kho (Repository)
        List<Staff> staffList;
        if (searchName == null || searchName.isBlank()) {
            staffList = staffRepo.findAll();
        } else {
            staffList = staffRepo.findByFullNameContainingIgnoreCase(searchName);
        }

        // 2. Tạo List rỗng để chứa DTO
        List<StaffResponse> responseList = new ArrayList<>();

        // 3. Lặp qua "Hàng thô"
        for (Staff staff : staffList) {

            // 4. "Đóng gói" DTO (LẶP LẠI CODE LẦN 1)
            // BẠN PHẢI VIẾT CODE "ĐÓNG GÓI" NGAY TẠI ĐÂY
            StaffResponse dto = StaffResponse.builder()
                    .staffId(staff.getStaffId())
                    .email(staff.getEmail())
                    .fullName(staff.getFullName())
                    .isActive(staff.getIsActive())
                    .stationId(staff.getStation() != null ? staff.getStation().getStationId() : null)
                    .build();

            // 5. Thêm DTO đã đóng gói vào list
            responseList.add(dto);
        }

        // 6. Trả về List DTO
        return responseList;
    }

    /**
     * CHỨC NĂNG 2: Tạo nhân viên mới
     */
    @Transactional
    public StaffResponse createStaff(StaffCreateRequest req) {

        // (Tất cả logic kiểm tra và tạo Staff giữ nguyên)
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

        // "Đóng gói" DTO (LẶP LẠI CODE LẦN 2)
        // BẠN PHẢI VIẾT LẠI CODE "ĐÓNG GÓI" NGAY TẠI ĐÂY
        return StaffResponse.builder()
                .staffId(saved.getStaffId())
                .email(saved.getEmail())
                .fullName(saved.getFullName())
                .isActive(saved.getIsActive())
                .stationId(saved.getStation() != null ? saved.getStation().getStationId() : null)
                .build();
    }

    // KHÔNG CÓ HÀM HELPER 'mapToStaffResponse' Ở ĐÂY
}