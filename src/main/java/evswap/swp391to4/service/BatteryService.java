package evswap.swp391to4.service;

import evswap.swp391to4.dto.BatteryRequest;
import evswap.swp391to4.dto.BatteryResponse;
import evswap.swp391to4.dto.BatteryUpdateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BatteryService {

    private final BatteryRepository batteryRepository;
    private final StationRepository stationRepository;

    @Transactional
    public BatteryResponse createBattery(BatteryRequest request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

        Battery battery = Battery.builder()
                .station(station)
                .model(request.getModel())
                .state(request.getState() != null ? request.getState() : "full")
                .sohPercent(request.getSohPercent())
                .socPercent(request.getSocPercent())
                .build();

        Battery saved = batteryRepository.save(battery);
        return toResponse(saved);
    }

    @Transactional
    public BatteryResponse updateBattery(Integer batteryId, BatteryUpdateRequest request) {
        Battery battery = batteryRepository.findById(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("Battery không tồn tại"));

        if (request.getStationId() != null && (battery.getStation() == null
                || !request.getStationId().equals(battery.getStation().getStationId()))) {
            Station newStation = stationRepository.findById(request.getStationId())
                    .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));
            battery.setStation(newStation);
        }

        if (request.getState() != null) {
            battery.setState(request.getState());
        }
        if (request.getSohPercent() != null) {
            battery.setSohPercent(request.getSohPercent());
        }
        if (request.getSocPercent() != null) {
            battery.setSocPercent(request.getSocPercent());
        }

        Battery updated = batteryRepository.save(battery);
        return toResponse(updated);
    }

    @Transactional(readOnly = true)
    public List<BatteryResponse> listAll() {
        return batteryRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BatteryResponse> listByStation(Integer stationId) {
        return batteryRepository.findByStationStationId(stationId).stream()
                .map(this::toResponse)
                .toList();
    }

    private BatteryResponse toResponse(Battery battery) {
        return BatteryResponse.builder()
                .batteryId(battery.getBatteryId())
                .stationId(battery.getStation() != null ? battery.getStation().getStationId() : null)
                .stationName(battery.getStation() != null ? battery.getStation().getName() : null)
                .model(battery.getModel())
                .state(battery.getState())
                .sohPercent(battery.getSohPercent())
                .socPercent(battery.getSocPercent())
                .build();
    }
}
