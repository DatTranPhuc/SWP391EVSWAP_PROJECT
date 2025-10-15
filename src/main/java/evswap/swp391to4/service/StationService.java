package evswap.swp391to4.service;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.dto.StationUpdateRequest;
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
     * 👑 Admin tạo trạm mới
     */
    @Transactional
    public StationResponse createStation(StationCreateRequest req) {
        String normalizedName = req.getName() != null ? req.getName().trim() : null;
        if (normalizedName == null || normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Tên trạm không được để trống");
        }

        stationRepo.findByNameIgnoreCase(normalizedName)
                .ifPresent(existing -> {
                    throw new IllegalStateException("Station đã tồn tại");
                });

        Station station = Station.builder()
                .name(normalizedName)
                .address(req.getAddress())
                .latitude(req.getLatitude())
                .longitude(req.getLongitude())
                .status(req.getStatus() != null && !req.getStatus().isBlank() ? req.getStatus() : "active")
                .build();

        Station saved = stationRepo.save(station);
        return toResponse(saved);
    }

    /**
     * ✏️ Admin cập nhật trạm hiện hữu
     */
    @Transactional
    public StationResponse updateStation(Integer stationId, StationUpdateRequest req) {
        Station station = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

        if (req.getName() != null) {
            String trimmed = req.getName().trim();
            if (trimmed.isEmpty()) {
                throw new IllegalArgumentException("Tên trạm không được để trống");
            }
            stationRepo.findByNameIgnoreCase(trimmed)
                    .filter(existing -> !existing.getStationId().equals(stationId))
                    .ifPresent(existing -> {
                        throw new IllegalStateException("Station đã tồn tại");
                    });
            station.setName(trimmed);
        }

        if (req.getAddress() != null) {
            station.setAddress(req.getAddress());
        }
        if (req.getLatitude() != null) {
            station.setLatitude(req.getLatitude());
        }
        if (req.getLongitude() != null) {
            station.setLongitude(req.getLongitude());
        }
        if (req.getStatus() != null) {
            if (req.getStatus().isBlank()) {
                station.setStatus(null);
            } else {
                station.setStatus(req.getStatus());
            }
        }

        Station updated = stationRepo.save(station);
        return toResponse(updated);
    }

    /**
     * 🗑️ Admin xóa trạm
     */
    @Transactional
    public void deleteStation(Integer stationId) {
        Station station = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));
        stationRepo.delete(station);
    }

    /**
     * 📋 Lấy tất cả trạm
     */
    public List<StationResponse> getAllStations() {
        return stationRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
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
                .toList();
    }

    /**
     * 📍 Tìm trạm gần vị trí (theo bán kính km)
     */
    public List<StationResponse> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm) {
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
                .toList();
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
