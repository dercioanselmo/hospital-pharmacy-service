package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CompleteDispensingRequest(@NotNull UUID staffId) {
}
