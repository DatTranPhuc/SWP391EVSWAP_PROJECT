package evswap.swp391to4.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.entity.VehicleBatteryCompatibility;
import evswap.swp391to4.entity.VehicleBatteryId;
import evswap.swp391to4.entity.VehicleType;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.VehicleBatteryCompatibilityRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final BatteryRepository batteryRepository;
    private final VehicleBatteryCompatibilityRepository compatibilityRepository;

    @Transactional
    public Vehicle addVehicleToDriver(Integer driverId, Vehicle vehicle) {
        var driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Tài khoản tài xế không tồn tại"));

        if (vehicle.getVin() == null || vehicle.getVin().isBlank()) {
            throw new IllegalArgumentException("Vui lòng cung cấp số VIN của phương tiện");
        }

        if (!Boolean.TRUE.equals(driver.getEmailVerified())) {
            throw new IllegalStateException("Vui lòng xác minh email trước khi thêm phương tiện");
        }

        if (vehicle.getVehicleType() == null) {
            throw new IllegalArgumentException("Vui lòng chọn loại phương tiện");
        }

        vehicleRepository.findByVin(vehicle.getVin())
                .ifPresent(existingVehicle -> {
                    if (existingVehicle.getDriver().getDriverId().equals(driverId)) {
                        throw new IllegalStateException("Phương tiện đã tồn tại trong tài khoản của bạn");
                    }
                    throw new IllegalStateException("Phương tiện đã được đăng ký bởi tài khoản khác");
                });

        if (vehicle.getPlateNumber() != null && !vehicle.getPlateNumber().isBlank()) {
            vehicleRepository.findByPlateNumber(vehicle.getPlateNumber())
                    .ifPresent(existingVehicle -> {
                        if (existingVehicle.getDriver().getDriverId().equals(driverId)) {
                            throw new IllegalStateException("Biển số đã tồn tại trong tài khoản của bạn");
                        }
                        throw new IllegalStateException("Biển số đã được đăng ký bởi tài khoản khác");
                    });
        }

        vehicle.setVehicleId(null);
        vehicle.setDriver(driver);
        vehicle.setCreatedAt(Instant.now());

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        assignDefaultCompatibility(savedVehicle);
        return savedVehicle;
    }

    @Transactional(readOnly = true)
    public List<Vehicle> getVehiclesForDriver(Integer driverId) {
        if (driverId == null) {
            throw new IllegalArgumentException("Thiếu thông tin tài khoản tài xế");
        }

        if (!driverRepository.existsById(driverId)) {
            throw new IllegalStateException("Tài khoản tài xế không tồn tại");
        }

        return vehicleRepository.findByDriverDriverIdOrderByCreatedAtDesc(driverId);
    }

    private void assignDefaultCompatibility(Vehicle vehicle) {
        VehicleType type = vehicle.getVehicleType();
        if (type == null) {
            return;
        }

        String targetModel = type.getDefaultBatteryModel();
        Battery representativeBattery = batteryRepository
                .findFirstByModelIgnoreCase(targetModel)
                .orElse(null);

        if (representativeBattery == null) {
            return;
        }

        boolean exists = compatibilityRepository
                .existsByVehicleVehicleIdAndBatteryModel(vehicle.getVehicleId(), representativeBattery.getModel());
        if (exists) {
            return;
        }

        VehicleBatteryCompatibility compatibility = VehicleBatteryCompatibility.builder()
                .id(new VehicleBatteryId(vehicle.getVehicleId(), representativeBattery.getBatteryId()))
                .vehicle(vehicle)
                .battery(representativeBattery)
                .build();

        compatibilityRepository.save(compatibility);
    }
}
