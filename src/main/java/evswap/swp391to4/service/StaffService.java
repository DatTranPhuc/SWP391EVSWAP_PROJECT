package evswap.swp391to4.service;

import evswap.swp391to4.dto.StaffCreateRequest;
import evswap.swp391to4.dto.StaffResponse;
import evswap.swp391to4.dto.StaffUpdateRequest;
import evswap.swp391to4.entity.Staff;

import java.util.List;
import java.util.Optional;

public interface StaffService {
    List<StaffResponse> getAllStaff(String searchName);
    StaffResponse createStaff(StaffCreateRequest req);
    StaffUpdateRequest getStaffDetails(Integer id);
    StaffResponse updateStaff(Integer id, StaffUpdateRequest req);
    void deleteStaff(Integer id);

    // Chuyển login trả về Optional<Staff> (chuẩn cho auth flow RESTful)
    Optional<Staff> login(String email, String password);
}
