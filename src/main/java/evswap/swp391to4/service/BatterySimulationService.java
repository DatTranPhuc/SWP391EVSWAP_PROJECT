package evswap.swp391to4.service; // (Gói của bạn)

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.repository.BatteryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatterySimulationService {

    private final BatteryRepository batteryRepo;

    // ----- CẤU HÌNH GIẢ LẬP (SIÊU NHANH ĐỂ TEST) -----

    /**
     * Tốc độ sạc: 20%
     * (Vì hàm chạy mỗi 1 giây, 100% / 20% = 5 giây)
     */
    private static final int CHARGE_RATE = 20; // <-- ĐÃ SỬA

    /**
     * Tốc độ giảm SOH (vẫn giữ nguyên)
     */
    private static final int SOH_DEGRADATION_RATE = 1;
    private static final int MAINTENANCE_SOH_THRESHOLD = 80;
    // ---------------------------------------------------


    /**
     * Hàm này sẽ tự động chạy mỗi 1 GIÂY (1000 ms).
     */
    @Transactional
    @Scheduled(fixedRate = 1000) // <-- ĐÃ SỬA (1000ms = 1 giây)
    public void simulateBatteryCharging() {

        // 1. Tìm tất cả pin đang "charging"
        List<Battery> chargingBatteries = batteryRepo.findByState("charging");

        if (chargingBatteries.isEmpty()) {
            return; // Không có gì để làm
        }

        log.info("[SIMULATION] Found {} batteries to charge.", chargingBatteries.size());

        for (Battery battery : chargingBatteries) {

            int currentSoc = battery.getSocPercent();
            int newSoc = currentSoc + CHARGE_RATE;

            if (newSoc >= 100) {
                // 2. NẾU PIN SẠC ĐẦY 100%

                // (Chỉ cập nhật nếu chưa đầy, tránh lặp lại log)
                if (currentSoc < 100) {
                    battery.setSocPercent(100);
                    battery.setState("full"); // -> Đổi trạng thái sang "full"

                    // 3. Giả lập hao mòn (SOH)
                    int currentSoh = battery.getSohPercent();
                    int newSoh = currentSoh - SOH_DEGRADATION_RATE;
                    battery.setSohPercent(newSoh);

                    log.info("[SIMULATION] Battery #{} is now FULL. New SOH: {}%",
                            battery.getBatteryId(), newSoh);

                    // 4. Kiểm tra SOH
                    if (newSoh < MAINTENANCE_SOH_THRESHOLD) {
                        battery.setState("maintenance");
                        log.warn("[SIMULATION] Battery #{} SOH is low ({}%). Moved to MAINTENANCE.",
                                battery.getBatteryId(), newSoh);
                    }
                }

            } else {
                // 5. NẾU PIN CHƯA ĐẦY
                battery.setSocPercent(newSoc); // Cập nhật SOC mới (ví dụ: 0 -> 20, 20 -> 40)
            }
        }

        // 6. Lưu tất cả thay đổi vào Database
        batteryRepo.saveAll(chargingBatteries);
    }
}