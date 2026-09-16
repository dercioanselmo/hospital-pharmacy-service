package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record VerifyOrderRequest(@NotNull UUID staffId) {
}
