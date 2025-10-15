package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Feedback;
import evswap.swp391to4.entity.Station;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.FeedbackRepository;
import evswap.swp391to4.repository.StationRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final DriverRepository driverRepository;
    private final StationRepository stationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getFeedbacks(@RequestHeader("Authorization") String authHeader) {
        try {
            List<Feedback> feedbacks = feedbackRepository.findAll();
            List<Map<String, Object>> feedbackList = feedbacks.stream()
                .map(feedback -> {
                    Map<String, Object> feedbackData = new HashMap<>();
                    feedbackData.put("feedbackId", feedback.getFeedbackId());
                    feedbackData.put("rating", feedback.getRating());
                    feedbackData.put("comment", feedback.getComment());
                    feedbackData.put("createdAt", feedback.getCreatedAt());
                    
                    if (feedback.getDriver() != null) {
                        feedbackData.put("driverId", feedback.getDriver().getDriverId());
                        feedbackData.put("driverName", feedback.getDriver().getFullName());
                    }
                    
                    if (feedback.getStation() != null) {
                        feedbackData.put("stationId", feedback.getStation().getStationId());
                        feedbackData.put("stationName", feedback.getStation().getName());
                    }
                    
                    return feedbackData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("feedbacks", feedbackList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createFeedback(@RequestHeader("Authorization") String authHeader,
                                                             @RequestBody Map<String, Object> request) {
        try {
            Integer rating = (Integer) request.get("rating");
            String comment = (String) request.get("comment");
            Integer stationId = (Integer) request.get("stationId");
            Integer driverId = (Integer) request.get("driverId");

            if (driverId == null) {
                driverId = 1; // Default driver for demo
            }

            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            Station station = null;
            if (stationId != null) {
                station = stationRepository.findById(stationId)
                    .orElse(null);
            }

            Feedback feedback = Feedback.builder()
                .rating(rating)
                .comment(comment)
                .driver(driver)
                .station(station)
                .createdAt(Instant.now())
                .build();

            Feedback savedFeedback = feedbackRepository.save(feedback);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("feedback", Map.of(
                "feedbackId", savedFeedback.getFeedbackId(),
                "rating", savedFeedback.getRating(),
                "comment", savedFeedback.getComment()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{feedbackId}")
    public ResponseEntity<Map<String, Object>> getFeedback(@PathVariable Integer feedbackId,
                                                          @RequestHeader("Authorization") String authHeader) {
        try {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

            Map<String, Object> feedbackData = new HashMap<>();
            feedbackData.put("feedbackId", feedback.getFeedbackId());
            feedbackData.put("rating", feedback.getRating());
            feedbackData.put("comment", feedback.getComment());
            feedbackData.put("createdAt", feedback.getCreatedAt());
            
            if (feedback.getDriver() != null) {
                feedbackData.put("driverId", feedback.getDriver().getDriverId());
                feedbackData.put("driverName", feedback.getDriver().getFullName());
            }
            
            if (feedback.getStation() != null) {
                feedbackData.put("stationId", feedback.getStation().getStationId());
                feedbackData.put("stationName", feedback.getStation().getName());
            }

            return ResponseEntity.ok(feedbackData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{feedbackId}")
    public ResponseEntity<Map<String, Object>> updateFeedback(@PathVariable Integer feedbackId,
                                                             @RequestHeader("Authorization") String authHeader,
                                                             @RequestBody Map<String, Object> request) {
        try {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

            Integer rating = (Integer) request.get("rating");
            String comment = (String) request.get("comment");

            if (rating != null) {
                feedback.setRating(rating);
            }
            if (comment != null) {
                feedback.setComment(comment);
            }

            feedbackRepository.save(feedback);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Feedback updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/{feedbackId}")
    public ResponseEntity<Map<String, Object>> deleteFeedback(@PathVariable Integer feedbackId,
                                                             @RequestHeader("Authorization") String authHeader) {
        try {
            Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

            feedbackRepository.delete(feedback);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Feedback deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
