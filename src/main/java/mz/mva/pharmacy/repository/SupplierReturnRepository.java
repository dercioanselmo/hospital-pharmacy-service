package mz.mva.pharmacy.repository;

import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.SupplierReturn;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierReturnRepository extends JpaRepository<SupplierReturn, UUID> {

    List<SupplierReturn> findAllByOrderByCreatedAtDesc();
}
