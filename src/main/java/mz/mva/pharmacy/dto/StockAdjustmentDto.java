package mz.mva.pharmacy.dto;

import java.time.Instant;
import java.util.UUID;
import mz.mva.pharmacy.domain.AdjustmentDirection;
import mz.mva.pharmacy.domain.AdjustmentStatus;
import mz.mva.pharmacy.domain.StockAdjustment;

public record StockAdjustmentDto(
        UUID id,
        String adjustmentNumber,
        UUID storeId,
        String storeName,
        UUID medicationId,
        String medicationName,
        String batchNumber,
        AdjustmentDirection direction,
        int quantity,
        String reason,
        Integer quantityBefore,
        Integer quantityAfter,
        AdjustmentStatus status,
        UUID createdByStaffId,
        UUID approvedByStaffId,
        Instant approvedAt,
        UUID postedByStaffId,
        Instant postedAt,
        Instant createdAt) {

    public static StockAdjustmentDto from(StockAdjustment adj) {
        return new StockAdjustmentDto(
                adj.getId(),
                adj.getAdjustmentNumber(),
                adj.getStore().getId(),
                adj.getStore().getName(),
                adj.getMedication().getId(),
                adj.getMedication().getName(),
                adj.getBatchNumber(),
                adj.getDirection(),
                adj.getQuantity(),
                adj.getReason(),
                adj.getQuantityBefore(),
                adj.getQuantityAfter(),
                adj.getStatus(),
                adj.getCreatedByStaffId(),
                adj.getApprovedByStaffId(),
                adj.getApprovedAt(),
                adj.getPostedByStaffId(),
                adj.getPostedAt(),
                adj.getCreatedAt());
    }
}
