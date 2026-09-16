package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record DispenseRequest(@Positive int quantityDispensed, @NotNull UUID staffId) {
}
