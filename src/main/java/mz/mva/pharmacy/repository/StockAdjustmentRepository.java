package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.StockAdjustment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockAdjustmentRepository extends JpaRepository<StockAdjustment, UUID> {

    List<StockAdjustment> findAllByOrderByCreatedAtDesc();
}
