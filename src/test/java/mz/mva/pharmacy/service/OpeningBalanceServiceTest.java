package mz.mva.pharmacy.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationForm;
import mz.mva.pharmacy.domain.OpeningBalance;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.dto.CreateOpeningBalanceRequest;
import mz.mva.pharmacy.repository.MedicationBatchRepository;
import mz.mva.pharmacy.repository.MedicationRepository;
import mz.mva.pharmacy.repository.OpeningBalanceRepository;
import mz.mva.pharmacy.repository.PharmacyInventoryTransactionRepository;
import mz.mva.pharmacy.repository.PharmacyStoreRepository;
import org.junit.jupiter.api.Test;

class OpeningBalanceServiceTest {

    private final OpeningBalanceRepository balanceRepository = mock(OpeningBalanceRepository.class);
    private final PharmacyStoreRepository storeRepository = mock(PharmacyStoreRepository.class);
    private final MedicationRepository medicationRepository = mock(MedicationRepository.class);
    private final MedicationBatchRepository batchRepository = mock(MedicationBatchRepository.class);
    private final PharmacyInventoryTransactionRepository transactionRepository = mock(PharmacyInventoryTransactionRepository.class);

    private final PharmacyStoreService storeService = new PharmacyStoreService(storeRepository);
    private final MedicationService medicationService = new MedicationService(medicationRepository, mock(mz.mva.pharmacy.repository.MedicationPriceVersionRepository.class));
    private final StockMovementService stockMovementService =
            new StockMovementService(batchRepository, transactionRepository, medicationService);
    private final OpeningBalanceNumberGenerator numberGenerator = mock(OpeningBalanceNumberGenerator.class);
    private final OpeningBalanceService balanceService =
            new OpeningBalanceService(balanceRepository, storeService, medicationService, stockMovementService, numberGenerator);

    private PharmacyStore store() {
        return new PharmacyStore(UUID.randomUUID(), "PRINCIPAL", "Farmácia Principal");
    }

    private Medication medication() {
        return new Medication(UUID.randomUUID(), "PARA-500", "Paracetamol", MedicationForm.TABLET, "500mg", 0);
    }

    @Test
    void createDoesNotTouchStockUntilPosted() {
        PharmacyStore store = store();
        Medication medication = medication();
        when(storeRepository.findById(store.getId())).thenReturn(Optional.of(store));
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));
        when(numberGenerator.next()).thenReturn("OB-2026-000001");
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CreateOpeningBalanceRequest(
                store.getId(), medication.getId(), "B-001", LocalDate.now().plusYears(1), 200,
                new BigDecimal("4.50"), "Go-live stock", UUID.randomUUID());

        var dto = balanceService.create(request);

        assertThat(dto.balanceNumber()).isEqualTo("OB-2026-000001");
        assertThat(dto.status()).isEqualTo(DocumentStatus.DRAFT);
        verify(batchRepository, never()).save(any());
        assertThat(medication.getOnHandQuantity()).isZero();
    }

    @Test
    void postingCreatesTheBatchAndSyncsAggregateStock() {
        PharmacyStore store = store();
        Medication medication = medication();
        OpeningBalance balance = new OpeningBalance(
                UUID.randomUUID(), "OB-2026-000002", store, medication, "B-002", 150, UUID.randomUUID());

        when(balanceRepository.findById(balance.getId())).thenReturn(Optional.of(balance));
        when(batchRepository.findByMedicationIdAndStoreIdAndBatchNumber(medication.getId(), store.getId(), "B-002"))
                .thenReturn(Optional.empty());
        when(batchRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(medicationRepository.findById(medication.getId())).thenReturn(Optional.of(medication));
        when(balanceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var dto = balanceService.post(balance.getId(), UUID.randomUUID());

        assertThat(dto.status()).isEqualTo(DocumentStatus.POSTED);
        assertThat(medication.getOnHandQuantity()).isEqualTo(150);
        verify(transactionRepository).save(any());
    }

    @Test
    void postingIsIdempotentOnAnAlreadyPostedOpeningBalance() {
        PharmacyStore store = store();
        Medication medication = medication();
        OpeningBalance balance = new OpeningBalance(
                UUID.randomUUID(), "OB-2026-000003", store, medication, "B-003", 100, UUID.randomUUID());
        balance.post(UUID.randomUUID());
        when(balanceRepository.findById(balance.getId())).thenReturn(Optional.of(balance));

        var dto = balanceService.post(balance.getId(), UUID.randomUUID());

        assertThat(dto.status()).isEqualTo(DocumentStatus.POSTED);
        verify(batchRepository, never()).save(any());
        verify(balanceRepository, never()).save(any());
    }
}
