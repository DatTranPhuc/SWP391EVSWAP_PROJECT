package evswap.swp391to4.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.dto.AvailableBatteryResponse;
import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BatteryService {

    private final BatteryRepository batteryRepo;
    private final VehicleRepository vehicleRepo;

    @Transactional(readOnly = true)
    public List<Battery> searchBatteriesForStation(Station station, String searchType, String searchTerm) {
        // (Code của bạn đã rất tốt, giữ nguyên)
        if (searchTerm == null || searchTerm.isBlank() || searchType == null || searchType.isBlank()) {
            return batteryRepo.findByStation(station);
        }
        switch (searchType) {
            case "id":
                try {
                    Integer id = Integer.parseInt(searchTerm);
                    return batteryRepo.findByStationAndBatteryId(station, id);
                } catch (NumberFormatException e) {
                    return Collections.emptyList();
                }
            case "model":
                return batteryRepo.findByStationAndModelContainingIgnoreCase(station, searchTerm);
            case "state":
                return batteryRepo.findByStationAndStateContainingIgnoreCase(station, searchTerm);
            default:
                return batteryRepo.findByStation(station);
        }
    }

    // ===============================================
    // ===== HÀM MỚI (BỊ THIẾU) ĐƯỢC THÊM VÀO =====
    // ===============================================
    /**
     * Lấy TẤT CẢ pin tại trạm (dùng cho đếm tổng và load lỗi)
     */
    @Transactional(readOnly = true)
    public List<Battery> getAllBatteriesForStation(Station station) {
        return batteryRepo.findByStation(station);
    }
    // ===============================================


    @Transactional
    public void updateBatteryState(Integer batteryId, String newState, Staff staff) {
        // (Code của bạn đã rất tốt, giữ nguyên)
        Battery battery = batteryRepo.findById(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pin với ID: " + batteryId));

        if (!battery.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("Bạn không có quyền sửa pin không thuộc trạm của mình.");
        }

        List<String> validStates = List.of("full", "charging", "maintenance", "retired");
        if (!validStates.contains(newState.toLowerCase())) {
            throw new IllegalArgumentException("Trạng thái mới không hợp lệ: " + newState);
        }

        battery.setState(newState.toLowerCase());
        batteryRepo.save(battery);
    }

    @Transactional(readOnly = true)
    public long countBatteriesByState(Station station, String state) {
        // (Code của bạn đã rất tốt, giữ nguyên)
        if (state == null || state.isBlank()) {
            return 0;
        }
        return batteryRepo.countByStationAndState(station, state);
    }

    @Transactional
    public void createBatteries(BatteryCreateRequest dto, Staff staff) {
        // (Code của bạn đã rất tốt, giữ nguyên)
        Station staffStation = staff.getStation();
        if (staffStation == null) {
            throw new IllegalStateException("Tài khoản staff của bạn chưa được gán trạm.");
        }

        List<String> validStates = List.of("charging", "maintenance", "full");
        if (!validStates.contains(dto.getState().toLowerCase())) {
            throw new IllegalArgumentException("Trạng thái ban đầu không hợp lệ.");
        }

        List<Battery> newBatteries = new ArrayList<>();
        int quantity = dto.getQuantity();

        for (int i = 0; i < quantity; i++) {
            Battery newBattery = Battery.builder()
                    .model(dto.getModel())
                    .vehicleType(dto.getVehicleType())
                    .station(staffStation)
                    .state(dto.getState().toLowerCase())
                    .sohPercent(dto.getSohPercent())
                    .socPercent(dto.getSocPercent())
                    .build();
            newBatteries.add(newBattery);
        }

        batteryRepo.saveAll(newBatteries);
    }

    /**
     * Tìm pin đủ điều kiện cho xe tại trạm
     * - state = "full"
     * - soc = 100%
     * - soh >= 80%
     * - vehicleType khớp với xe
     */
    @Transactional(readOnly = true)
    public List<AvailableBatteryResponse> findEligibleBatteriesForVehicle(Integer stationId, Integer vehicleId) {
        // Lấy thông tin xe để lấy vehicleType
        Vehicle vehicle = vehicleRepo.findById(vehicleId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy xe với ID: " + vehicleId));
        
        String vehicleType = vehicle.getVehicleType();
        
        // Lấy danh sách pin tại trạm với điều kiện cơ bản
        List<Battery> eligibleBatteries = batteryRepo.findByStationStationIdAndStateAndSocPercentAndSohPercentGreaterThanEqual(
            stationId, "full", 100, 80);

        // Lọc theo vehicleType khớp
        List<AvailableBatteryResponse> result = new ArrayList<>();
        for (Battery battery : eligibleBatteries) {
            if (vehicleType != null && vehicleType.equalsIgnoreCase(battery.getVehicleType())) {
                result.add(AvailableBatteryResponse.builder()
                    .batteryId(battery.getBatteryId())
                    .model(battery.getModel())
                    .sohPercent(battery.getSohPercent())
                    .socPercent(battery.getSocPercent())
                    .state(battery.getState())
                    .stationName(battery.getStation().getName())
                    .stationAddress(battery.getStation().getAddress())
                    .build());
            }
        }
        
        return result;
    }

    /**
     * Đánh dấu pin đã được đặt (reserve)
     * Thêm trường reservedForReservationId vào Battery entity nếu cần
     */
    @Transactional
    public void reserveBattery(Integer batteryId, Integer reservationId) {
        Battery battery = batteryRepo.findById(batteryId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pin với ID: " + batteryId));
        
        // Kiểm tra pin có đủ điều kiện không
        if (!"full".equals(battery.getState()) || 
            battery.getSocPercent() != 100 || 
            battery.getSohPercent() < 80) {
            throw new IllegalStateException("Pin không đủ điều kiện để đặt");
        }
        
        // Đánh dấu pin đã được reserve (có thể thêm trường reservedForReservationId)
        // Hiện tại chỉ log để tracking, có thể extend Battery entity sau
        System.out.println("Pin #" + batteryId + " đã được đặt cho reservation #" + reservationId);
        
        // TODO: Thêm trường reservedForReservationId vào Battery entity để track reservation
        // battery.setReservedForReservationId(reservationId);
        // batteryRepo.save(battery);
    }

    @Transactional(readOnly = true)
    public Battery getBatteryById(Integer batteryId) {
        return batteryRepo.findById(batteryId).orElse(null);
    }

    /**
     * Đếm số pin khả dụng cho loại xe cụ thể (motorcycle/car) tại trạm
     * Pin khả dụng: state="full", SOC=100%, SOH>=80%, và vehicleType khớp
     */
    @Transactional(readOnly = true)
    public Integer countAvailableBatteriesByVehicleType(Integer stationId, String vehicleType) {
        if (vehicleType == null || vehicleType.isBlank()) {
            return 0;
        }

        // Lấy tất cả pin đủ điều kiện cơ bản và loại xe tại trạm
        List<Battery> eligibleBatteries = batteryRepo.findByStationStationIdAndStateAndSocPercentAndSohPercentGreaterThanEqual(
            stationId, "full", 100, 80);

        // Count batteries matching this vehicle type
        int count = 0;
        for (Battery battery : eligibleBatteries) {
            if (vehicleType.equalsIgnoreCase(battery.getVehicleType())) {
                count++;
            }
        }
        
        return count;
    }
}