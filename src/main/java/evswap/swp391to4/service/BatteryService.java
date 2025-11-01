package evswap.swp391to4.service;

import evswap.swp391to4.dto.AvailableBatteryResponse;
import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.entity.VehicleType;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BatteryService {

    private final BatteryRepository batteryRepo;
    private final VehicleRepository vehicleRepository;

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

        if (battery.getReservedForReservationId() != null) {
            throw new IllegalStateException(
                    "Pin đang được giữ cho đặt lịch #" + battery.getReservedForReservationId() + ". Không thể cập nhật thủ công.");
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
        return batteryRepo.countByStationAndStateAndReservedForReservationIdIsNull(station, state);
    }

    @Transactional(readOnly = true)
    public long countReservedBatteries(Station station) {
        return batteryRepo.countByStationAndReservedForReservationIdIsNotNull(station);
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
     * - Tương thích với xe
     */
    @Transactional(readOnly = true)
    public List<AvailableBatteryResponse> findEligibleBatteriesForVehicle(Integer stationId, Integer vehicleId) {
        // Lấy danh sách pin tại trạm với điều kiện cơ bản
        List<Battery> eligibleBatteries =
                batteryRepo.findByStationStationIdAndStateAndSocPercentAndSohPercentGreaterThanEqualAndReservedForReservationIdIsNull(
                        stationId, "full", 100, 80);

        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy xe"));
        VehicleType type = vehicle.getVehicleType() == null ? VehicleType.UNIVERSAL : vehicle.getVehicleType();
        List<String> compatibleModels = type.getCompatibleBatteryModels();

        List<AvailableBatteryResponse> result = new ArrayList<>();
        for (Battery battery : eligibleBatteries) {
            boolean isCompatible = compatibleModels.isEmpty() ||
                    compatibleModels.stream().anyMatch(model -> model.equalsIgnoreCase(battery.getModel()));

            if (isCompatible) {
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
    public Battery reserveBattery(Integer batteryId, Integer reservationId) {
        Battery battery = batteryRepo.findById(batteryId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pin với ID: " + batteryId));

        // Kiểm tra pin có đủ điều kiện không
        if (!"full".equalsIgnoreCase(battery.getState()) ||
            battery.getSocPercent() == null || battery.getSocPercent() != 100 ||
            battery.getSohPercent() == null || battery.getSohPercent() < 80) {
            throw new IllegalStateException("Pin không đủ điều kiện để đặt");
        }

        if (battery.getReservedForReservationId() != null &&
                !battery.getReservedForReservationId().equals(reservationId)) {
            throw new IllegalStateException(
                    "Pin đang được giữ cho đặt lịch khác (#" + battery.getReservedForReservationId() + ")");
        }

        if (battery.getReservedForReservationId() != null) {
            return battery;
        }

        battery.setReservedForReservationId(reservationId);
        battery.setReservedAt(Instant.now());

        return batteryRepo.save(battery);
    }

    @Transactional(readOnly = true)
    public Battery getBatteryById(Integer batteryId) {
        return batteryRepo.findById(batteryId).orElse(null);
    }

    @Transactional
    public void releaseReservationHold(Integer reservationId) {
        List<Battery> reserved = batteryRepo.findByReservedForReservationId(reservationId);
        if (reserved.isEmpty()) {
            return;
        }

        for (Battery battery : reserved) {
            battery.setReservedForReservationId(null);
            battery.setReservedAt(null);
        }

        batteryRepo.saveAll(reserved);
    }

    @Transactional(readOnly = true)
    public Optional<Battery> suggestEligibleBattery(Station station, Vehicle vehicle) {
        if (station == null || vehicle == null) {
            return Optional.empty();
        }

        VehicleType type = vehicle.getVehicleType() == null ? VehicleType.UNIVERSAL : vehicle.getVehicleType();

        return batteryRepo.findByStation(station).stream()
                .filter(battery -> battery.getReservedForReservationId() == null)
                .filter(battery -> "full".equalsIgnoreCase(battery.getState()))
                .filter(battery -> battery.getSocPercent() != null && battery.getSocPercent() == 100)
                .filter(battery -> battery.getSohPercent() != null && battery.getSohPercent() >= 80)
                .filter(battery -> {
                    List<String> compatibleModels = type.getCompatibleBatteryModels();
                    return compatibleModels.isEmpty() ||
                            compatibleModels.stream().anyMatch(model -> model.equalsIgnoreCase(battery.getModel()));
                })
                .findFirst();
    }
}