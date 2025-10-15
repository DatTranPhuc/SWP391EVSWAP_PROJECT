package evswap.swp391to4.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import evswap.swp391to4.entity.SwapTransaction;

@Repository
public interface SwapTransactionRepository extends JpaRepository<SwapTransaction, Integer> {
}
