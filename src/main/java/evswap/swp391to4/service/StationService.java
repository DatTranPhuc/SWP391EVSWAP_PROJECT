package evswap.swp391to4.service;

import evswap.swp391to4.dto.StationCreateRequest;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationRepository stationRepo;

    @Transactional
    public StationResponse createStation(StationCreateRequest req) {
        // Kiểm tra tên station đã tồn tại chưa
        if (stationRepo.findAll().stream().anyMatch(s -> s.getName().equalsIgnoreCase(req.getName()))) {
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

        return StationResponse.builder()
                .stationId(saved.getStationId())
                .name(saved.getName())
                .address(saved.getAddress())
                .latitude(saved.getLatitude())
                .longitude(saved.getLongitude())
                .status(saved.getStatus())
                .build();
    }

    @Transactional(readOnly = true)
    public java.util.List<StationResponse> getAllStations() {
        return stationRepo.findAll().stream()
                .map(station -> StationResponse.builder()
                        .stationId(station.getStationId())
                        .name(station.getName())
                        .address(station.getAddress())
                        .latitude(station.getLatitude())
                        .longitude(station.getLongitude())
                        .status(station.getStatus())
                        .build())
                .toList();
    }
}
