package mz.mva.pharmacy.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationBatch;
import mz.mva.pharmacy.domain.PharmacyInventoryTransaction;
import mz.mva.pharmacy.domain.PharmacyStore;
import mz.mva.pharmacy.domain.PharmacyTransactionType;
import mz.mva.pharmacy.repository.MedicationBatchRepository;
import mz.mva.pharmacy.repository.PharmacyInventoryTransactionRepository;
import org.springframework.stereotype.Component;

/**
 * The one place every stocking document's posting logic funnels through — finds-or-creates the
 * target {@link MedicationBatch}, applies the quantity delta, writes an immutable {@link
 * PharmacyInventoryTransaction}, and keeps {@link Medication#getOnHandQuantity()}'s aggregate in
 * sync. Shared across GRN/Supplier Return/Opening Balance/Stock Adjustment because it's genuinely
 * the same operation every time (v9 §32: "Pharmacy Stocking -> Inventory Operations -> Product ->
 * Batch -> Location -> Inventory Transaction -> Stock Balance").
 */
@Component
class StockMovementService {

    private final MedicationBatchRepository batchRepository;
    private final PharmacyInventoryTransactionRepository transactionRepository;
    private final MedicationService medicationService;

    StockMovementService(
            MedicationBatchRepository batchRepository,
            PharmacyInventoryTransactionRepository transactionRepository,
            MedicationService medicationService) {
        this.batchRepository = batchRepository;
        this.transactionRepository = transactionRepository;
        this.medicationService = medicationService;
    }

    /**
     * Applies {@code delta} (positive to receive, negative to issue) to the batch identified by
     * (medication, store, batchNumber), creating it if it doesn't exist yet. Rejects a negative
     * result — stock can never go below zero.
     */
    MedicationBatch apply(
            Medication medication,
            PharmacyStore store,
            String batchNumber,
            LocalDate expiryDate,
            BigDecimal costPrice,
            BigDecimal salePrice,
            int delta,
            PharmacyTransactionType type,
            String referenceType,
            UUID referenceId,
            String reason,
            UUID staffId) {
        MedicationBatch batch = batchRepository
                .findByMedicationIdAndStoreIdAndBatchNumber(medication.getId(), store.getId(), batchNumber)
                .orElseGet(() -> new MedicationBatch(UUID.randomUUID(), medication, store, batchNumber));
        int before = batch.getQuantityAvailable();
        int after = before + delta;
        if (after < 0) {
            throw new InsufficientStockException(
                    "Batch " + batchNumber + " of " + medication.getCode() + " at " + store.getCode() + " has only "
                            + before + " available, cannot remove " + (-delta));
        }
        if (expiryDate != null) {
            batch.setExpiryDate(expiryDate);
        }
        if (costPrice != null) {
            batch.setCostPrice(costPrice);
        }
        if (salePrice != null) {
            batch.setSalePrice(salePrice);
        }
        batch.adjustQuantity(delta);
        MedicationBatch saved = batchRepository.save(batch);

        transactionRepository.save(new PharmacyInventoryTransaction(
                UUID.randomUUID(), type, medication.getId(), store.getId(), batchNumber, Math.abs(delta), before, after,
                referenceType, referenceId, reason, staffId));

        if (delta > 0) {
            medicationService.incrementStock(medication.getId(), delta);
        } else if (delta < 0) {
            medicationService.decrementStock(medication.getId(), -delta);
        }
        return saved;
    }
}
