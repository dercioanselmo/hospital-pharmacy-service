package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.SupplierReturnLine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierReturnLineRepository extends JpaRepository<SupplierReturnLine, UUID> {

    List<SupplierReturnLine> findBySupplierReturnId(UUID supplierReturnId);
}
