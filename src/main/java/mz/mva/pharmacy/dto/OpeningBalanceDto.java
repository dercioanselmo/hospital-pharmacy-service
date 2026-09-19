package mz.mva.pharmacy.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.OpeningBalance;

public record OpeningBalanceDto(
        UUID id,
        String balanceNumber,
        UUID storeId,
        String storeName,
        UUID medicationId,
        String medicationName,
        String batchNumber,
        LocalDate expiryDate,
        int quantity,
        BigDecimal costPrice,
        String reason,
        DocumentStatus status,
        UUID createdByStaffId,
        UUID postedByStaffId,
        Instant postedAt,
        Instant createdAt) {

    public static OpeningBalanceDto from(OpeningBalance ob) {
        return new OpeningBalanceDto(
                ob.getId(),
                ob.getBalanceNumber(),
                ob.getStore().getId(),
                ob.getStore().getName(),
                ob.getMedication().getId(),
                ob.getMedication().getName(),
                ob.getBatchNumber(),
                ob.getExpiryDate(),
                ob.getQuantity(),
                ob.getCostPrice(),
                ob.getReason(),
                ob.getStatus(),
                ob.getCreatedByStaffId(),
                ob.getPostedByStaffId(),
                ob.getPostedAt(),
                ob.getCreatedAt());
    }
}
