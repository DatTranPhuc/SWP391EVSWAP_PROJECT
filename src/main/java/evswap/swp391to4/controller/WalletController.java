package evswap.swp391to4.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import evswap.swp391to4.service.WalletService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Lấy thông tin ví của driver
     */
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getWalletBalance(@RequestHeader("Authorization") String authHeader) {
        try {
            // Extract driver ID from token (for demo, we'll use driver ID 1)
            Integer driverId = 1; // In production, extract from JWT token
            
            Map<String, Object> walletInfo = walletService.getWalletBalance(driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("wallet", walletInfo);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy thống kê chi tiêu
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getSpendingStatistics(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer driverId = 1; // In production, extract from JWT token
            
            Map<String, Object> stats = walletService.getSpendingStatistics(driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("statistics", stats);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy lịch sử giao dịch
     */
    @GetMapping("/transactions")
    public ResponseEntity<Map<String, Object>> getRecentTransactions(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            Integer driverId = 1; // In production, extract from JWT token
            
            List<Map<String, Object>> transactions = walletService.getRecentTransactions(driverId, limit);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("transactions", transactions);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy trạm yêu thích
     */
    @GetMapping("/favorite-station")
    public ResponseEntity<Map<String, Object>> getFavoriteStation(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer driverId = 1; // In production, extract from JWT token
            
            Map<String, Object> favoriteStation = walletService.getFavoriteStation(driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favoriteStation", favoriteStation);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy chi phí tháng này
     */
    @GetMapping("/this-month-cost")
    public ResponseEntity<Map<String, Object>> getThisMonthCost(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer driverId = 1; // In production, extract from JWT token
            
            Map<String, Object> monthlyCost = walletService.getThisMonthCost(driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("monthlyCost", monthlyCost);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy chi phí trung bình mỗi lượt đổi pin
     */
    @GetMapping("/average-cost-per-swap")
    public ResponseEntity<Map<String, Object>> getAverageCostPerSwap(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer driverId = 1; // In production, extract from JWT token
            
            Map<String, Object> avgCost = walletService.getAverageCostPerSwap(driverId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("averageCost", avgCost);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Lấy tổng hợp thông tin dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer driverId = 1; // In production, extract from JWT token
            
            // Lấy tất cả thông tin cần thiết
            Map<String, Object> walletInfo = walletService.getWalletBalance(driverId);
            Map<String, Object> stats = walletService.getSpendingStatistics(driverId);
            Map<String, Object> favoriteStation = walletService.getFavoriteStation(driverId);
            Map<String, Object> monthlyCost = walletService.getThisMonthCost(driverId);
            Map<String, Object> avgCostPerSwap = walletService.getAverageCostPerSwap(driverId);
            
            // Tạo response tổng hợp
            Map<String, Object> dashboardData = new HashMap<>();
            dashboardData.put("currentBalance", walletInfo.get("currentBalance"));
            dashboardData.put("totalSwaps", stats.get("totalSwaps"));
            dashboardData.put("favoriteStation", favoriteStation.get("stationName"));
            dashboardData.put("averageCost", stats.get("monthlyAverage"));
            dashboardData.put("thisMonthCost", monthlyCost.get("thisMonthCost"));
            dashboardData.put("averageCostPerSwap", avgCostPerSwap.get("averageCostPerSwap"));
            dashboardData.put("currency", "VND");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dashboard", dashboardData);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
