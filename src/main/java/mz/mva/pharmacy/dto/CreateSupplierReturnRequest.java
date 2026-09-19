package mz.mva.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateSupplierReturnRequest(
        UUID originalGrnId,
        @NotNull UUID storeId,
        @NotNull LocalDate returnDate,
        String returnType,
        @NotBlank String reason,
        @NotNull UUID createdByStaffId,
        @NotEmpty @Valid List<SupplierReturnLineRequest> lines) {
}
