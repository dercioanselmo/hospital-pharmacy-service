package mz.mva.pharmacy.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import mz.mva.pharmacy.domain.DocumentStatus;
import mz.mva.pharmacy.domain.GoodsReceivedNote;

public record GoodsReceivedNoteDto(
        UUID id,
        String grnNumber,
        UUID storeId,
        String storeName,
        String supplierName,
        LocalDate receiptDate,
        DocumentStatus status,
        UUID createdByStaffId,
        UUID postedByStaffId,
        Instant postedAt,
        Instant createdAt,
        List<GrnLineDto> lines) {

    public static GoodsReceivedNoteDto from(GoodsReceivedNote grn, List<GrnLineDto> lines) {
        return new GoodsReceivedNoteDto(
                grn.getId(),
                grn.getGrnNumber(),
                grn.getStore().getId(),
                grn.getStore().getName(),
                grn.getSupplierName(),
                grn.getReceiptDate(),
                grn.getStatus(),
                grn.getCreatedByStaffId(),
                grn.getPostedByStaffId(),
                grn.getPostedAt(),
                grn.getCreatedAt(),
                lines);
    }
}
