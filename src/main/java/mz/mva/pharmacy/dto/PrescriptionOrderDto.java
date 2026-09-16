package mz.mva.pharmacy.dto;

import java.time.Instant;
import java.util.UUID;
import mz.mva.pharmacy.domain.PrescriptionOrder;
import mz.mva.pharmacy.domain.PrescriptionOrderStatus;

public record PrescriptionOrderDto(
        UUID id,
        UUID clinicalOrderId,
        UUID encounterId,
        UUID patientId,
        UUID orderingStaffId,
        UUID medicationId,
        String medicationName,
        int quantityPrescribed,
        String dosageInstructions,
        UUID receivedByStaffId,
        UUID verifiedByStaffId,
        PrescriptionOrderStatus status,
        Instant createdAt,
        Instant updatedAt) {

    public static PrescriptionOrderDto from(PrescriptionOrder order) {
        return new PrescriptionOrderDto(
                order.getId(),
                order.getClinicalOrderId(),
                order.getEncounterId(),
                order.getPatientId(),
                order.getOrderingStaffId(),
                order.getMedication().getId(),
                order.getMedication().getName(),
                order.getQuantityPrescribed(),
                order.getDosageInstructions(),
                order.getReceivedByStaffId(),
                order.getVerifiedByStaffId(),
                order.getStatus(),
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
