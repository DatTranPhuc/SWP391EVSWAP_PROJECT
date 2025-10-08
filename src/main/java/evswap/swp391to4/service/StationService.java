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

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepo;

    /**
     * 👑 Chỉ admin được phép tạo station
     */
    @Transactional
    public StationResponse createStation(StationCreateRequest req) {
        if (stationRepo.findAll().stream()
                .anyMatch(s -> s.getName().equalsIgnoreCase(req.getName()))) {
            throw new IllegalStateException("Station đã tồn tại");
        }

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
     * 📋 Lấy danh sách tất cả trạm
     */
    public List<StationResponse> getAllStations() {
        return stationRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * 🔎 Tìm trạm theo tên
     */
    public List<StationResponse> searchByName(String name) {
        return stationRepo.findAll().stream()
                .filter(s -> s.getName() != null && s.getName().toLowerCase().contains(name.toLowerCase()))
                .map(this::toResponse)
                .toList();
    }

    /**
     * 📍 Tìm trạm gần vị trí (bán kính km)
     */
    public List<StationResponse> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm) {
        return stationRepo.findAll().stream()
                .filter(s -> s.getLatitude() != null && s.getLongitude() != null)
                .filter(s -> distanceInKm(
                        lat.doubleValue(), lng.doubleValue(),
                        s.getLatitude().doubleValue(), s.getLongitude().doubleValue()
                ) <= radiusKm)
                .map(this::toResponse)
                .toList();
    }

    /**
     * 🧭 Tính khoảng cách giữa 2 toạ độ theo công thức Haversine
     */
    private double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Bán kính Trái đất (km)
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    /**
     * 🔄 Convert Entity → DTO
     */
    private StationResponse toResponse(Station s) {
        return StationResponse.builder()
                .stationId(s.getStationId())
                .name(s.getName())
                .address(s.getAddress())
                .latitude(s.getLatitude())
                .longitude(s.getLongitude())
                .status(s.getStatus())
                .build();
    }
}
