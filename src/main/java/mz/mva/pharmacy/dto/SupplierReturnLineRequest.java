package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record SupplierReturnLineRequest(@NotNull UUID medicationId, @NotBlank String batchNumber, @Positive int quantity) {
}
