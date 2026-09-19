package mz.mva.pharmacy.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Year;
import org.springframework.stereotype.Component;

@Component
public class SupplierReturnNumberGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    public String next() {
        Number nextValue =
                (Number) entityManager.createNativeQuery("SELECT nextval('supplier_return_number_seq')").getSingleResult();
        return "RET-" + Year.now() + "-" + String.format("%06d", nextValue.longValue());
    }
}
