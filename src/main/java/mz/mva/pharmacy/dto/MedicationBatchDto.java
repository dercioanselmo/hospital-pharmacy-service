package mz.mva.pharmacy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import mz.mva.pharmacy.domain.MedicationBatch;

public record MedicationBatchDto(
        UUID id,
        UUID medicationId,
        String medicationName,
        UUID storeId,
        String storeName,
        String batchNumber,
        LocalDate expiryDate,
        int quantityAvailable,
        BigDecimal costPrice,
        BigDecimal salePrice,
        Instant updatedAt) {

    public static MedicationBatchDto from(MedicationBatch batch) {
        return new MedicationBatchDto(
                batch.getId(),
                batch.getMedication().getId(),
                batch.getMedication().getName(),
                batch.getStore().getId(),
                batch.getStore().getName(),
                batch.getBatchNumber(),
                batch.getExpiryDate(),
                batch.getQuantityAvailable(),
                batch.getCostPrice(),
                batch.getSalePrice(),
                batch.getUpdatedAt());
    }
}
