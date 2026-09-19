package mz.mva.pharmacy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.GoodsReceivedNote;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationBatch;
import mz.mva.pharmacy.domain.MedicationForm;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.domain.SupplierReturn;
import mz.mva.pharmacy.domain.SupplierReturnLine;
import mz.mva.pharmacy.dto.CreateSupplierReturnRequest;
import mz.mva.pharmacy.dto.SupplierReturnLineRequest;
import mz.mva.pharmacy.repository.GoodsReceivedNoteRepository;
import mz.mva.pharmacy.repository.GrnLineRepository;
import mz.mva.pharmacy.repository.MedicationBatchRepository;
import mz.mva.pharmacy.repository.MedicationRepository;
import mz.mva.pharmacy.repository.PharmacyInventoryTransactionRepository;
import mz.mva.pharmacy.repository.PharmacyStoreRepository;
import mz.mva.pharmacy.repository.SupplierReturnLineRepository;
import mz.mva.pharmacy.repository.SupplierReturnRepository;
import org.junit.jupiter.api.Test;

class SupplierReturnServiceTest {

    private final SupplierReturnRepository returnRepository = mock(SupplierReturnRepository.class);
    private final SupplierReturnLineRepository lineRepository = mock(SupplierReturnLineRepository.class);
    private final PharmacyStoreRepository storeRepository = mock(PharmacyStoreRepository.class);
    private final MedicationRepository medicationRepository = mock(MedicationRepository.class);
    private final MedicationBatchRepository batchRepository = mock(MedicationBatchRepository.class);
    private final PharmacyInventoryTransactionRepository transactionRepository = mock(PharmacyInventoryTransactionRepository.class);
    private final GoodsReceivedNoteRepository grnRepository = mock(GoodsReceivedNoteRepository.class);
    private final GrnLineRepository grnLineRepository = mock(GrnLineRepository.class);

    private final PharmacyStoreService storeService = new PharmacyStoreService(storeRepository);
    private final MedicationService medicationService = new MedicationService(medicationRepository, mock(mz.mva.pharmacy.repository.MedicationPriceVersionRepository.class));
    private final StockMovementService stockMovementService =
            new StockMovementService(batchRepository, transactionRepository, medicationService);
    private final GrnService grnService = new GrnService(
            grnRepository, grnLineRepository, storeService, medicationService, stockMovementService, mock(GrnNumberGenerator.class));
    private final SupplierReturnNumberGenerator numberGenerator = mock(SupplierReturnNumberGenerator.class);
    private final SupplierReturnService returnService = new SupplierReturnService(
            returnRepository, lineRepository, storeService, medicationService, grnService, stockMovementService, numberGenerator);

    private PharmacyStore store() {
        return new PharmacyStore(UUID.randomUUID(), "PRINCIPAL", "Farmácia Principal");
    }

    private Medication medication() {
        return new Medication(UUID.randomUUID(), "PARA-500", "Paracetamol", MedicationForm.TABLET, "500mg", 0);
    }

    @Test
    void createLinksOriginalGrnWhenProvided() {
        PharmacyStore store = store();
        Medication medication = medication();
        GoodsReceivedNote grn = new GoodsReceivedNote(UUID.randomUUID(), "GRN-2026-000001", store, "Farmatex", LocalDate.now(), UUID.randomUUID());

        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(grnRepository.findById(grn.getId())).thenReturn(Optional.of(grn));
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));
        when(numberGenerator.next()).thenReturn("RET-2026-000001");
        when(returnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lineRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(lineRepository.findBySupplierReturnId(any())).thenReturn(List.of());

        var request = new CreateSupplierReturnRequest(
                grn.getId(), store.getId(), LocalDate.now(), "DAMAGED", "Broken on receipt", UUID.randomUUID(),
                List.of(new SupplierReturnLineRequest(medication.getId(), "B-001", 10)));

        var dto = returnService.create(request);

        assertThat(dto.returnNumber()).isEqualTo("RET-2026-000001");
        assertThat(dto.status()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(dto.originalGrnId()).isEqualTo(grn.getId());
    }

    @Test
    void postingDecrementsTheBatchAndAggregateStock() {
        PharmacyStore store = store();
        Medication medication = medication();
        medication.setOnHandQuantity(50);
        SupplierReturn supplierReturn = new SupplierReturn(
                UUID.randomUUID(), "RET-2026-000002", null, store, LocalDate.now(), "DAMAGED", "Broken", UUID.randomUUID());
        SupplierReturnLine line = new SupplierReturnLine(UUID.randomUUID(), supplierReturn, medication, "B-002", 20);
        MedicationBatch batch = new MedicationBatch(UUID.randomUUID(), medication, store, "B-002");
        batch.adjustQuantity(30);

        when(returnRepository.findById(supplierReturn.getId())).thenReturn(Optional.of(supplierReturn));
        when(lineRepository.findBySupplierReturnId(supplierReturn.getId())).thenReturn(List.of(line));
        when(batchRepository.findByMedicationIdAndStoreIdAndBatchNumber(medication.getId(), store.getId(), "B-002"))
                .thenReturn(Optional.of(batch));
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));
        when(returnRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = returnService.post(supplierReturn.getId(), UUID.randomUUID());

        assertThat(dto.status()).isEqualTo(DocumentStatus.POSTED);
        assertThat(batch.getQuantityAvailable()).isEqualTo(10);
        assertThat(medication.getOnHandQuantity()).isEqualTo(30);
    }

    @Test
    void postingRejectsAReturnQuantityExceedingAvailableBatchStock() {
        PharmacyStore store = store();
        Medication medication = medication();
        medication.setOnHandQuantity(50);
        SupplierReturn supplierReturn = new SupplierReturn(
                UUID.randomUUID(), "RET-2026-000003", null, store, LocalDate.now(), "DAMAGED", "Broken", UUID.randomUUID());
        SupplierReturnLine line = new SupplierReturnLine(UUID.randomUUID(), supplierReturn, medication, "B-003", 50);
        MedicationBatch batch = new MedicationBatch(UUID.randomUUID(), medication, store, "B-003");
        batch.adjustQuantity(30);

        when(returnRepository.findById(supplierReturn.getId())).thenReturn(Optional.of(supplierReturn));
        when(lineRepository.findBySupplierReturnId(supplierReturn.getId())).thenReturn(List.of(line));
        when(batchRepository.findByMedicationIdAndStoreIdAndBatchNumber(medication.getId(), store.getId(), "B-003"))
                .thenReturn(Optional.of(batch));

        assertThatThrownBy(() -> returnService.post(supplierReturn.getId(), UUID.randomUUID()))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(supplierReturn.getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(medication.getOnHandQuantity()).isEqualTo(50);
    }

    @Test
    void postingIsIdempotentOnAnAlreadyPostedReturn() {
        PharmacyStore store = store();
        SupplierReturn supplierReturn = new SupplierReturn(
                UUID.randomUUID(), "RET-2026-000004", null, store, LocalDate.now(), "DAMAGED", "Broken", UUID.randomUUID());
        supplierReturn.post(UUID.randomUUID());

        when(returnRepository.findById(supplierReturn.getId())).thenReturn(Optional.of(supplierReturn));
        when(lineRepository.findBySupplierReturnId(supplierReturn.getId())).thenReturn(List.of());

        var dto = returnService.post(supplierReturn.getId(), UUID.randomUUID());

        assertThat(dto.status()).isEqualTo(DocumentStatus.POSTED);
        org.mockito.Mockito.verify(batchRepository, never()).save(any());
    }
}
