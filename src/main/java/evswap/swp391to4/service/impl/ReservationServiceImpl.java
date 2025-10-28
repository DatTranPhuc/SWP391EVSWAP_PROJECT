package evswap.swp391to4.service.impl;

import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepo;
    private final DriverRepository driverRepo;
    private final StationRepository stationRepo;

    @Override
    @Transactional
    public Reservation createReservation(Integer driverId, Integer stationId, Instant reservedStart) {
        // ... (logic từ code cũ)
        // Nếu chưa viết logic hoặc chỉ muốn hết lỗi, trả về null/tạo dummy
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReservationSummary> getUpcomingReservations(Integer driverId) {
        // ... (logic từ code cũ)
        // Nếu chưa viết logic hoặc chỉ muốn hết lỗi, trả về mảng rỗng
        return new ArrayList<>();
    }
}
