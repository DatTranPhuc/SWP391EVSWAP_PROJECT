package evswap.swp391to4.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import evswap.swp391to4.dto.FeedbackRequest;
import evswap.swp391to4.dto.FeedbackResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Feedback;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.FeedbackRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepo;
    private final StationRepository stationRepo;

    /**
     * Tạo feedback mới từ driver
     */
    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request, Driver driver) {
        // Kiểm tra station có tồn tại không
        Station station = stationRepo.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy trạm"));

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