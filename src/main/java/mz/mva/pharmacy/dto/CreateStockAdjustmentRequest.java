package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import mz.mva.pharmacy.domain.AdjustmentDirection;

public record CreateStockAdjustmentRequest(
        @NotNull UUID storeId,
        @NotNull UUID medicationId,
        @NotBlank String batchNumber,
        @NotNull AdjustmentDirection direction,
        @Positive int quantity,
        @NotBlank String reason,
        @NotNull UUID createdByStaffId) {
}
