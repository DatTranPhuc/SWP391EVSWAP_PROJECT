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

    @Transactional
    public StationResponse createStation(StationCreateRequest req) {
        if (stationRepo.findByNameIgnoreCase(req.getName()).isPresent()) {
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

    public List<StationResponse> getAllStations() {
        return stationRepo.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<StationResponse> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllStations();
        }
        return stationRepo.findByNameContainingIgnoreCase(name).stream()
                .map(this::toResponse)
                .toList();
    }

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

    public StationResponse findById(Integer stationId) {
        Station s = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm"));
        return toResponse(s);
    }

    @Transactional
    public StationResponse updateStation(Integer stationId, StationCreateRequest req) {
        Station s = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm"));
        s.setName(req.getName());
        s.setAddress(req.getAddress());
        s.setLatitude(req.getLatitude());
        s.setLongitude(req.getLongitude());
        s.setStatus(req.getStatus() != null ? req.getStatus() : s.getStatus());
        Station saved = stationRepo.save(s);
        return toResponse(saved);
    }

    @Transactional
    public void deleteStation(Integer stationId) {
        if (!stationRepo.existsById(stationId)) {
            throw new IllegalStateException("Không tìm thấy trạm");
        }
        stationRepo.deleteById(stationId);
    }

    private double distanceInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * R * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

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
