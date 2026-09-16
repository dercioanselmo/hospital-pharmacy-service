package mz.mva.pharmacy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.UUID;
import mz.mva.pharmacy.domain.Medication;
import mz.mva.pharmacy.domain.MedicationForm;

public record MedicationDto(
        UUID id,
        @NotBlank String code,
        @NotBlank String name,
        @NotNull MedicationForm form,
        @NotBlank String strength,
        @PositiveOrZero int onHandQuantity,
        boolean active) {

    public static MedicationDto from(Medication medication) {
        return new MedicationDto(
                medication.getId(),
                medication.getCode(),
                medication.getName(),
                medication.getForm(),
                medication.getStrength(),
                medication.getOnHandQuantity(),
                medication.isActive());
    }
}
