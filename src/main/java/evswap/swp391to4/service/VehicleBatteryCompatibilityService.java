package evswap.swp391to4.service;

import evswap.swp391to4.dto.VehicleBatteryCompatibilityResponse;
import evswap.swp391to4.dto.VehicleBatteryLinkRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.entity.VehicleBatteryCompatibility;
import evswap.swp391to4.entity.VehicleBatteryId;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.VehicleBatteryCompatibilityRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleBatteryCompatibilityService {

    private final VehicleBatteryCompatibilityRepository compatibilityRepository;
    private final VehicleRepository vehicleRepository;
    private final BatteryRepository batteryRepository;

    @Transactional
    public VehicleBatteryCompatibilityResponse link(VehicleBatteryLinkRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new IllegalArgumentException("Vehicle không tồn tại"));
        Battery battery = batteryRepository.findById(request.getBatteryId())
                .orElseThrow(() -> new IllegalArgumentException("Battery không tồn tại"));

        VehicleBatteryId id = new VehicleBatteryId(vehicle.getVehicleId(), battery.getBatteryId());
        if (compatibilityRepository.existsById(id)) {
            throw new IllegalStateException("Quan hệ tương thích đã tồn tại");
        }

        VehicleBatteryCompatibility compatibility = VehicleBatteryCompatibility.builder()
                .id(id)
                .vehicle(vehicle)
                .battery(battery)
                .build();

        VehicleBatteryCompatibility saved = compatibilityRepository.save(compatibility);
        return toResponse(saved);
    }

    @Transactional
    public void unlink(VehicleBatteryLinkRequest request) {
        VehicleBatteryId id = new VehicleBatteryId(request.getVehicleId(), request.getBatteryId());
        if (!compatibilityRepository.existsById(id)) {
            throw new IllegalArgumentException("Quan hệ tương thích không tồn tại");
        }
        compatibilityRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<VehicleBatteryCompatibilityResponse> listForVehicle(Integer vehicleId) {
        return compatibilityRepository.findByVehicleVehicleId(vehicleId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VehicleBatteryCompatibilityResponse> listForBattery(Integer batteryId) {
        return compatibilityRepository.findByBatteryBatteryId(batteryId).stream()
                .map(this::toResponse)
                .toList();
    }

    private VehicleBatteryCompatibilityResponse toResponse(VehicleBatteryCompatibility compatibility) {
        return VehicleBatteryCompatibilityResponse.builder()
                .vehicleId(compatibility.getVehicle() != null ? compatibility.getVehicle().getVehicleId() : null)
                .batteryId(compatibility.getBattery() != null ? compatibility.getBattery().getBatteryId() : null)
                .vehicleModel(compatibility.getVehicle() != null ? compatibility.getVehicle().getModel() : null)
                .batteryModel(compatibility.getBattery() != null ? compatibility.getBattery().getModel() : null)
                .build();
    }
}
