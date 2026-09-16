package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.DispensingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DispensingRecordRepository extends JpaRepository<DispensingRecord, UUID> {

    List<DispensingRecord> findByPrescriptionOrderId(UUID prescriptionOrderId);
}
