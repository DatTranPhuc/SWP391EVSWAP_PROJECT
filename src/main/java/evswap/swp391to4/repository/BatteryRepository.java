package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatteryRepository extends JpaRepository<Battery, Integer> {

    /**
     * Tìm tất cả pin thuộc về một Station cụ thể.
     */
    List<Battery> findByStation(Station station);

    /**
     * Tìm pin theo trạm VÀ trạng thái (dùng cho tìm kiếm).
     */
    List<Battery> findByStationAndStateContainingIgnoreCase(Station station, String state);

    /**
     * Tìm pin theo ID (dù ID là duy nhất, trả về List cho đồng bộ).
     */
    List<Battery> findByStationAndBatteryId(Station station, Integer batteryId);

    /**
     * Tìm pin theo Model (dùng cho tìm kiếm).
     */
    List<Battery> findByStationAndModelContainingIgnoreCase(Station station, String model);

    // ===============================================
    // ===== CÁC HÀM MỚI ĐƯỢC THÊM VÀO =====
    // ===============================================

    /**
     * (HÀM MỚI 1 - CHO DASHBOARD)
     * Đếm số lượng pin tại 1 trạm theo 1 trạng thái cụ thể.
     * Nhanh hơn việc tải List về rồi .size().
     */
    long countByStationAndState(Station station, String state);

    /**
     * (HÀM MỚI 2 - CHO DASHBOARD)
     * Đếm TỔNG số pin tại 1 trạm.
     * Dùng cho Dashboard (thay vì getAllBatteriesForStation().size())
     */
    long countByStation(Station station);

    /**
     * (HÀM MỚI 3 - CHO SIMULATION)
     * Tìm tất cả pin theo trạng thái, KHÔNG PHÂN BIỆT TRẠM.
     * Dùng cho service chạy ngầm (BatterySimulationService)
     * để tìm tất cả pin "charging" trong hệ thống.
     */
    List<Battery> findByState(String state);
}