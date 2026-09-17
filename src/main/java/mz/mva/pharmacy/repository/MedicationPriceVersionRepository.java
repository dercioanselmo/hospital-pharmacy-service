package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.MedicationPriceVersion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationPriceVersionRepository extends JpaRepository<MedicationPriceVersion, UUID> {

    List<MedicationPriceVersion> findByMedicationIdOrderByEffectiveFromDesc(UUID medicationId);
}
