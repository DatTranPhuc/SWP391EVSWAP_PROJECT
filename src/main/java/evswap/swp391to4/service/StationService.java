package evswap.swp391to4.service;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepo;

    /**
     * 1. CREATE ➕: Tạo một trạm pin mới
     */
    @Transactional
    public StationResponse createStation(StationCreateRequest req) {
        if (stationRepo.findByNameIgnoreCase(req.getName()).isPresent()) {
            throw new IllegalStateException("Tên trạm '" + req.getName() + "' đã tồn tại.");
        }

        Station station = Station.builder()
                .name(req.getName())
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .status(req.getStatus() != null ? req.getStatus() : "OPERATIONAL") // Mặc định là đang hoạt động
                .build();

        Station savedStation = stationRepo.save(station);
        return toResponse(savedStation);
    }

    /**
     * 2. READ 📖: Lấy danh sách tất cả các trạm
     */
    @Transactional(readOnly = true)
    public List<StationResponse> getAllStations() {
        return stationRepo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 3. READ BY ID 🆔: Lấy thông tin một trạm theo ID
     */
    @Transactional(readOnly = true)
    public StationResponse getStationById(Integer id) {
        return stationRepo.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạm với ID: " + id));
    }

    /**
     * 4. UPDATE ✏️: Cập nhật thông tin của một trạm
     */
    @Transactional
    public StationResponse updateStation(Integer id, StationCreateRequest req) {
        Station stationToUpdate = stationRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạm với ID: " + id));

        // Cập nhật các trường thông tin
        stationToUpdate.setName(req.getName());
        stationToUpdate.setAddress(req.getAddress());
        stationToUpdate.setLatitude(req.getLatitude());
        stationToUpdate.setLongitude(req.getLongitude());
        if (req.getStatus() != null) {
            stationToUpdate.setStatus(req.getStatus());
        }

        Station updatedStation = stationRepo.save(stationToUpdate);
        return toResponse(updatedStation);
    }

    /**
     * 5. DELETE 🗑️: Xóa một trạm
     */
    @Transactional
    public void deleteStation(Integer id) {
        if (!stationRepo.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy trạm với ID: " + id);
        }
        stationRepo.deleteById(id);
    }

    /**
     * 🔎 Tìm trạm theo tên (nếu không nhập → trả tất cả)
     */
    public List<StationResponse> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllStations();
        }
        return stationRepo.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 📍 Tìm trạm gần vị trí (theo bán kính km)
     */
    public List<StationResponse> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm) {
        // Lưu ý: Cách làm này sẽ chậm nếu có nhiều trạm.
        // Giải pháp tối ưu hơn là dùng Spatial Query của database.
        return stationRepo.findAll().stream()
                .filter(s -> {
                    if (s.getLatitude() == null || s.getLongitude() == null) return false;
                    double distance = distanceInKm(
                            lat.doubleValue(), lng.doubleValue(),
                            s.getLatitude().doubleValue(), s.getLongitude().doubleValue()
                    );
                    return distance <= radiusKm;
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 📏 Công thức Haversine tính khoảng cách giữa 2 tọa độ
     */
    private double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Bán kính Trái đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * 🔄 HELPER: Chuyển đổi từ Entity sang DTO Response
     */
    private StationResponse toResponse(Station station) {
        return StationResponse.builder()
                .stationId(station.getStationId())
                .name(station.getName())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .status(station.getStatus() != null ? station.getStatus().toUpperCase() : "UNKNOWN")
                .build();
    }
}