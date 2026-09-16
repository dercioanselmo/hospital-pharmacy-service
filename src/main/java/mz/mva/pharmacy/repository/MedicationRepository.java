package mz.mva.pharmacy.repository;

import java.util.UUID;
import mz.mva.pharmacy.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, UUID> {
}
