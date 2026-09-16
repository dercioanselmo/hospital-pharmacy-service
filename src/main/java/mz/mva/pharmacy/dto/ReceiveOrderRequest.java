package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record ReceiveOrderRequest(
        @NotNull UUID clinicalOrderId,
        @NotNull UUID staffId,
        @NotNull UUID medicationId,
        @Positive int quantityPrescribed,
        @NotBlank String dosageInstructions) {
}
