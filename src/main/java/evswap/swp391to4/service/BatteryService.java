package evswap.swp391to4.service;

import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.BatteryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatteryService {

    private final BatteryRepository batteryRepo;

    /**
     * Search batteries within a station by type (ID, model, state) and term.
     * Returns all batteries if no search term or type is provided.
     */
    @Transactional(readOnly = true)
    public List<Battery> searchBatteriesForStation(Station station, String searchType, String searchTerm) {
        if (searchTerm == null || searchTerm.isBlank() || searchType == null || searchType.isBlank()) {
            return batteryRepo.findByStation(station); // Return all if no search
        }
        switch (searchType) {
            case "id":
                try {
                    Integer id = Integer.parseInt(searchTerm);
                    return batteryRepo.findByStationAndBatteryId(station, id); // Find by ID
                } catch (NumberFormatException e) {
                    return Collections.emptyList(); // Return empty if ID is not a number
                }
            case "model":
                return batteryRepo.findByStationAndModelContainingIgnoreCase(station, searchTerm); // Find by model
            case "state":
                return batteryRepo.findByStationAndStateContainingIgnoreCase(station, searchTerm); // Find by state
            default:
                return batteryRepo.findByStation(station); // Default to all if type is unknown
        }
    }

    /**
     * Get ALL batteries for a specific station. Used for totals and error loading.
     */
    @Transactional(readOnly = true)
    public List<Battery> getAllBatteriesForStation(Station station) {
        return batteryRepo.findByStation(station);
    }

    /**
     * Update the state of a battery (charging, maintenance, retired).
     * Includes security check to ensure staff manages the battery's station.
     */
    @Transactional
    public void updateBatteryState(Integer batteryId, String newState, Staff staff) {
        Battery battery = batteryRepo.findById(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find battery with ID: " + batteryId));

        // Security Check: Staff must belong to the battery's station
        if (!battery.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("You do not have permission to modify batteries in other stations.");
        }

        // Validate the new state
        List<String> validStates = List.of("charging", "maintenance", "retired"); // Staff cannot manually set 'full'
        if (!validStates.contains(newState.toLowerCase())) {
            throw new IllegalArgumentException("Invalid target state: " + newState);
        }

        battery.setState(newState.toLowerCase());
        batteryRepo.save(battery);
    }

    /**
     * Count batteries in a station by a specific state.
     */
    @Transactional(readOnly = true)
    public long countBatteriesByState(Station station, String state) {
        if (state == null || state.isBlank()) {
            return 0; // Or throw an error if state is mandatory
        }
        return batteryRepo.countByStationAndState(station, state);
    }

    /**
     * Create multiple batteries in bulk based on the DTO request.
     * All created batteries belong to the staff's station.
     */
    @Transactional
    public void createBatteries(BatteryCreateRequest dto, Staff staff) {
        Station staffStation = staff.getStation();
        if (staffStation == null) {
            throw new IllegalStateException("Your staff account is not assigned to any station.");
        }

        // Validate initial state (cannot be 'retired' on creation)
        List<String> validInitialStates = List.of("charging", "maintenance", "full");
        if (!validInitialStates.contains(dto.getState().toLowerCase())) {
            throw new IllegalArgumentException("Invalid initial state for new battery.");
        }

        List<Battery> newBatteries = new ArrayList<>();
        int quantity = dto.getQuantity();

        // Create batteries in a loop
        for (int i = 0; i < quantity; i++) {
            Battery newBattery = Battery.builder()
                    .model(dto.getModel())
                    .station(staffStation) // Assign to staff's station
                    .state(dto.getState().toLowerCase())
                    .sohPercent(dto.getSohPercent())
                    .socPercent(dto.getSocPercent())
                    .build();
            newBatteries.add(newBattery);
        }

        // Save all created batteries at once
        batteryRepo.saveAll(newBatteries);
    }

    /**
     * Simulate IoT action: Mark a battery as fully charged ('full' state, SOC 100%).
     * Only works if the battery is currently 'charging'.
     */
    @Transactional
    public void markBatteryAsFull(Integer batteryId, Staff staff) {
        Battery battery = batteryRepo.findById(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find battery."));

        // Security Check
        if (!battery.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("Cannot operate on batteries from other stations.");
        }

        // Business Logic Check: Only 'charging' batteries can be marked 'full'
        if (!"charging".equalsIgnoreCase(battery.getState())) {
            throw new IllegalStateException("Only batteries in 'Charging' state can be marked as 'Full'.");
        }

        // Update state and SOC
        battery.setState("full");
        battery.setSocPercent(100);
        batteryRepo.save(battery);
    }

    /**
     * Simulate battery usage: Set a new SOC percentage.
     * If the battery was 'full', its state changes to 'charging'.
     */
    @Transactional
    public void simulateBatteryUsage(Integer batteryId, int newSocPercent, Staff staff) {
        Battery battery = batteryRepo.findById(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find battery."));

        // Security Check
        if (!battery.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("Cannot operate on batteries from other stations.");
        }

        // Validate SOC range
        if (newSocPercent < 0 || newSocPercent > 100) {
            throw new IllegalArgumentException("SOC must be between 0 and 100.");
        }

        // Business Logic: If battery was 'full' and SOC drops, it should become 'charging'
        if ("full".equalsIgnoreCase(battery.getState()) && newSocPercent < 100) {
            battery.setState("charging");
        }

        // Update SOC
        battery.setSocPercent(newSocPercent);
        batteryRepo.save(battery);
    }
}