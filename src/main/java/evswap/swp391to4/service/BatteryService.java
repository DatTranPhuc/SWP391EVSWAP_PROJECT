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
     * Tìm kiếm pin tại trạm
     */
    @Transactional(readOnly = true)
    public List<Battery> searchBatteriesForStation(Station station, String searchType, String searchTerm) {
        // (Giữ nguyên code của bạn, đã tốt)
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

    /**
     * Lấy TẤT CẢ pin tại trạm
     */
    @Transactional(readOnly = true)
    public List<Battery> getAllBatteriesForStation(Station station) {
        return batteryRepo.findByStation(station);
    }

    /**
     * Cập nhật trạng thái thủ công (VD: Maintenance, Retired)
     */
    @Transactional
    public void updateBatteryState(Integer batteryId, String newState, Staff staff) {
        Battery battery = batteryRepo.findById(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pin với ID: " + batteryId));

        if (!battery.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("Bạn không có quyền sửa pin không thuộc trạm của mình.");
        }

        List<String> validStates = List.of("maintenance", "retired");
        if (!validStates.contains(newState.toLowerCase())) {
            throw new IllegalStateException("Bạn chỉ có thể cập nhật trạng thái thủ công thành 'maintenance' hoặc 'retired'.");
        }

        battery.setState(newState.toLowerCase());
        batteryRepo.save(battery);
    }

    /**
     * Bắt đầu sạc một pin (do Staff yêu cầu).
     * (SỬA LẠI ĐỂ DÙNG INTEGER)
     */
    @Transactional
    public void startChargingBattery(Integer batteryId, Staff staff) {
        Battery battery = batteryRepo.findById(batteryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy pin với ID: " + batteryId));

        // 1. Kiểm tra quyền
        if (!battery.getStation().getStationId().equals(staff.getStation().getStationId())) {
            throw new IllegalStateException("Bạn không có quyền sửa pin không thuộc trạm của mình.");
        }

        // 2. Kiểm tra logic
        String currentState = battery.getState();
        if (!currentState.equals("maintenance") && !currentState.equals("retired")) {
            throw new IllegalStateException("Chỉ có thể bắt đầu sạc cho pin đang ở trạng thái 'maintenance' hoặc 'retired'. Pin này đang '" + currentState + "'.");
        }

        // 3. "Bật công tắc": Đặt trạng thái và reset SOC
        battery.setState("charging");
        battery.setSocPercent(0); // <-- Sửa thành số nguyên (Integer)

        batteryRepo.save(battery);
    }

    /**
     * Đếm pin theo trạng thái (Dùng cho Dashboard)
     */
    @Transactional(readOnly = true)
    public long countBatteriesByState(Station station, String state) {
        if (state == null || state.isBlank()) {
            return 0;
        }
        return batteryRepo.countByStationAndState(station, state);
    }


    /**
     * Tạo pin mới (SỬA LẠI ĐỂ DÙNG INTEGER)
     * Giả định BatteryCreateRequest của bạn cũng dùng Integer
     */
    @Transactional
    public void createBatteries(BatteryCreateRequest dto, Staff staff) {
        Station staffStation = staff.getStation();
        if (staffStation == null) {
            throw new IllegalStateException("Tài khoản staff của bạn chưa được gán trạm.");
        }

        List<String> validStates = List.of("charging", "maintenance", "full");
        if (!validStates.contains(dto.getState().toLowerCase())) {
            throw new IllegalArgumentException("Trạng thái ban đầu không hợp lệ.");
        }

        // (Sửa) Kiểm tra SOH/SOC (dưới dạng Integer)
        if (dto.getSohPercent() < 0 || dto.getSohPercent() > 100) {
            throw new IllegalArgumentException("SOH phải ở trong khoảng 0 đến 100");
        }
        if (dto.getSocPercent() < 0 || dto.getSocPercent() > 100) {
            throw new IllegalArgumentException("SOC phải ở trong khoảng 0 đến 100");
        }

        List<Battery> newBatteries = new ArrayList<>();
        int quantity = dto.getQuantity();

        for (int i = 0; i < quantity; i++) {
            Battery newBattery = Battery.builder()
                    .model(dto.getModel())
                    .station(staffStation)
                    .state(dto.getState().toLowerCase())
                    // (Sửa) Đảm bảo DTO truyền vào là Integer
                    .sohPercent(dto.getSohPercent())
                    .socPercent(dto.getSocPercent())
                    .build();
            newBatteries.add(newBattery);
        }

        batteryRepo.saveAll(newBatteries);
    }
}