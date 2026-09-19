package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.GrnLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrnLineRepository extends JpaRepository<GrnLine, UUID> {

    List<GrnLine> findByGrnId(UUID grnId);
}
