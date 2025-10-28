package evswap.swp391to4.service.impl;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationDistance;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.service.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepo;
    private final StaffRepository staffRepo;

    @Override
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

    @Override
    public List<StationResponse> getAllStations() {
        return stationRepo.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public List<StationResponse> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllStations();
        }
        return stationRepo.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(name, name).stream()
                .map(this::toResponse).toList();
    }

    @Override
    public List<StationResponse> findNearby(BigDecimal lat, BigDecimal lng, double radiusKm) {
        List<StationDistance> results = stationRepo.findNearbyStations(lat.doubleValue(), lng.doubleValue(), radiusKm);
        return results.stream()
                .map(result -> StationResponse.builder()
                        .stationId(result.getStationId())
                        .name(result.getName())
                        .address(result.getAddress())
                        .status(result.getStatus())
                        .latitude(result.getLatitude())
                        .longitude(result.getLongitude())
                        .distance(result.getDistance()).build())
                .toList();
    }

    @Override
    public StationResponse findById(Integer stationId) {
        Station s = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm với ID: " + stationId));
        return toResponse(s);
    }

    @Override
    @Transactional
    public StationResponse updateStation(Integer stationId, StationCreateRequest req) {
        Station s = stationRepo.findById(stationId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy trạm với ID: " + stationId));

        stationRepo.findByNameIgnoreCase(req.getName()).ifPresent(existingStation -> {
            if (!existingStation.getStationId().equals(stationId)) {
                throw new IllegalStateException("Tên trạm '" + req.getName() + "' đã bị trạm khác sử dụng.");
            }
        });

        s.setName(req.getName());
        s.setAddress(req.getAddress());
        s.setLatitude(req.getLatitude());
        s.setLongitude(req.getLongitude());
        s.setStatus(req.getStatus() != null ? req.getStatus() : s.getStatus());

        Station saved = stationRepo.save(s);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteStation(Integer stationId) {
        if (!stationRepo.existsById(stationId)) {
            throw new IllegalStateException("Không tìm thấy trạm với ID: " + stationId);
        }
        if (staffRepo.existsByStationStationId(stationId)) {
            throw new IllegalStateException("Không thể xóa trạm. Vẫn còn nhân viên được gán cho trạm này.");
        }
        stationRepo.deleteById(stationId);
    }

    // Helper method (private)
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
