package evswap.swp391to4.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.entity.Vehicle;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import evswap.swp391to4.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleDetailService {

    private final VehicleRepository vehicleRepository;
    private final BatteryRepository batteryRepository;
    private final SwapTransactionRepository swapTransactionRepository;

    /**
     * Lấy thông tin chi tiết xe và pin của driver
     */
    public Map<String, Object> getVehicleDetail(Integer driverId) {
        try {
            // Lấy xe đầu tiên của driver (giả sử mỗi driver có 1 xe)
            List<Vehicle> vehicles = vehicleRepository.findAll().stream()
                .filter(v -> v.getDriver() != null && v.getDriver().getDriverId().equals(driverId))
                .toList();

            if (vehicles.isEmpty()) {
                return createDefaultVehicleDetail();
            }

            Vehicle vehicle = vehicles.get(0);
            
            // Lấy thông tin pin hiện tại (giả sử từ giao dịch đổi pin gần nhất)
            Map<String, Object> batteryInfo = getCurrentBatteryInfo(driverId);
            
            // Tạo response
            Map<String, Object> vehicleDetail = new HashMap<>();
            vehicleDetail.put("vehicleId", vehicle.getVehicleId());
            vehicleDetail.put("model", vehicle.getModel() != null ? vehicle.getModel() : "Chưa cập nhật");
            vehicleDetail.put("vin", vehicle.getVin() != null ? vehicle.getVin() : "Chưa cập nhật");
            vehicleDetail.put("plateNumber", vehicle.getPlateNumber() != null ? vehicle.getPlateNumber() : "Chưa cập nhật");
            vehicleDetail.put("registrationDate", formatRegistrationDate(vehicle.getCreatedAt()));
            vehicleDetail.put("status", "Hoạt động");
            
            // Thông tin pin
            vehicleDetail.put("batteryInfo", batteryInfo);
            
            return vehicleDetail;
        } catch (Exception e) {
            return createDefaultVehicleDetail();
        }
    }

    /**
     * Lấy thông tin pin hiện tại
     */
    private Map<String, Object> getCurrentBatteryInfo(Integer driverId) {
        try {
            // Lấy giao dịch đổi pin gần nhất để xác định pin hiện tại
            List<SwapTransaction> recentSwaps = swapTransactionRepository.findAll().stream()
                .filter(t -> t.getReservation() != null && 
                           t.getReservation().getDriver() != null &&
                           t.getReservation().getDriver().getDriverId().equals(driverId))
                .sorted((a, b) -> {
                    if (a.getSwappedAt() == null && b.getSwappedAt() == null) return 0;
                    if (a.getSwappedAt() == null) return 1;
                    if (b.getSwappedAt() == null) return -1;
                    return b.getSwappedAt().compareTo(a.getSwappedAt());
                })
                .toList();

            if (recentSwaps.isEmpty()) {
                return createDefaultBatteryInfo();
            }

            // Lấy pin từ giao dịch gần nhất
            SwapTransaction lastSwap = recentSwaps.get(0);
            if (lastSwap.getBatteryOut() != null) {
                return createBatteryInfoFromBattery(lastSwap.getBatteryOut());
            }

            return createDefaultBatteryInfo();
        } catch (Exception e) {
            return createDefaultBatteryInfo();
        }
    }

    /**
     * Tạo thông tin pin từ entity Battery
     */
    private Map<String, Object> createBatteryInfoFromBattery(Battery battery) {
        Map<String, Object> batteryInfo = new HashMap<>();
        batteryInfo.put("model", battery.getModel() != null ? battery.getModel() : "VinFast 48V-20Ah");
        batteryInfo.put("status", "Đang sử dụng");
        batteryInfo.put("sohPercent", battery.getSohPercent() != null ? battery.getSohPercent() : 98);
        batteryInfo.put("condition", getBatteryCondition(battery.getSohPercent()));
        return batteryInfo;
    }

    /**
     * Tạo thông tin pin mặc định
     */
    private Map<String, Object> createDefaultBatteryInfo() {
        Map<String, Object> batteryInfo = new HashMap<>();
        batteryInfo.put("model", "VinFast 48V-20Ah");
        batteryInfo.put("status", "Đang sử dụng");
        batteryInfo.put("sohPercent", 98);
        batteryInfo.put("condition", "Tuyệt vời");
        return batteryInfo;
    }

    /**
     * Tạo thông tin xe mặc định
     */
    private Map<String, Object> createDefaultVehicleDetail() {
        Map<String, Object> vehicleDetail = new HashMap<>();
        vehicleDetail.put("vehicleId", null);
        vehicleDetail.put("model", "Chưa cập nhật");
        vehicleDetail.put("vin", "Chưa cập nhật");
        vehicleDetail.put("plateNumber", "Chưa cập nhật");
        vehicleDetail.put("registrationDate", "Chưa cập nhật");
        vehicleDetail.put("status", "Chưa kích hoạt");
        vehicleDetail.put("batteryInfo", createDefaultBatteryInfo());
        return vehicleDetail;
    }

    /**
     * Xác định tình trạng pin dựa trên SOH
     */
    private String getBatteryCondition(Integer sohPercent) {
        if (sohPercent == null) return "Tuyệt vời";
        
        if (sohPercent >= 90) return "Tuyệt vời";
        if (sohPercent >= 80) return "Tốt";
        if (sohPercent >= 70) return "Khá";
        if (sohPercent >= 60) return "Trung bình";
        return "Cần thay thế";
    }

    /**
     * Format ngày đăng ký
     */
    private String formatRegistrationDate(Instant createdAt) {
        if (createdAt == null) return "Chưa cập nhật";
        
        try {
            LocalDate date = createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
            return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        } catch (Exception e) {
            return "Chưa cập nhật";
        }
    }
}
