package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import mz.mva.pharmacy.domain.PharmacyStore;

public record PharmacyStoreDto(UUID id, @NotBlank String code, @NotBlank String name, boolean active) {

    public static PharmacyStoreDto from(PharmacyStore store) {
        return new PharmacyStoreDto(store.getId(), store.getCode(), store.getName(), store.isActive());
    }
}
