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
import mz.mva.pharmacy.domain.AdjustmentDirection;
import mz.mva.pharmacy.domain.AdjustmentStatus;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationBatch;
import mz.mva.pharmacy.domain.MedicationForm;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.domain.StockAdjustment;
import mz.mva.pharmacy.repository.MedicationBatchRepository;
import mz.mva.pharmacy.repository.MedicationRepository;
import mz.mva.pharmacy.repository.PharmacyInventoryTransactionRepository;
import mz.mva.pharmacy.repository.PharmacyStoreRepository;
import mz.mva.pharmacy.repository.StockAdjustmentRepository;
import org.junit.jupiter.api.Test;

class StockAdjustmentServiceTest {

    private final StockAdjustmentRepository adjustmentRepository = mock(StockAdjustmentRepository.class);
    private final PharmacyStoreRepository storeRepository = mock(PharmacyStoreRepository.class);
    private final MedicationRepository medicationRepository = mock(MedicationRepository.class);
    private final MedicationBatchRepository batchRepository = mock(MedicationBatchRepository.class);
    private final PharmacyInventoryTransactionRepository transactionRepository = mock(PharmacyInventoryTransactionRepository.class);

    private final PharmacyStoreService storeService = new PharmacyStoreService(storeRepository);
    private final MedicationService medicationService = new MedicationService(medicationRepository, mock(mz.mva.pharmacy.repository.MedicationPriceVersionRepository.class));
    private final StockMovementService stockMovementService =
            new StockMovementService(batchRepository, transactionRepository, medicationService);
    private final StockAdjustmentNumberGenerator numberGenerator = mock(StockAdjustmentNumberGenerator.class);
    private final StockAdjustmentService adjustmentService = new StockAdjustmentService(
            adjustmentRepository, batchRepository, storeService, medicationService, stockMovementService, numberGenerator);

    private PharmacyStore store() {
        return new PharmacyStore(UUID.randomUUID(), "PRINCIPAL", "Farmácia Principal");
    }

    private Medication medication() {
        return new Medication(UUID.randomUUID(), "PARA-500", "Paracetamol", MedicationForm.TABLET, "500mg", 0);
    }

    @Test
    void postingWithoutApprovalIsRejected() {
        PharmacyStore store = store();
        Medication medication = medication();
        StockAdjustment adjustment = new StockAdjustment(
                UUID.randomUUID(), "ADJ-2026-000001", store, medication, "B-001", AdjustmentDirection.RECEIVE, 10,
                "Physical count higher", UUID.randomUUID());
        when(adjustmentRepository.findById(adjustment.getId())).thenReturn(Optional.of(adjustment));

        assertThatThrownBy(() -> adjustmentService.post(adjustment.getId(), UUID.randomUUID()))
                .isInstanceOf(InvalidStateTransitionException.class);

        verify(batchRepository, never()).save(any());
        assertThat(adjustment.getStatus()).isEqualTo(AdjustmentStatus.DRAFT);
    }

    @Test
    void approvingATwicePendingAdjustmentIsRejected() {
        PharmacyStore store = store();
        Medication medication = medication();
        StockAdjustment adjustment = new StockAdjustment(
                UUID.randomUUID(), "ADJ-2026-000002", store, medication, "B-002", AdjustmentDirection.ISSUE, 5,
                "Physical count lower", UUID.randomUUID());
        adjustment.approve(UUID.randomUUID());
        when(adjustmentRepository.findById(adjustment.getId())).thenReturn(Optional.of(adjustment));

        assertThatThrownBy(() -> adjustmentService.approve(adjustment.getId(), UUID.randomUUID()))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void postingAnApprovedReceiveAdjustmentIncreasesStockAndRecordsBeforeAfter() {
        PharmacyStore store = store();
        Medication medication = medication();
        StockAdjustment adjustment = new StockAdjustment(
                UUID.randomUUID(), "ADJ-2026-000003", store, medication, "B-003", AdjustmentDirection.RECEIVE, 20,
                "Physical count higher", UUID.randomUUID());
        adjustment.approve(UUID.randomUUID());
        MedicationBatch batch = new MedicationBatch(UUID.randomUUID(), medication, store, "B-003");
        batch.adjustQuantity(30);

        when(adjustmentRepository.findById(adjustment.getId())).thenReturn(Optional.of(adjustment));
        when(batchRepository.findByMedicationIdAndStoreIdAndBatchNumber(medication.getId(), store.getId(), "B-003"))
                .thenReturn(Optional.of(batch));
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));
        when(adjustmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = adjustmentService.post(adjustment.getId(), UUID.randomUUID());

        assertThat(dto.status()).isEqualTo(AdjustmentStatus.POSTED);
        assertThat(dto.quantityBefore()).isEqualTo(30);
        assertThat(dto.quantityAfter()).isEqualTo(50);
        assertThat(batch.getQuantityAvailable()).isEqualTo(50);
    }

    @Test
    void postingAnApprovedIssueAdjustmentDecreasesStock() {
        PharmacyStore store = store();
        Medication medication = medication();
        StockAdjustment adjustment = new StockAdjustment(
                UUID.randomUUID(), "ADJ-2026-000004", store, medication, "B-004", AdjustmentDirection.ISSUE, 10,
                "Physical count lower", UUID.randomUUID());
        adjustment.approve(UUID.randomUUID());
        medication.setOnHandQuantity(15);
        MedicationBatch batch = new MedicationBatch(UUID.randomUUID(), medication, store, "B-004");
        batch.adjustQuantity(15);

        when(adjustmentRepository.findById(adjustment.getId())).thenReturn(Optional.of(adjustment));
        when(batchRepository.findByMedicationIdAndStoreIdAndBatchNumber(medication.getId(), store.getId(), "B-004"))
                .thenReturn(Optional.of(batch));
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));
        when(adjustmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = adjustmentService.post(adjustment.getId(), UUID.randomUUID());

        assertThat(dto.quantityBefore()).isEqualTo(15);
        assertThat(dto.quantityAfter()).isEqualTo(5);
        assertThat(batch.getQuantityAvailable()).isEqualTo(5);
    }

    @Test
    void postingIsIdempotentOnAnAlreadyPostedAdjustment() {
        PharmacyStore store = store();
        Medication medication = medication();
        StockAdjustment adjustment = new StockAdjustment(
                UUID.randomUUID(), "ADJ-2026-000005", store, medication, "B-005", AdjustmentDirection.RECEIVE, 5,
                "reason", UUID.randomUUID());
        adjustment.approve(UUID.randomUUID());
        adjustment.post(UUID.randomUUID(), 0, 5);
        when(adjustmentRepository.findById(adjustment.getId())).thenReturn(Optional.of(adjustment));

        var dto = adjustmentService.post(adjustment.getId(), UUID.randomUUID());

        assertThat(dto.status()).isEqualTo(AdjustmentStatus.POSTED);
        verify(batchRepository, never()).save(any());
        verify(adjustmentRepository, never()).save(any());
    }
}
