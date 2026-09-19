package mz.mva.pharmacy.dto;

import java.time.Instant;
import java.util.UUID;
import mz.mva.pharmacy.domain.PharmacyInventoryTransaction;
import mz.mva.pharmacy.domain.PharmacyTransactionType;

public record PharmacyInventoryTransactionDto(
        UUID id,
        PharmacyTransactionType transactionType,
        UUID medicationId,
        UUID storeId,
        String batchNumber,
        int quantity,
        int quantityBefore,
        int quantityAfter,
        String referenceType,
        UUID referenceId,
        String reason,
        UUID staffId,
        Instant createdAt) {

    public static PharmacyInventoryTransactionDto from(PharmacyInventoryTransaction tx) {
        return new PharmacyInventoryTransactionDto(
                tx.getId(),
                tx.getTransactionType(),
                tx.getMedicationId(),
                tx.getStoreId(),
                tx.getBatchNumber(),
                tx.getQuantity(),
                tx.getQuantityBefore(),
                tx.getQuantityAfter(),
                tx.getReferenceType(),
                tx.getReferenceId(),
                tx.getReason(),
                tx.getStaffId(),
                tx.getCreatedAt());
    }
}
