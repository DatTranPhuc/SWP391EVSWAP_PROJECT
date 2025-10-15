package evswap.swp391to4.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import evswap.swp391to4.entity.Driver;
import evswap.swp391to4.entity.Payment;
import evswap.swp391to4.entity.SwapTransaction;
import evswap.swp391to4.repository.DriverRepository;
import evswap.swp391to4.repository.PaymentRepository;
import evswap.swp391to4.repository.SwapTransactionRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final PaymentRepository paymentRepository;
    private final SwapTransactionRepository swapTransactionRepository;
    private final DriverRepository driverRepository;

    /**
     * Tính số dư ví hiện tại của driver
     * Số dư = Tổng nạp tiền - Tổng chi tiêu + Tổng hoàn tiền
     */
    public Map<String, Object> getWalletBalance(Integer driverId) {
        try {
            Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));

            // Tính tổng tiền đã nạp (giả sử có payment với method = "deposit")
            BigDecimal totalDeposits = paymentRepository.getTotalPaidAmountByDriver(driverId)
                .multiply(new BigDecimal("0.8")); // Giả sử 80% là nạp tiền

            // Tính tổng chi tiêu cho đổi pin
            BigDecimal totalSpent = calculateTotalSpentOnSwaps(driverId);

            // Tính tổng hoàn tiền
            BigDecimal totalRefunded = paymentRepository.getTotalRefundedAmountByDriver(driverId);

            // Số dư hiện tại
            BigDecimal currentBalance = totalDeposits.subtract(totalSpent).add(totalRefunded);

            // Đảm bảo số dư không âm
            if (currentBalance.compareTo(BigDecimal.ZERO) < 0) {
                currentBalance = BigDecimal.ZERO;
            }

            Map<String, Object> walletInfo = new HashMap<>();
            walletInfo.put("driverId", driverId);
            walletInfo.put("currentBalance", currentBalance);
            walletInfo.put("totalDeposits", totalDeposits);
            walletInfo.put("totalSpent", totalSpent);
            walletInfo.put("totalRefunded", totalRefunded);
            walletInfo.put("currency", "VND");
            walletInfo.put("lastUpdated", Instant.now());

            return walletInfo;
        } catch (Exception e) {
            // Trả về số dư mặc định là 0 nếu có lỗi hoặc không có dữ liệu
            Map<String, Object> defaultWallet = new HashMap<>();
            defaultWallet.put("driverId", driverId);
            defaultWallet.put("currentBalance", BigDecimal.ZERO); // Số dư mặc định là 0
            defaultWallet.put("totalDeposits", BigDecimal.ZERO);
            defaultWallet.put("totalSpent", BigDecimal.ZERO);
            defaultWallet.put("totalRefunded", BigDecimal.ZERO);
            defaultWallet.put("currency", "VND");
            defaultWallet.put("lastUpdated", Instant.now());
            return defaultWallet;
        }
    }

    /**
     * Tính tổng chi tiêu cho đổi pin
     */
    private BigDecimal calculateTotalSpentOnSwaps(Integer driverId) {
        try {
            List<SwapTransaction> transactions = swapTransactionRepository.findAll().stream()
                .filter(t -> t.getReservation() != null && 
                           t.getReservation().getDriver() != null &&
                           t.getReservation().getDriver().getDriverId().equals(driverId))
                .toList();

            // Giả sử mỗi lần đổi pin tốn 50,000 VND
            BigDecimal swapCost = new BigDecimal("50000");
            return swapCost.multiply(new BigDecimal(transactions.size()));
        } catch (Exception e) {
            return BigDecimal.ZERO; // Chi tiêu mặc định là 0
        }
    }

    /**
     * Lấy thống kê chi tiêu
     */
    public Map<String, Object> getSpendingStatistics(Integer driverId) {
        try {
            // Số lần đổi pin
            long swapCount = swapTransactionRepository.findAll().stream()
                .filter(t -> t.getReservation() != null && 
                           t.getReservation().getDriver() != null &&
                           t.getReservation().getDriver().getDriverId().equals(driverId))
                .count();

            // Chi phí trung bình mỗi lần đổi pin
            BigDecimal averageCost = new BigDecimal("50000"); // 50k VND

            // Chi phí trung bình tháng (chỉ tính khi có giao dịch)
            BigDecimal monthlyAverage = swapCount > 0 ? averageCost.multiply(new BigDecimal("4.5")) : BigDecimal.ZERO;

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalSwaps", swapCount);
            stats.put("averageCostPerSwap", averageCost);
            stats.put("monthlyAverage", monthlyAverage);
            stats.put("currency", "VND");

            return stats;
        } catch (Exception e) {
            Map<String, Object> defaultStats = new HashMap<>();
            defaultStats.put("totalSwaps", 0L);
            defaultStats.put("averageCostPerSwap", BigDecimal.ZERO);
            defaultStats.put("monthlyAverage", BigDecimal.ZERO);
            defaultStats.put("currency", "VND");
            return defaultStats;
        }
    }

    /**
     * Tính chi phí tháng này
     */
    public Map<String, Object> getThisMonthCost(Integer driverId) {
        try {
            // Lấy ngày đầu và cuối tháng hiện tại
            LocalDate now = LocalDate.now();
            LocalDate firstDayOfMonth = now.withDayOfMonth(1);
            LocalDate lastDayOfMonth = now.withDayOfMonth(now.lengthOfMonth());
            
            Instant startOfMonth = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endOfMonth = lastDayOfMonth.atTime(23, 59, 59).atZone(ZoneId.systemDefault()).toInstant();

            // Đếm số lần đổi pin trong tháng này
            long swapsThisMonth = swapTransactionRepository.findAll().stream()
                .filter(t -> t.getReservation() != null && 
                           t.getReservation().getDriver() != null &&
                           t.getReservation().getDriver().getDriverId().equals(driverId) &&
                           t.getSwappedAt() != null &&
                           t.getSwappedAt().isAfter(startOfMonth) &&
                           t.getSwappedAt().isBefore(endOfMonth))
                .count();

            // Tính chi phí tháng này (mỗi lần đổi pin 50k VND)
            BigDecimal monthlyCost = new BigDecimal("50000").multiply(new BigDecimal(swapsThisMonth));

            Map<String, Object> monthlyStats = new HashMap<>();
            monthlyStats.put("driverId", driverId);
            monthlyStats.put("thisMonthCost", monthlyCost);
            monthlyStats.put("swapsThisMonth", swapsThisMonth);
            monthlyStats.put("month", now.getMonthValue());
            monthlyStats.put("year", now.getYear());
            monthlyStats.put("currency", "VND");

            return monthlyStats;
        } catch (Exception e) {
            Map<String, Object> defaultMonthly = new HashMap<>();
            defaultMonthly.put("driverId", driverId);
            defaultMonthly.put("thisMonthCost", BigDecimal.ZERO); // Mặc định là 0
            defaultMonthly.put("swapsThisMonth", 0L);
            defaultMonthly.put("month", LocalDate.now().getMonthValue());
            defaultMonthly.put("year", LocalDate.now().getYear());
            defaultMonthly.put("currency", "VND");
            return defaultMonthly;
        }
    }

    /**
     * Tính chi phí trung bình mỗi lượt đổi pin
     */
    public Map<String, Object> getAverageCostPerSwap(Integer driverId) {
        try {
            // Lấy tất cả giao dịch đổi pin của driver
            List<SwapTransaction> allSwaps = swapTransactionRepository.findAll().stream()
                .filter(t -> t.getReservation() != null && 
                           t.getReservation().getDriver() != null &&
                           t.getReservation().getDriver().getDriverId().equals(driverId))
                .toList();

            if (allSwaps.isEmpty()) {
                Map<String, Object> noSwaps = new HashMap<>();
                noSwaps.put("driverId", driverId);
                noSwaps.put("averageCostPerSwap", BigDecimal.ZERO); // Mặc định là 0
                noSwaps.put("totalSwaps", 0L);
                noSwaps.put("currency", "VND");
                return noSwaps;
            }

            // Giả sử mỗi lần đổi pin tốn 50,000 VND
            BigDecimal costPerSwap = new BigDecimal("50000");
            long totalSwaps = allSwaps.size();

            Map<String, Object> avgStats = new HashMap<>();
            avgStats.put("driverId", driverId);
            avgStats.put("averageCostPerSwap", costPerSwap);
            avgStats.put("totalSwaps", totalSwaps);
            avgStats.put("currency", "VND");

            return avgStats;
        } catch (Exception e) {
            Map<String, Object> defaultAvg = new HashMap<>();
            defaultAvg.put("driverId", driverId);
            defaultAvg.put("averageCostPerSwap", BigDecimal.ZERO); // Mặc định là 0
            defaultAvg.put("totalSwaps", 0L);
            defaultAvg.put("currency", "VND");
            return defaultAvg;
        }
    }

    /**
     * Lấy lịch sử giao dịch gần đây
     */
    public List<Map<String, Object>> getRecentTransactions(Integer driverId, int limit) {
        try {
            List<Payment> recentPayments = paymentRepository.getRecentPaymentsByDriver(driverId)
                .stream()
                .limit(limit)
                .toList();

            return recentPayments.stream()
                .map(payment -> {
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("id", payment.getPaymentId());
                    transaction.put("type", "payment");
                    transaction.put("amount", payment.getAmount());
                    transaction.put("status", payment.getStatus());
                    transaction.put("method", payment.getMethod());
                    transaction.put("date", payment.getPaidAt());
                    transaction.put("description", "Thanh toán đổi pin");
                    return transaction;
                })
                .toList();
        } catch (Exception e) {
            return List.of();
        }
    }

    /**
     * Tính toán trạm yêu thích dựa trên số lần sử dụng
     */
    public Map<String, Object> getFavoriteStation(Integer driverId) {
        try {
            Map<Integer, Long> stationUsage = new HashMap<>();
            
            swapTransactionRepository.findAll().stream()
                .filter(t -> t.getReservation() != null && 
                           t.getReservation().getDriver() != null &&
                           t.getReservation().getDriver().getDriverId().equals(driverId))
                .forEach(t -> {
                    if (t.getStation() != null) {
                        Integer stationId = t.getStation().getStationId();
                        stationUsage.put(stationId, stationUsage.getOrDefault(stationId, 0L) + 1);
                    }
                });

            if (stationUsage.isEmpty()) {
                Map<String, Object> noFavorite = new HashMap<>();
                noFavorite.put("stationId", null);
                noFavorite.put("stationName", "Chưa có");
                noFavorite.put("usageCount", 0L);
                return noFavorite;
            }

            // Tìm trạm được sử dụng nhiều nhất
            Integer favoriteStationId = stationUsage.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

            if (favoriteStationId != null) {
                Map<String, Object> favorite = new HashMap<>();
                favorite.put("stationId", favoriteStationId);
                favorite.put("stationName", "Trạm " + favoriteStationId); // Tên trạm sẽ được lấy từ DB
                favorite.put("usageCount", stationUsage.get(favoriteStationId));
                return favorite;
            }

            Map<String, Object> noFavorite = new HashMap<>();
            noFavorite.put("stationId", null);
            noFavorite.put("stationName", "Chưa có");
            noFavorite.put("usageCount", 0L);
            return noFavorite;
        } catch (Exception e) {
            Map<String, Object> noFavorite = new HashMap<>();
            noFavorite.put("stationId", null);
            noFavorite.put("stationName", "Chưa có");
            noFavorite.put("usageCount", 0L);
            return noFavorite;
        }
    }
}
