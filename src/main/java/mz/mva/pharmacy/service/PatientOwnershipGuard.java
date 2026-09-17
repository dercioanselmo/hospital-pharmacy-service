package mz.mva.pharmacy.service;

import java.util.UUID;
import mz.mva.pharmacy.client.PatientOwnershipClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

/**
 * Row-level security (flagged since Phase 9g): a PATIENT-role caller may only
 * ever see their own prescription orders. A caller with no linked patient record (every
 * staff role) is unaffected.
 */
@Component
public class PatientOwnershipGuard {

    private final PatientOwnershipClient patientOwnershipClient;

    public PatientOwnershipGuard(PatientOwnershipClient patientOwnershipClient) {
        this.patientOwnershipClient = patientOwnershipClient;
    }

    /**
     * {@code subject} is the JWT subject claim — a real end-user's identity
     * {@code userId} (a UUID) for a normal request, but a fixed technical
     * string (e.g. {@code "notification-service-internal"}) for a
     * self-signed internal service token (Phase 8d). A non-UUID subject can
     * never be patient-linked, so it's treated the same as the staff path —
     * skip the check — rather than throwing.
     */
    public void enforce(String bearerToken, String subject, UUID requestedPatientId) {
        UUID subjectUserId = parseUserId(subject);
        if (subjectUserId == null) {
            return;
        }
        patientOwnershipClient.findOwnPatientId(bearerToken, subjectUserId).ifPresent(ownPatientId -> {
            if (!ownPatientId.equals(requestedPatientId)) {
                throw new AccessDeniedException("Patients may only access their own record");
            }
        });
    }

    private UUID parseUserId(String subject) {
        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
