package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateOpeningBalanceRequest(
        @NotNull UUID storeId,
        @NotNull UUID medicationId,
        @NotBlank String batchNumber,
        LocalDate expiryDate,
        @Positive int quantity,
        BigDecimal costPrice,
        String reason,
        @NotNull UUID createdByStaffId) {
}
