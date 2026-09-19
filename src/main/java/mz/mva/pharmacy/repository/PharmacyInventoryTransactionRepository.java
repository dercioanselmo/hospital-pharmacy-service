package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.PharmacyInventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyInventoryTransactionRepository extends JpaRepository<PharmacyInventoryTransaction, UUID> {

    List<PharmacyInventoryTransaction> findByMedicationIdOrderByCreatedAtDesc(UUID medicationId);

    List<PharmacyInventoryTransaction> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);
}
