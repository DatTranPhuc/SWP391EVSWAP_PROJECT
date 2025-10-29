package evswap.swp391to4.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.dto.FeedbackRequest;
import evswap.swp391to4.dto.FeedbackResponse;
import evswap.swp391to4.dto.StationResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Feedback;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.FeedbackRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final StationRepository stationRepo;
    private final SwapTransactionRepository swapRepo;

    /**
     * Tạo feedback mới từ driver
     */
    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request, Driver driver) {
        // Kiểm tra station có tồn tại không
        Station station = stationRepo.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạm"));

        // Rule: chỉ cho phép tạo feedback khi có giao dịch swap trong 15 ngày
        Instant since = Instant.now().minus(15, ChronoUnit.DAYS);
        boolean allowed = swapRepo.existsByReservation_Driver_DriverIdAndSwappedAtAfter(driver.getDriverId(), since);
        if (!allowed) {
            throw new IllegalStateException("Chỉ tạo feedback khi có giao dịch trong 15 ngày");
        }

        // Rule: chỉ cho phép chọn trạm đã giao dịch trong 15 ngày
        List<Integer> recentStationIds = swapRepo.findByReservation_Driver_DriverIdAndSwappedAtAfter(driver.getDriverId(), since)
                .stream().map(tx -> tx.getStation().getStationId()).distinct().toList();
        if (!recentStationIds.contains(station.getStationId())) {
            throw new IllegalStateException("Chỉ có thể chọn trạm đã giao dịch trong 15 ngày");
        }

        // Kiểm tra rating hợp lệ
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");
        }

        // Tạo feedback entity
        Feedback feedback = Feedback.builder()
                .driver(driver)
                .station(station)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(Instant.now())
                .build();

        Feedback saved = feedbackRepo.save(feedback);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StationResponse> getRecentStationsForDriver(Integer driverId) {
        Instant since = Instant.now().minus(15, ChronoUnit.DAYS);
        return swapRepo.findByReservation_Driver_DriverIdAndSwappedAtAfter(driverId, since)
                .stream()
                .map(tx -> tx.getStation())
                .distinct()
                .map(this::toStationResponse)
                .toList();
    }

    private StationResponse toStationResponse(Station station) {
        return StationResponse.builder()
                .stationId(station.getStationId())
                .name(station.getName())
                .address(station.getAddress())
                .status(station.getStatus())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .build();
    }

    /**
     * Lấy tất cả feedback (cho admin)
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getAllFeedback() {
        List<Feedback> feedbackList = feedbackRepo.findAllByOrderByCreatedAtDesc();
        List<FeedbackResponse> responseList = new ArrayList<>();
        
        for (Feedback feedback : feedbackList) {
            responseList.add(mapToResponse(feedback));
        }
        
        return responseList;
    }

    /**
     * Lấy feedback của một driver cụ thể
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByDriver(Integer driverId) {
        List<Feedback> feedbackList = feedbackRepo.findByDriverDriverId(driverId);
        List<FeedbackResponse> responseList = new ArrayList<>();
        
        for (Feedback feedback : feedbackList) {
            responseList.add(mapToResponse(feedback));
        }
        
        return responseList;
    }

    /**
     * Lấy feedback của một trạm cụ thể
     */
    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackByStationId(Long stationId) {
        List<Feedback> feedbackList = feedbackRepo.findByStationStationIdOrderByCreatedAtDesc(stationId);
        List<FeedbackResponse> responseList = new ArrayList<>();
        
        for (Feedback feedback : feedbackList) {
            responseList.add(mapToResponse(feedback));
        }
        
        return responseList;
    }

    /**
     * Cập nhật feedback (chỉ driver sở hữu mới được cập nhật)
     */
    @Transactional
    public FeedbackResponse updateFeedback(Integer feedbackId, FeedbackRequest request, Integer driverId) {
        Feedback feedback = feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy feedback"));

        // Kiểm tra quyền sở hữu
        if (!feedback.getDriver().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Bạn không có quyền cập nhật feedback này");
        }

        // Kiểm tra station có tồn tại không
        Station station = stationRepo.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạm"));

        // Kiểm tra rating hợp lệ
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating phải từ 1 đến 5");
        }

        // Cập nhật thông tin
        feedback.setStation(station);
        feedback.setRating(request.getRating());
        feedback.setComment(request.getComment());

        Feedback saved = feedbackRepo.save(feedback);
        return mapToResponse(saved);
    }

    /**
     * Xóa feedback (chỉ driver sở hữu mới được xóa)
     */
    @Transactional
    public void deleteFeedback(Integer feedbackId, Integer driverId) {
        Feedback feedback = feedbackRepo.findById(feedbackId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy feedback"));

        // Kiểm tra quyền sở hữu
        if (!feedback.getDriver().getDriverId().equals(driverId)) {
            throw new IllegalStateException("Bạn không có quyền xóa feedback này");
        }

        feedbackRepo.delete(feedback);
    }

    /**
     * Helper method để map Entity sang DTO
     */
    private FeedbackResponse mapToResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .driverId(feedback.getDriver().getDriverId())
                .driverName(feedback.getDriver().getFullName())
                .stationId(feedback.getStation().getStationId())
                .stationName(feedback.getStation().getName())
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}