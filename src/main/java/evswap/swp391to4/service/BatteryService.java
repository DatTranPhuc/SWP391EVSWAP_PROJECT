package evswap.swp391to4.service;

import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;

import java.util.List;

public interface BatteryService {
    List<Battery> searchBatteriesForStation(Station station, String searchType, String searchTerm);
    List<Battery> getAllBatteriesForStation(Station station);
    void updateBatteryState(Integer batteryId, String newState, Staff staff);
    long countBatteriesByState(Station station, String state);
    void createBatteries(BatteryCreateRequest dto, Staff staff);
}
