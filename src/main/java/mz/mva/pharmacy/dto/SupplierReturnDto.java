package mz.mva.pharmacy.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.SupplierReturn;

public record SupplierReturnDto(
        UUID id,
        String returnNumber,
        UUID originalGrnId,
        String originalGrnNumber,
        UUID storeId,
        String storeName,
        LocalDate returnDate,
        String returnType,
        String reason,
        DocumentStatus status,
        UUID createdByStaffId,
        UUID postedByStaffId,
        Instant postedAt,
        Instant createdAt,
        List<SupplierReturnLineDto> lines) {

    public static SupplierReturnDto from(SupplierReturn supplierReturn, List<SupplierReturnLineDto> lines) {
        return new SupplierReturnDto(
                supplierReturn.getId(),
                supplierReturn.getReturnNumber(),
                supplierReturn.getOriginalGrn() != null ? supplierReturn.getOriginalGrn().getId() : null,
                supplierReturn.getOriginalGrn() != null ? supplierReturn.getOriginalGrn().getGrnNumber() : null,
                supplierReturn.getStore().getId(),
                supplierReturn.getStore().getName(),
                supplierReturn.getReturnDate(),
                supplierReturn.getReturnType(),
                supplierReturn.getReason(),
                supplierReturn.getStatus(),
                supplierReturn.getCreatedByStaffId(),
                supplierReturn.getPostedByStaffId(),
                supplierReturn.getPostedAt(),
                supplierReturn.getCreatedAt(),
                lines);
    }
}
