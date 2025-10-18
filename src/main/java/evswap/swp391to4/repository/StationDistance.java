package evswap.swp391to4.repository;

import java.math.BigDecimal;

/**
 * Đây là một "Projection Interface".
 * Nó định nghĩa cấu trúc dữ liệu mà câu lệnh native query trong StationRepository sẽ trả về.
 * Các tên phương thức getter (getStationId, getName,...) phải khớp với tên các cột
 * được định nghĩa trong câu lệnh SELECT (AS stationId, AS name,...).
 */
public interface StationDistance {
    Integer getStationId();
    String getName();
    String getAddress();
    String getStatus();
    BigDecimal getLatitude();
    BigDecimal getLongitude();
    Double getDistance();
}