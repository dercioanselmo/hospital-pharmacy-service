package mz.mva.pharmacy.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import mz.mva.pharmacy.client.PatientOwnershipClient;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class PatientOwnershipGuardTest {

    private final PatientOwnershipClient client = mock(PatientOwnershipClient.class);
    private final PatientOwnershipGuard guard = new PatientOwnershipGuard(client);

    @Test
    void allowsAMatchingRequest() {
        UUID userId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        when(client.findOwnPatientId("Bearer t", userId)).thenReturn(Optional.of(patientId));

        assertThatCode(() -> guard.enforce("Bearer t", userId, patientId)).doesNotThrowAnyException();
    }

    @Test
    void deniesAMismatch() {
        UUID userId = UUID.randomUUID();
        when(client.findOwnPatientId("Bearer t", userId)).thenReturn(Optional.of(UUID.randomUUID()));

        assertThatThrownBy(() -> guard.enforce("Bearer t", userId, UUID.randomUUID()))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void staffCallerWithNoLinkedPatientIsUnaffected() {
        UUID userId = UUID.randomUUID();
        when(client.findOwnPatientId("Bearer t", userId)).thenReturn(Optional.empty());

        assertThatCode(() -> guard.enforce("Bearer t", userId, UUID.randomUUID())).doesNotThrowAnyException();
    }
}
