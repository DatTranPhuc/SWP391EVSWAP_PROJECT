package evswap.swp391to4.controller;

import evswap.swp391to4.dto.SwapTransactionRequest;
import evswap.swp391to4.dto.SwapTransactionResponse;
import evswap.swp391to4.service.SwapTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swaps")
@RequiredArgsConstructor
public class SwapTransactionController {

    private final SwapTransactionService swapTransactionService;

    @PostMapping
    public ResponseEntity<SwapTransactionResponse> record(@RequestBody SwapTransactionRequest request) {
        return ResponseEntity.status(201).body(swapTransactionService.recordSwap(request));
    }

    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<SwapTransactionResponse> getByReservation(@PathVariable Integer reservationId) {
        return ResponseEntity.ok(swapTransactionService.getByReservation(reservationId));
    }

    @GetMapping("/station/{stationId}")
    public ResponseEntity<List<SwapTransactionResponse>> listByStation(@PathVariable Integer stationId) {
        return ResponseEntity.ok(swapTransactionService.listByStation(stationId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<SwapTransactionResponse>> listByDriver(@PathVariable Integer driverId) {
        return ResponseEntity.ok(swapTransactionService.listByDriver(driverId));
    }
}
