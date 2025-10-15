package evswap.swp391to4.controller;

import evswap.swp391to4.dto.FeedbackRequest;
import evswap.swp391to4.dto.FeedbackResponse;
import evswap.swp391to4.service.FeedbackService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@RequestBody FeedbackRequest request) {
        return ResponseEntity.status(201).body(feedbackService.createFeedback(request));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<FeedbackResponse>> listForStation(@PathVariable Integer stationId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForStation(stationId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<FeedbackResponse>> listForDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(feedbackService.getFeedbackForDriver(driverId));
    }
}
