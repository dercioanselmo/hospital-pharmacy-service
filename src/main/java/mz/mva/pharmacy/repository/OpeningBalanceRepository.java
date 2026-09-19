package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.OpeningBalance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OpeningBalanceRepository extends JpaRepository<OpeningBalance, UUID> {

    List<OpeningBalance> findAllByOrderByCreatedAtDesc();
}
