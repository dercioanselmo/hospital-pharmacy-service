package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GrnLineRequest(
        @NotNull UUID medicationId,
        @NotBlank String batchNumber,
        LocalDate expiryDate,
        @Positive int quantity,
        @PositiveOrZero int freeQuantity,
        @NotNull BigDecimal costPrice,
        @NotNull BigDecimal salePrice) {
}
