package evswap.swp391to4.service.impl;

import evswap.swp391to4.dto.BatteryCreateRequest;
import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Staff;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.service.BatteryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BatteryServiceImpl implements BatteryService {

    private final BatteryRepository batteryRepo;

    @Override
    @Transactional(readOnly = true)
    public List<Battery> searchBatteriesForStation(Station station, String searchType, String searchTerm) {
        // ... (logic từ code cũ)
        // Nếu chưa viết, trả về mảng rỗng để IDE không báo lỗi
        return new ArrayList<>();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Battery> getAllBatteriesForStation(Station station) {
        return batteryRepo.findByStation(station);
    }

    @Override
    @Transactional
    public void updateBatteryState(Integer batteryId, String newState, Staff staff) {
        // ... (logic từ code cũ)
        // Nếu chưa viết, không cần return ở void
    }

    @Override
    @Transactional(readOnly = true)
    public long countBatteriesByState(Station station, String state) {
        // ... (logic từ code cũ)
        // Nếu chưa viết, trả về mặc định để IDE không báo lỗi
        return 0L;
    }

    @Override
    @Transactional
    public void createBatteries(BatteryCreateRequest dto, Staff staff) {
        // ... (logic từ code cũ)
        // void nên không cần return
    }
}
