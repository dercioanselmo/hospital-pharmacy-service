package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mz.mva.pharmacy.domain.PrescriptionOrder;
import mz.mva.pharmacy.domain.PrescriptionOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionOrderRepository extends JpaRepository<PrescriptionOrder, UUID> {

    Optional<PrescriptionOrder> findByClinicalOrderId(UUID clinicalOrderId);

    List<PrescriptionOrder> findByStatusOrderByCreatedAtAsc(PrescriptionOrderStatus status);

    List<PrescriptionOrder> findByPatientIdOrderByCreatedAtAsc(UUID patientId);
}
