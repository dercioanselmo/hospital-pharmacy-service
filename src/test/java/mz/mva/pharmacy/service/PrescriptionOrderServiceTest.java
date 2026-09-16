package mz.mva.pharmacy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import mz.mva.pharmacy.client.ClinicalOrderClient;
import mz.mva.pharmacy.client.ClinicalOrderView;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationForm;
import mz.mva.pharmacy.domain.PrescriptionOrder;
import mz.mva.pharmacy.domain.PrescriptionOrderStatus;
import mz.mva.pharmacy.dto.ReceiveOrderRequest;
import mz.mva.pharmacy.repository.PrescriptionOrderRepository;
import org.junit.jupiter.api.Test;

class PrescriptionOrderServiceTest {

    private final PrescriptionOrderRepository prescriptionOrderRepository = mock(PrescriptionOrderRepository.class);
    private final MedicationService medicationService = mock(MedicationService.class);
    private final ClinicalOrderClient clinicalOrderClient = mock(ClinicalOrderClient.class);
    private final PrescriptionOrderService prescriptionOrderService =
            new PrescriptionOrderService(prescriptionOrderRepository, medicationService, clinicalOrderClient);

    private static final String TOKEN = "test-token";

    @Test
    void receivingAnOrderClaimsItInClinicalAndCreatesLocalRecord() {
        UUID clinicalOrderId = UUID.randomUUID();
        UUID staffId = UUID.randomUUID();
        UUID medicationId = UUID.randomUUID();
        UUID encounterId = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();

        when(prescriptionOrderRepository.findByClinicalOrderId(clinicalOrderId)).thenReturn(Optional.empty());
        when(clinicalOrderClient.fetch(clinicalOrderId, TOKEN)).thenReturn(new ClinicalOrderView(
                clinicalOrderId, encounterId, patientId, "PRESCRIPTION", staffId, UUID.randomUUID(), null,
                "Paracetamol 500mg", "PLACED"));
        when(medicationService.getOrThrow(medicationId))
                .thenReturn(new Medication(medicationId, "PARA-500", "Paracetamol", MedicationForm.TABLET, "500mg", 200));
        when(prescriptionOrderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = prescriptionOrderService.receive(
                new ReceiveOrderRequest(clinicalOrderId, staffId, medicationId, 20, "1 comprimido de 8/8h"), TOKEN);

        assertThat(result.clinicalOrderId()).isEqualTo(clinicalOrderId);
        assertThat(result.patientId()).isEqualTo(patientId);
        assertThat(result.status()).isEqualTo(PrescriptionOrderStatus.RECEIVED);
        verify(clinicalOrderClient).claim(clinicalOrderId, staffId, TOKEN);
    }

    @Test
    void receivingAnAlreadyReceivedOrderFails() {
        UUID clinicalOrderId = UUID.randomUUID();
        PrescriptionOrder existing = new PrescriptionOrder(
                UUID.randomUUID(), clinicalOrderId, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new Medication(UUID.randomUUID(), "X", "X", MedicationForm.TABLET, "1mg", 10), 1, "...",
                UUID.randomUUID());
        when(prescriptionOrderRepository.findByClinicalOrderId(clinicalOrderId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> prescriptionOrderService.receive(
                        new ReceiveOrderRequest(clinicalOrderId, UUID.randomUUID(), UUID.randomUUID(), 1, "..."),
                        TOKEN))
                .isInstanceOf(InvalidStateTransitionException.class);

        verify(clinicalOrderClient, never()).claim(any(), any(), any());
    }

    @Test
    void startDispensingRequiresVerifiedFirst() {
        UUID id = UUID.randomUUID();
        PrescriptionOrder order = new PrescriptionOrder(
                id, UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                new Medication(UUID.randomUUID(), "X", "X", MedicationForm.TABLET, "1mg", 10), 1, "...",
                UUID.randomUUID());
        when(prescriptionOrderRepository.findById(id)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> prescriptionOrderService.startDispensing(id, TOKEN))
                .isInstanceOf(InvalidStateTransitionException.class);
        verify(clinicalOrderClient, never()).start(any(), any());
    }
}
