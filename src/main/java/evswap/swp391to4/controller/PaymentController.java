package evswap.swp391to4.controller;

import evswap.swp391to4.dto.PaymentRequest;
import evswap.swp391to4.dto.PaymentResponse;
import evswap.swp391to4.dto.PaymentStatusUpdateRequest;
import evswap.swp391to4.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> create(@RequestBody PaymentRequest request) {
        return ResponseEntity.status(201).body(paymentService.createPayment(request));
    }

    @PatchMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> updateStatus(@PathVariable Integer paymentId,
                                                        @RequestBody PaymentStatusUpdateRequest request) {
        return ResponseEntity.ok(paymentService.updateStatus(paymentId, request));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<PaymentResponse>> listForDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(paymentService.getPaymentsForDriver(driverId));
    }
}
