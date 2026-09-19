package mz.mva.pharmacy.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateGrnRequest(
        @NotNull UUID storeId,
        @NotBlank String supplierName,
        @NotNull LocalDate receiptDate,
        @NotNull UUID createdByStaffId,
        @NotEmpty @Valid List<GrnLineRequest> lines) {
}
