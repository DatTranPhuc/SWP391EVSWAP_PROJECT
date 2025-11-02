package evswap.swp391to4.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.entity.VehicleBatteryCompatibility;
import evswap.swp391to4.entity.VehicleBatteryId;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.StaffRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.VehicleBatteryCompatibilityRepository;
import evswap.swp391to4.repository.VehicleRepository;
import evswap.swp391to4.service.PaymentService;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final PasswordEncoder passwordEncoder;
    private final DriverRepository driverRepository;
    private final StaffRepository staffRepository;
    private final StationRepository stationRepository;
    private final VehicleRepository vehicleRepository;
    private final BatteryRepository batteryRepository;
    private final VehicleBatteryCompatibilityRepository compatibilityRepository;
    private final PaymentService paymentService;

    @Override
    public void run(String... args) throws Exception {
        // Station
        Station station = stationRepository.findAll().stream().findFirst().orElseGet(() -> {
            Station s = Station.builder()
                    .name("EVS-001 - District 5")
                    .address("123 Nguyen Van Cu, District 5, HCM")
                    .latitude(new BigDecimal("10.7769"))
                    .longitude(new BigDecimal("106.7009"))
                    .status("active")
                    .build();
            return stationRepository.save(s);
        });

        // Driver account
        String driverEmail = "driver1@test.com";
        String driverPass = "123456";
        Driver driver = driverRepository.findByEmail(driverEmail)
                .or(() -> driverRepository.findByPhone("0900000001"))
                .orElseGet(() -> {
                    Driver d = Driver.builder()
                            .email(driverEmail)
                            .passwordHash(passwordEncoder.encode(driverPass))
                            .fullName("Test Driver 1")
                            .phone("0900000001")
                            .emailVerified(true)
                            .createdAt(Instant.now())
                            .balance(BigDecimal.ZERO)
                            .build();
                    d = driverRepository.save(d);
                    // Seed initial wallet balance via successful payment record
                    paymentService.createPayment(d, null, new BigDecimal("200000"), "wallet", "succeed"); // 200,000 VND
                    return d;
                });

        // Vehicle for driver
        Vehicle vehicle = vehicleRepository.findByDriverDriverIdOrderByCreatedAtDesc(driver.getDriverId())
                .stream().findFirst().orElseGet(() -> {
                    Vehicle v = Vehicle.builder()
                            .driver(driver)
                            .vin("VIN-TEST-0001")
                            .plateNumber("59A1-000.01")
                            .model("MODEL-A")
                            .vehicleType("motorcycle")
                            .createdAt(Instant.now())
                            .build();
                    return vehicleRepository.save(v);
                });

        // Batteries at station
        if (batteryRepository.findAll().isEmpty()) {
            // Motorcycle batteries - MODEL-A
            Battery b1 = batteryRepository.save(Battery.builder()
                    .station(station)
                    .model("MODEL-A")
                    .vehicleType("motorcycle")
                    .state("full")
                    .sohPercent(90)
                    .socPercent(100)
                    .build());

            Battery b2 = batteryRepository.save(Battery.builder()
                    .station(station)
                    .model("MODEL-A")
                    .vehicleType("motorcycle")
                    .state("full")
                    .sohPercent(95)
                    .socPercent(100)
                    .build());

            Battery b3 = batteryRepository.save(Battery.builder()
                    .station(station)
                    .model("MODEL-A")
                    .vehicleType("motorcycle")
                    .state("charging")
                    .sohPercent(98)
                    .socPercent(50)
                    .build());

            // Car batteries - MODEL-B
            Battery b4 = batteryRepository.save(Battery.builder()
                    .station(station)
                    .model("MODEL-B")
                    .vehicleType("car")
                    .state("full")
                    .sohPercent(85)
                    .socPercent(100)
                    .build());

            Battery b5 = batteryRepository.save(Battery.builder()
                    .station(station)
                    .model("MODEL-B")
                    .vehicleType("car")
                    .state("full")
                    .sohPercent(82)
                    .socPercent(100)
                    .build());

            // Ineligible battery (SOH < 80%)
            Battery b6 = batteryRepository.save(Battery.builder()
                    .station(station)
                    .model("MODEL-A")
                    .vehicleType("motorcycle")
                    .state("full")
                    .sohPercent(70)
                    .socPercent(100)
                    .build());

            // Compatibility: motorcycle vehicle compatible with MODEL-A batteries
            compatibilityRepository.saveAll(List.of(
                    VehicleBatteryCompatibility.builder()
                            .id(new VehicleBatteryId(vehicle.getVehicleId(), b1.getBatteryId()))
                            .vehicle(vehicle)
                            .battery(b1)
                            .build(),
                    VehicleBatteryCompatibility.builder()
                            .id(new VehicleBatteryId(vehicle.getVehicleId(), b2.getBatteryId()))
                            .vehicle(vehicle)
                            .battery(b2)
                            .build()
            ));

            // Create a car vehicle for testing car batteries
            Vehicle carVehicle = vehicleRepository.findByDriverDriverIdOrderByCreatedAtDesc(driver.getDriverId())
                    .stream()
                    .filter(v -> "car".equals(v.getVehicleType()))
                    .findFirst()
                    .orElseGet(() -> {
                        Vehicle cv = Vehicle.builder()
                                .driver(driver)
                                .vin("VIN-CAR-0001")
                                .plateNumber("30A-12345")
                                .model("VinFast VF8")
                                .vehicleType("car")
                                .createdAt(Instant.now())
                                .build();
                        return vehicleRepository.save(cv);
                    });

            // Compatibility: car vehicle compatible with MODEL-B batteries
            compatibilityRepository.saveAll(List.of(
                    VehicleBatteryCompatibility.builder()
                            .id(new VehicleBatteryId(carVehicle.getVehicleId(), b4.getBatteryId()))
                            .vehicle(carVehicle)
                            .battery(b4)
                            .build(),
                    VehicleBatteryCompatibility.builder()
                            .id(new VehicleBatteryId(carVehicle.getVehicleId(), b5.getBatteryId()))
                            .vehicle(carVehicle)
                            .battery(b5)
                            .build()
            ));
        }

        // Staff account for this station
        String staffEmail = "staff1@test.com";
        String staffPass = "123456";
        staffRepository.findByEmail(staffEmail).orElseGet(() -> {
            Staff s = Staff.builder()
                    .station(station)
                    .fullName("Test Staff 1")
                    .email(staffEmail)
                    .passwordHash(passwordEncoder.encode(staffPass))
                    .isActive(true)
                    .build();
            return staffRepository.save(s);
        });

        System.out.println("Seeded test data:\n" +
                "- Driver: " + driverEmail + " / " + driverPass + " (wallet: 200,000 VND)\n" +
                "- Staff:  " + staffEmail + " / " + staffPass + " (station: " + station.getName() + ")\n" +
                "- Motorcycle: " + vehicle.getModel() + " (" + vehicle.getPlateNumber() + ")\n" +
                "- Station has 2 eligible motorcycle batteries (MODEL-A) and 2 eligible car batteries (MODEL-B)");
    }
}


