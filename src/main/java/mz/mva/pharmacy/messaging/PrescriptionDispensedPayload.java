package mz.mva.pharmacy.messaging;

import java.util.UUID;

public record PrescriptionDispensedPayload(
        UUID dispensingRecordId, UUID prescriptionOrderId, UUID encounterId, UUID patientId, int quantityDispensed) {
}
