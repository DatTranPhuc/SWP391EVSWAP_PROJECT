package evswap.swp391to4.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationDistance;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepo;
    private final StaffRepository staffRepo;

    /**
     * Tạo một trạm mới dựa trên yêu cầu.
     * Ném ra lỗi nếu tên trạm đã tồn tại.
     * @param req Đối tượng chứa thông tin trạm mới.
     * @return DTO của trạm vừa được tạo.
     */
    @Transactional
    public StationResponse createStation(StationCreateRequest req) {
        stationRepo.findByNameIgnoreCase(req.getName()).ifPresent(s -> {
            throw new IllegalStateException("Tên trạm '" + req.getName() + "' đã tồn tại.");
        });

        Station station = Station.builder()
                .name(req.getName())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .status(req.getStatus() != null ? req.getStatus() : "active")
                .build();

        Station saved = stationRepo.save(station);
        return toResponse(saved);
    }

    /**
     * Lấy danh sách tất cả các trạm.
     * @return Danh sách DTO của tất cả các trạm.
     */
    public List<StationResponse> getAllStations() {
        return stationRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Tìm kiếm trạm theo tên (chứa từ khóa, không phân biệt hoa thường).
     * @param name Từ khóa để tìm kiếm.
     * @return Danh sách các trạm phù hợp.
     */
    public List<StationResponse> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllStations();
        }
        return stationRepo.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Tìm các trạm trong một bán kính cho trước bằng cách sử dụng Native Query đã được tối ưu.
     * @param lat Vĩ độ của người dùng.
     * @param lng Kinh độ của người dùng.
     * @param radiusKm Bán kính tìm kiếm (tính bằng km).
     * @return Danh sách DTO của các trạm trong bán kính, đã sắp xếp từ gần đến xa.
     */
    public List<StationResponse> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm) {
        // Gọi phương thức đã được tối ưu từ Repository
        List<StationDistance> results = stationRepo.findNearbyStations(lat.doubleValue(), lng.doubleValue(), radiusKm);

        // Chuyển đổi (map) danh sách kết quả từ projection sang StationResponse DTO
        return results.stream()
                .map(result -> StationResponse.builder()
                        .stationId(result.getStationId())
                        .name(result.getName())
                        .address(result.getAddress())
                        .status(result.getStatus())
                        .latitude(result.getLatitude())
                        .longitude(result.getLongitude())
                        .distance(result.getDistance()) // Lấy distance trực tiếp từ kết quả
                        .build())
                .toList();
    }

    /**
     * Tìm một trạm theo ID.
     * @param stationId ID của trạm cần tìm.
     * @return DTO của trạm tìm thấy.
     * @throws IllegalStateException nếu không tìm thấy trạm.
     */
    public StationResponse findById(Integer stationId) {
        Station s = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm với ID: " + stationId));
        return toResponse(s);
    }

    /**
     * Cập nhật thông tin của một trạm.
     * @param stationId ID của trạm cần cập nhật.
     * @param req Đối tượng chứa thông tin cập nhật.
     * @return DTO của trạm sau khi đã cập nhật.
     */

    @Transactional
    public StationResponse updateStation(Integer stationId, StationCreateRequest req) {
        // 1. Tìm trạm
        Station s = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm với ID: " + stationId));

        // 2. KIỂM TRA TÊN TRÙNG (LOGIC BỊ THIẾU)
        // Kiểm tra xem tên mới (req.getName()) có tồn tại không
        stationRepo.findByNameIgnoreCase(req.getName())
                .ifPresent(existingStation -> {
                    // Nếu tên này đã tồn tại VÀ nó thuộc về một trạm KHÁC
                    if (!existingStation.getStationId().equals(stationId)) {
                        throw new IllegalStateException("Tên trạm '" + req.getName() + "' đã bị trạm khác sử dụng.");
                    }
                });

        // 3. Cập nhật thông tin
        s.setName(req.getName());
        s.setAddress(req.getAddress());
        s.setLatitude(req.getLatitude());
        s.setLongitude(req.getLongitude());
        s.setStatus(req.getStatus() != null ? req.getStatus() : s.getStatus());

        Station saved = stationRepo.save(s);
        return toResponse(saved);
    }

    /**
     * Xóa một trạm theo ID.
     * @param stationId ID của trạm cần xóa.
     */
    @Transactional
    public void deleteStation(Integer stationId) {
        // 1. Kiểm tra trạm có tồn tại không
        if (!stationRepo.existsById(stationId)) {
            throw new IllegalStateException("Không tìm thấy trạm với ID: " + stationId);
        }

        // 2. !!! KIỂM TRA MỚI !!!
        // Kiểm tra xem có Staff nào đang được gán cho trạm này không
        if (staffRepo.existsByStationStationId(stationId)) {
            throw new IllegalStateException("Không thể xóa trạm. Vẫn còn nhân viên được gán cho trạm này.");
        }

        // 3. Nếu an toàn, tiến hành xóa
        stationRepo.deleteById(stationId);
    }

    /**
     * Phương thức private để chuyển đổi từ Entity Station sang DTO StationResponse.
     * Dùng cho các trường hợp không cần tính toán khoảng cách.
     * @param s Entity Station.
     * @return DTO StationResponse.
     */
    private StationResponse toResponse(Station s) {
        return StationResponse.builder()
                .stationId(s.getStationId())
                .name(s.getName())
                .address(s.getAddress())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .status(s.getStatus())
                // Lưu ý: trường distance sẽ là null/0.0 khi gọi từ đây, đó là điều mong muốn.
                .build();
    }
}