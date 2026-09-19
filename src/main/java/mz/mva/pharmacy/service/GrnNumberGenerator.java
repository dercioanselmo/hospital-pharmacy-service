package mz.mva.pharmacy.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.Year;
import org.springframework.stereotype.Component;

/** Generates GRN numbers like "GRN-2026-000123" from a DB sequence — same pattern as EncounterNumberGenerator. */
@Component
public class GrnNumberGenerator {

    @PersistenceContext
    private EntityManager entityManager;

    public String next() {
        Number nextValue = (Number) entityManager.createNativeQuery("SELECT nextval('grn_number_seq')").getSingleResult();
        return "GRN-" + Year.now() + "-" + String.format("%06d", nextValue.longValue());
    }
}
