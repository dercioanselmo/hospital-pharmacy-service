package mz.mva.pharmacy.repository;

import java.util.UUID;
import mz.mva.pharmacy.domain.PharmacyStore;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PharmacyStoreRepository extends JpaRepository<PharmacyStore, UUID> {
}
