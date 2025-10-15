package evswap.swp391to4.service;

import evswap.swp391to4.dto.FeedbackRequest;
import evswap.swp391to4.dto.FeedbackResponse;
import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Feedback;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.FeedbackRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final DriverRepository driverRepository;
    private final StationRepository stationRepository;

    @Transactional
    public FeedbackResponse createFeedback(FeedbackRequest request) {
        Driver driver = driverRepository.findById(request.getDriverId())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản tài xế không tồn tại"));
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new IllegalArgumentException("Station không tồn tại"));

        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1-5");
        }

        Feedback feedback = Feedback.builder()
                .driver(driver)
                .station(station)
                .rating(request.getRating())
                .comment(request.getComment())
                .createdAt(Instant.now())
                .build();

        Feedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackForStation(Integer stationId) {
        return feedbackRepository.findByStationStationIdOrderByCreatedAtDesc(stationId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> getFeedbackForDriver(Integer driverId) {
        return feedbackRepository.findByDriverDriverIdOrderByCreatedAtDesc(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    private FeedbackResponse toResponse(Feedback feedback) {
        return FeedbackResponse.builder()
                .feedbackId(feedback.getFeedbackId())
                .driverId(feedback.getDriver() != null ? feedback.getDriver().getDriverId() : null)
                .driverName(feedback.getDriver() != null ? feedback.getDriver().getFullName() : null)
                .stationId(feedback.getStation() != null ? feedback.getStation().getStationId() : null)
                .stationName(feedback.getStation() != null ? feedback.getStation().getName() : null)
                .rating(feedback.getRating())
                .comment(feedback.getComment())
                .createdAt(feedback.getCreatedAt())
                .build();
    }
}
