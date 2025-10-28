package evswap.swp391to4.service;

import evswap.swp391to4.entity.Vehicle;

import java.util.List;

public interface VehicleService {
    Vehicle addVehicleToDriver(Integer driverId, Vehicle vehicle);
    List<Vehicle> getVehiclesForDriver(Integer driverId);
}
