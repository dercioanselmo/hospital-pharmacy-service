package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Shared body shape for post/approve actions across the stocking document types — just who's acting. */
public record StaffActionRequest(@NotNull UUID staffId) {
}
