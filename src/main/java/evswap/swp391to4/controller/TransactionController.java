package evswap.swp391to4.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.entity.Battery;
import evswap.swp391to4.entity.Reservation;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.repository.BatteryRepository;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.ReservationRepository;
import evswap.swp391to4.repository.StationRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final SwapTransactionRepository swapTransactionRepository;
    private final ReservationRepository reservationRepository;
    private final BatteryRepository batteryRepository;
    private final DriverRepository driverRepository;
    private final StationRepository stationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTransactions(@RequestHeader("Authorization") String authHeader) {
        try {
            List<SwapTransaction> transactions = swapTransactionRepository.findAll();
            List<Map<String, Object>> transactionList = transactions.stream()
                .map(transaction -> {
                    Map<String, Object> transactionData = new HashMap<>();
                    transactionData.put("transactionId", transaction.getSwapId());
                    transactionData.put("result", transaction.getResult());
                    transactionData.put("swappedAt", transaction.getSwappedAt());
                    
                    if (transaction.getReservation() != null) {
                        transactionData.put("reservationId", transaction.getReservation().getReservationId());
                        if (transaction.getReservation().getDriver() != null) {
                            transactionData.put("driverId", transaction.getReservation().getDriver().getDriverId());
                            transactionData.put("driverName", transaction.getReservation().getDriver().getFullName());
                        }
                    }
                    
                    if (transaction.getStation() != null) {
                        transactionData.put("stationId", transaction.getStation().getStationId());
                        transactionData.put("stationName", transaction.getStation().getName());
                    }
                    
                    if (transaction.getBatteryIn() != null) {
                        transactionData.put("batteryInId", transaction.getBatteryIn().getBatteryId());
                        transactionData.put("batteryInModel", transaction.getBatteryIn().getModel());
                    }
                    
                    if (transaction.getBatteryOut() != null) {
                        transactionData.put("batteryOutId", transaction.getBatteryOut().getBatteryId());
                        transactionData.put("batteryOutModel", transaction.getBatteryOut().getModel());
                    }
                    
                    return transactionData;
                })
                .collect(Collectors.toList());
            
            Map<String, Object> response = new HashMap<>();
            response.put("transactions", transactionList);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<Map<String, Object>> getTransaction(@PathVariable Integer transactionId,
                                                             @RequestHeader("Authorization") String authHeader) {
        try {
            SwapTransaction transaction = swapTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

            Map<String, Object> transactionData = new HashMap<>();
            transactionData.put("transactionId", transaction.getSwapId());
            transactionData.put("result", transaction.getResult());
            transactionData.put("swappedAt", transaction.getSwappedAt());
            
            if (transaction.getReservation() != null) {
                transactionData.put("reservationId", transaction.getReservation().getReservationId());
                if (transaction.getReservation().getDriver() != null) {
                    transactionData.put("driverId", transaction.getReservation().getDriver().getDriverId());
                    transactionData.put("driverName", transaction.getReservation().getDriver().getFullName());
                }
            }
            
            if (transaction.getStation() != null) {
                transactionData.put("stationId", transaction.getStation().getStationId());
                transactionData.put("stationName", transaction.getStation().getName());
            }
            
            if (transaction.getBatteryIn() != null) {
                transactionData.put("batteryInId", transaction.getBatteryIn().getBatteryId());
                transactionData.put("batteryInModel", transaction.getBatteryIn().getModel());
                transactionData.put("batteryInSoc", transaction.getBatteryIn().getSocPercent());
            }
            
            if (transaction.getBatteryOut() != null) {
                transactionData.put("batteryOutId", transaction.getBatteryOut().getBatteryId());
                transactionData.put("batteryOutModel", transaction.getBatteryOut().getModel());
                transactionData.put("batteryOutSoc", transaction.getBatteryOut().getSocPercent());
            }

            return ResponseEntity.ok(transactionData);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/swap")
    public ResponseEntity<Map<String, Object>> performSwap(@RequestHeader("Authorization") String authHeader,
                                                          @RequestBody Map<String, Object> request) {
        try {
            Integer batteryId = (Integer) request.get("batteryId");
            Integer reservationId = (Integer) request.get("reservationId");

            if (reservationId == null) {
                throw new RuntimeException("Reservation ID is required for swap");
            }

            Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

            Battery battery = batteryRepository.findById(batteryId)
                .orElseThrow(() -> new RuntimeException("Battery not found"));

            // Create swap transaction
            SwapTransaction transaction = SwapTransaction.builder()
                .reservation(reservation)
                .station(battery.getStation())
                .batteryIn(battery)
                .result("success")
                .swappedAt(Instant.now())
                .build();

            SwapTransaction savedTransaction = swapTransactionRepository.save(transaction);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transaction", Map.of(
                "transactionId", savedTransaction.getSwapId(),
                "result", savedTransaction.getResult(),
                "swappedAt", savedTransaction.getSwappedAt()
            ));
            response.put("battery", Map.of(
                "batteryId", battery.getBatteryId(),
                "model", battery.getModel(),
                "socPercent", battery.getSocPercent()
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
