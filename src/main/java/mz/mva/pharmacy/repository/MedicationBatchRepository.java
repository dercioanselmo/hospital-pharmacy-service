package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mz.mva.pharmacy.domain.MedicationBatch;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationBatchRepository extends JpaRepository<MedicationBatch, UUID> {

    List<MedicationBatch> findByMedicationIdOrderByExpiryDateAsc(UUID medicationId);

    List<MedicationBatch> findByStoreIdOrderByExpiryDateAsc(UUID storeId);

    Optional<MedicationBatch> findByMedicationIdAndStoreIdAndBatchNumber(UUID medicationId, UUID storeId, String batchNumber);
}
