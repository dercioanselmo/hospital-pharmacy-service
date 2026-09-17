package mz.mva.pharmacy.service;

import java.util.UUID;
import mz.mva.pharmacy.client.PatientOwnershipClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Row-level security (flagged since Phase 9g): a PATIENT-role caller may only
 * ever see their own prescription orders. A caller with no linked patient
 * record (every staff role) is unaffected.
 */
@Component
public class PatientOwnershipGuard {

    private final PatientOwnershipClient patientOwnershipClient;

    public PatientOwnershipGuard(PatientOwnershipClient patientOwnershipClient) {
        this.patientOwnershipClient = patientOwnershipClient;
    }

    public void enforce(String bearerToken, UUID subjectUserId, UUID requestedPatientId) {
        patientOwnershipClient.findOwnPatientId(bearerToken, subjectUserId).ifPresent(ownPatientId -> {
            if (!ownPatientId.equals(requestedPatientId)) {
                throw new AccessDeniedException("Patients may only access their own record");
            }
        });
    }
}
