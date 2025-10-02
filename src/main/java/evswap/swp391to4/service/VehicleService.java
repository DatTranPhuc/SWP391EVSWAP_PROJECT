package evswap.swp391to4.service;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;

    @Transactional
    public Vehicle addVehicleToDriver(Integer driverId, Vehicle vehicle) {
        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new IllegalStateException("Tài khoản tài xế không tồn tại"));

        if (vehicle.getVin() == null || vehicle.getVin().isBlank()) {
            throw new IllegalArgumentException("Vui lòng cung cấp số VIN của phương tiện");
        }

        if (!Boolean.TRUE.equals(driver.getEmailVerified())) {
            throw new IllegalStateException("Vui lòng xác minh email trước khi thêm phương tiện");
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

        return vehicleRepository.save(vehicle);
    }
}
