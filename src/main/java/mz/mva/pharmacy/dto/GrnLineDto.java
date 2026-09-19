package mz.mva.pharmacy.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import mz.mva.pharmacy.domain.GrnLine;

public record GrnLineDto(
        UUID id,
        UUID medicationId,
        String medicationName,
        String batchNumber,
        LocalDate expiryDate,
        int quantity,
        int freeQuantity,
        BigDecimal costPrice,
        BigDecimal salePrice) {

    public static GrnLineDto from(GrnLine line) {
        return new GrnLineDto(
                line.getId(),
                line.getMedication().getId(),
                line.getMedication().getName(),
                line.getBatchNumber(),
                line.getExpiryDate(),
                line.getQuantity(),
                line.getFreeQuantity(),
                line.getCostPrice(),
                line.getSalePrice());
    }
}
