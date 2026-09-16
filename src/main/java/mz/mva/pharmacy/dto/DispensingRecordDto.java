package mz.mva.pharmacy.dto;

import java.time.Instant;
import java.util.UUID;
import mz.mva.pharmacy.domain.DispensingRecord;

public record DispensingRecordDto(
        UUID id,
        int quantityDispensed,
        UUID dispensedByStaffId,
        Instant dispensedAt,
        UUID completedByStaffId,
        Instant completedAt) {

    public static DispensingRecordDto from(DispensingRecord record) {
        return new DispensingRecordDto(
                record.getId(),
                record.getQuantityDispensed(),
                record.getDispensedByStaffId(),
                record.getDispensedAt(),
                record.getCompletedByStaffId(),
                record.getCompletedAt());
    }
}
