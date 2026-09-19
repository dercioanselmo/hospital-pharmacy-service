package mz.mva.pharmacy.dto;

import java.util.UUID;
import mz.mva.pharmacy.domain.SupplierReturnLine;

public record SupplierReturnLineDto(UUID id, UUID medicationId, String medicationName, String batchNumber, int quantity) {

    public static SupplierReturnLineDto from(SupplierReturnLine line) {
        return new SupplierReturnLineDto(
                line.getId(), line.getMedication().getId(), line.getMedication().getName(), line.getBatchNumber(), line.getQuantity());
    }
}
