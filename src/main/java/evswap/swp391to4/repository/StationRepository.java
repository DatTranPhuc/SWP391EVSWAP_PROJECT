package evswap.swp391to4.repository;

import evswap.swp391to4.entity.Station;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StationRepository extends JpaRepository<Station, Integer> {

    // 🔍 Tìm trạm theo tên chính xác (bỏ qua hoa thường)
    Optional<Station> findByNameIgnoreCase(String name);

    // 🔍 Tìm các trạm có tên chứa keyword (bỏ qua hoa thường)
    List<Station> findByNameContainingIgnoreCase(String keyword);

    // 🔍 Tìm theo trạng thái (active / closed)
    List<Station> findByStatus(String status);

    long countByStatusIgnoreCase(String status);
}
